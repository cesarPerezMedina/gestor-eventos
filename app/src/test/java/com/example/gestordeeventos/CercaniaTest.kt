package com.example.gestordeeventos

import com.example.gestordeeventos.data.localizacion.CajaGeografica
import com.example.gestordeeventos.data.localizacion.Ubicacion
import com.example.gestordeeventos.data.localizacion.cajaDelimitadora
import com.example.gestordeeventos.data.localizacion.distanciaKm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Cálculo de distancias y rectángulo de búsqueda por cercanía (Issues 57 y 60). Se ejecuta sin emulador. */
class CercaniaTest {

    private val madrid = Ubicacion(latitud = 40.4168, longitud = -3.7038)

    // --- distanciaKm ---

    @Test
    fun mismoPuntoEstaACeroKm() {
        assertEquals(0.0, distanciaKm(40.4168, -3.7038, 40.4168, -3.7038), 1e-9)
    }

    @Test
    fun madridBarcelonaSonUnos505Km() {
        assertEquals(505.1, distanciaKm(40.4168, -3.7038, 41.3874, 2.1686), 1.0)
    }

    @Test
    fun madridToledoSonUnos67Km() {
        assertEquals(67.5, distanciaKm(40.4168, -3.7038, 39.8628, -4.0273), 1.0)
    }

    @Test
    fun ungradoDeLatitudSonUnos111Km() {
        assertEquals(111.2, distanciaKm(0.0, 0.0, 1.0, 0.0), 0.1)
    }

    @Test
    fun ungradoDeLongitudMideLaMitadEnLaLatitud60() {
        assertEquals(55.6, distanciaKm(60.0, 0.0, 60.0, 1.0), 0.1)
    }

    @Test
    fun laDistanciaEsLaMismaEnLosDosSentidos() {
        val ida = distanciaKm(40.4168, -3.7038, 41.3874, 2.1686)
        val vuelta = distanciaKm(41.3874, 2.1686, 40.4168, -3.7038)
        assertEquals(ida, vuelta, 1e-9)
    }

    @Test
    fun cruzarElMeridianoOpuestoNoSeDispara() {
        // De 179.5 a -179.5 de longitud hay 1 grado, no 359
        assertEquals(111.2, distanciaKm(0.0, 179.5, 0.0, -179.5), 0.1)
    }

    @Test
    fun deUnPoloAlOtroEsMediaVueltaALaTierra() {
        assertEquals(20015.1, distanciaKm(90.0, 0.0, -90.0, 0.0), 1.0)
    }

    @Test
    fun nuncaEsNegativa() {
        assertTrue(distanciaKm(-33.9, 151.2, 40.4, -3.7) > 0.0)
    }

    // --- cajaDelimitadora ---

    @Test
    fun laCajaEstaCentradaEnElPunto() {
        val caja = cajaDelimitadora(madrid, 10.0)
        assertEquals(madrid.latitud, (caja.latMin + caja.latMax) / 2, 1e-9)
        assertEquals(madrid.longitud, (caja.lonMin + caja.lonMax) / 2, 1e-9)
    }

    @Test
    fun laAlturaDeLaCajaEsDoblePorElRadio() {
        // 10 km de radio = 20 km de alto = unos 0,18 grados de latitud
        val caja = cajaDelimitadora(madrid, 10.0)
        assertEquals(0.18, caja.latMax - caja.latMin, 0.001)
    }

    @Test
    fun enElEcuadorLaCajaEsCuadrada() {
        val caja = cajaDelimitadora(Ubicacion(0.0, 0.0), 10.0)
        assertEquals(caja.latMax - caja.latMin, caja.lonMax - caja.lonMin, 1e-9)
    }

    @Test
    fun cuantoMasAlNorteMasAnchaEnGradosDeLongitud() {
        val ecuador = cajaDelimitadora(Ubicacion(0.0, 0.0), 10.0)
        val norte = cajaDelimitadora(Ubicacion(60.0, 0.0), 10.0)
        // A 60 grados de latitud un grado de longitud mide la mitad, así que la caja es el doble de ancha
        assertEquals(2.0, (norte.lonMax - norte.lonMin) / (ecuador.lonMax - ecuador.lonMin), 0.01)
    }

    @Test
    fun cercaDelPoloLaCajaNoSeDispara() {
        val caja = cajaDelimitadora(Ubicacion(89.99, 0.0), 10.0)
        val ancho = caja.lonMax - caja.lonMin
        assertTrue("El ancho debe ser finito", ancho.isFinite())
        assertTrue("El ancho debe estar limitado (era $ancho)", ancho <= 18.0)
    }

    @Test
    fun conRadioCeroLaCajaEsUnPunto() {
        val caja = cajaDelimitadora(madrid, 0.0)
        assertEquals(madrid.latitud, caja.latMin, 1e-12)
        assertEquals(madrid.latitud, caja.latMax, 1e-12)
        assertEquals(madrid.longitud, caja.lonMin, 1e-12)
        assertEquals(madrid.longitud, caja.lonMax, 1e-12)
    }

    @Test
    fun conMasRadioLaCajaEsMasGrande() {
        val pequena = cajaDelimitadora(madrid, 2.0)
        val grande = cajaDelimitadora(madrid, 25.0)
        assertTrue(grande.latMin < pequena.latMin && grande.latMax > pequena.latMax)
        assertTrue(grande.lonMin < pequena.lonMin && grande.lonMax > pequena.lonMax)
    }

    // --- La caja y el radio juntos ---

    /** Punto a [km] kilómetros de [origen] en la dirección [rumboGrados] (0 = norte, 90 = este). */
    private fun puntoA(origen: Ubicacion, km: Double, rumboGrados: Double): Ubicacion {
        val radioTierra = 6371.0088
        val d = km / radioTierra
        val rumbo = Math.toRadians(rumboGrados)
        val fi1 = Math.toRadians(origen.latitud)
        val lambda1 = Math.toRadians(origen.longitud)
        val fi2 = asin(sin(fi1) * cos(d) + cos(fi1) * sin(d) * cos(rumbo))
        val lambda2 = lambda1 + atan2(sin(rumbo) * sin(d) * cos(fi1), cos(d) - sin(fi1) * sin(fi2))
        return Ubicacion(Math.toDegrees(fi2), Math.toDegrees(lambda2))
    }

    private fun CajaGeografica.contiene(p: Ubicacion) =
        p.latitud in latMin..latMax && p.longitud in lonMin..lonMax

    @Test
    fun todoPuntoDentroDelRadioCaeDentroDeLaCaja() {
        val centros = listOf(madrid, Ubicacion(0.0, 0.0), Ubicacion(59.9, 10.7), Ubicacion(-33.9, 151.2))
        for (centro in centros) {
            for (radio in listOf(2.0, 5.0, 10.0, 25.0)) {
                val caja = cajaDelimitadora(centro, radio)
                for (rumbo in 0 until 360 step 15) {
                    val punto = puntoA(centro, radio * 0.99, rumbo.toDouble())
                    assertTrue(
                        "A ${radio * 0.99} km de $centro con rumbo $rumbo debería caer en la caja",
                        caja.contiene(punto),
                    )
                }
            }
        }
    }

    @Test
    fun unPuntoMuyLejosCaeFueraDeLaCaja() {
        val toledo = Ubicacion(latitud = 39.8628, longitud = -4.0273)
        assertFalse(cajaDelimitadora(madrid, 10.0).contiene(toledo))
        assertTrue(cajaDelimitadora(madrid, 100.0).contiene(toledo))
    }

    @Test
    fun alNorteyAlSurUnPuntoAlDobleDelRadioCaeFuera() {
        val caja = cajaDelimitadora(madrid, 10.0)
        assertFalse(caja.contiene(puntoA(madrid, 20.0, 0.0)))
        assertFalse(caja.contiene(puntoA(madrid, 20.0, 180.0)))
    }
}
