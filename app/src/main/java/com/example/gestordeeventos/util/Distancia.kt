package com.example.gestordeeventos.util

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utilidades para calcular distancias geográficas entre coordenadas (latitud y longitud).
 */
object Distancia {

    private const val RADIO_TIERRA_KM = 6371.0

    /**
     * Calcula la distancia en kilómetros entre dos puntos geográficos usando la fórmula de Haversine.
     */
    fun calcularKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = grados2rad(lat2 - lat1)
        val dLon = grados2rad(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(grados2rad(lat1)) * cos(grados2rad(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * asin(sqrt(a))
        return RADIO_TIERRA_KM * c
    }

    private fun grados2rad(grados: Double): Double = Math.toRadians(grados)
}
