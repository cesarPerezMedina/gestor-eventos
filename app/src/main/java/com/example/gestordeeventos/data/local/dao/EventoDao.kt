package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.EventoConCupo
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

    /** Para Buscar: solo públicos, con el número de apuntados de cada uno. */
    @Query(
        "SELECT e.*, (SELECT COUNT(*) FROM participaciones p WHERE p.eventoId = e.id) AS apuntados " +
            "FROM eventos e WHERE e.publico = 1 ORDER BY e.fechaHora"
    )
    fun observarPublicosConCupo(): Flow<List<EventoConCupo>>

    /**
     * Para "Cerca de mí": públicos dentro de un rectángulo del mapa (primer filtro, rápido).
     * La distancia exacta y el orden los calcula el repositorio. Los eventos sin punto en el mapa
     * (latitud y longitud 0) quedan fuera, porque no tienen ubicación que comparar.
     */
    @Query(
        "SELECT e.*, (SELECT COUNT(*) FROM participaciones p WHERE p.eventoId = e.id) AS apuntados " +
            "FROM eventos e WHERE e.publico = 1 " +
            "AND NOT (e.latitud = 0 AND e.longitud = 0) " +
            "AND e.latitud BETWEEN :latMin AND :latMax AND e.longitud BETWEEN :lonMin AND :lonMax"
    )
    fun observarPublicosEnCaja(latMin: Double, latMax: Double, lonMin: Double, lonMax: Double): Flow<List<EventoConCupo>>

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



    /**
     * Devuelve todos los eventos creados por el usuario indicado.
     * Se ordenan por fecha, de más próximos a más lejanos.
     */
    @Query("SELECT * FROM eventos WHERE creadorId = :usuarioId ORDER BY fechaHora ASC")
    suspend fun listarCreadosPorUsuario(usuarioId: Long): List<Evento>

    /**
     * Devuelve los eventos en los que el usuario tiene una participación.
     * El JOIN evita depender de que el usuario sea también el creador.
     * Se ordenan por fecha, de más próximos a más lejanos.
     */
    @Query(
        "SELECT e.* FROM eventos e " +
            "INNER JOIN participaciones p ON e.id = p.eventoId " +
            "WHERE p.usuarioId = :usuarioId " +
            "ORDER BY e.fechaHora ASC"
    )
    suspend fun listarEventosParticipadosPorUsuario(usuarioId: Long): List<Evento>

}

