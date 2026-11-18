package com.example.gestordeeventos.ui.inicio

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Groups
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestordeeventos.data.model.EstadoEvento
import com.example.gestordeeventos.data.model.EventoConCupo
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import com.example.gestordeeventos.ui.buscar.textoCuando
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lo que pinta la pantalla de Inicio: el próximo evento del usuario y las sugerencias para unirse. */
data class EstadoInicio(
    val eventos: List<EventoResumen> = emptyList(),
    val proximo: ProximoEvento? = null,
    val cargando: Boolean = true,
)

/** Cuántas sugerencias como máximo se enseñan en Inicio. */
const val MAX_SUGERENCIAS_INICIO = 10

/**
 * ViewModel de la pantalla de Inicio.
 *
 * - "Tu agenda deportiva": el evento más próximo de los que he creado o a los que me he unido.
 * - "Eventos disponibles": eventos públicos futuros a los que todavía puedo unirme.
 * Todo viene de Flows de Room: si alguien crea un evento o me apunto, la pantalla se actualiza sola.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class InicioViewModel(
    private val repositorio: EventoRepositorio,
    /** (suspend) id del usuario con sesión iniciada, o null si no hay sesión. */
    private val miId: suspend () -> Long?,
    private val ahora: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    val estado: StateFlow<EstadoInicio> = flow { emit(miId()) }
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(EstadoInicio(cargando = false))
            } else {
                combine(
                    repositorio.eventosPublicosConCupo(id),
                    repositorio.observarMisCreadosConCupo(id),
                    repositorio.observarMisUnidosConCupo(id),
                ) { publicos, creados, unidos ->
                    val momento = ahora()
                    EstadoInicio(
                        eventos = seleccionarSugerencias(publicos, id, momento),
                        proximo = seleccionarProximo(creados + unidos, momento),
                        cargando = false,
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EstadoInicio(),
        )

    /**
     * Botón "Unirme" de una sugerencia: apunta al usuario con sesión al evento.
     * [onResultado] trae el mensaje para enseñar; la lista se actualiza sola (el evento deja de ser sugerencia).
     */
    fun unirse(idEvento: String, onResultado: (ok: Boolean, mensaje: String) -> Unit) {
        val id = idEvento.toLongOrNull()
        if (id == null) {
            onResultado(false, "Evento no válido")
            return
        }
        viewModelScope.launch {
            val yoId = miId()
            if (yoId == null) {
                onResultado(false, "Inicia sesión para apuntarte a un evento")
                return@launch
            }
            val resultado = repositorio.apuntarse(id, yoId)
            onResultado(resultado.ok, resultado.mensaje)
        }
    }
}

class InicioViewModelFactory(
    private val repositorio: EventoRepositorio,
    private val miId: suspend () -> Long?,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = InicioViewModel(repositorio, miId) as T
}

// ---------- De Room a lo que pinta la pantalla ----------

/**
 * Eventos públicos a los que [miId] puede unirse ahora mismo: publicados, que no han empezado, que no
 * ha creado él, a los que no está apuntado y que no están completos. Del más próximo al más lejano.
 */
internal fun seleccionarSugerencias(
    publicos: List<EventoConCupo>,
    miId: Long,
    ahora: Long,
    maximo: Int = MAX_SUGERENCIAS_INICIO,
): List<EventoResumen> =
    publicos
        .filter { it.evento.publico && it.evento.estado == EstadoEvento.PUBLICADO }
        .filter { it.evento.fechaHora >= ahora }
        .filter { it.evento.creadorId != miId && !it.yoApuntado }
        .filter { !it.estaCompleto() }
        .sortedBy { it.evento.fechaHora }
        .take(maximo)
        .map { it.aResumen() }

/** El evento más próximo (que no haya empezado) de los que ha creado o a los que se ha unido el usuario. */
internal fun seleccionarProximo(misEventos: List<EventoConCupo>, ahora: Long): ProximoEvento? =
    misEventos
        .filter { it.evento.estado == EstadoEvento.PUBLICADO && it.evento.fechaHora >= ahora }
        .minByOrNull { it.evento.fechaHora }
        ?.let {
            ProximoEvento(
                titulo = it.evento.nombre,
                lugar = it.evento.direccion.ifBlank { "Sin ubicación" },
                hora = textoCuando(it.evento.fechaHora),
            )
        }

private fun EventoConCupo.estaCompleto(): Boolean =
    evento.maxParticipantes > 0 && apuntados >= evento.maxParticipantes

private fun EventoConCupo.aResumen(): EventoResumen {
    val maximo = evento.maxParticipantes
    val faltan = maximo - apuntados
    val urgente = maximo > 0 && faltan == 1
    val inscritos = if (maximo > 0) "$apuntados/$maximo inscritos" else "$apuntados inscritos"
    val aviso = when {
        maximo <= 0 -> "Sin límite de plazas"
        urgente -> "¡Última plaza disponible!"
        else -> "$faltan plazas libres"
    }
    return EventoResumen(
        id = evento.id.toString(),
        titulo = evento.nombre,
        deporte = evento.deporte.name,
        detalle = "${textoCuando(evento.fechaHora)} · $inscritos",
        inscritos = apuntados,
        plazas = maximo,
        aviso = aviso,
        avisoIcono = if (urgente) Icons.Outlined.FlashOn else Icons.Outlined.Groups,
        urgente = urgente,
    )
}
