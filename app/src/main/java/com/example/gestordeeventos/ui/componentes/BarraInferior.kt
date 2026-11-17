package com.example.gestordeeventos.ui.componentes

import com.example.gestordeeventos.navegacion.Ruta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BarVioleta = Color(0xFF5B0FBF)
private val BarVioletaClaro = Color(0xFF7C3AED)
private val BarBoton = Color(0xFF6D28D9)
private val BarGris = Color(0xFF7D8397)
private val BarLinea = Color(0xFFE0E3F5)

private data class ItemBarra(val ruta: String, val etiqueta: String, val icono: ImageVector)

private val itemsIzquierda = listOf(
    ItemBarra(Ruta.Inicio.ruta, "Inicio", Icons.Outlined.Home),
    ItemBarra(Ruta.Buscar.ruta, "Buscar", Icons.Outlined.Search),
)

private val itemsDerecha = listOf(
    ItemBarra(Ruta.Eventos.ruta, "Eventos", Icons.Outlined.Event),
    ItemBarra(Ruta.Amigos.ruta, "Amigos", Icons.Outlined.Group),
)

/**
 * Menú principal de abajo: Inicio, Buscar, "+" (crear), Eventos y Amigos.
 *
 * @param rutaActual la ruta de la pantalla que se está viendo (para marcar la opción activa).
 * @param onNavegar se llama con la ruta de la sección pulsada.
 * @param onCrearClick se llama al pulsar el botón "+".
 */
@Composable
fun BarraInferior(
    rutaActual: String?,
    onNavegar: (String) -> Unit,
    onCrearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(BarLinea))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                itemsIzquierda.forEach { item ->
                    ItemNavegacion(item, rutaActual == item.ruta, Modifier.weight(1f)) {
                        onNavegar(item.ruta)
                    }
                }
                Spacer(Modifier.weight(1f)) // hueco para el botón central
                itemsDerecha.forEach { item ->
                    ItemNavegacion(item, rutaActual == item.ruta, Modifier.weight(1f)) {
                        onNavegar(item.ruta)
                    }
                }
            }
        }

        // Botón central elevado "+"
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-22).dp)
                .size(58.dp)
                .shadow(10.dp, CircleShape, spotColor = BarVioleta)
                .background(Brush.linearGradient(listOf(BarVioletaClaro, BarVioleta)), CircleShape)
                .clickable(onClick = onCrearClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Crear evento",
                tint = Color.White,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun ItemNavegacion(
    item: ItemBarra,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val color = if (seleccionado) BarBoton else BarGris
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(item.icono, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(2.dp))
        Text(item.etiqueta, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, widthDp = 390)
@Composable
private fun BarraInferiorPreview() {
    BarraInferior(
        rutaActual = Ruta.Inicio.ruta,
        onNavegar = {},
        onCrearClick = {},
        modifier = Modifier.padding(top = 30.dp),
    )
}
