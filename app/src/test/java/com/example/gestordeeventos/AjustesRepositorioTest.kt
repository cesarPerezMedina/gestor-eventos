package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.ModoTema
import com.example.gestordeeventos.data.repositorio.AjustesRepositorio
import com.example.gestordeeventos.data.repositorio.AlmacenAjustes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class AlmacenAjustesEnMemoria : AlmacenAjustes {
    val datos = mutableMapOf<String, String>()
    override fun leer(clave: String): String? = datos[clave]
    override fun guardar(clave: String, valor: String) {
        datos[clave] = valor
    }
}

/** Comprueba los ajustes de tema sin necesidad de emulador. */
class AjustesRepositorioTest {

    private val almacen = AlmacenAjustesEnMemoria()
    private val repositorio = AjustesRepositorio(almacen)

    @Test
    fun pordefectoSigueAlSistema() {
        assertEquals(ModoTema.SISTEMA, repositorio.modoTema.value)
    }

    @Test
    fun cambiarElModoActualizaElValorObservado() {
        repositorio.cambiarModoTema(ModoTema.OSCURO)

        assertEquals(ModoTema.OSCURO, repositorio.modoTema.value)
    }

    @Test
    fun elModoElegidoSeRecuerdaAlCrearOtroRepositorio() {
        repositorio.cambiarModoTema(ModoTema.CLARO)

        assertEquals(ModoTema.CLARO, AjustesRepositorio(almacen).modoTema.value)
    }

    @Test
    fun unValorGuardadoDesconocidoVuelveAlSistema() {
        almacen.guardar("modo_tema", "MORADO")

        assertEquals(ModoTema.SISTEMA, AjustesRepositorio(almacen).modoTema.value)
    }

    @Test
    fun desdeNombreAceptaNuloYDesconocidos() {
        assertEquals(ModoTema.SISTEMA, ModoTema.desdeNombre(null))
        assertEquals(ModoTema.SISTEMA, ModoTema.desdeNombre(""))
        assertEquals(ModoTema.OSCURO, ModoTema.desdeNombre("OSCURO"))
    }

    @Test
    fun elModoSistemaCopiaAlMovil() {
        assertTrue(ModoTema.SISTEMA.esOscuro(sistemaOscuro = true))
        assertFalse(ModoTema.SISTEMA.esOscuro(sistemaOscuro = false))
    }

    @Test
    fun claroYOscuroIgnoranAlMovil() {
        assertFalse(ModoTema.CLARO.esOscuro(sistemaOscuro = true))
        assertTrue(ModoTema.OSCURO.esOscuro(sistemaOscuro = false))
    }
}
