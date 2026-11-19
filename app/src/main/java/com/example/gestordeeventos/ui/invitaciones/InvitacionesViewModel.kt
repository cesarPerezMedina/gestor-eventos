package com.example.gestordeeventos.ui.invitaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestordeeventos.R
import com.example.gestordeeventos.data.model.InvitacionPendiente
import com.example.gestordeeventos.data.repositorio.InvitacionRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoRespuestaInvitacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lo que necesita pintar la pantalla de invitaciones (Issue 75).
 * [cargando] es true hasta que llega la primera lista; [mensaje] es un aviso listo para enseñar
 * (null = no hay aviso).
 */
data class InvitacionesUiState(
    val invitaciones: List<InvitacionPendiente> = emptyList(),
    val cargando: Boolean = true,
    val mensaje: String? = null
)

/**
 * Conecta la pantalla de invitaciones con el repositorio.
 *
 * - La lista viene de un `Flow` de Room, así que **se actualiza sola**: al aceptar o rechazar, la invitación
 *   desaparece de las pendientes sin recargar nada.
 * - Aceptar apunta al usuario al evento (Issue 73): en cuanto se guarda, el evento sale en las listas de
 *   eventos del usuario (`EventoRepositorio.observarEventosUnidos`). Al rechazar, el evento no se añade.
 * - Cada respuesta deja un [InvitacionesUiState.mensaje] para decirle al usuario qué ha pasado.
 *
 * [texto] convierte el id de un string de recursos y el nombre del evento en el texto final, así el
 * ViewModel no necesita un Context.
 */
class InvitacionesViewModel(
    private val repositorio: InvitacionRepositorio,
    private val usuarioId: Long,
    private val texto: (Int, String) -> String
) : ViewModel() {

    private val _estado = MutableStateFlow(InvitacionesUiState())
    val estado: StateFlow<InvitacionesUiState> = _estado.asStateFlow()

    /** Invitaciones que se están respondiendo ahora mismo, para que dos toques seguidos no la manden dos veces. */
    private val enCurso = mutableSetOf<Long>()

    init {
        viewModelScope.launch {
            repositorio.observarPendientes(usuarioId).collect { lista ->
                _estado.update { it.copy(invitaciones = lista, cargando = false) }
            }
        }
    }

    fun aceptar(invitacion: InvitacionPendiente) = responder(invitacion, aceptar = true)

    fun rechazar(invitacion: InvitacionPendiente) = responder(invitacion, aceptar = false)

    /** La pantalla lo llama cuando ya ha enseñado el aviso. */
    fun mensajeMostrado() {
        _estado.update { it.copy(mensaje = null) }
    }

    private fun responder(invitacion: InvitacionPendiente, aceptar: Boolean) {
        if (!enCurso.add(invitacion.invitacionId)) return
        viewModelScope.launch {
            try {
                val resultado = if (aceptar) {
                    repositorio.aceptar(invitacion.invitacionId, usuarioId)
                } else {
                    repositorio.rechazar(invitacion.invitacionId, usuarioId)
                }
                val mensaje = texto(textoDe(resultado, aceptar), invitacion.eventoNombre)
                _estado.update { it.copy(mensaje = mensaje) }
            } finally {
                enCurso.remove(invitacion.invitacionId)
            }
        }
    }

    private fun textoDe(resultado: ResultadoRespuestaInvitacion, aceptar: Boolean): Int = when (resultado) {
        ResultadoRespuestaInvitacion.OK ->
            if (aceptar) R.string.invitaciones_msg_aceptada else R.string.invitaciones_msg_rechazada
        ResultadoRespuestaInvitacion.EVENTO_COMPLETO -> R.string.invitaciones_msg_completo
        ResultadoRespuestaInvitacion.NO_EXISTE -> R.string.invitaciones_msg_no_existe
        ResultadoRespuestaInvitacion.NO_ES_PARA_TI -> R.string.invitaciones_msg_no_es_para_ti
        ResultadoRespuestaInvitacion.YA_RESPONDIDA -> R.string.invitaciones_msg_ya_respondida
    }
}

class InvitacionesViewModelFactory(
    private val repositorio: InvitacionRepositorio,
    private val usuarioId: Long,
    private val texto: (Int, String) -> String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        InvitacionesViewModel(repositorio, usuarioId, texto) as T
}
