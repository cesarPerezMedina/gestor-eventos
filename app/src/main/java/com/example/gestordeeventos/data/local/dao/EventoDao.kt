package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Participacion
import kotlinx.coroutines.flow.Flow

/**
 * Consultas sobre "eventos". Cada Issue añade aquí las que necesite (búsqueda, listados, borrado...).
 *
 * Regla público / privado (Issue 43):
 * - Público (publico = 1): lo ve y se puede apuntar cualquiera.
 * - Privado (publico = 0): solo lo ven su creador, quien tenga una invitación (no rechazada)
 *   y quien ya participa. Para apuntarse hace falta invitación.
 */
@Dao
interface EventoDao {
    @Insert
    suspend fun insertar(evento: Evento): Long

    @Query("SELECT * FROM eventos WHERE id = :id")
    suspend fun buscarPorId(id: Long): Evento?

    /** Para la pantalla Buscar: SOLO eventos públicos. Los privados nunca salen aquí. */
    @Query("SELECT * FROM eventos WHERE publico = 1 ORDER BY fechaHora")
    fun observarPublicos(): Flow<List<Evento>>

    /** Para "Mis eventos": los que creo, en los que participo y a los que me han invitado (sean públicos o privados). */
    @Query(
        "SELECT * FROM eventos e WHERE e.creadorId = :usuarioId " +
            "OR EXISTS (SELECT 1 FROM participaciones p WHERE p.eventoId = e.id AND p.usuarioId = :usuarioId) " +
            "OR EXISTS (SELECT 1 FROM invitaciones i WHERE i.eventoId = e.id AND i.paraUsuarioId = :usuarioId AND i.estado != 'RECHAZADA') " +
            "ORDER BY e.fechaHora"
    )
    fun observarMisEventos(usuarioId: Long): Flow<List<Evento>>

    /** ¿Puede este usuario ver este evento? Público: sí. Privado: solo creador, invitado o participante. */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM eventos e WHERE e.id = :eventoId AND (" +
            "e.publico = 1 OR e.creadorId = :usuarioId " +
            "OR EXISTS (SELECT 1 FROM invitaciones i WHERE i.eventoId = e.id AND i.paraUsuarioId = :usuarioId AND i.estado != 'RECHAZADA') " +
            "OR EXISTS (SELECT 1 FROM participaciones p WHERE p.eventoId = e.id AND p.usuarioId = :usuarioId)))"
    )
    suspend fun puedeVer(eventoId: Long, usuarioId: Long): Boolean

    // --- Apuntarse a un evento ---

    @Query("SELECT EXISTS(SELECT 1 FROM participaciones WHERE eventoId = :eventoId AND usuarioId = :usuarioId)")
    suspend fun estaApuntado(eventoId: Long, usuarioId: Long): Boolean

    @Query("SELECT COUNT(*) FROM participaciones WHERE eventoId = :eventoId")
    suspend fun contarParticipantes(eventoId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM invitaciones WHERE eventoId = :eventoId AND paraUsuarioId = :usuarioId AND estado != 'RECHAZADA')")
    suspend fun tieneInvitacion(eventoId: Long, usuarioId: Long): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarParticipacion(participacion: Participacion): Long

    /** Al apuntarse con una invitación pendiente, se marca como aceptada. */
    @Query("UPDATE invitaciones SET estado = 'ACEPTADA' WHERE eventoId = :eventoId AND paraUsuarioId = :usuarioId AND estado = 'PENDIENTE'")
    suspend fun aceptarInvitacion(eventoId: Long, usuarioId: Long): Int
}