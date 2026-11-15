package com.example.gestordeeventos.ui.buscar

import java.text.Normalizer

/**
 * Filtra los eventos por lo que escribe el usuario en el buscador ("Nombre de evento, pista o club...").
 * Ignora mayúsculas y tildes ("FUTBOL" encuentra "Fútbol") y mira el título y el lugar del evento.
 * Con la consulta vacía o solo con espacios devuelve la lista entera.
 */
fun filtrarPorNombre(eventos: List<EventoBuscarItem>, consulta: String): List<EventoBuscarItem> {
    val buscado = normalizar(consulta)
    if (buscado.isEmpty()) return eventos
    return eventos.filter { normalizar(it.titulo).contains(buscado) || normalizar(it.lugar).contains(buscado) }
}

/** Minúsculas y sin tildes, y sin espacios sobrantes en los lados. */
private fun normalizar(texto: String): String =
    Normalizer.normalize(texto.trim().lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
