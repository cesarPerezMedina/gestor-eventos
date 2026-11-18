package com.example.gestordeeventos.ui.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.R
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EscalaTexto
import kotlin.math.roundToInt

/** Título de sección (icono + texto) y, debajo, una tarjeta con los ajustes de esa sección. */
@Composable
internal fun SeccionAjustes(titulo: String, icono: ImageVector, contenido: @Composable () -> Unit) {
    Row(
        modifier = Modifier.padding(start = 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = titulo,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
    TarjetaAjustes(contenido = contenido)
}

/** Tarjeta blanca con esquinas redondeadas y sombra suave. */
@Composable
internal fun TarjetaAjustes(modifier: Modifier = Modifier, contenido: @Composable () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        contenido()
    }
}

/** Línea fina que separa dos ajustes dentro de la misma tarjeta. */
@Composable
internal fun SeparadorAjustes() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    )
}

/**
 * Fila con título, descripción y un interruptor a la derecha. Se pulsa en toda la fila, no solo en el interruptor.
 * Si se pasa [icono], se pinta en un cuadrado de color a la izquierda.
 */
@Composable
internal fun FilaInterruptor(
    titulo: String,
    descripcion: String,
    marcado: Boolean,
    onCambio: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    icono: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(value = marcado, role = Role.Switch, onValueChange = onCambio)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icono != null) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = descripcion,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
        Spacer(Modifier.size(12.dp))
        // El clic lo gestiona la fila entera (toggleable); el interruptor solo dibuja el estado.
        Switch(checked = marcado, onCheckedChange = null)
    }
}

/** Título del ajuste a la izquierda y, a la derecha, una etiqueta pequeña en violeta (p. ej. "NORMAL (100%)"). */
@Composable
private fun CabeceraAjuste(titulo: String, etiqueta: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = titulo,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = etiqueta,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

/** Deslizador de 4 posiciones para el tamaño del texto, con una frase de ejemplo debajo. */
@Composable
internal fun SelectorTamanoTexto(
    escala: EscalaTexto,
    onCambio: (EscalaTexto) -> Unit,
    modifier: Modifier = Modifier,
) {
    val ultima = EscalaTexto.entries.lastIndex
    val porcentaje = (escala.factor * 100).roundToInt()
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        CabeceraAjuste(
            titulo = stringResource(R.string.ajustes_texto_titulo),
            etiqueta = stringResource(
                R.string.ajustes_texto_valor,
                stringResource(escala.etiqueta).uppercase(),
                porcentaje,
            ),
        )
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val colorA = MaterialTheme.colorScheme.onSurfaceVariant
            Text("A", color = colorA, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("A", color = colorA, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        // 4 posiciones = 2 puntos intermedios entre los extremos
        Slider(
            value = escala.ordinal.toFloat(),
            onValueChange = { onCambio(EscalaTexto.entries[it.roundToInt().coerceIn(0, ultima)]) },
            valueRange = 0f..ultima.toFloat(),
            steps = ultima - 1,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.ajustes_texto_ejemplo),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Colores de los chips: violeta con texto blanco al seleccionarlos, gris azulado en reposo. */
@Composable
private fun coloresChip() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.secondaryContainer,
    labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
    iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
    selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimary,
)

/** Radio de búsqueda: un chip por cada radio posible; solo uno queda seleccionado. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectorRadioBusqueda(
    radioKm: Int,
    opciones: List<Int>,
    onCambio: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        CabeceraAjuste(
            titulo = stringResource(R.string.ajustes_radio_titulo),
            etiqueta = stringResource(R.string.ajustes_radio_etiqueta),
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            opciones.forEach { km ->
                FilterChip(
                    selected = km == radioKm,
                    onClick = { onCambio(km) },
                    label = {
                        Text(
                            text = stringResource(R.string.ajustes_radio_km, km),
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp),
                            textAlign = TextAlign.Center,
                        )
                    },
                    colors = coloresChip(),
                    border = null,
                    shape = RoundedCornerShape(14.dp),
                )
            }
        }
    }
}

/** Deportes favoritos: chips que se marcan y desmarcan (varios a la vez). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SelectorDeportesFavoritos(
    disponibles: List<Deporte>,
    favoritos: Set<Deporte>,
    onAlternar: (Deporte) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text(
            text = stringResource(R.string.ajustes_deportes_titulo),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = stringResource(R.string.ajustes_deportes_descripcion),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            disponibles.forEach { deporte ->
                val marcado = deporte in favoritos
                FilterChip(
                    selected = marcado,
                    onClick = { onAlternar(deporte) },
                    label = { Text(text = stringResource(deporte.etiqueta)) },
                    trailingIcon = {
                        Icon(
                            imageVector = if (marcado) Icons.Filled.Check else Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                    },
                    colors = coloresChip(),
                    border = null,
                    shape = RoundedCornerShape(50),
                )
            }
        }
    }
}
