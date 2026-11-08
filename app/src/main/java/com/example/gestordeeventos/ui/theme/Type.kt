package com.example.gestordeeventos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.R

/*
 * FUENTE CORPORATIVA: Poppins (licencia SIL OFL, libre para uso en apps).
 * Archivos en res/font: poppins_regular.ttf, poppins_medium.ttf, poppins_bold.ttf
 */
val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private val base = Typography()

// Escala tipográfica: se parte de la de Material 3 y se cambia la familia,
// y se redefinen los estilos que más se usan en la app.
val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Poppins),
    displayMedium = base.displayMedium.copy(fontFamily = Poppins),
    displaySmall = base.displaySmall.copy(fontFamily = Poppins),

    headlineLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = 0.sp
    ),

    titleLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp
    ),

    labelLarge = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Poppins, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    )
)
