package com.example.gestordeeventos.util

import com.example.gestordeeventos.data.model.Evento

/**
 * Errores de un evento por campo (null = el campo está bien). Sirve para que el formulario marque cada campo.
 * Los textos están en español, igual que en [ValidadorRegistro].
 */
data class ErroresEvento(
    val nombre: String? = null,
    val descripcion: String? = null,
    val participantes: String? = null,
    val ubicacion: String? = null,
    val fecha: String? = null
) {
    val esValido: Boolean get() = primerError == null

    /** El primer error en el orden del formulario, o null si todo es correcto. */
    val primerError: String? get() = nombre ?: descripcion ?: participantes ?: ubicacion ?: fecha
}

/**
 * Reglas de validación de un evento antes de guardarlo (Issues 49 y 50). Es puro Kotlin: no usa Android ni la base
 * de datos, así que se prueba sin emulador.
 *
 * Convenciones de [Evento]: participantes 0 significa "sin límite"; ubicación vacía (sin dirección y con
 * latitud y longitud a 0) significa "sin ubicación".
 */
object ValidadorEvento {
    const val LONGITUD_MINIMA_NOMBRE = 3
    const val LONGITUD_MAXIMA_NOMBRE = 60
    const val LONGITUD_MAXIMA_DESCRIPCION = 500
    private const val LATITUD_MAXIMA = 90.0
    private const val LONGITUD_GEOGRAFICA_MAXIMA = 180.0

    fun validarNombre(nombre: String): ResultadoValidacion {
        val valor = nombre.trim()
        return when {
            valor.isEmpty() -> ResultadoValidacion.Error("El nombre del evento es obligatorio")
            valor.length !in LONGITUD_MINIMA_NOMBRE..LONGITUD_MAXIMA_NOMBRE ->
                ResultadoValidacion.Error(
                    "El nombre debe tener entre $LONGITUD_MINIMA_NOMBRE y $LONGITUD_MAXIMA_NOMBRE caracteres"
                )
            else -> ResultadoValidacion.Valido
        }
    }

    fun validarDescripcion(descripcion: String): ResultadoValidacion =
        if (descripcion.trim().length > LONGITUD_MAXIMA_DESCRIPCION) {
            ResultadoValidacion.Error("La descripción no puede superar los $LONGITUD_MAXIMA_DESCRIPCION caracteres")
        } else {
            ResultadoValidacion.Valido
        }

    /** [minimo] y [maximo] a 0 significan "sin límite". Si hay máximo, no puede ser menor que el mínimo. */
    fun validarParticipantes(minimo: Int, maximo: Int): ResultadoValidacion = when {
        minimo < 0 -> ResultadoValidacion.Error("El mínimo de jugadores no puede ser negativo")
        maximo < 0 -> ResultadoValidacion.Error("El máximo de jugadores no puede ser negativo")
        maximo in 1 until minimo ->
            ResultadoValidacion.Error("El máximo de jugadores no puede ser menor que el mínimo")
        else -> ResultadoValidacion.Valido
    }

    /** Hace falta una dirección o unas coordenadas; si hay coordenadas, deben ser reales. */
    fun validarUbicacion(direccion: String, latitud: Double, longitud: Double): ResultadoValidacion {
        val hayCoordenadas = latitud != 0.0 || longitud != 0.0
        return when {
            direccion.isBlank() && !hayCoordenadas -> ResultadoValidacion.Error("Indica dónde será el evento")
            hayCoordenadas && !coordenadasValidas(latitud, longitud) ->
                ResultadoValidacion.Error("Las coordenadas de la ubicación no son válidas")
            else -> ResultadoValidacion.Valido
        }
    }

    /** La fecha y hora del evento (milisegundos desde 1970) tiene que ser posterior a [ahora]. */
    fun validarFecha(fechaHora: Long, ahora: Long = System.currentTimeMillis()): ResultadoValidacion =
        if (fechaHora > ahora) ResultadoValidacion.Valido else ResultadoValidacion.Error("La fecha debe ser futura")

    /**
     * Valida todos los campos del evento.
     * [exigirFecha] se puede poner a false mientras el formulario no pida fecha y hora (Issue 47).
     */
    fun validar(
        evento: Evento,
        ahora: Long = System.currentTimeMillis(),
        exigirFecha: Boolean = true
    ): ErroresEvento = ErroresEvento(
        nombre = validarNombre(evento.nombre).mensajeError,
        descripcion = validarDescripcion(evento.descripcion).mensajeError,
        participantes = validarParticipantes(evento.minParticipantes, evento.maxParticipantes).mensajeError,
        ubicacion = validarUbicacion(evento.direccion, evento.latitud, evento.longitud).mensajeError,
        fecha = if (exigirFecha) validarFecha(evento.fechaHora, ahora).mensajeError else null
    )

    private fun coordenadasValidas(latitud: Double, longitud: Double): Boolean =
        !latitud.isNaN() && !longitud.isNaN() &&
            latitud in -LATITUD_MAXIMA..LATITUD_MAXIMA &&
            longitud in -LONGITUD_GEOGRAFICA_MAXIMA..LONGITUD_GEOGRAFICA_MAXIMA
}
