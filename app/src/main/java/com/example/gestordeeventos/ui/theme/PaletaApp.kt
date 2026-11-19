package com.example.gestordeeventos.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Modo claro/oscuro para los colores propios de cada pantalla (Inicio, Buscar, Amigos…).
 *
 * Esas pantallas definen su paleta con colores fijos (`val IniPagina = …`). Para que sigan el interruptor
 * de Ajustes, cada color se declara con [elegir]: se pasa el color claro y el oscuro, y se devuelve el que
 * toca. [oscuro] es un estado de Compose: al cambiarlo, todas las pantallas se redibujan solas.
 * Lo actualiza [GestorDeEventosTheme] con el valor que viene de MainActivity.
 */
object PaletaApp {
    var oscuro by mutableStateOf(false)

    fun elegir(claro: Color, oscuroColor: Color): Color = if (oscuro) oscuroColor else claro
}

/** Fondo de las tarjetas, campos y barras (era `Color.White`). */
val SuperficieApp: Color
    get() = PaletaApp.elegir(Color.White, Color(0xFF1E1B2E))
