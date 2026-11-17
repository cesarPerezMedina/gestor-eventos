package com.example.gestordeeventos

import com.example.gestordeeventos.data.local.dao.InvitacionDao
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.InvitacionPendiente
import com.example.gestordeeventos.data.model.Participacion
import com.example.gestordeeventos.data.model.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * DAO falso en memoria para probar el repositorio de invitaciones sin base de datos ni emulador.
 * Imita la consulta real: une cada invitación con su evento y con quien invita.
 * (La SQL de verdad se comprueba en el test con emulador `InvitacionDaoTest`.)
 */
class FakeInvitacionDao(
    private val eventos: List<Evento>,
    private val usuarios: List<Usuario>
) : InvitacionDao {

    private val invitaciones = MutableStateFlow<List<Invitacion>>(emptyList())
    private val participaciones = mutableListOf<Participacion>()

    override suspend fun insertar(invitacion: Invitacion): Long {
        val id = invitaciones.value.size + 1L
        invitaciones.value = invitaciones.value + invitacion.copy(id = id)
        return id
    }

    override suspend fun buscarPorId(id: Long): Invitacion? = invitaciones.value.firstOrNull { it.id == id }

    override suspend fun buscarEvento(eventoId: Long): Evento? = eventos.firstOrNull { it.id == eventoId }

    override suspend fun contarParticipantes(eventoId: Long): Int = participaciones.count { it.eventoId == eventoId }

    override suspend fun yaParticipa(eventoId: Long, usuarioId: Long): Boolean =
        participaciones.any { it.eventoId == eventoId && it.usuarioId == usuarioId }

    override suspend fun insertarParticipacion(participacion: Participacion): Long {
        if (yaParticipa(participacion.eventoId, participacion.usuarioId)) return -1
        participaciones.add(participacion)
        return participaciones.size.toLong()
    }

    /** Para los tests: ids de los usuarios apuntados al evento, en orden de llegada. */
    fun participantesDe(eventoId: Long): List<Long> =
        participaciones.filter { it.eventoId == eventoId }.map { it.usuarioId }

    /** Para los tests: el estado actual de una invitación. */
    fun estadoDe(invitacionId: Long): EstadoInvitacion? =
        invitaciones.value.firstOrNull { it.id == invitacionId }?.estado

    override suspend fun responderSiPendiente(id: Long, usuarioId: Long, estado: EstadoInvitacion): Int {
        val actual = invitaciones.value
        val indice = actual.indexOfFirst {
            it.id == id && it.paraUsuarioId == usuarioId && it.estado == EstadoInvitacion.PENDIENTE
        }
        if (indice == -1) return 0
        invitaciones.value = actual.toMutableList().also { it[indice] = it[indice].copy(estado = estado) }
        return 1
    }

    override fun observarPendientes(usuarioId: Long) = invitaciones.map { todas ->
        todas
            .filter { it.paraUsuarioId == usuarioId && it.estado == EstadoInvitacion.PENDIENTE }
            .mapNotNull { invitacion ->
                val evento = eventos.firstOrNull { it.id == invitacion.eventoId }
                val quienInvita = usuarios.firstOrNull { it.id == invitacion.deUsuarioId }
                if (evento == null || quienInvita == null) {
                    null
                } else {
                    InvitacionPendiente(
                        invitacionId = invitacion.id,
                        eventoId = evento.id,
                        eventoNombre = evento.nombre,
                        deporte = evento.deporte,
                        fechaHora = evento.fechaHora,
                        deUsuarioId = quienInvita.id,
                        deNombreUsuario = quienInvita.nombreUsuario
                    )
                }
            }
            .sortedWith(compareBy({ it.fechaHora }, { it.invitacionId }))
    }
}
