package com.example.gestordeeventos

import com.example.gestordeeventos.util.Distancia
import org.junit.Assert.assertEquals
import org.junit.Test

class DistanciaTest {

    @Test
    fun calcularDistanciaMismoPuntoEsCero() {
        val distancia = Distancia.calcularKm(40.4168, -3.7038, 40.4168, -3.7038)
        assertEquals(0.0, distancia, 0.001)
    }

    @Test
    fun calcularDistanciaEntreCiudadesConocidas() {
        // Madrid (40.4168, -3.7038) a Toledo (39.8628, -4.0273) aprox 60 km
        val distancia = Distancia.calcularKm(40.4168, -3.7038, 39.8628, -4.0273)
        assertEquals(60.0, distancia, 5.0) // Margen razonable por la aproximación esférica
    }
}
