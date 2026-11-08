package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Amistad
import org.junit.Assert.assertEquals
import org.junit.Test

/** Comprueba las reglas de [Amistad] sin necesidad de emulador. */
class AmistadTest {

    @Test
    fun entreOrdenaLosIdsDeMenorAMayor() {
        val amistad = Amistad.entre(5, 2)
        assertEquals(2L, amistad.usuarioId)
        assertEquals(5L, amistad.amigoId)
    }

    @Test
    fun entreDaLaMismaAmistadEnCualquierOrden() {
        assertEquals(Amistad.entre(2, 5), Amistad.entre(5, 2))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unUsuarioNoPuedeSerAmigoDeSiMismo() {
        Amistad.entre(3, 3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun elConstructorDirectoRechazaIdsDesordenados() {
        Amistad(usuarioId = 5, amigoId = 2)
    }
}
