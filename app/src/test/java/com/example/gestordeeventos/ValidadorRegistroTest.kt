package com.example.gestordeeventos

import com.example.gestordeeventos.util.ValidadorRegistro
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorRegistroTest {

    // ---- Correo ----
    @Test
    fun correo_validos() {
        listOf("a@b.com", "usuario.nombre+tag@dominio.es", "x_y-z@sub.dominio.org")
            .forEach { assertTrue(it, ValidadorRegistro.validarCorreo(it).esValido) }
    }

    @Test
    fun correo_conEspaciosAlrededor_seAcepta() {
        assertTrue(ValidadorRegistro.validarCorreo("  a@b.com  ").esValido)
    }

    @Test
    fun correo_vacio_esObligatorio() {
        assertEquals("El correo es obligatorio", ValidadorRegistro.validarCorreo("  ").mensajeError)
    }

    @Test
    fun correo_invalidos() {
        listOf("usuario", "usuario@", "@dominio.com", "usuario@dominio", "usuario@dominio.c",
            "usuario@@dominio.com", "usu ario@dominio.com", "usuario@dominio..com", "usuario@-dominio.com")
            .forEach { assertFalse(it, ValidadorRegistro.validarCorreo(it).esValido) }
    }

    // ---- Contraseña ----
    @Test
    fun password_fuerte_esValida() {
        assertTrue(ValidadorRegistro.validarPassword("Abcdef1!").esValido)
        assertNull(ValidadorRegistro.validarPassword("Abcdef1!").mensajeError)
    }

    @Test
    fun password_vacia() {
        assertFalse(ValidadorRegistro.validarPassword("").esValido)
    }

    @Test
    fun password_muyCorta() {
        assertFalse(ValidadorRegistro.validarPassword("Ab1!").esValido)
    }

    @Test
    fun password_sinMayuscula() {
        assertFalse(ValidadorRegistro.validarPassword("abcdef1!").esValido)
    }

    @Test
    fun password_sinMinuscula() {
        assertFalse(ValidadorRegistro.validarPassword("ABCDEF1!").esValido)
    }

    @Test
    fun password_sinNumero() {
        assertFalse(ValidadorRegistro.validarPassword("Abcdefg!").esValido)
    }

    @Test
    fun password_sinCaracterEspecial() {
        assertFalse(ValidadorRegistro.validarPassword("Abcdefg1").esValido)
    }

    @Test
    fun password_conEspacios() {
        assertFalse(ValidadorRegistro.validarPassword("Abcd ef1!").esValido)
    }

    // ---- Confirmación ----
    @Test
    fun confirmacion_coincide() {
        assertTrue(ValidadorRegistro.validarConfirmacionPassword("Abcdef1!", "Abcdef1!").esValido)
    }

    @Test
    fun confirmacion_noCoincide() {
        assertEquals(
            "Las contraseñas no coinciden",
            ValidadorRegistro.validarConfirmacionPassword("Abcdef1!", "Abcdef1?").mensajeError
        )
    }

    @Test
    fun confirmacion_vacia() {
        assertFalse(ValidadorRegistro.validarConfirmacionPassword("Abcdef1!", "").esValido)
    }

    // ---- Conjunto ----
    @Test
    fun registroValido_todoCorrecto() {
        assertTrue(ValidadorRegistro.registroValido("a@b.com", "Abcdef1!", "Abcdef1!"))
    }

    @Test
    fun registroValido_algunCampoMal() {
        assertFalse(ValidadorRegistro.registroValido("a@b", "Abcdef1!", "Abcdef1!"))
        assertFalse(ValidadorRegistro.registroValido("a@b.com", "abc", "abc"))
        assertFalse(ValidadorRegistro.registroValido("a@b.com", "Abcdef1!", "otra"))
    }
}
