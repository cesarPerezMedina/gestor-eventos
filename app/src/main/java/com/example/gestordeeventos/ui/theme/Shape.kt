package com.example.gestordeeventos.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * Formas base. Los componentes de Material 3 las toman solos:
 *  - extraSmall: chips pequeños, snackbars
 *  - small:      botones, campos de texto, chips
 *  - medium:     tarjetas (EventoCard), menús
 *  - large:      hojas inferiores, diálogos
 *  - extraLarge: elementos destacados
 */
val Formas = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
