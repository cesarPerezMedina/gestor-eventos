package com.example.gestordeeventos.ui.buscar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gestordeeventos.data.localizacion.Ubicacion
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EventoConCupo
import com.example.gestordeeventos.data.repositorio.EventoRepositorio
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Lo que pinta la pantalla Buscar: la lista de eventos y los chips de deporte con su cantidad. */
data class EstadoBuscar(
    val eventos: List<EventoBuscarItem> = emptyList(),
    val filtrosDeporte: List<FiltroDeporteBuscar> = listOf(FiltroDeporteBuscar("todos", "Todos", null, 0)),
)

/**
 * ViewModel de "Buscar eventos" (Issues 56 y 57).
 *
 * - Solo trabaja con eventos públicos (la consulta de Room ya descarta los privados y aquí se vuelve a comprobar).
 * - En "Cerca de mí", si se conoce la ubicación del usuario, enseña los eventos a menos de [radio] km,
 *   del más cercano al más lejano, con su distancia.
 * - Si no hay ubicación (sin permiso, GPS apagado...) o se busca "Por nombre", enseña todos los públicos por fecha.
 * Todo viene de Flows de Room: si alguien crea un evento o se apunta, la lista se actualiza sola.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class BuscarViewModel(private val repositorio: EventoRepositorio) : ViewModel() {

    private val ubicacion = MutableStateFlow<Ubicacion?>(null)
    private val radioKm = MutableStateFlow(RADIOS_KM.first())
    private val buscarCerca = MutableStateFlow(true)

    val estado: StateFlow<EstadoBuscar> = combine(ubicacion, radioKm, buscarCerca) { u, r, cerca ->
        ParametrosBusqueda(if (cerca) u else null, r)
    }.flatMapLatest { parametros ->
        val centro = parametros.centro
        if (centro != null) {
            repositorio.eventosPublicosCerca(centro, parametros.radioKm.toDouble())
                .map { lista -> lista.filter { it.evento.evento.publico }.map { it.evento to it.distanciaKm }.aEstado() }
        } else {
            repositorio.eventosPublicosConCupo()
                .map { lista -> lista.filter { it.evento.publico }.map { it to null }.aEstado() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EstadoBuscar(),
    )

    /** La pantalla avisa cuando obtiene la ubicación del usuario. */
    fun actualizarUbicacion(nueva: Ubicacion?) {
        ubicacion.value = nueva
    }

    /** Chips "< 2 km", "5 km"... */
    fun cambiarRadio(km: Int) {
        radioKm.value = km
    }

    /** Pestañas "Cerca de mí" / "Por nombre". */
    fun cambiarModo(modo: ModoBusqueda) {
        buscarCerca.value = modo == ModoBusqueda.CERCA_DE_MI
    }
}

private data class ParametrosBusqueda(val centro: Ubicacion?, val radioKm: Int)

class BuscarViewModelFactory(
    private val repositorio: EventoRepositorio,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = BuscarViewModel(repositorio) as T
}

// ---------- De Room a lo que pinta la pantalla ----------

/** Cada pareja es un evento y su distancia en km (null si no se conoce). */
private fun List<Pair<EventoConCupo, Double?>>.aEstado(): EstadoBuscar {
    val eventos = map { (evento, distancia) -> evento.aItem(distancia) }
    val porDeporte = groupingBy { it.first.evento.deporte }.eachCount()
    val chips = porDeporte.map { (deporte, cantidad) ->
        FiltroDeporteBuscar(
            id = deporte.name,
            nombre = nombreDe(deporte),
            icono = iconoDe(deporte),
            cantidad = cantidad,
        )
    }
    return EstadoBuscar(
        eventos = eventos,
        filtrosDeporte = listOf(FiltroDeporteBuscar("todos", "Todos", null, eventos.size)) + chips,
    )
}

private fun EventoConCupo.aItem(distanciaKm: Double?): EventoBuscarItem {
    val maximo = evento.maxParticipantes
    val faltan = maximo - apuntados
    val (etiqueta, estilo) = when {
        maximo > 0 && faltan <= 0 -> "Completo" to EstiloEtiqueta.ALERTA
        maximo > 0 && faltan == 1 -> "¡Última plaza!" to EstiloEtiqueta.ALERTA
        else -> "Público" to EstiloEtiqueta.NEUTRA
    }
    return EventoBuscarItem(
        id = evento.id.toString(),
        titulo = evento.nombre,
        icono = iconoDe(evento.deporte),
        cuando = textoCuando(evento.fechaHora),
        distanciaKm = distanciaKm,
        lugar = evento.direccion.ifBlank { "Sin ubicación" },
        apuntados = apuntados,
        maximo = maximo,
        cupoNota = if (evento.minParticipantes > apuntados) "Mín. ${evento.minParticipantes}" else null,
        etiqueta = etiqueta,
        estiloEtiqueta = estilo,
    )
}

/** "Hoy 20:00", "Mañana 17:00", "Sáb 18:00" (esta semana) o "24/12 18:00". */
private fun textoCuando(millis: Long): String {
    val fechaHora = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    val hora = String.format(Locale.US, "%02d:%02d", fechaHora.hour, fechaHora.minute)
    val dias = ChronoUnit.DAYS.between(LocalDate.now(), fechaHora.toLocalDate())
    val dia = when (dias) {
        0L -> "Hoy"
        1L -> "Mañana"
        in 2L..6L -> fechaHora.dayOfWeek
            .getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("es-ES"))
            .trimEnd('.')
            .replaceFirstChar { it.uppercase() }
        else -> "${fechaHora.dayOfMonth}/${fechaHora.monthValue}"
    }
    return "$dia $hora"
}

/** "FUTBOL_SALA" -> "Futbol sala". Cámbialo si tu enum Deporte ya tiene un nombre para mostrar. */
private fun nombreDe(deporte: Deporte): String =
    deporte.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

private fun iconoDe(deporte: Deporte): ImageVector {
    val nombre = deporte.name.uppercase()
    return when {
        "FUTBOL" in nombre || "FÚTBOL" in nombre -> Icons.Outlined.SportsSoccer
        "BALONCESTO" in nombre || "BASKET" in nombre -> Icons.Outlined.SportsBasketball
        "TENIS" in nombre || "PADEL" in nombre || "PÁDEL" in nombre -> Icons.Outlined.SportsTennis
        else -> Icons.Outlined.SportsScore
    }
}