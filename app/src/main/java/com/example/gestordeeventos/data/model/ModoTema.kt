package com.example.gestordeeventos.data.model

import androidx.annotation.StringRes
import com.example.gestordeeventos.R

/** Aspecto de la app que elige el usuario en Ajustes. Se guarda por su nombre (p. ej. "OSCURO"). */
enum class ModoTema(@StringRes val etiqueta: Int) {
    SISTEMA(R.string.ajustes_tema_sistema),
    CLARO(R.string.ajustes_tema_claro),
    OSCURO(R.string.ajustes_tema_oscuro);

    /** true si hay que pintar la app en oscuro. [sistemaOscuro] es lo que tiene activado el móvil. */
    fun esOscuro(sistemaOscuro: Boolean): Boolean = when (this) {
        SISTEMA -> sistemaOscuro
        CLARO -> false
        OSCURO -> true
    }

    companion object {
        /** Convierte el texto guardado en el modo; si no existe o es desconocido, sigue al sistema. */
        fun desdeNombre(nombre: String?): ModoTema = entries.firstOrNull { it.name == nombre } ?: SISTEMA
    }
}
