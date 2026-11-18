package com.example.gestordeeventos.ui.buscar

import java.text.Normalizer

/** Id del chip "Todos": no filtra por deporte. */
private const val FILTRO_TODOS = "todos"

private val MARCAS = Regex("\\p{InCombiningDiacriticalMarks}+")
private val ESPACIOS = Regex("\\s+")

/** Minúsculas, sin tildes y sin espacios sobrantes: "  Pádel " -> "padel". */
private fun normalizarBusqueda(texto: String): String =
    Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD).replace(MARCAS, "")

/**
 * Aplica a la lista de Buscar lo que ha elegido el usuario en la pantalla.
 *
 * - [consulta]: el texto del buscador. El evento debe contener TODAS sus palabras en el título o en el lugar
 *   (el buscador dice "Nombre de evento, pista o club..."), sin importar mayúsculas ni tildes.
 *   Con la consulta vacía no se filtra por texto.
 * - [deporteId]: el chip de deporte elegido (el nombre del enum `Deporte`). "todos" o vacío no filtra.
 * - [ordenarPorNombre]: true en la pestaña "Por nombre": orden alfabético por título.
 */
fun filtrarEventos(
    eventos: List<EventoBuscarItem>,
    consulta: String,
    deporteId: String,
    ordenarPorNombre: Boolean = false,
): List<EventoBuscarItem> {
    val palabras = normalizarBusqueda(consulta).split(ESPACIOS).filter { it.isNotEmpty() }
    val filtrados = eventos.filter { evento ->
        val coincideDeporte = deporteId.isBlank() || deporteId == FILTRO_TODOS || evento.deporteId == deporteId
        val texto = normalizarBusqueda("${evento.titulo} ${evento.lugar}")
        coincideDeporte && palabras.all { it in texto }
    }
    return if (ordenarPorNombre) filtrados.sortedBy { normalizarBusqueda(it.titulo) } else filtrados
}
