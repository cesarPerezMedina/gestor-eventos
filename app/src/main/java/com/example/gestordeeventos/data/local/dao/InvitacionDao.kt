package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.InvitacionPendiente
import com.example.gestordeeventos.data.model.Participacion
import kotlinx.coroutines.flow.Flow

/** Consultas sobre "invitaciones". */
@Dao
interface InvitacionDao {
    @Insert
    suspend fun insertar(invitacion: Invitacion): Long

    @Query("SELECT * FROM invitaciones WHERE id = :id")
    suspend fun buscarPorId(id: Long): Invitacion?

    /**
     * Invitaciones PENDIENTES recibidas por [usuarioId], con el nombre del evento y de quien invita.
     * Se ordenan por fecha del evento (las más próximas primero) y, a igualdad, por orden de llegada.
     * Es un Flow: se vuelve a emitir sola cuando se crea, acepta o rechaza una invitación.
     */
    @Query(
        "SELECT i.id AS invitacionId, e.id AS eventoId, e.nombre AS eventoNombre, " +
            "e.deporte AS deporte, e.fechaHora AS fechaHora, " +
            "u.id AS deUsuarioId, u.nombreUsuario AS deNombreUsuario " +
            "FROM invitaciones i " +
            "INNER JOIN eventos e ON e.id = i.eventoId " +
            "INNER JOIN usuarios u ON u.id = i.deUsuarioId " +
            "WHERE i.paraUsuarioId = :usuarioId AND i.estado = 'PENDIENTE' " +
            "ORDER BY e.fechaHora ASC, i.id ASC"
    )
    fun observarPendientes(usuarioId: Long): Flow<List<InvitacionPendiente>>

    /**
     * Cambia el estado de la invitación [id] a [estado], pero SOLO si es para [usuarioId] y sigue PENDIENTE.
     * Devuelve cuántas filas ha cambiado: 1 si se ha cambiado, 0 si no existe, es de otro usuario o ya
     * estaba respondida. Se hace en una sola sentencia para que dos toques seguidos no la respondan dos veces.
     */
    @Query(
        "UPDATE invitaciones SET estado = :estado " +
            "WHERE id = :id AND paraUsuarioId = :usuarioId AND estado = 'PENDIENTE'"
    )
    suspend fun responderSiPendiente(id: Long, usuarioId: Long, estado: EstadoInvitacion): Int

    // --- Aceptar una invitación (Issue 73): apuntarse al evento ---

    /** El evento de la invitación, para conocer su cupo máximo. null si ya no existe. */
    @Query("SELECT * FROM eventos WHERE id = :eventoId")
    suspend fun buscarEvento(eventoId: Long): Evento?

    /** Cuántas personas están ya apuntadas al evento. */
    @Query("SELECT COUNT(*) FROM participaciones WHERE eventoId = :eventoId")
    suspend fun contarParticipantes(eventoId: Long): Int

    /** true si [usuarioId] ya está apuntado al evento. */
    @Query("SELECT EXISTS(SELECT 1 FROM participaciones WHERE eventoId = :eventoId AND usuarioId = :usuarioId)")
    suspend fun yaParticipa(eventoId: Long, usuarioId: Long): Boolean

    /** Apunta al usuario al evento. Si ya estaba apuntado no hace nada y devuelve -1. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarParticipacion(participacion: Participacion): Long
}
