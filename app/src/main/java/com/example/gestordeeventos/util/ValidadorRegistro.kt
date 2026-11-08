package com.example.gestordeeventos.util

/**
 * Resultado de validar un campo del formulario de registro.
 */
sealed class ResultadoValidacion {
    object Valido : ResultadoValidacion()
    data class Error(val mensaje: String) : ResultadoValidacion()

    val esValido: Boolean get() = this is Valido

    /** Mensaje de error o null si el campo es válido (cómodo para Compose: isError / supportingText). */
    val mensajeError: String? get() = (this as? Error)?.mensaje
}

/**
 * Lógica de validación de los campos del registro (Issue 12).
 * Kotlin puro (sin dependencias de Android) para poder probarla con tests unitarios JVM.
 */
object ValidadorRegistro {

    const val LONGITUD_MINIMA_PASSWORD = 8
    const val LONGITUD_MAXIMA_CORREO = 254
    const val LONGITUD_MINIMA_NOMBRE_USUARIO = 3
    const val LONGITUD_MAXIMA_NOMBRE_USUARIO = 20

    private val REGEX_NOMBRE_USUARIO = Regex("^[A-Za-z0-9_]+$")

    private val REGEX_CORREO = Regex(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?" +
            "(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)*\\.[A-Za-z]{2,}$"
    )

    /** Nombre de usuario (Issue 13): de 3 a 20 caracteres, solo letras, números y guion bajo. */
    fun validarNombreUsuario(nombreUsuario: String): ResultadoValidacion {
        val valor = nombreUsuario.trim()
        return when {
            valor.isEmpty() ->
                ResultadoValidacion.Error("El nombre de usuario es obligatorio")
            valor.length !in LONGITUD_MINIMA_NOMBRE_USUARIO..LONGITUD_MAXIMA_NOMBRE_USUARIO ->
                ResultadoValidacion.Error(
                    "Debe tener entre $LONGITUD_MINIMA_NOMBRE_USUARIO y $LONGITUD_MAXIMA_NOMBRE_USUARIO caracteres"
                )
            !REGEX_NOMBRE_USUARIO.matches(valor) ->
                ResultadoValidacion.Error("Solo puede contener letras, números y guion bajo (_)")
            else -> ResultadoValidacion.Valido
        }
    }

    fun validarCorreo(correo: String): ResultadoValidacion {
        val valor = correo.trim()
        return when {
            valor.isEmpty() ->
                ResultadoValidacion.Error("El correo es obligatorio")
            valor.length > LONGITUD_MAXIMA_CORREO || ".." in valor ->
                ResultadoValidacion.Error("El formato del correo no es válido")
            !REGEX_CORREO.matches(valor) ->
                ResultadoValidacion.Error("El formato del correo no es válido")
            else -> ResultadoValidacion.Valido
        }
    }

    fun validarPassword(password: String): ResultadoValidacion {
        return when {
            password.isEmpty() ->
                ResultadoValidacion.Error("La contraseña es obligatoria")
            password.length < LONGITUD_MINIMA_PASSWORD ->
                ResultadoValidacion.Error("Debe tener al menos $LONGITUD_MINIMA_PASSWORD caracteres")
            password.any { it.isWhitespace() } ->
                ResultadoValidacion.Error("No puede contener espacios")
            !password.any { it.isUpperCase() } ->
                ResultadoValidacion.Error("Debe incluir al menos una mayúscula")
            !password.any { it.isLowerCase() } ->
                ResultadoValidacion.Error("Debe incluir al menos una minúscula")
            !password.any { it.isDigit() } ->
                ResultadoValidacion.Error("Debe incluir al menos un número")
            !password.any { !it.isLetterOrDigit() } ->
                ResultadoValidacion.Error("Debe incluir al menos un carácter especial (por ejemplo ! @ # $ %)")
            else -> ResultadoValidacion.Valido
        }
    }

    fun validarConfirmacionPassword(password: String, confirmacion: String): ResultadoValidacion {
        return when {
            confirmacion.isEmpty() ->
                ResultadoValidacion.Error("Confirma tu contraseña")
            password != confirmacion ->
                ResultadoValidacion.Error("Las contraseñas no coinciden")
            else -> ResultadoValidacion.Valido
        }
    }

    /** true si correo, contraseña y confirmación son válidos a la vez. */
    fun registroValido(correo: String, password: String, confirmacion: String): Boolean =
        validarCorreo(correo).esValido &&
            validarPassword(password).esValido &&
            validarConfirmacionPassword(password, confirmacion).esValido
}
