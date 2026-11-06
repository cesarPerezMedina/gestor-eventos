package com.example.gestordeeventos.data.model

import androidx.annotation.StringRes
import com.example.gestordeeventos.R

/** Tipos de deporte disponibles al crear un evento. Room lo guarda como texto con el nombre (p. ej. "FUTBOL"). */
enum class Deporte(@StringRes val etiqueta: Int) {
    FUTBOL(R.string.deporte_futbol),
    BALONCESTO(R.string.deporte_baloncesto),
    TENIS(R.string.deporte_tenis),
    PADEL(R.string.deporte_padel),
    ATLETISMO(R.string.deporte_atletismo),
    OTROS(R.string.deporte_otros)
}
