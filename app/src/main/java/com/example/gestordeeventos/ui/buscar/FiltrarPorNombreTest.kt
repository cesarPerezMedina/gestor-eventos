package com.example.gestordeeventos.ui.buscar


import kotlin.test.assertEquals

class FiltrarPorNombreTest {

    private val lista = eventosBuscarDeEjemplo

    @Test
    fun consultaVaciaDevuelveTodo() {
        assertEquals(lista.size, filtrarPorNombre(lista, "   ").size)
    }

    @Test
    fun ignoraTildesYMayusculas() {
        assertEquals(listOf("3"), filtrarPorNombre(lista, "FUTBOL").map { it.id })
    }

    @Test
    fun variasPalabrasEnCualquierOrden() {
        assertEquals(listOf("4"), filtrarPorNombre(lista, "mixto padel").map { it.id })
    }

    @Test
    fun sinCoincidenciasDevuelveVacio() {
        assertEquals(emptyList<String>(), filtrarPorNombre(lista, "rugby").map { it.id })
    }
}

annotation class Test
