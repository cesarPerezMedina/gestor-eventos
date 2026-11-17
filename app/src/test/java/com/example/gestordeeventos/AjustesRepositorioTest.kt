package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.EscalaTexto
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

    // ---------- Tamaño de texto (Issue 79) ----------

    @Test
    fun porDefectoElTextoEsNormal() {
        assertEquals(EscalaTexto.NORMAL, repositorio.escalaTexto.value)
    }

    @Test
    fun cambiarLaEscalaActualizaElValorObservado() {
        repositorio.cambiarEscalaTexto(EscalaTexto.GRANDE)

        assertEquals(EscalaTexto.GRANDE, repositorio.escalaTexto.value)
    }

    @Test
    fun laEscalaElegidaSeRecuerdaAlCrearOtroRepositorio() {
        repositorio.cambiarEscalaTexto(EscalaTexto.MUY_GRANDE)

        assertEquals(EscalaTexto.MUY_GRANDE, AjustesRepositorio(almacen).escalaTexto.value)
    }

    @Test
    fun unaEscalaGuardadaDesconocidaVuelveANormal() {
        almacen.guardar("escala_texto", "ENORME")

        assertEquals(EscalaTexto.NORMAL, AjustesRepositorio(almacen).escalaTexto.value)
    }

    @Test
    fun elTemaYElTextoSeGuardanPorSeparado() {
        repositorio.cambiarModoTema(ModoTema.OSCURO)
        repositorio.cambiarEscalaTexto(EscalaTexto.PEQUENO)

        val otro = AjustesRepositorio(almacen)
        assertEquals(ModoTema.OSCURO, otro.modoTema.value)
        assertEquals(EscalaTexto.PEQUENO, otro.escalaTexto.value)
    }

    @Test
    fun losFactoresCrecenDeMenorAMayorYElNormalEsUno() {
        val factores = EscalaTexto.entries.map { it.factor }

        assertEquals(factores.sorted(), factores)
        assertEquals(1f, EscalaTexto.NORMAL.factor, 0f)
    }

    @Test
    fun desdeNombreDeEscalaAceptaNuloYDesconocidos() {
        assertEquals(EscalaTexto.NORMAL, EscalaTexto.desdeNombre(null))
        assertEquals(EscalaTexto.NORMAL, EscalaTexto.desdeNombre("xl"))
        assertEquals(EscalaTexto.GRANDE, EscalaTexto.desdeNombre("GRANDE"))
    }
}
