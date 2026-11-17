package com.example.gestordeeventos

import com.example.gestordeeventos.data.local.dao.InvitacionDao
import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.data.model.EstadoInvitacion
import com.example.gestordeeventos.data.model.Invitacion
import com.example.gestordeeventos.data.model.InvitacionPendiente
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

    override suspend fun insertar(invitacion: Invitacion): Long {
        val id = invitaciones.value.size + 1L
        invitaciones.value = invitaciones.value + invitacion.copy(id = id)
        return id
    }

    override suspend fun buscarPorId(id: Long): Invitacion? = invitaciones.value.firstOrNull { it.id == id }

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
