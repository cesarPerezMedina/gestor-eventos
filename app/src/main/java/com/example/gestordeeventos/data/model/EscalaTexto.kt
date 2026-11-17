package com.example.gestordeeventos.data.model

import androidx.annotation.StringRes
import com.example.gestordeeventos.R

/**
 * Tamaño del texto de la app (accesibilidad). [factor] multiplica el tamaño de todos los textos
 * (1.0 = normal). Se guarda por su nombre (p. ej. "GRANDE").
 */
enum class EscalaTexto(val factor: Float, @StringRes val etiqueta: Int) {
    PEQUENO(0.85f, R.string.ajustes_texto_pequeno),
    NORMAL(1f, R.string.ajustes_texto_normal),
    GRANDE(1.15f, R.string.ajustes_texto_grande),
    MUY_GRANDE(1.3f, R.string.ajustes_texto_muy_grande);

    companion object {
        /** Convierte el texto guardado en la escala; si no existe o es desconocido, tamaño normal. */
        fun desdeNombre(nombre: String?): EscalaTexto = entries.firstOrNull { it.name == nombre } ?: NORMAL
    }
}
