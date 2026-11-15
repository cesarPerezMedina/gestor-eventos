package com.example.gestordeeventos.data.repositorio

import com.example.gestordeeventos.data.local.dao.InvitacionDao
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.InvitacionPendiente
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Qué ha pasado al responder a una invitación. La pantalla decide el texto que enseña en cada caso. */
enum class ResultadoRespuestaInvitacion {
    /** Se ha guardado la respuesta. */
    OK,

    /** La invitación no existe (por ejemplo, se borró el evento). */
    NO_EXISTE,

    /** La invitación es de otro usuario: nadie puede responder por los demás. */
    NO_ES_PARA_TI,

    /** Ya se había aceptado o rechazado antes; no se puede cambiar. */
    YA_RESPONDIDA
}

/** Lógica de las invitaciones a eventos. Lee y guarda en la base de datos local (Room). */
class InvitacionRepositorio(private val invitacionDao: InvitacionDao) {

    /** Invitaciones pendientes que ha recibido [usuarioId]; se actualiza sola al cambiar la base de datos. */
    fun observarPendientes(usuarioId: Long): Flow<List<InvitacionPendiente>> =
        invitacionDao.observarPendientes(usuarioId)

    /** Cuántas invitaciones pendientes tiene [usuarioId] (por ejemplo, para un globo con el número). */
    fun observarNumeroPendientes(usuarioId: Long): Flow<Int> =
        invitacionDao.observarPendientes(usuarioId).map { it.size }

    /**
     * [usuarioId] rechaza la invitación [invitacionId]. Pasa a RECHAZADA (no se borra) y deja de salir
     * en las pendientes. Solo puede rechazarla quien la recibió y mientras siga pendiente.
     */
    suspend fun rechazar(invitacionId: Long, usuarioId: Long): ResultadoRespuestaInvitacion =
        responder(invitacionId, usuarioId, EstadoInvitacion.RECHAZADA)

    private suspend fun responder(
        invitacionId: Long,
        usuarioId: Long,
        estado: EstadoInvitacion
    ): ResultadoRespuestaInvitacion {
        if (invitacionDao.responderSiPendiente(invitacionId, usuarioId, estado) > 0) {
            return ResultadoRespuestaInvitacion.OK
        }
        // No se ha cambiado nada: miramos por qué para dar un resultado claro.
        val invitacion = invitacionDao.buscarPorId(invitacionId)
            ?: return ResultadoRespuestaInvitacion.NO_EXISTE
        return if (invitacion.paraUsuarioId != usuarioId) {
            ResultadoRespuestaInvitacion.NO_ES_PARA_TI
        } else {
            ResultadoRespuestaInvitacion.YA_RESPONDIDA
        }
    }
}
