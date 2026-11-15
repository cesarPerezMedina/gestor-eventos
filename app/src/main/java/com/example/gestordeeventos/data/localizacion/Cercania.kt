package com.example.gestordeeventos.data.localizacion

import com.example.gestordeeventos.data.model.EventoConCupo
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

/** Un evento y a cuántos kilómetros está del usuario. */
data class EventoConDistancia(
    val evento: EventoConCupo,
    val distanciaKm: Double,
)

/** Rectángulo en el mapa. Sirve para que la base de datos descarte rápido lo que queda muy lejos. */
data class CajaGeografica(
    val latMin: Double,
    val latMax: Double,
    val lonMin: Double,
    val lonMax: Double,
)

private const val RADIO_TIERRA_KM = 6371.0088
private const val KM_POR_GRADO = 111.32

/**
 * Distancia en línea recta entre dos puntos de la Tierra, en kilómetros (fórmula de Haversine).
 * Es la distancia "en pájaro": no tiene en cuenta calles ni caminos.
 */
fun distanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val fi1 = Math.toRadians(lat1)
    val fi2 = Math.toRadians(lat2)
    val dFi = Math.toRadians(lat2 - lat1)
    val dLambda = Math.toRadians(lon2 - lon1)
    val a = sin(dFi / 2) * sin(dFi / 2) + cos(fi1) * cos(fi2) * sin(dLambda / 2) * sin(dLambda / 2)
    return 2 * RADIO_TIERRA_KM * asin(sqrt(a))
}

/**
 * Rectángulo que contiene todo el círculo de [radioKm] alrededor de [centro].
 * Todo punto que esté a menos de [radioKm] cae dentro; algunos de las esquinas quedan fuera del círculo,
 * por eso después se comprueba la distancia exacta con [distanciaKm].
 */
fun cajaDelimitadora(centro: Ubicacion, radioKm: Double): CajaGeografica {
    val dLat = radioKm / KM_POR_GRADO
    // Cerca de los polos los grados de longitud miden muy poco: se evita dividir por casi cero
    val dLon = radioKm / (KM_POR_GRADO * max(cos(Math.toRadians(centro.latitud)), 0.01))
    return CajaGeografica(
        latMin = centro.latitud - dLat,
        latMax = centro.latitud + dLat,
        lonMin = centro.longitud - dLon,
        lonMax = centro.longitud + dLon,
    )
}
