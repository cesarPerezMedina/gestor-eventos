package com.example.gestordeeventos.data.localizacion

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import com.example.gestordeeventos.util.PermisoApp
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/** Milisegundos máximos que se espera a que el dispositivo calcule su posición. */
private const val TIEMPO_MAXIMO_MS = 10_000L

/**
 * Obtiene la ubicación actual del dispositivo, una sola vez (no sigue al usuario ni gasta batería en segundo
 * plano).
 *
 * No pide el permiso: eso lo hace `rememberSolicitudPermiso(PermisoApp.UBICACION)`. Si falta el permiso, o la
 * ubicación está apagada, no lanza ningún error: devuelve el [ResultadoUbicacion] que corresponda. La posición no
 * se guarda ni se escribe en ningún log.
 *
 * Usa los servicios de ubicación de Google, así que el dispositivo debe tener Google Play Services.
 */
class LocalizacionProvider(context: Context) {
    // Se guarda el contexto de la aplicación, no el de una pantalla, para no retener una Activity en memoria.
    private val contexto = context.applicationContext
    private val cliente by lazy { LocationServices.getFusedLocationProviderClient(contexto) }

    /** Pide la posición actual. Se puede cancelar: si la corrutina se cancela, se cancela también la petición. */
    suspend fun obtenerUbicacionActual(): ResultadoUbicacion {
        val concedidos = permisosConcedidos()
        return when {
            !PermisoApp.UBICACION.estaConcedido(concedidos) -> ResultadoUbicacion.SinPermiso
            !ubicacionActivada() -> ResultadoUbicacion.UbicacionDesactivada
            else -> leerPosicion(prioridadPara(concedidos))
        }
    }

    private suspend fun leerPosicion(prioridad: Int): ResultadoUbicacion = try {
        val posicion = withTimeoutOrNull(TIEMPO_MAXIMO_MS) { pedirPosicion(prioridad) }
        if (posicion == null) ResultadoUbicacion.NoDisponible else ResultadoUbicacion.Obtenida(posicion.aUbicacion())
    } catch (e: SecurityException) {
        // El usuario pudo quitar el permiso justo después de comprobarlo.
        ResultadoUbicacion.SinPermiso
    } catch (e: ApiException) {
        ResultadoUbicacion.Fallo(e)
    }

    @SuppressLint("MissingPermission") // El permiso se comprueba en obtenerUbicacionActual() antes de llegar aquí.
    private suspend fun pedirPosicion(prioridad: Int): Location? = suspendCancellableCoroutine { continuacion ->
        val cancelacion = CancellationTokenSource()
        continuacion.invokeOnCancellation { cancelacion.cancel() }
        cliente.getCurrentLocation(prioridad, cancelacion.token)
            .addOnSuccessListener { continuacion.resumeWith(Result.success(it)) }
            .addOnFailureListener { continuacion.resumeWith(Result.failure(it)) }
    }

    private fun permisosConcedidos(): Set<String> = PermisoApp.UBICACION.permisos
        .filter { contexto.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
        .toSet()

    private fun ubicacionActivada(): Boolean =
        contexto.getSystemService(LocationManager::class.java)?.isLocationEnabled == true

    private fun Location.aUbicacion() = Ubicacion(
        latitud = latitude,
        longitud = longitude,
        precisionMetros = if (hasAccuracy()) accuracy else null
    )
}
