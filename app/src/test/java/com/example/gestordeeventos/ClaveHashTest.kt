package com.example.gestordeeventos

import com.example.gestordeeventos.util.ClaveHash
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClaveHashTest {

    private val clave = "Clave#2026"

    @Test
    fun elHashNoContieneLaClaveEnTextoPlano() {
        assertFalse(ClaveHash.generar(clave).contains(clave))
    }

    @Test
    fun dosHashesDeLaMismaClaveSonDistintosPorLaSal() {
        assertNotEquals(ClaveHash.generar(clave), ClaveHash.generar(clave))
    }

    @Test
    fun verificaLaClaveCorrecta() {
        assertTrue(ClaveHash.verificar(clave, ClaveHash.generar(clave)))
    }

    @Test
    fun rechazaUnaClaveIncorrecta() {
        val hash = ClaveHash.generar(clave)
        assertFalse(ClaveHash.verificar("clave#2026", hash))
        assertFalse(ClaveHash.verificar("", hash))
    }

    @Test
    fun unFormatRotoNuncaCoincide() {
        listOf("", clave, "pbkdf2:abc:x:y", "pbkdf2:100000:!!!:!!!", "pbkdf2:0:AAAA:AAAA", "otro:100000:AAAA:AAAA")
            .forEach { assertFalse(it, ClaveHash.verificar(clave, it)) }
    }
}
