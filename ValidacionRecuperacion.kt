package com.example.gestordeeventos.ui.auth

/** Validaciones de la pantalla "He olvidado mi contraseña" (Issue 14, independiente de la Issue 12). */
internal object ValidacionRecuperacion {

    const val LONGITUD_MINIMA_CLAVE = 8

    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /** Devuelve el mensaje de error, o null si el correo es válido. */
    fun errorCorreo(correo: String): String? = when {
        correo.isBlank() -> "Introduce tu correo electrónico"
        !REGEX_CORREO.matches(correo.trim()) -> "El formato del correo no es válido"
        else -> null
    }

    /** Devuelve el mensaje de error, o null si la contraseña es segura. */
    fun errorClave(clave: String): String? = when {
        clave.isEmpty() -> "Introduce una contraseña"
        clave.length < LONGITUD_MINIMA_CLAVE -> "Debe tener al menos $LONGITUD_MINIMA_CLAVE caracteres"
        clave.none { it.isUpperCase() } -> "Debe incluir una mayúscula"
        clave.none { it.isLowerCase() } -> "Debe incluir una minúscula"
        clave.none { it.isDigit() } -> "Debe incluir un número"
        clave.all { it.isLetterOrDigit() } -> "Debe incluir un símbolo (por ejemplo ! o #)"
        else -> null
    }

    fun errorConfirmacion(clave: String, confirmacion: String): String? =
        if (clave != confirmacion) "Las contraseñas no coinciden" else null
}
