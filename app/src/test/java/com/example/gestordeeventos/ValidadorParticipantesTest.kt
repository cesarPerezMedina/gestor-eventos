package com.example.gestordeeventos

import com.example.gestordeeventos.util.ErrorParticipantes
import com.example.gestordeeventos.util.ValidadorParticipantes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadorParticipantesTest {

    @Test
    fun minimo_validoEnTodoElRango() {
        listOf("1", "2", "50", "999", "1000").forEach {
            assertNull(ValidadorParticipantes.errorMinimo(it))
        }
    }

    @Test
    fun minimo_conEspaciosAlrededor_seAcepta() {
        assertNull(ValidadorParticipantes.errorMinimo(" 5 "))
    }

    @Test
    fun minimo_vacio_oFueraDeRango_esInvalido() {
        listOf("", "   ", "0", "1001", "-3", "abc", "2.5").forEach {
            assertEquals(ErrorParticipantes.MINIMO_INVALIDO, ValidadorParticipantes.errorMinimo(it))
        }
    }

    @Test
    fun maximo_mayorOIgualQueElMinimo_esValido() {
        assertNull(ValidadorParticipantes.errorMaximo("4", "10"))
        assertNull(ValidadorParticipantes.errorMaximo("4", "4"))
    }

    @Test
    fun maximo_vacio_oFueraDeRango_esInvalido() {
        listOf("", "0", "1001", "abc").forEach {
            assertEquals(
                ErrorParticipantes.MAXIMO_INVALIDO,
                ValidadorParticipantes.errorMaximo("1", it)
            )
        }
    }

    @Test
    fun maximo_menorQueElMinimo() {
        assertEquals(
            ErrorParticipantes.MAXIMO_MENOR_QUE_MINIMO,
            ValidadorParticipantes.errorMaximo("10", "4")
        )
    }

    @Test
    fun maximo_noSeCompara_siElMinimoEsInvalido() {
        assertNull(ValidadorParticipantes.errorMaximo("", "5"))
        assertNull(ValidadorParticipantes.errorMaximo("0", "1"))
        assertNull(ValidadorParticipantes.errorMaximo("abc", "1"))
    }

    @Test
    fun rango_devuelveLosValoresCuandoSonValidos() {
        assertEquals(4, ValidadorParticipantes.rango("4", "10")?.minimo)
        assertEquals(10, ValidadorParticipantes.rango("4", "10")?.maximo)
    }

    @Test
    fun rango_esNuloCuandoLosDatosNoSonValidos() {
        assertNull(ValidadorParticipantes.rango("", "10"))
        assertNull(ValidadorParticipantes.rango("4", ""))
        assertNull(ValidadorParticipantes.rango("10", "4"))
    }
}
