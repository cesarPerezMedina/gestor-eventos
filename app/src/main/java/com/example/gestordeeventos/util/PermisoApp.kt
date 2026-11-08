package com.example.gestordeeventos.util

import android.Manifest
import android.annotation.SuppressLint

/** Primera versión de Android (API 33) en la que hay que pedir permiso para mostrar notificaciones. */
private const val API_NOTIFICACIONES = 33

/**
 * Permisos de Android que la app pide al usuario mientras la usa (los llamados permisos "peligrosos").
 *
 * INTERNET y ACCESS_NETWORK_STATE no están aquí: son permisos normales, se conceden al instalar la app
 * y basta con declararlos en el AndroidManifest.xml. Android no muestra ningún diálogo para ellos.
 *
 * @property permisos permisos de Android que se piden juntos, en un solo diálogo.
 * @property bastaConUno si es true, con que el usuario conceda uno de ellos ya se considera concedido
 *   (en la ubicación, desde Android 12 el usuario puede dar solo la aproximada).
 * @property apiMinima versión de Android desde la que hay que pedirlo; en versiones anteriores no existe.
 */
@SuppressLint("InlinedApi") // POST_NOTIFICATIONS es de la API 33, pero solo se pide desde esa versión
enum class PermisoApp(
    val permisos: List<String>,
    val bastaConUno: Boolean = false,
    private val apiMinima: Int = 0
) {
    UBICACION(
        permisos = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        bastaConUno = true
    ),
    NOTIFICACIONES(
        permisos = listOf(Manifest.permission.POST_NOTIFICATIONS),
        apiMinima = API_NOTIFICACIONES
    );

    /** Indica si en esta versión de Android (`sdkActual`) hay que pedir el permiso al usuario. */
    fun requiereSolicitud(sdkActual: Int): Boolean = sdkActual >= apiMinima

    /** Indica si, con los permisos que ya tiene la app (`concedidos`), este permiso cuenta como concedido. */
    fun estaConcedido(concedidos: Set<String>): Boolean =
        if (bastaConUno) permisos.any { it in concedidos } else permisos.all { it in concedidos }
}
