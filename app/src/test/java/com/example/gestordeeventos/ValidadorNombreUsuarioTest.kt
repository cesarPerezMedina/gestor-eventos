package com.example.gestordeeventos

import com.example.gestordeeventos.util.ValidadorRegistro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorNombreUsuarioTest {

    @Test
    fun nombresValidos() {
        listOf("ana", "ana_23", "ANA", "a1_", "a".repeat(20))
            .forEach { assertTrue(it, ValidadorRegistro.validarNombreUsuario(it).esValido) }
    }

    @Test
    fun conEspaciosAlrededorSeAcepta() {
        assertTrue(ValidadorRegistro.validarNombreUsuario("  ana_23  ").esValido)
    }

    @Test
    fun vacioEsObligatorio() {
        assertEquals("El nombre de usuario es obligatorio", ValidadorRegistro.validarNombreUsuario("  ").mensajeError)
    }

    @Test
    fun demasiadoCortoOLargo() {
        val esperado = "Debe tener entre 3 y 20 caracteres"
        assertEquals(esperado, ValidadorRegistro.validarNombreUsuario("ab").mensajeError)
        assertEquals(esperado, ValidadorRegistro.validarNombreUsuario("a".repeat(21)).mensajeError)
    }

    @Test
    fun caracteresNoPermitidos() {
        val esperado = "Solo puede contener letras, números y guion bajo (_)"
        listOf("ana 23", "ana-23", "ana.23", "ana@23", "añadir")
            .forEach { assertEquals(it, esperado, ValidadorRegistro.validarNombreUsuario(it).mensajeError) }
    }
}
