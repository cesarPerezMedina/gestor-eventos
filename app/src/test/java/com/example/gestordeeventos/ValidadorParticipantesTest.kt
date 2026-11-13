package com.example.gestordeeventos

import com.example.gestordeeventos.util.ErrorParticipantes
import com.example.gestordeeventos.util.ValidadorParticipantes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorParticipantesTest {

    // ---- Mínimo ----
    @Test
    fun minimo_validoEnTodoElRango() {
        listOf("1", "2", "50", "999").forEach { assertNull(it, ValidadorParticipantes.validarMinimo(it)) }
    }

    @Test
    fun minimo_conEspaciosAlrededor_seAcepta() {
        assertNull(ValidadorParticipantes.validarMinimo(" 5 "))
    }

    @Test
    fun minimo_vacioEsObligatorio() {
        assertEquals(ErrorParticipantes.MINIMO_VACIO, ValidadorParticipantes.validarMinimo(""))
        assertEquals(ErrorParticipantes.MINIMO_VACIO, ValidadorParticipantes.validarMinimo("   "))
    }

    @Test
    fun minimo_fueraDeRango() {
        listOf("0", "1000", "-3", "abc", "2.5", "99999999999999999999").forEach {
            assertEquals(it, ErrorParticipantes.FUERA_DE_RANGO, ValidadorParticipantes.validarMinimo(it))
        }
    }

    // ---- Máximo ----
    @Test
    fun maximo_mayorOIgualQueElMinimo_esValido() {
        assertNull(ValidadorParticipantes.validarMaximo("4", "10"))
        assertNull(ValidadorParticipantes.validarMaximo("4", "4"))
    }

    @Test
    fun maximo_vacioEsObligatorio() {
        assertEquals(ErrorParticipantes.MAXIMO_VACIO, ValidadorParticipantes.validarMaximo("4", ""))
    }

    @Test
    fun maximo_fueraDeRango() {
        listOf("0", "1000", "abc").forEach {
            assertEquals(it, ErrorParticipantes.FUERA_DE_RANGO, ValidadorParticipantes.validarMaximo("1", it))
        }
    }

    @Test
    fun maximo_menorQueElMinimo() {
        assertEquals(ErrorParticipantes.MAXIMO_MENOR_QUE_MINIMO, ValidadorParticipantes.validarMaximo("10", "4"))
    }

    @Test
    fun maximo_noSeCompara_siElMinimoEstaVacioOEsInvalido() {
        assertNull(ValidadorParticipantes.validarMaximo("", "5"))
        assertNull(ValidadorParticipantes.validarMaximo("0", "1"))
        assertNull(ValidadorParticipantes.validarMaximo("abc", "1"))
    }

    // ---- Los dos a la vez ----
    @Test
    fun sonValidos_soloSiLosDosLoSon() {
        assertTrue(ValidadorParticipantes.sonValidos("4", "10"))
        assertFalse(ValidadorParticipantes.sonValidos("", "10"))
        assertFalse(ValidadorParticipantes.sonValidos("4", ""))
        assertFalse(ValidadorParticipantes.sonValidos("10", "4"))
    }
}
