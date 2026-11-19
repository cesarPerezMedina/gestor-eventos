package com.example.gestordeeventos

import androidx.compose.ui.graphics.Color
import com.example.gestordeeventos.ui.theme.PaletaApp
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

class PaletaAppTest {
    private val claro = Color(0xFFF6F4FF)
    private val oscuro = Color(0xFF15131F)

    @After
    fun volverAClaro() {
        PaletaApp.oscuro = false
    }

    @Test
    fun enModoClaroDevuelveElColorClaro() {
        PaletaApp.oscuro = false
        assertEquals(claro, PaletaApp.elegir(claro, oscuro))
    }

    @Test
    fun enModoOscuroDevuelveElColorOscuro() {
        PaletaApp.oscuro = true
        assertEquals(oscuro, PaletaApp.elegir(claro, oscuro))
    }

    @Test
    fun alCambiarDeModoElMismoColorCambia() {
        PaletaApp.oscuro = false
        val antes = PaletaApp.elegir(claro, oscuro)
        PaletaApp.oscuro = true
        assertEquals(claro, antes)
        assertEquals(oscuro, PaletaApp.elegir(claro, oscuro))
    }
}
