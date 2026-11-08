package com.example.gestordeeventos.util

import android.Manifest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Comprueba las reglas de los permisos sin necesidad de emulador. */
class PermisosTest {

    @Test
    fun ubicacionCuentaComoConcedidaConSoloLaAproximada() {
        val concedidos = setOf(Manifest.permission.ACCESS_COARSE_LOCATION)
        assertTrue(PermisoApp.UBICACION.estaConcedido(concedidos))
    }

    @Test
    fun ubicacionNoEstaConcedidaSiNoHayNingunPermiso() {
        assertFalse(PermisoApp.UBICACION.estaConcedido(emptySet()))
    }

    @Test
    fun notificacionesExigeSuPermiso() {
        assertFalse(PermisoApp.NOTIFICACIONES.estaConcedido(emptySet()))
        assertTrue(PermisoApp.NOTIFICACIONES.estaConcedido(setOf(Manifest.permission.POST_NOTIFICATIONS)))
    }

    @Test
    fun notificacionesSoloSePidenDesdeAndroid13() {
        assertFalse(PermisoApp.NOTIFICACIONES.requiereSolicitud(sdkActual = 32))
        assertTrue(PermisoApp.NOTIFICACIONES.requiereSolicitud(sdkActual = 33))
    }

    @Test
    fun ubicacionSeSolicitaEnTodasLasVersionesSoportadas() {
        assertTrue(PermisoApp.UBICACION.requiereSolicitud(sdkActual = 29))
    }

    @Test
    fun estadoConcedidoTienePrioridadSobreLoDemas() {
        val estado = EstadoPermiso.calcular(concedido = true, debeExplicar = true, yaSolicitado = true)
        assertEquals(EstadoPermiso.CONCEDIDO, estado)
    }

    @Test
    fun estadoEsPendienteSiNuncaSeHaPedido() {
        val estado = EstadoPermiso.calcular(concedido = false, debeExplicar = false, yaSolicitado = false)
        assertEquals(EstadoPermiso.PENDIENTE, estado)
    }

    @Test
    fun estadoEsExplicarSiAndroidPideJustificarlo() {
        val estado = EstadoPermiso.calcular(concedido = false, debeExplicar = true, yaSolicitado = true)
        assertEquals(EstadoPermiso.EXPLICAR, estado)
    }

    @Test
    fun estadoEsBloqueadoSiSeDenegoParaSiempre() {
        val estado = EstadoPermiso.calcular(concedido = false, debeExplicar = false, yaSolicitado = true)
        assertEquals(EstadoPermiso.BLOQUEADO, estado)
    }
}
