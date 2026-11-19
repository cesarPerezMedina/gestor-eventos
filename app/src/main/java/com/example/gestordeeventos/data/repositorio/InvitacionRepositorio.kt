package com.example.gestordeeventos.data.repositorio

import android.content.Context
import androidx.room.withTransaction
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.local.dao.InvitacionDao
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.InvitacionPendiente
import com.example.gestordeeventos.data.model.Participacion
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
    YA_RESPONDIDA,

    /** Solo al aceptar: el evento ya tiene todas sus plazas ocupadas. La invitación sigue pendiente. */
    EVENTO_COMPLETO
}

/**
 * Lógica de las invitaciones a eventos. Lee y guarda en la base de datos local (Room).
 *
 * [enTransaccion] ejecuta un bloque como una sola operación de base de datos: o se guarda todo o nada.
 * En la app se usa la transacción de Room (ver [crear]); en los tests, que no tienen base de datos, el
 * valor por defecto se limita a ejecutar el bloque.
 */
class InvitacionRepositorio(
    private val invitacionDao: InvitacionDao,
    private val enTransaccion: suspend (suspend () -> ResultadoRespuestaInvitacion) -> ResultadoRespuestaInvitacion =
        { bloque -> bloque() }
) {

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

    /**
     * [usuarioId] acepta la invitación [invitacionId] y se apunta al evento (Issue 73).
     *
     * Funciona igual para eventos privados y públicos. Pasos, todos dentro de una transacción:
     * 1. Comprueba que la invitación existe, es para [usuarioId] y sigue pendiente.
     * 2. Comprueba que el evento existe y que hay plaza (`maxParticipantes` 0 = sin límite).
     *    Si está completo devuelve [ResultadoRespuestaInvitacion.EVENTO_COMPLETO] y no cambia nada:
     *    la invitación sigue pendiente (se puede rechazar).
     * 3. Marca la invitación como ACEPTADA y añade la participación (si ya estaba apuntado no se duplica).
     */
    suspend fun aceptar(invitacionId: Long, usuarioId: Long): ResultadoRespuestaInvitacion =
        enTransaccion { aceptarSinTransaccion(invitacionId, usuarioId) }

    private suspend fun aceptarSinTransaccion(
        invitacionId: Long,
        usuarioId: Long
    ): ResultadoRespuestaInvitacion {
        val invitacion = invitacionDao.buscarPorId(invitacionId)
            ?: return ResultadoRespuestaInvitacion.NO_EXISTE
        if (invitacion.paraUsuarioId != usuarioId) return ResultadoRespuestaInvitacion.NO_ES_PARA_TI
        if (invitacion.estado != EstadoInvitacion.PENDIENTE) return ResultadoRespuestaInvitacion.YA_RESPONDIDA

        val evento = invitacionDao.buscarEvento(invitacion.eventoId)
            ?: return ResultadoRespuestaInvitacion.NO_EXISTE
        val yaParticipa = invitacionDao.yaParticipa(evento.id, usuarioId)
        val hayLimite = evento.maxParticipantes > 0
        if (!yaParticipa && hayLimite && invitacionDao.contarParticipantes(evento.id) >= evento.maxParticipantes) {
            return ResultadoRespuestaInvitacion.EVENTO_COMPLETO
        }

        val resultado = responder(invitacionId, usuarioId, EstadoInvitacion.ACEPTADA)
        if (resultado == ResultadoRespuestaInvitacion.OK && !yaParticipa) {
            invitacionDao.insertarParticipacion(Participacion(eventoId = evento.id, usuarioId = usuarioId))
        }
        return resultado
    }

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

    companion object {
        /** Crea el repositorio con la base de datos local de la aplicación (aceptar va en una transacción). */
        fun crear(context: Context): InvitacionRepositorio {
            val db = AppDatabase.obtener(context)
            return InvitacionRepositorio(db.invitacionDao()) { bloque -> db.withTransaction { bloque() } }
        }
    }
}
