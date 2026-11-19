package com.example.gestordeeventos

import android.Manifest
import com.example.gestordeeventos.data.localizacion.prioridadPara
import com.google.android.gms.location.Priority
import org.junit.Assert.assertEquals
import org.junit.Test

/** Comprueba la regla de precisión de la ubicación sin necesidad de emulador. */
class LocalizacionTest {

    @Test
    fun conUbicacionPrecisaSePideAltaPrecision() {
        val concedidos = setOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        assertEquals(Priority.PRIORITY_HIGH_ACCURACY, prioridadPara(concedidos))
    }

    @Test
    fun soloConLaPrecisaTambienSePideAltaPrecision() {
        val concedidos = setOf(Manifest.permission.ACCESS_FINE_LOCATION)
        assertEquals(Priority.PRIORITY_HIGH_ACCURACY, prioridadPara(concedidos))
    }

    @Test
    fun soloConLaAproximadaSePideUnaPrecisionEquilibrada() {
        val concedidos = setOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        assertEquals(Priority.PRIORITY_BALANCED_POWER_ACCURACY, prioridadPara(concedidos))
    }
}
