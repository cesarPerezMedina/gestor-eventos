package com.example.gestordeeventos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.InvitacionPendiente
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
}
