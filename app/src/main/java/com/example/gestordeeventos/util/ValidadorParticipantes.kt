package com.example.gestordeeventos.util

/** Qué está mal en los campos de participantes de un evento. */
enum class ErrorParticipantes {
    MINIMO_INVALIDO,
    MAXIMO_INVALIDO,
    MAXIMO_MENOR_QUE_MINIMO
}

/** Mínimo y máximo de participantes ya validados (los valores de `Evento.minParticipantes` y `maxParticipantes`). */
data class RangoParticipantes(val minimo: Int, val maximo: Int)

/**
 * Validación de los campos de participantes de un evento (Issue 44).
 * Kotlin puro (sin dependencias de Android) para poder probarla con tests unitarios JVM.
 *
 * Reglas: el mínimo y el máximo son números enteros de [VALOR_MINIMO] a [VALOR_MAXIMO], y el máximo no puede
 * ser menor que el mínimo. Los espacios al principio y al final se ignoran.
 */
object ValidadorParticipantes {

    const val VALOR_MINIMO = 1
    const val VALOR_MAXIMO = 1000

    /** Cifras de [VALOR_MAXIMO]: lo máximo que tiene sentido dejar escribir en un campo. */
    const val CIFRAS_MAXIMAS = 4

    /** Error del campo "mínimo", o null si es válido. */
    fun errorMinimo(minimo: String): ErrorParticipantes? =
        if (numeroValido(minimo) == null) ErrorParticipantes.MINIMO_INVALIDO else null

    /** Error del campo "máximo" (que también depende del mínimo), o null si es válido. */
    fun errorMaximo(minimo: String, maximo: String): ErrorParticipantes? {
        val max = numeroValido(maximo)
        val min = numeroValido(minimo)
        return when {
            max == null -> ErrorParticipantes.MAXIMO_INVALIDO
            min != null && max < min -> ErrorParticipantes.MAXIMO_MENOR_QUE_MINIMO
            else -> null
        }
    }

    /** El rango listo para guardar en el evento, o null si algún campo no es válido. */
    fun rango(minimo: String, maximo: String): RangoParticipantes? {
        val min = numeroValido(minimo)
        val max = numeroValido(maximo)
        return if (min != null && max != null && max >= min) RangoParticipantes(min, max) else null
    }

    private fun numeroValido(texto: String): Int? =
        texto.trim().toIntOrNull()?.takeIf { it in VALOR_MINIMO..VALOR_MAXIMO }
}
