package com.example.gestordeeventos.ui.evento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel de "Mis eventos" (Issue 63).
 *
 * Junta en un solo estado los eventos que ha creado el usuario con sesión y los que se ha unido.
 * Vienen de Flows de Room: si se crea, se elimina o se acepta una invitación, las listas se actualizan solas.
 * Los chips, la búsqueda y las pestañas los resuelve la pantalla con [filtrarMisEventos].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EventosViewModel(
    private val repositorio: EventoRepositorio,
    /** (suspend) id del usuario con sesión iniciada, o null si no hay sesión. */
    private val miId: suspend () -> Long?,
) : ViewModel() {

    val estado: StateFlow<EstadoMisEventos> = flow { emit(miId()) }
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(EstadoMisEventos(cargando = false))
            } else {
                combine(
                    repositorio.observarMisCreadosConCupo(id),
                    repositorio.observarMisUnidosConCupo(id),
                ) { creados, unidos ->
                    EstadoMisEventos(
                        creados = creados.map { it.aMiEventoItem(esCreador = true) },
                        unidos = unidos.map { it.aMiEventoItem(esCreador = false) },
                        cargando = false,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EstadoMisEventos(),
        )

    /**
     * Botón "Eliminar" (si lo creé yo) o "Salir" (si me uní) de una tarjeta, ya confirmado en el diálogo.
     * [onResultado] trae el mensaje para enseñar; la lista se actualiza sola.
     */
    fun eliminar(evento: MiEventoItem, onResultado: (ok: Boolean, mensaje: String) -> Unit) {
        viewModelScope.launch {
            val yoId = miId()
            if (yoId == null) {
                onResultado(false, "Inicia sesión para gestionar tus eventos")
                return@launch
            }
            val resultado = if (evento.esCreador) {
                repositorio.eliminarEvento(evento.id, yoId)
            } else {
                repositorio.salirDeEvento(evento.id, yoId)
            }
            onResultado(resultado.ok, resultado.mensaje)
        }
    }
}

class EventosViewModelFactory(
    private val repositorio: EventoRepositorio,
    private val miId: suspend () -> Long?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = EventosViewModel(repositorio, miId) as T
}
