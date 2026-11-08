package com.example.gestordeeventos.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * PALETA CORPORATIVA - Gestor de Eventos Deportivos
 *
 *  Verde  -> color de marca (césped / deporte al aire libre)
 *  Ámbar  -> acento (energía, llamadas a la acción secundarias)
 *  Azul   -> apoyo (información, enlaces, mapas)
 *
 * Todas las parejas texto/fondo cumplen contraste WCAG AA (>= 4.5:1).
 * Para cambiar la identidad de la app basta con editar los valores de este archivo.
 */

// ---------- Tema claro ----------
val VerdePrimario = Color(0xFF0A7B43)
val VerdeSobrePrimario = Color(0xFFFFFFFF)
val VerdeContenedor = Color(0xFFB7F0C9)
val VerdeSobreContenedor = Color(0xFF00210F)

val AmbarSecundario = Color(0xFFF59E0B)
val AmbarSobreSecundario = Color(0xFF1A1100)
val AmbarContenedor = Color(0xFFFFDDAE)
val AmbarSobreContenedor = Color(0xFF2B1A00)

val AzulTerciario = Color(0xFF1D5FBF)
val AzulSobreTerciario = Color(0xFFFFFFFF)
val AzulContenedor = Color(0xFFD6E3FF)
val AzulSobreContenedor = Color(0xFF001B3F)

val FondoClaro = Color(0xFFF6F8F5)
val SuperficieClara = Color(0xFFFFFFFF)
val SuperficieVariantClara = Color(0xFFE3EAE3)
val TextoClaro = Color(0xFF17211B)
val TextoSecundarioClaro = Color(0xFF414B44)
val BordeClaro = Color(0xFF717B73)

val ErrorClaro = Color(0xFFBA1A1A)
val SobreErrorClaro = Color(0xFFFFFFFF)
val ErrorContenedorClaro = Color(0xFFFFDAD6)
val SobreErrorContenedorClaro = Color(0xFF410002)

// ---------- Tema oscuro ----------
val VerdePrimarioOscuro = Color(0xFF6FDC98)
val VerdeSobrePrimarioOscuro = Color(0xFF00391C)
val VerdeContenedorOscuro = Color(0xFF005229)
val VerdeSobreContenedorOscuro = Color(0xFFB7F0C9)

val AmbarSecundarioOscuro = Color(0xFFFFB84D)
val AmbarSobreSecundarioOscuro = Color(0xFF3A2400)
val AmbarContenedorOscuro = Color(0xFF5A3C00)
val AmbarSobreContenedorOscuro = Color(0xFFFFDDAE)

val AzulTerciarioOscuro = Color(0xFFA9C7FF)
val AzulSobreTerciarioOscuro = Color(0xFF002F65)
val AzulContenedorOscuro = Color(0xFF00458E)
val AzulSobreContenedorOscuro = Color(0xFFD6E3FF)

val FondoOscuro = Color(0xFF0F1511)
val SuperficieOscura = Color(0xFF151B17)
val SuperficieVariantOscura = Color(0xFF3B453E)
val TextoOscuro = Color(0xFFE1E9E2)
val TextoSecundarioOscuro = Color(0xFFBFC9C1)
val BordeOscuro = Color(0xFF89938B)

val ErrorOscuro = Color(0xFFFFB4AB)
val SobreErrorOscuro = Color(0xFF690005)
val ErrorContenedorOscuro = Color(0xFF93000A)
val SobreErrorContenedorOscuro = Color(0xFFFFDAD6)

// ---------- Colores semánticos propios de la app ----------
// (estado de un evento; se usan con MaterialTheme.colorScheme o directamente)
val EventoPublico = VerdePrimario
val EventoPrivado = AzulTerciario
val EventoCompleto = AmbarSecundario
