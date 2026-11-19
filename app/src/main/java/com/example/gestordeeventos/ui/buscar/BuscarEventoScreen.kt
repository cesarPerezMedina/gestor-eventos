package com.example.gestordeeventos.ui.buscar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import java.text.Normalizer
import java.util.Locale
import com.example.gestordeeventos.ui.theme.SuperficieApp

// --- Modelos de la interfaz (solo lo que se pinta; más adelante se rellenarán desde Room) ---

enum class ModoBusqueda(val etiqueta: String, val icono: ImageVector) {
    CERCA_DE_MI("Cerca de mí", Icons.Outlined.Navigation),
    POR_NOMBRE("Por nombre", Icons.Outlined.SortByAlpha),
}

/** Radios de búsqueda en kilómetros. */
val RADIOS_KM = listOf(2, 5, 10, 25)

/** Chip de deporte de la fila de filtros. [icono] es null para "Todos". */
data class FiltroDeporteBuscar(
    val id: String,
    val nombre: String,
    val icono: ImageVector?,
    val cantidad: Int,
)


/** Un evento tal como se pinta en la tarjeta. */
/*data class EventoBuscarItem(
    val id: String,
    val titulo: String,
    val icono: ImageVector,
    val cuando: String,
    val distanciaKm: Double? = null, // null mientras no se conozca la ubicación del usuario
    val lugar: String,
    val apuntados: Int,
    val maximo: Int,
    val cupoUnidad: String = "jugadores",
    val cupoNota: String? = null,
    val etiqueta: String? = null,
    val estiloEtiqueta: EstiloEtiqueta = EstiloEtiqueta.NEUTRA,
    val organizador: String? = null,
    val avatares: List<String> = emptyList(),
)*/
private val MARCAS_DIACRITICAS = Regex("\\p{InCombiningDiacriticalMarks}+")

/** Minúsculas, sin tildes y sin espacios sobrantes: "  Pádel " -> "padel". */
private fun String.normalizar(): String =
    Normalizer.normalize(trim().lowercase(Locale.getDefault()), Normalizer.Form.NFD)
        .replace(MARCAS_DIACRITICAS, "")

/**
 * Devuelve los eventos cuyo título contiene TODAS las palabras de la consulta
 * (sin importar mayúsculas ni tildes). Con la consulta vacía devuelve la lista entera.
 */
fun filtrarPorNombre(eventos: List<EventoBuscarItem>, consulta: String): List<EventoBuscarItem> {
    val palabras = consulta.normalizar().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (palabras.isEmpty()) return eventos
    return eventos.filter { evento ->
        val titulo = evento.titulo.normalizar()
        palabras.all { it in titulo }
    }
}

/**
 * Pantalla "Buscar eventos" (Issue 54). Recibe los eventos ya cargados ([BuscarEventoRoute] los trae de Room) y
 * aplica ella misma el texto del buscador, el chip de deporte y el orden de la pestaña "Por nombre"
 * ([filtrarEventos]). El modo y el radio se avisan con callbacks, porque cambian la consulta a la base de datos.
 *
 * Todos los parámetros tienen valor por defecto, así que `BuscarEventoScreen()` sigue compilando.
 * La lista y los filtros por defecto son datos de ejemplo, solo para las vistas previas.
 */
@Composable
fun BuscarEventoScreen(
    modifier: Modifier = Modifier,
    eventos: List<EventoBuscarItem> = eventosBuscarDeEjemplo,
    filtrosDeporte: List<FiltroDeporteBuscar> = filtrosDeporteDeEjemplo,
    onMapaClick: () -> Unit = {},
    onModoChange: (ModoBusqueda) -> Unit = {},
    onRadioChange: (Int) -> Unit = {},
    avisoUbicacion: String? = null,
    onAvisoUbicacionClick: () -> Unit = {},
    onBuscar: (String) -> Unit = {},
    onDetalleClick: (String) -> Unit = {},
    onUnirmeClick: (String) -> Unit = {},
    onRechazarClick: (String) -> Unit = {},
    onCrearEventoClick: () -> Unit = {},
) {
    var consulta by rememberSaveable { mutableStateOf("") }
    var modo by rememberSaveable { mutableStateOf(ModoBusqueda.CERCA_DE_MI) }
    var radioKm by rememberSaveable { mutableStateOf(RADIOS_KM.first()) }
    var deporteElegido by rememberSaveable { mutableStateOf(filtrosDeporte.firstOrNull()?.id.orEmpty()) }

    // Si el deporte elegido ya no tiene eventos (su chip desaparece), se vuelve a "Todos"
    val deporteId = if (filtrosDeporte.any { it.id == deporteElegido }) deporteElegido else filtrosDeporte.firstOrNull()?.id.orEmpty()

    // Lo que se enseña: el texto del buscador y el chip de deporte se aplican aquí, sobre los eventos recibidos
    val visibles = filtrarEventos(
        eventos = eventos,
        consulta = consulta,
        deporteId = deporteId,
        ordenarPorNombre = modo == ModoBusqueda.POR_NOMBRE,
    )
    val hayFiltrosPuestos = consulta.isNotBlank() || deporteId != filtrosDeporte.firstOrNull()?.id.orEmpty()
    val orden = when {
        modo == ModoBusqueda.POR_NOMBRE -> "A-Z"
        eventos.any { it.distanciaKm != null } -> "Distancia"
        else -> "Fecha"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BusPagina),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Cabecera(onMapaClick) }

        item {
            BarraBusqueda(
                consulta = consulta,
                onCambio = { consulta = it },
                onBuscar = { onBuscar(consulta.trim()) },
            )
        }

        item { PestanasModo(modo = modo, onCambio = { modo = it; onModoChange(it) }) }

        // El radio solo tiene sentido al buscar cerca de ti
        if (modo == ModoBusqueda.CERCA_DE_MI) {
            item { FilaRadio(seleccionado = radioKm, onCambio = { radioKm = it; onRadioChange(it) }) }
            if (avisoUbicacion != null) {
                item { AvisoUbicacion(texto = avisoUbicacion, onClick = onAvisoUbicacionClick) }
            }
        }

        item {
            FilaDeportes(
                filtros = filtrosDeporte,
                seleccionadoId = deporteId,
                onCambio = { deporteElegido = it },
            )
        }

        item {
            EncabezadoLista(
                titulo = if (modo == ModoBusqueda.CERCA_DE_MI) "Eventos cercanos" else "Resultados",
                disponibles = visibles.size,
                orden = orden,
            )
        }

        items(visibles, key = { it.id }) { evento ->
            TarjetaEventoBuscar(
                evento = evento,
                onClick = { onDetalleClick(evento.id) },
                // Botón de la izquierda: "Ver detalle" / "Ver cuadro", o "Rechazar" en una invitación
                onSecundarioClick = {
                    if (evento.variante == VarianteTarjeta.PRIVADO) onRechazarClick(evento.id) else onDetalleClick(evento.id)
                },
                // Botón de la derecha: "Unirme", "Reservar", "Solicitar plaza" o "Aceptar"
                onPrincipalClick = { onUnirmeClick(evento.id) },
            )
        }

        if (visibles.isEmpty()) {
            item {
                Text(
                    text = when {
                        hayFiltrosPuestos && eventos.isNotEmpty() -> "No hay eventos con estos filtros"
                        modo == ModoBusqueda.CERCA_DE_MI && avisoUbicacion == null ->
                            "No hay eventos en este radio. Prueba con más km o busca «Por nombre»"
                        else -> "Todavía no hay eventos públicos. ¡Crea el primero!"
                    },
                    color = BusGris,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                )
            }
        }

        item { BannerCrear(onCrearEventoClick) }
    }
}

// ---------- Cabecera ----------

@Composable
private fun Cabecera(onMapaClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Buscar eventos", color = BusTinta, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Encuentra partidos y torneos cerca de ti",
                color = BusSubtitulo,
                fontSize = 14.sp,
                lineHeight = 19.sp,
            )
        }
        Spacer(Modifier.width(10.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(BusChip)
                .clickable(onClick = onMapaClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Map, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Mapa", color = BusVioleta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------- Búsqueda ----------

@Composable
private fun BarraBusqueda(
    consulta: String,
    onCambio: (String) -> Unit,
    onBuscar: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        OutlinedTextField(
            value = consulta,
            onValueChange = onCambio,
            modifier = Modifier.weight(1f),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            placeholder = {
                Text(
                    "Nombre de evento, pista o club...",
                    color = BusGris,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = BusSubtitulo) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onBuscar() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SuperficieApp,
                unfocusedContainerColor = SuperficieApp,
                focusedBorderColor = BusVioletaClaro,
                unfocusedBorderColor = BusLinea,
                cursorColor = BusVioleta,
            ),
        )
    }
}

@Composable
private fun PestanasModo(modo: ModoBusqueda, onCambio: (ModoBusqueda) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BusCampo)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ModoBusqueda.entries.forEach { opcion ->
            val activa = opcion == modo
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .background(if (activa) SuperficieApp else Color.Transparent)
                    .clickable { onCambio(opcion) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    opcion.icono,
                    contentDescription = null,
                    tint = if (activa) BusVioleta else BusGris,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    opcion.etiqueta,
                    color = if (activa) BusVioleta else BusGris,
                    fontSize = 13.sp,
                    fontWeight = if (activa) FontWeight.Bold else FontWeight.Medium,
                )
            }
        }
    }
}

// ---------- Filtros ----------

@Composable
private fun FilaRadio(seleccionado: Int, onCambio: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("RADIO:", color = BusGris, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        RADIOS_KM.forEach { km ->
            val activo = km == seleccionado
            Text(
                text = if (km == RADIOS_KM.first()) "< $km km" else "$km km",
                color = if (activo) Color.White else BusTinta,
                fontSize = 12.sp,
                fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (activo) BusBoton else BusChip)
                    .clickable { onCambio(km) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
    }
}

/** Franja que explica por qué no se puede buscar cerca (sin permiso, ubicación apagada...) y deja arreglarlo. */
@Composable
private fun AvisoUbicacion(texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BusChip)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, color = BusVioleta, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun FilaDeportes(
    filtros: List<FiltroDeporteBuscar>,
    seleccionadoId: String,
    onCambio: (String) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filtros, key = { it.id }) { filtro ->
            val activo = filtro.id == seleccionadoId
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (activo) BusBoton else SuperficieApp)
                    .border(1.dp, if (activo) BusBoton else BusLinea, RoundedCornerShape(50))
                    .clickable { onCambio(filtro.id) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (filtro.icono != null) {
                    Icon(
                        filtro.icono,
                        contentDescription = null,
                        tint = if (activo) Color.White else BusVioleta,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = if (activo) "${filtro.nombre} ${filtro.cantidad}" else "${filtro.nombre} (${filtro.cantidad})",
                    color = if (activo) Color.White else BusTinta,
                    fontSize = 13.sp,
                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun EncabezadoLista(titulo: String, disponibles: Int, orden: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(titulo, color = BusTinta, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (disponibles == 1) "1 disponible" else "$disponibles disponibles",
                color = BusVioleta,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(BusChip)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
        Row(
            modifier = Modifier.padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.SwapVert, contentDescription = null, tint = BusSubtitulo, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Orden: $orden", color = BusSubtitulo, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ---------- Banner final ----------

@Composable
private fun BannerCrear(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(BusVioleta, BusVioletaClaro)))
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("¿NO ENCUENTRAS PARTIDO?", color = Color(0xCCFFFFFF), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Crea tu propio reto deportivo",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 22.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Invita jugadores en tu zona en menos de 2 minutos.",
                color = Color(0xE6FFFFFF),
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
        Spacer(Modifier.width(14.dp))
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SuperficieApp)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.AddCircleOutline,
                contentDescription = "Crear evento",
                tint = BusVioleta,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

// ---------- Datos de ejemplo (solo para ver el diseño) ----------

val filtrosDeporteDeEjemplo = listOf(
    FiltroDeporteBuscar("todos", "Todos", null, 14),
    FiltroDeporteBuscar("futbol", "Fútbol", Icons.Outlined.SportsSoccer, 6),
    FiltroDeporteBuscar("padel", "Pádel", Icons.Outlined.SportsTennis, 4),
    FiltroDeporteBuscar("baloncesto", "Baloncesto", Icons.Outlined.SportsBasketball, 3),
)

// --- Vistas previas (solo Android Studio) ---

@Preview(showBackground = true, widthDp = 390, heightDp = 1500, name = "Cerca de mí")
@Composable
private fun BuscarEventoScreenPreview() {
    GestorDeEventosTheme {
        BuscarEventoScreen()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 700, name = "Sin resultados")
@Composable
private fun BuscarEventoSinResultadosPreview() {
    GestorDeEventosTheme {
        BuscarEventoScreen(eventos = emptyList())
    }
}