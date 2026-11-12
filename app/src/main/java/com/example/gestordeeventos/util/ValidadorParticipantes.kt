package com.example.gestordeeventos.util

/** Qué falla en el número de participantes de un evento. La pantalla lo traduce a un texto (español o inglés). */
enum class ErrorParticipantes {
    MINIMO_VACIO,
    MAXIMO_VACIO,
    FUERA_DE_RANGO,
    MAXIMO_MENOR_QUE_MINIMO
}

/**
 * Reglas del número de participantes (mínimo y máximo) al crear un evento (Issue 44).
 * Kotlin puro, sin dependencias de Android, para poder probarlo con tests unitarios JVM.
 *
 * Ambos números son obligatorios, van de [MINIMO_PERMITIDO] a [MAXIMO_PERMITIDO] y el máximo no puede ser
 * menor que el mínimo (pueden ser iguales). Los datos llegan como texto porque vienen de campos de texto.
 */
object ValidadorParticipantes {

    const val MINIMO_PERMITIDO = 1
    const val MAXIMO_PERMITIDO = 999

    private val rango = MINIMO_PERMITIDO..MAXIMO_PERMITIDO

    /** Devuelve qué falla en el mínimo, o null si es válido. */
    fun validarMinimo(minimo: String): ErrorParticipantes? {
        val valor = minimo.trim()
        val numero = valor.toIntOrNull()
        return when {
            valor.isEmpty() -> ErrorParticipantes.MINIMO_VACIO
            numero == null || numero !in rango -> ErrorParticipantes.FUERA_DE_RANGO
            else -> null
        }
    }

    /**
     * Devuelve qué falla en el máximo, o null si es válido.
     * Se compara con [minimo] solo si el mínimo es un número válido; si no, ese error ya se avisa en su campo.
     */
    fun validarMaximo(minimo: String, maximo: String): ErrorParticipantes? {
        val valor = maximo.trim()
        val numero = valor.toIntOrNull()
        val numeroMinimo = minimo.trim().toIntOrNull()?.takeIf { it in rango }
        return when {
            valor.isEmpty() -> ErrorParticipantes.MAXIMO_VACIO
            numero == null || numero !in rango -> ErrorParticipantes.FUERA_DE_RANGO
            numeroMinimo != null && numero < numeroMinimo -> ErrorParticipantes.MAXIMO_MENOR_QUE_MINIMO
            else -> null
        }
    }

    /** true si el mínimo y el máximo son válidos a la vez. */
    fun sonValidos(minimo: String, maximo: String): Boolean =
        validarMinimo(minimo) == null && validarMaximo(minimo, maximo) == null
}
