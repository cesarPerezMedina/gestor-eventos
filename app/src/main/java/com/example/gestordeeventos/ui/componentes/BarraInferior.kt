package com.example.gestordeeventos.ui.componentes

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.gestordeeventos.R
import com.example.gestordeeventos.navegacion.Ruta

/** Una pestaña de la barra inferior: a qué ruta lleva, su texto y su icono. */
data class PestanaInferior(
    val ruta: Ruta,
    @StringRes val etiqueta: Int,
    val icono: ImageVector
)

/** Pestañas de la barra inferior, en el orden en que se muestran (Material recomienda entre 3 y 5). */
val pestanasInferiores: List<PestanaInferior> = listOf(
    PestanaInferior(Ruta.MisEventos, R.string.nav_mis_eventos, Icons.Default.Home),
    PestanaInferior(Ruta.Buscar, R.string.nav_buscar, Icons.Default.Search),
    PestanaInferior(Ruta.Crear, R.string.nav_crear, Icons.Default.Add),
    PestanaInferior(Ruta.Amigos, R.string.nav_amigos, Icons.Default.Person),
    PestanaInferior(Ruta.Ajustes, R.string.nav_ajustes, Icons.Default.Settings)
)

/**
 * Barra de navegación inferior de la app, con los colores corporativos (verde primario).
 *
 * @param rutaActual ruta de la pantalla visible; la pestaña que coincide se marca como seleccionada.
 * @param onNavegar se llama con la [Ruta] de la pestaña pulsada.
 */
@Composable
fun BarraInferior(
    rutaActual: String?,
    onNavegar: (Ruta) -> Unit,
    modifier: Modifier = Modifier
) {
    val colores = MaterialTheme.colorScheme
    val colorSinSeleccionar = colores.onPrimary.copy(alpha = 0.7f)

    NavigationBar(
        modifier = modifier,
        containerColor = colores.primary,
        contentColor = colores.onPrimary
    ) {
        pestanasInferiores.forEach { pestana ->
            NavigationBarItem(
                selected = rutaActual == pestana.ruta.ruta,
                onClick = { onNavegar(pestana.ruta) },
                icon = { Icon(imageVector = pestana.icono, contentDescription = null) },
                label = { Text(text = stringResource(pestana.etiqueta)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colores.onPrimaryContainer,
                    selectedTextColor = colores.onPrimary,
                    indicatorColor = colores.primaryContainer,
                    unselectedIconColor = colorSinSeleccionar,
                    unselectedTextColor = colorSinSeleccionar
                )
            )
        }
    }
}
