package com.example.gestordeeventos

import com.example.gestordeeventos.data.model.Deporte
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

/** Comprueba los ajustes (tema, texto, radio, deportes y avisos) sin necesidad de emulador. */
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

    // ---------- Preferencias de juego (Issue 77) ----------

    @Test
    fun porDefectoElRadioEsDiezKm() {
        assertEquals(10, repositorio.radioBusquedaKm.value)
    }

    @Test
    fun cambiarElRadioActualizaYSeRecuerda() {
        repositorio.cambiarRadioBusquedaKm(25)

        assertEquals(25, repositorio.radioBusquedaKm.value)
        assertEquals(25, AjustesRepositorio(almacen).radioBusquedaKm.value)
    }

    @Test
    fun unRadioQueNoEstaEnLaListaSeIgnora() {
        repositorio.cambiarRadioBusquedaKm(7)

        assertEquals(10, repositorio.radioBusquedaKm.value)
        assertEquals(null, almacen.leer("radio_busqueda_km"))
    }

    @Test
    fun unRadioGuardadoInvalidoVuelveAlPorDefecto() {
        almacen.guardar("radio_busqueda_km", "999")

        assertEquals(10, AjustesRepositorio(almacen).radioBusquedaKm.value)
    }

    @Test
    fun porDefectoNoHayDeportesFavoritos() {
        assertTrue(repositorio.deportesFavoritos.value.isEmpty())
    }

    @Test
    fun alternarUnDeporteLoMarcaYLoDesmarca() {
        repositorio.alternarDeporteFavorito(Deporte.PADEL)
        assertEquals(setOf(Deporte.PADEL), repositorio.deportesFavoritos.value)

        repositorio.alternarDeporteFavorito(Deporte.PADEL)
        assertTrue(repositorio.deportesFavoritos.value.isEmpty())
    }

    @Test
    fun losDeportesFavoritosSeRecuerdan() {
        repositorio.alternarDeporteFavorito(Deporte.FUTBOL)
        repositorio.alternarDeporteFavorito(Deporte.TENIS)

        assertEquals(setOf(Deporte.FUTBOL, Deporte.TENIS), AjustesRepositorio(almacen).deportesFavoritos.value)
    }

    @Test
    fun losDeportesGuardadosDesconocidosOReservadosSeIgnoran() {
        almacen.guardar("deportes_favoritos", "FUTBOL,CURLING,OTROS")

        assertEquals(setOf(Deporte.FUTBOL), AjustesRepositorio(almacen).deportesFavoritos.value)
    }

    @Test
    fun otrosNoSePuedeElegirComoDeporteFavorito() {
        assertFalse(Deporte.OTROS in AjustesRepositorio.DEPORTES_FAVORITOS_DISPONIBLES)
    }

    // ---------- Notificaciones y avisos (Issue 77) ----------

    @Test
    fun losAvisosTienenSusValoresPorDefecto() {
        assertTrue(repositorio.recordatorioPartidos.value)
        assertTrue(repositorio.avisosPlazasVacantes.value)
        assertFalse(repositorio.invitacionesDirectas.value)
    }

    @Test
    fun cambiarCadaAvisoActualizaYSeRecuerda() {
        repositorio.cambiarRecordatorioPartidos(false)
        repositorio.cambiarAvisosPlazasVacantes(false)
        repositorio.cambiarInvitacionesDirectas(true)

        val otro = AjustesRepositorio(almacen)
        assertFalse(otro.recordatorioPartidos.value)
        assertFalse(otro.avisosPlazasVacantes.value)
        assertTrue(otro.invitacionesDirectas.value)
    }

    @Test
    fun unAvisoGuardadoConBasuraVuelveAlPorDefecto() {
        almacen.guardar("aviso_recordatorio_partidos", "quizas")

        assertTrue(AjustesRepositorio(almacen).recordatorioPartidos.value)
    }
}