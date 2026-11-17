package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Evento
import com.example.gestordeeventos.util.ValidadorEvento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorEventoTest {

    private val ahora = 1_000_000L

    private fun eventoValido() = Evento(
        nombre = "Partido de fútbol 7",
        descripcion = "Traed camiseta clara",
        minParticipantes = 10,
        maxParticipantes = 14,
        direccion = "Polideportivo Norte",
        fechaHora = ahora + 3_600_000L,
        creadorId = 1L
    )

    // --- Nombre ---

    @Test
    fun nombresValidos() {
        listOf("Pádel", "Partido de fútbol 7", "a".repeat(60), "  Pádel  ")
            .forEach { assertTrue(it, ValidadorEvento.validarNombre(it).esValido) }
    }

    @Test
    fun nombreVacioEsObligatorio() {
        assertEquals("El nombre del evento es obligatorio", ValidadorEvento.validarNombre("   ").mensajeError)
    }

    @Test
    fun nombreMuyCortoOMuyLargo() {
        val esperado = "El nombre debe tener entre 3 y 60 caracteres"
        assertEquals(esperado, ValidadorEvento.validarNombre("ab").mensajeError)
        assertEquals(esperado, ValidadorEvento.validarNombre("a".repeat(61)).mensajeError)
    }

    // --- Descripción ---

    @Test
    fun descripcionVaciaOHastaElLimiteEsValida() {
        assertTrue(ValidadorEvento.validarDescripcion("").esValido)
        assertTrue(ValidadorEvento.validarDescripcion("a".repeat(500)).esValido)
    }

    @Test
    fun descripcionDemasiadoLargaNoEsValida() {
        assertNotNull(ValidadorEvento.validarDescripcion("a".repeat(501)).mensajeError)
    }

    // --- Participantes ---

    @Test
    fun participantesValidos() {
        assertTrue(ValidadorEvento.validarParticipantes(0, 0).esValido) // sin límite
        assertTrue(ValidadorEvento.validarParticipantes(4, 0).esValido) // solo mínimo
        assertTrue(ValidadorEvento.validarParticipantes(2, 2).esValido)
        assertTrue(ValidadorEvento.validarParticipantes(10, 14).esValido)
    }

    @Test
    fun elMaximoNoPuedeSerMenorQueElMinimo() {
        assertEquals(
            "El máximo de jugadores no puede ser menor que el mínimo",
            ValidadorEvento.validarParticipantes(10, 6).mensajeError
        )
    }

    @Test
    fun losParticipantesNoPuedenSerNegativos() {
        assertNotNull(ValidadorEvento.validarParticipantes(-1, 5).mensajeError)
        assertNotNull(ValidadorEvento.validarParticipantes(0, -5).mensajeError)
    }

    // --- Ubicación ---

    @Test
    fun conDireccionOConCoordenadasLaUbicacionEsValida() {
        assertTrue(ValidadorEvento.validarUbicacion("Polideportivo Norte", 0.0, 0.0).esValido)
        assertTrue(ValidadorEvento.validarUbicacion("", 40.4168, -3.7038).esValido)
        assertTrue(ValidadorEvento.validarUbicacion("Parque", 40.4168, -3.7038).esValido)
    }

    @Test
    fun sinDireccionNiCoordenadasFalta() {
        assertEquals("Indica dónde será el evento", ValidadorEvento.validarUbicacion("  ", 0.0, 0.0).mensajeError)
    }

    @Test
    fun coordenadasFueraDeRangoONoNumericas() {
        assertNotNull(ValidadorEvento.validarUbicacion("", 91.0, 10.0).mensajeError)
        assertNotNull(ValidadorEvento.validarUbicacion("", 10.0, 181.0).mensajeError)
        assertNotNull(ValidadorEvento.validarUbicacion("Parque", Double.NaN, 10.0).mensajeError)
    }

    // --- Fecha ---

    @Test
    fun laFechaTieneQueSerFutura() {
        assertTrue(ValidadorEvento.validarFecha(ahora + 1, ahora).esValido)
        assertEquals("La fecha debe ser futura", ValidadorEvento.validarFecha(ahora, ahora).mensajeError)
        assertEquals("La fecha debe ser futura", ValidadorEvento.validarFecha(ahora - 1, ahora).mensajeError)
    }

    // --- Evento completo ---

    @Test
    fun unEventoCorrectoNoTieneErrores() {
        val errores = ValidadorEvento.validar(eventoValido(), ahora)
        assertTrue(errores.esValido)
        assertNull(errores.primerError)
    }

    @Test
    fun marcaCadaCampoIncorrecto() {
        val evento = eventoValido().copy(
            nombre = "",
            descripcion = "a".repeat(501),
            minParticipantes = 10,
            maxParticipantes = 4,
            direccion = "",
            fechaHora = ahora - 1
        )
        val errores = ValidadorEvento.validar(evento, ahora)
        assertNotNull(errores.nombre)
        assertNotNull(errores.descripcion)
        assertNotNull(errores.participantes)
        assertNotNull(errores.ubicacion)
        assertNotNull(errores.fecha)
        assertFalse(errores.esValido)
    }

    @Test
    fun elPrimerErrorSigueElOrdenDelFormulario() {
        val evento = eventoValido().copy(nombre = "ab", direccion = "")
        val errores = ValidadorEvento.validar(evento, ahora)
        assertEquals(errores.nombre, errores.primerError)
    }

    @Test
    fun sePuedeNoExigirLaFechaMientrasElFormularioNoLaPide() {
        val evento = eventoValido().copy(fechaHora = ahora) // "ahora", como hoy hace el formulario
        assertNotNull(ValidadorEvento.validar(evento, ahora).fecha)
        assertNull(ValidadorEvento.validar(evento, ahora, exigirFecha = false).fecha)
        assertTrue(ValidadorEvento.validar(evento, ahora, exigirFecha = false).esValido)
    }
}
