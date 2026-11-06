package com.example.gestordeeventos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

private val EsquemaClaro = lightColorScheme(
    primary = VerdePrimario,
    onPrimary = VerdeSobrePrimario,
    primaryContainer = VerdeContenedor,
    onPrimaryContainer = VerdeSobreContenedor,
    secondary = AmbarSecundario,
    onSecondary = AmbarSobreSecundario,
    secondaryContainer = AmbarContenedor,
    onSecondaryContainer = AmbarSobreContenedor,
    tertiary = AzulTerciario,
    onTertiary = AzulSobreTerciario,
    tertiaryContainer = AzulContenedor,
    onTertiaryContainer = AzulSobreContenedor,
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = SuperficieClara,
    onSurface = TextoClaro,
    surfaceVariant = SuperficieVariantClara,
    onSurfaceVariant = TextoSecundarioClaro,
    outline = BordeClaro,
    error = ErrorClaro,
    onError = SobreErrorClaro,
    errorContainer = ErrorContenedorClaro,
    onErrorContainer = SobreErrorContenedorClaro
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdePrimarioOscuro,
    onPrimary = VerdeSobrePrimarioOscuro,
    primaryContainer = VerdeContenedorOscuro,
    onPrimaryContainer = VerdeSobreContenedorOscuro,
    secondary = AmbarSecundarioOscuro,
    onSecondary = AmbarSobreSecundarioOscuro,
    secondaryContainer = AmbarContenedorOscuro,
    onSecondaryContainer = AmbarSobreContenedorOscuro,
    tertiary = AzulTerciarioOscuro,
    onTertiary = AzulSobreTerciarioOscuro,
    tertiaryContainer = AzulContenedorOscuro,
    onTertiaryContainer = AzulSobreContenedorOscuro,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = SuperficieOscura,
    onSurface = TextoOscuro,
    surfaceVariant = SuperficieVariantOscura,
    onSurfaceVariant = TextoSecundarioOscuro,
    outline = BordeOscuro,
    error = ErrorOscuro,
    onError = SobreErrorOscuro,
    errorContainer = ErrorContenedorOscuro,
    onErrorContainer = SobreErrorContenedorOscuro
)

/**
 * Tema global de la app. Envuelve TODA la interfaz (ver MainActivity).
 *
 * @param darkTheme   true = oscuro, false = claro. Por defecto sigue al sistema.
 *                    Cuando AjustesRepositorio.temaOscuro tenga valor, pásalo aquí.
 * @param escalaTexto multiplicador del tamaño de texto (1.0 = normal), pensado para
 *                    el ajuste de accesibilidad de AjustesRepositorio.escalaTexto.
 */
@Composable
fun GestorDeEventosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    escalaTexto: Float = 1f,
    content: @Composable () -> Unit
) {
    val densidad = LocalDensity.current
    val densidadEscalada = Density(
        density = densidad.density,
        fontScale = densidad.fontScale * escalaTexto
    )

    CompositionLocalProvider(LocalDensity provides densidadEscalada) {
        MaterialTheme(
            colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
            typography = Typography,
            shapes = Formas,
            content = content
        )
    }
}
