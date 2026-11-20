package com.example.gestordeeventos.util

import com.example.gestordeeventos.data.model.Evento

/**
 * Un mensaje de error por campo del formulario de crear evento (o `null` si el campo está bien).
 * Así el formulario puede marcar cada campo por separado.
 */
data class ErroresEvento(
    val nombre: String? = null,
    val descripcion: String? = null,
    val participantes: String? = null,
    val ubicacion: String? = null,
    val fecha: String? = null
) {
    /** El primer error, en el mismo orden en que aparecen los campos en el formulario. */
    val primerError: String? get() = nombre ?: descripcion ?: participantes ?: ubicacion ?: fecha

    val esValido: Boolean get() = primerError == null
}

/**
 * Validación de un evento antes de guardarlo (Issues 49 y 50).
 * Kotlin puro (sin Android ni base de datos) para poder probarla con tests unitarios JVM.
 * Las reglas están en `docs/VALIDACION_EVENTO.md`.
 */
object ValidadorEvento {

    const val NOMBRE_MINIMO = 3
    const val NOMBRE_MAXIMO = 60
    const val DESCRIPCION_MAXIMA = 500

    fun validarNombre(nombre: String): ResultadoValidacion {
        val limpio = nombre.trim()
        return when {
            limpio.isEmpty() -> ResultadoValidacion.Error("El nombre del evento es obligatorio")
            limpio.length !in NOMBRE_MINIMO..NOMBRE_MAXIMO ->
                ResultadoValidacion.Error("El nombre debe tener entre $NOMBRE_MINIMO y $NOMBRE_MAXIMO caracteres")
            else -> ResultadoValidacion.Valido
        }
    }

    fun validarDescripcion(descripcion: String): ResultadoValidacion =
        if (descripcion.trim().length > DESCRIPCION_MAXIMA) {
            ResultadoValidacion.Error("La descripción no puede superar los $DESCRIPCION_MAXIMA caracteres")
        } else {
            ResultadoValidacion.Valido
        }

    /** Un máximo de 0 significa "sin límite". */
    fun validarParticipantes(minimo: Int, maximo: Int): ResultadoValidacion = when {
        minimo < 0 || maximo < 0 -> ResultadoValidacion.Error("Los participantes no pueden ser negativos")
        maximo > 0 && maximo < minimo ->
            ResultadoValidacion.Error("El máximo de jugadores no puede ser menor que el mínimo")
        else -> ResultadoValidacion.Valido
    }

    /** Hace falta una dirección o unas coordenadas (0,0 se entiende como "sin coordenadas"). */
    fun validarUbicacion(direccion: String, latitud: Double, longitud: Double): ResultadoValidacion {
        val coordenadasValidas = !latitud.isNaN() && !longitud.isNaN() &&
            latitud in -90.0..90.0 && longitud in -180.0..180.0
        val hayCoordenadas = latitud != 0.0 || longitud != 0.0
        return when {
            !coordenadasValidas -> ResultadoValidacion.Error("Las coordenadas de la ubicación no son válidas")
            direccion.isBlank() && !hayCoordenadas -> ResultadoValidacion.Error("Indica dónde será el evento")
            else -> ResultadoValidacion.Valido
        }
    }

    fun validarFecha(fechaHora: Long, ahora: Long = System.currentTimeMillis()): ResultadoValidacion =
        if (fechaHora > ahora) {
            ResultadoValidacion.Valido
        } else {
            ResultadoValidacion.Error("La fecha debe ser futura")
        }

    /**
     * Valida el evento entero. Con [exigirFecha] en `false` no se comprueba la fecha
     * (útil mientras un formulario no la pida).
     */
    fun validar(evento: Evento, ahora: Long = System.currentTimeMillis(), exigirFecha: Boolean = true): ErroresEvento =
        ErroresEvento(
            nombre = validarNombre(evento.nombre).mensajeError,
            descripcion = validarDescripcion(evento.descripcion).mensajeError,
            participantes = validarParticipantes(evento.minParticipantes, evento.maxParticipantes).mensajeError,
            ubicacion = validarUbicacion(evento.direccion, evento.latitud, evento.longitud).mensajeError,
            fecha = if (exigirFecha) validarFecha(evento.fechaHora, ahora).mensajeError else null
        )
}