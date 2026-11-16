package com.example.gestordeeventos.util

import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.toRadians

/**
 * Utilidades para calcular distancias geográficas entre coordenadas (latitud y longitud).
 */
object Distancia {

    /**
     * Calcula la distancia en kilómetros entre dos puntos geográficos usando la fórmula de Haversine.
     */
    fun calcularKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val radioTierraKm = 6371.0
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(toRadians(lat1)) * cos(toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * acos(kotlin.math.sqrt(a))
        return radioTierraKm * c
    }
}
