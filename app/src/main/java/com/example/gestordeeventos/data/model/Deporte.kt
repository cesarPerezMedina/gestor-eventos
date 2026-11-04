package com.example.gestordeeventos.data.model

/** Tipos de deporte disponibles al crear un evento. Room lo guarda como texto con el nombre (p. ej. "FUTBOL"). */
enum class Deporte(val etiqueta: String) {
    FUTBOL("Fútbol"),
    BALONCESTO("Baloncesto"),
    TENIS("Tenis"),
    PADEL("Pádel"),
    ATLETISMO("Atletismo"),
    OTROS("Otros")
}
