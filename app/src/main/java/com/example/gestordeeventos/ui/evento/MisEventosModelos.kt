package com.example.gestordeeventos.ui.evento

import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EstadoEvento
import com.example.gestordeeventos.data.model.EventoConCupo
import com.example.gestordeeventos.data.model.HORAS_HASTA_ARCHIVAR
import java.text.Normalizer
import java.time.Instant
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

// --- Modelos de la pantalla "Mis eventos" (Issue 63): solo lo que se pinta y las reglas para clasificarlo ---

/** Pestañas de arriba: eventos que he creado / eventos a los que me he unido. */
enum class PestanaMisEventos { CREADOS, UNIDOS }

/** Chips de la segunda fila. Cada evento pertenece a exactamente uno (ver [categoria]). */
enum class FiltroMisEventos { PROXIMOS, HISTORIAL, BORRADORES, CANCELADOS }

/** Etiqueta pequeña de la tarjeta ("Público", "Privado", "Confirmado"...). */
enum class EtiquetaEvento { PUBLICO, PRIVADO, CONFIRMADO, BORRADOR, CANCELADO, ABANDONADO }

private const val MILIS_POR_HORA = 3_600_000L
private val IDIOMA_ES: Locale = Locale.forLanguageTag("es-ES")
private val MARCAS_DIACRITICAS = Regex("\\p{InCombiningDiacriticalMarks}+")

/** Un evento tal como lo pinta la tarjeta de "Mis eventos". */
data class MiEventoItem(
    val id: Long,
    val nombre: String,
    val deporte: Deporte,
    val fechaHora: Long,
    val lugar: String,
    val apuntados: Int,
    /** Mínimo de jugadores para que se juegue (0 = sin mínimo). */
    val minimo: Int,
    /** Plazas totales (0 = sin límite). */
    val maximo: Int,
    val publico: Boolean,
    val estado: EstadoEvento,
    /** true en la pestaña Creados (puede eliminarlo); false en Unidos (solo puede salir). */
    val esCreador: Boolean,
    /** true si me uní y luego salí: el evento sigue saliendo en Unidos > Cancelados, como "Abandonado". */
    val abandonado: Boolean = false,
) {
    val completo: Boolean get() = maximo > 0 && apuntados >= maximo
    val faltanParaJugar: Int get() = (minimo - apuntados).coerceAtLeast(0)
    val plazasLibres: Int get() = if (maximo > 0) (maximo - apuntados).coerceAtLeast(0) else 0

    /** En pádel y tenis se habla de "inscritos"; en el resto, de "plazas". */
    val hablaDeInscritos: Boolean get() = deporte == Deporte.PADEL || deporte == Deporte.TENIS

    val etiqueta: EtiquetaEvento
        get() = when {
            abandonado -> EtiquetaEvento.ABANDONADO
            estado == EstadoEvento.BORRADOR -> EtiquetaEvento.BORRADOR
            estado == EstadoEvento.CANCELADO -> EtiquetaEvento.CANCELADO
            completo -> EtiquetaEvento.CONFIRMADO
            publico -> EtiquetaEvento.PUBLICO
            else -> EtiquetaEvento.PRIVADO
        }
}

/** Lo que recibe la pantalla: las dos listas ya cargadas desde Room. */
data class EstadoMisEventos(
    val creados: List<MiEventoItem> = emptyList(),
    /** Incluye los que he abandonado (con [MiEventoItem.abandonado] en true). */
    val unidos: List<MiEventoItem> = emptyList(),
    val cargando: Boolean = true,
)

/** De Room a la tarjeta. [esCreador] depende de la pestaña de la que venga la lista. */
fun EventoConCupo.aMiEventoItem(esCreador: Boolean): MiEventoItem = MiEventoItem(
    id = evento.id,
    nombre = evento.nombre,
    deporte = evento.deporte,
    fechaHora = evento.fechaHora,
    lugar = evento.direccion,
    apuntados = apuntados,
    minimo = evento.minParticipantes,
    maximo = evento.maxParticipantes,
    publico = evento.publico,
    estado = evento.estado,
    esCreador = esCreador,
)

/** Un evento que el usuario abandonó, tal como se pinta en Unidos > Cancelados. */
fun EventoConCupo.aMiEventoAbandonado(): MiEventoItem = aMiEventoItem(esCreador = false).copy(abandonado = true)

/**
 * En qué chip cae el evento. Un evento publicado sigue en "Próximos" hasta [HORAS_HASTA_ARCHIVAR] horas
 * después de su fecha (todavía no se guarda cuánto dura) y luego pasa al historial.
 */
fun MiEventoItem.categoria(ahora: Long): FiltroMisEventos = if (abandonado) FiltroMisEventos.CANCELADOS else when (estado) {
    EstadoEvento.BORRADOR -> FiltroMisEventos.BORRADORES
    EstadoEvento.CANCELADO -> FiltroMisEventos.CANCELADOS
    EstadoEvento.ARCHIVADO -> FiltroMisEventos.HISTORIAL
    EstadoEvento.PUBLICADO ->
        if (fechaHora + HORAS_HASTA_ARCHIVAR * MILIS_POR_HORA > ahora) FiltroMisEventos.PROXIMOS
        else FiltroMisEventos.HISTORIAL
}

/** Cuántos eventos de la lista caen en [filtro]. */
fun List<MiEventoItem>.contar(filtro: FiltroMisEventos, ahora: Long): Int = count { it.categoria(ahora) == filtro }

/**
 * Eventos del chip [filtro] cuyo nombre, deporte o lugar contienen TODAS las palabras de [consulta]
 * (sin importar mayúsculas ni tildes). Próximos va del más cercano al más lejano; el historial, del más reciente.
 * [etiquetaDeporte] da el texto que ve el usuario ("Pádel"), porque el enum solo guarda un id de recurso.
 */
fun filtrarMisEventos(
    eventos: List<MiEventoItem>,
    filtro: FiltroMisEventos,
    consulta: String,
    ahora: Long,
    etiquetaDeporte: (Deporte) -> String,
): List<MiEventoItem> {
    val palabras = consulta.normalizar().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val delFiltro = eventos.filter { it.categoria(ahora) == filtro }
    val coinciden = if (palabras.isEmpty()) {
        delFiltro
    } else {
        delFiltro.filter { evento ->
            val texto = "${evento.nombre} ${etiquetaDeporte(evento.deporte)} ${evento.lugar}".normalizar()
            palabras.all { it in texto }
        }
    }
    return if (filtro == FiltroMisEventos.HISTORIAL) coinciden.sortedByDescending { it.fechaHora }
    else coinciden.sortedBy { it.fechaHora }
}

/** "Sáb 14 Nov • 18:00". */
fun textoFechaHora(millis: Long): String {
    val fecha = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
    val dia = fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, IDIOMA_ES).abreviar()
    val mes = fecha.month.getDisplayName(TextStyle.SHORT, IDIOMA_ES).abreviar()
    val hora = String.format(Locale.US, "%02d:%02d", fecha.hour, fecha.minute)
    return "$dia ${fecha.dayOfMonth} $mes • $hora"
}

private fun String.abreviar(): String = trimEnd('.').replaceFirstChar { it.uppercase() }

/** Minúsculas, sin tildes y sin espacios sobrantes: "  Pádel " -> "padel". */
private fun String.normalizar(): String =
    Normalizer.normalize(trim().lowercase(Locale.getDefault()), Normalizer.Form.NFD)
        .replace(MARCAS_DIACRITICAS, "")