package com.example.gestordeeventos.data.localizacion

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Convierte entre direcciones y coordenadas (Issue 46), con el [Geocoder] del propio Android.
 *
 * - [direccionDe]: coordenadas -> texto ("Calle de Alcalá 12, Madrid").
 * - [buscarDireccion]: texto -> coordenadas.
 *
 * Necesita internet y un dispositivo con Google Play Services. Si no hay servicio, no hay red o no se
 * encuentra nada, devuelve `null` y no lanza ningún error. Nada se guarda ni se escribe en ningún log.
 */
class GeocodificadorProvider(context: Context) {
    private val contexto = context.applicationContext
    private val geocoder by lazy { Geocoder(contexto, Locale.forLanguageTag("es-ES")) }

    /** Dirección legible del punto, o `null` si no se pudo obtener. */
    suspend fun direccionDe(latitud: Double, longitud: Double): String? =
        buscarPorCoordenadas(latitud, longitud).firstOrNull()?.getAddressLine(0)

    /** Primer punto que encaja con la dirección escrita, o `null` si no se encontró. */
    suspend fun buscarDireccion(texto: String): Ubicacion? {
        val consulta = texto.trim()
        if (consulta.isEmpty()) return null
        return buscarPorTexto(consulta).firstOrNull()?.let { Ubicacion(latitud = it.latitude, longitud = it.longitude) }
    }

    private suspend fun buscarPorCoordenadas(latitud: Double, longitud: Double): List<Address> {
        if (!Geocoder.isPresent()) return emptyList()
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuacion ->
                    geocoder.getFromLocation(
                        latitud,
                        longitud,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) = continuacion.resume(addresses)

                            override fun onError(errorMessage: String?) = continuacion.resume(emptyList())
                        }
                    )
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(latitud, longitud, 1).orEmpty()
                }
            }
        } catch (e: IOException) {
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }

    private suspend fun buscarPorTexto(consulta: String): List<Address> {
        if (!Geocoder.isPresent()) return emptyList()
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuacion ->
                    geocoder.getFromLocationName(
                        consulta,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(addresses: MutableList<Address>) = continuacion.resume(addresses)

                            override fun onError(errorMessage: String?) = continuacion.resume(emptyList())
                        }
                    )
                }
            } else {
                withContext(Dispatchers.IO) {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocationName(consulta, 1).orEmpty()
                }
            }
        } catch (e: IOException) {
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }
}