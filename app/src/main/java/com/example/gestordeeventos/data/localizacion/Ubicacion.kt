package com.example.gestordeeventos.data.localizacion

import android.Manifest
import com.google.android.gms.location.Priority

/**
 * Posición del dispositivo en un momento dado.
 *
 * Es solo un valor en memoria: no es una tabla y no se guarda en la base de datos.
 *
 * @property precisionMetros margen de error aproximado en metros, o `null` si el dispositivo no lo informa.
 */
data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Float? = null
)

/** Resultado de pedir la ubicación actual. Cada caso se puede mostrar al usuario de forma distinta. */
sealed interface ResultadoUbicacion {
    /** Se obtuvo la posición. */
    data class Obtenida(val ubicacion: Ubicacion) : ResultadoUbicacion

    /** La app no tiene permiso de ubicación (ver `PermisoApp.UBICACION`). */
    data object SinPermiso : ResultadoUbicacion

    /** El usuario tiene la ubicación apagada en los ajustes del dispositivo. */
    data object UbicacionDesactivada : ResultadoUbicacion

    /** No se pudo calcular la posición (por ejemplo, sin señal) o se agotó el tiempo de espera. */
    data object NoDisponible : ResultadoUbicacion

    /** Los servicios de ubicación de Google devolvieron un error. */
    data class Fallo(val causa: Throwable) : ResultadoUbicacion
}

/**
 * Elige la precisión con la que se pide la posición según los permisos concedidos: alta si el usuario dio la
 * ubicación precisa, y equilibrada (menos batería) si solo dio la aproximada.
 */
internal fun prioridadPara(concedidos: Set<String>): Int =
    if (Manifest.permission.ACCESS_FINE_LOCATION in concedidos) {
        Priority.PRIORITY_HIGH_ACCURACY
    } else {
        Priority.PRIORITY_BALANCED_POWER_ACCURACY
    }
