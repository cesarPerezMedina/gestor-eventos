package com.example.gestordeeventos.data.repositorio

import com.example.gestordeeventos.data.local.dao.InvitacionDao
import com.example.gestordeeventos.data.model.InvitacionPendiente
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Lógica de las invitaciones a eventos. Lee de la base de datos local (Room). */
class InvitacionRepositorio(private val invitacionDao: InvitacionDao) {

    /** Invitaciones pendientes que ha recibido [usuarioId]; se actualiza sola al cambiar la base de datos. */
    fun observarPendientes(usuarioId: Long): Flow<List<InvitacionPendiente>> =
        invitacionDao.observarPendientes(usuarioId)

    /** Cuántas invitaciones pendientes tiene [usuarioId] (por ejemplo, para un globo con el número). */
    fun observarNumeroPendientes(usuarioId: Long): Flow<Int> =
        invitacionDao.observarPendientes(usuarioId).map { it.size }
}
