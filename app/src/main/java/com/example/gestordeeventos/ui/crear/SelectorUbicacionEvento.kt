package com.example.gestordeeventos.ui.crear

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.data.localizacion.Ubicacion
import com.example.gestordeeventos.ui.componentes.MapaSelector
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import com.example.gestordeeventos.ui.theme.SuperficieApp

/** Dónde se juega el evento (Issue 45): recinto público, polideportivo o descampado. */
enum class TipoUbicacion(
    val etiqueta: String,
    val descripcion: String,
    val icono: ImageVector,
    val ayuda: String,
    val ejemplo: String,
    val etiquetaMapa: String,
) {
    RECINTO_PUBLICO(
        etiqueta = "Recinto público",
        descripcion = "Parque, plaza o pista municipal",
        icono = Icons.Outlined.Park,
        ayuda = "Sitio público de uso libre. Indica su nombre y dónde está.",
        ejemplo = "Ej: Pista municipal del parque del Retiro",
        etiquetaMapa = "Recinto público",
    ),
    POLIDEPORTIVO(
        etiqueta = "Polideportivo",
        descripcion = "Instalación deportiva",
        icono = Icons.Outlined.Stadium,
        ayuda = "Indica el nombre y la dirección del polideportivo. Recuerda reservar la pista.",
        ejemplo = "Ej: Polideportivo Retiro - Pista Central",
        etiquetaMapa = "Polideportivo",
    ),
    DESCAMPADO(
        etiqueta = "Descampado",
        descripcion = "Terreno libre sin reserva",
        icono = Icons.Outlined.Landscape,
        ayuda = "Sitio libre, sin reserva. Indica un punto de encuentro fácil de reconocer.",
        ejemplo = "Ej: Campo de tierra junto al parque Norte",
        etiquetaMapa = "Zona libre",
    ),
}

const val UBICACION_MIN = 3
const val UBICACION_MAX = 100
const val INDICACIONES_MAX = 150

/** Comprueba el lugar del evento. Devuelve el mensaje de error, o null si es válido. */
fun errorUbicacionEvento(ubicacion: String): String? {
    val texto = ubicacion.trim()
    return when {
        texto.isEmpty() -> "Indica dónde se juega"
        texto.length < UBICACION_MIN -> "Mínimo $UBICACION_MIN caracteres"
        else -> null
    }
}

/**
 * Selector de ubicación del evento: primero se elige el tipo (recinto o descampado) y debajo aparecen
 * los campos que corresponden a ese tipo, con una vista previa del mapa.
 *
 * Es un componente sin estado: quien lo usa guarda los valores y los cambia en los callbacks.
 *
 * @param ubicacion nombre y dirección del recinto, o punto de encuentro si es un descampado.
 * @param indicaciones solo para descampado: cómo encontrar el sitio (opcional).
 * @param mostrarError enseña el error del campo aunque el usuario aún no lo haya tocado.
 * @param coordenadas punto marcado en el mapa, o null si todavía no se ha elegido ninguno.
 * @param onCoordenadasElegidas se llama cuando el usuario toca el mapa.
 * @param onBuscarDireccion se llama al pulsar "buscar" en el teclado: el mapa debe ir a la dirección escrita.
 */
@Composable
fun SelectorUbicacionEvento(
    tipo: TipoUbicacion,
    onTipoChange: (TipoUbicacion) -> Unit,
    ubicacion: String,
    onUbicacionChange: (String) -> Unit,
    indicaciones: String,
    onIndicacionesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    mostrarError: Boolean = false,
    coordenadas: Ubicacion? = null,
    onCoordenadasElegidas: (Ubicacion) -> Unit = {},
    onUbicacionActualClick: () -> Unit = {},
    onBuscarDireccion: () -> Unit = {},
) {
    val error = if (mostrarError) errorUbicacionEvento(ubicacion) else null
    val esDescampado = tipo == TipoUbicacion.DESCAMPADO

    Column(modifier = modifier.fillMaxWidth()) {
        Etiqueta("Ubicación del evento")
        Spacer(Modifier.height(6.dp))

        // Tipo de lugar: dos opciones, solo se elige una
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TipoUbicacion.entries.forEach { opcion ->
                OpcionTipoUbicacion(
                    tipo = opcion,
                    seleccionada = opcion == tipo,
                    onClick = { onTipoChange(opcion) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = CreVioletaClaro, modifier = Modifier.size(15.dp))
            Spacer(Modifier.size(6.dp))
            Text(
                text = tipo.ayuda,
                color = CreVioleta,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }

        Spacer(Modifier.height(10.dp))

        // Lugar
        OutlinedTextField(
            value = ubicacion,
            onValueChange = { onUbicacionChange(it.take(UBICACION_MAX)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(14.dp),
            placeholder = {
                Text(
                    text = tipo.ejemplo,
                    color = CreGris,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = { Icon(Icons.Outlined.Place, contentDescription = null, tint = CreSubtitulo) },
            trailingIcon = {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CreChip)
                        .clickable(onClick = onUbicacionActualClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = "Usar mi ubicación", tint = CreVioleta, modifier = Modifier.size(18.dp))
                }
            },
            supportingText = error?.let { mensaje -> { Text(mensaje, color = CreError, fontSize = 11.sp) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = { onBuscarDireccion() }),
            colors = coloresCampoEvento(),
        )

        // Solo en descampado: cómo llegar
        if (esDescampado) {
            Spacer(Modifier.height(10.dp))
            Etiqueta("Indicaciones para encontrarlo", derecha = "Opcional", derechaGris = true)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = indicaciones,
                onValueChange = { onIndicacionesChange(it.take(INDICACIONES_MAX)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text("Ej: Entrada por la verja azul, detrás del kiosco", color = CreGris, fontSize = 13.sp) },
                supportingText = {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Text("${indicaciones.length}/$INDICACIONES_MAX", color = CreGris, fontSize = 11.sp)
                    }
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = coloresCampoEvento(),
            )
        }

        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CreLinea, RoundedCornerShape(16.dp)),
        ) {
            MapaSelector(
                ubicacion = coordenadas,
                onUbicacionElegida = onCoordenadasElegidas,
                modifier = Modifier.fillMaxSize(),
            )
            Text(
                text = tipo.etiquetaMapa,
                color = CreTinta,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SuperficieApp)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (coordenadas == null) "Toca el mapa para marcar el punto exacto" else "Punto marcado. Toca otro sitio para cambiarlo",
            color = CreSubtitulo,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun OpcionTipoUbicacion(
    tipo: TipoUbicacion,
    seleccionada: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val forma = RoundedCornerShape(14.dp)
    Column(
        modifier = modifier
            .clip(forma)
            .background(if (seleccionada) CreChip else SuperficieApp)
            .border(if (seleccionada) 2.dp else 1.dp, if (seleccionada) CreVioletaClaro else CreLinea, forma)
            .selectable(selected = seleccionada, onClick = onClick, role = Role.RadioButton)
            .padding(10.dp),
    ) {
        Icon(tipo.icono, contentDescription = null, tint = if (seleccionada) CreVioleta else CreGris, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(tipo.etiqueta, color = if (seleccionada) CreVioleta else CreTinta, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 15.sp)
        Spacer(Modifier.height(2.dp))
        Text(tipo.descripcion, color = CreSubtitulo, fontSize = 10.sp, lineHeight = 13.sp)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 700, name = "Interactivo")
@Composable
private fun SelectorUbicacionEventoPreview() {
    GestorDeEventosTheme {
        var tipo by remember { mutableStateOf(TipoUbicacion.POLIDEPORTIVO) }
        var ubicacion by remember { mutableStateOf("") }
        var indicaciones by remember { mutableStateOf("") }
        Column(Modifier.padding(16.dp)) {
            SelectorUbicacionEvento(
                tipo = tipo,
                onTipoChange = { tipo = it },
                ubicacion = ubicacion,
                onUbicacionChange = { ubicacion = it },
                indicaciones = indicaciones,
                onIndicacionesChange = { indicaciones = it },
                mostrarError = true,
            )
        }
    }
}