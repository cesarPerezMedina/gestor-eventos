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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import java.util.Locale

// --- Colores de la pantalla (mismos tonos que Amigos y Crear evento) ---
private val BusPagina = Color(0xFFF6F4FF)
private val BusVioleta = Color(0xFF5B0FBF)
private val BusVioletaClaro = Color(0xFF7C3AED)
private val BusBoton = Color(0xFF6D28D9)
private val BusTinta = Color(0xFF1B1B3A)
private val BusSubtitulo = Color(0xFF4A5068)
private val BusGris = Color(0xFF7D8397)
private val BusChip = Color(0xFFE6DDFB)
private val BusCampo = Color(0xFFEEF0FF)
private val BusLinea = Color(0xFFE3E1F2)
private val BusAlerta = Color(0xFFC2185B)
private val BusAlertaFondo = Color(0xFFFFE4EC)

// --- Modelos de la interfaz (solo lo que se pinta; más adelante se rellenarán desde Room) ---

enum class ModoBusqueda(val etiqueta: String, val icono: ImageVector) {
    CERCA_DE_MI("Cerca de mí", Icons.Outlined.Navigation),
    POR_NOMBRE("Por nombre", Icons.Outlined.SortByAlpha),
}

/** Radios de búsqueda en kilómetros. */
val RADIOS_KM = listOf(2, 5, 10, 25)

enum class EstiloEtiqueta { ALERTA, INFO, NEUTRA }

/** Chip de deporte de la fila de filtros. [icono] es null para "Todos". */
data class FiltroDeporteBuscar(
    val id: String,
    val nombre: String,
    val icono: ImageVector?,
    val cantidad: Int,
)

/** Un evento tal como se pinta en la tarjeta. */
data class EventoBuscarItem(
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
)

/**
 * Pantalla "Buscar eventos" (Issue 54). De momento es solo la interfaz: lo que el usuario toca
 * (modo, radio, deporte, texto) se guarda en variables locales y los botones avisan con callbacks.
 * Filtrar de verdad y cargar los eventos de la base de datos llegará en las siguientes issues.
 *
 * Todos los parámetros tienen valor por defecto, así que `BuscarEventoScreen()` sigue compilando.
 * La lista y los filtros por defecto son datos de ejemplo para ver el diseño.
 */
@Composable
fun BuscarEventoScreen(
    modifier: Modifier = Modifier,
    eventos: List<EventoBuscarItem> = eventosBuscarDeEjemplo,
    filtrosDeporte: List<FiltroDeporteBuscar> = filtrosDeporteDeEjemplo,
    onMapaClick: () -> Unit = {},
    onFiltrosClick: () -> Unit = {},
    onOrdenClick: () -> Unit = {},
    onModoChange: (ModoBusqueda) -> Unit = {},
    onRadioChange: (Int) -> Unit = {},
    avisoUbicacion: String? = null,
    onAvisoUbicacionClick: () -> Unit = {},
    onBuscar: (String) -> Unit = {},
    onDetalleClick: (String) -> Unit = {},
    onUnirmeClick: (String) -> Unit = {},
    onCrearEventoClick: () -> Unit = {},
) {
    var consulta by rememberSaveable { mutableStateOf("") }
    var modo by rememberSaveable { mutableStateOf(ModoBusqueda.CERCA_DE_MI) }
    var radioKm by rememberSaveable { mutableStateOf(RADIOS_KM.first()) }
    var deporteId by rememberSaveable { mutableStateOf(filtrosDeporte.firstOrNull()?.id.orEmpty()) }

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
                onFiltrosClick = onFiltrosClick,
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
                onCambio = { deporteId = it },
            )
        }

        item {
            EncabezadoLista(
                titulo = if (modo == ModoBusqueda.CERCA_DE_MI) "Eventos cercanos" else "Resultados",
                disponibles = eventos.size,
                onOrdenClick = onOrdenClick,
            )
        }

        items(eventos, key = { it.id }) { evento ->
            TarjetaEventoBuscar(
                evento = evento,
                onDetalleClick = { onDetalleClick(evento.id) },
                onUnirmeClick = { onUnirmeClick(evento.id) },
            )
        }

        if (eventos.isEmpty()) {
            item {
                Text(
                    text = "No hay eventos con estos filtros",
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
    onFiltrosClick: () -> Unit,
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
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = BusVioletaClaro,
                unfocusedBorderColor = BusLinea,
                cursorColor = BusVioleta,
            ),
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.dp, BusLinea, RoundedCornerShape(14.dp))
                .clickable(onClick = onFiltrosClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Tune,
                contentDescription = "Filtros",
                tint = BusVioleta,
            )
        }
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
                    .background(if (activa) Color.White else Color.Transparent)
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
                    .background(if (activo) BusBoton else Color.White)
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
private fun EncabezadoLista(titulo: String, disponibles: Int, onOrdenClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(titulo, color = BusTinta, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$disponibles disponibles",
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
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onOrdenClick)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.SwapVert, contentDescription = null, tint = BusSubtitulo, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Distancia", color = BusSubtitulo, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ---------- Tarjeta de evento ----------

@Composable
private fun TarjetaEventoBuscar(
    evento: EventoBuscarItem,
    onDetalleClick: () -> Unit,
    onUnirmeClick: () -> Unit,
) {
    val tieneLimite = evento.maximo > 0
    val casiLleno = tieneLimite && evento.maximo - evento.apuntados <= 1
    val progreso = if (evento.maximo > 0) (evento.apuntados.toFloat() / evento.maximo).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BusChip),
                contentAlignment = Alignment.Center,
            ) {
                Icon(evento.icono, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = evento.titulo,
                        color = BusTinta,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (evento.etiqueta != null) {
                        Spacer(Modifier.width(8.dp))
                        EtiquetaEvento(evento.etiqueta, evento.estiloEtiqueta)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(evento.cuando, color = BusVioleta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (evento.distanciaKm != null) {
                        Spacer(Modifier.width(10.dp))
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = BusGris, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "≈ ${String.format(Locale.US, "%.1f", evento.distanciaKm)} km",
                            color = BusSubtitulo,
                            fontSize = 12.sp,
                        )
                    }
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Place, contentDescription = null, tint = BusGris, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = evento.lugar,
                        color = BusSubtitulo,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (evento.organizador != null) {
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Org. por ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BusTinta)) { append(evento.organizador) }
                        },
                        color = BusGris,
                        fontSize = 11.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Barra de plazas ocupadas (solo si el evento tiene máximo de jugadores)
        if (tieneLimite) Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(BusLinea),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progreso)
                    .clip(RoundedCornerShape(50))
                    .background(if (casiLleno) BusAlerta else BusBoton),
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = BusVioleta)) {
                        append(if (tieneLimite) "${evento.apuntados}/${evento.maximo} " else "${evento.apuntados} ")
                    }
                    append(evento.cupoUnidad)
                    if (evento.cupoNota != null) {
                        withStyle(SpanStyle(color = if (casiLleno) BusAlerta else BusGris)) { append(" (${evento.cupoNota})") }
                    }
                },
                color = BusSubtitulo,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 3,
                modifier = Modifier.weight(1f),
            )
            if (evento.avatares.isNotEmpty()) Avatares(evento.avatares)
            Text(
                text = "Detalle",
                color = BusVioleta,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BusCampo)
                    .clickable(onClick = onDetalleClick)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BusBoton)
                    .clickable(onClick = onUnirmeClick)
                    .padding(start = 14.dp, end = 8.dp, top = 9.dp, bottom = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Unirme", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun EtiquetaEvento(texto: String, estilo: EstiloEtiqueta) {
    val (fondo, tinta) = when (estilo) {
        EstiloEtiqueta.ALERTA -> BusAlertaFondo to BusAlerta
        EstiloEtiqueta.INFO -> BusChip to BusVioleta
        EstiloEtiqueta.NEUTRA -> Color(0xFFEFEFF4) to BusSubtitulo
    }
    Text(
        text = texto,
        color = tinta,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** Hasta tres círculos con las iniciales de quienes ya se han apuntado, algo solapados. */
@Composable
private fun Avatares(iniciales: List<String>) {
    val colores = listOf(Color(0xFFB794F4), Color(0xFFF6AD55), Color(0xFF63B3ED))
    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
        iniciales.take(3).forEachIndexed { indice, texto ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(colores[indice % colores.size])
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(texto.take(1).uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
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
                .background(Color.White)
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

val eventosBuscarDeEjemplo = listOf(
    EventoBuscarItem(
        id = "1",
        titulo = "Baloncesto 3×3",
        icono = Icons.Outlined.SportsBasketball,
        cuando = "Hoy 20:00",
        distanciaKm = 0.8,
        lugar = "Cancha Municipal Norte",
        apuntados = 5,
        maximo = 8,
        etiqueta = "¡Última plaza!",
        estiloEtiqueta = EstiloEtiqueta.ALERTA,
        avatares = listOf("C", "I", "M"),
    ),
    EventoBuscarItem(
        id = "2",
        titulo = "Tenis individual",
        icono = Icons.Outlined.SportsTennis,
        cuando = "Mañana 17:00",
        distanciaKm = 1.9,
        lugar = "Club Deportivo Retiro",
        apuntados = 1,
        maximo = 2,
        cupoUnidad = "plazas",
        cupoNota = "Falta 1 rival",
        etiqueta = "Nivel 4.0",
        estiloEtiqueta = EstiloEtiqueta.INFO,
    ),
    EventoBuscarItem(
        id = "3",
        titulo = "Fútbol 7 en el parque",
        icono = Icons.Outlined.SportsSoccer,
        cuando = "Sáb 18:00",
        distanciaKm = 1.2,
        lugar = "Parque Central, Campo 2",
        apuntados = 8,
        maximo = 14,
        cupoUnidad = "inscritos",
        cupoNota = "Mín. 10",
        etiqueta = "Público",
        estiloEtiqueta = EstiloEtiqueta.NEUTRA,
        organizador = "carlos_m",
    ),
    EventoBuscarItem(
        id = "4",
        titulo = "Pádel dobles mixto",
        icono = Icons.Outlined.SportsTennis,
        cuando = "Dom 11:00",
        distanciaKm = 2.4,
        lugar = "Pádel Center Las Rozas",
        apuntados = 3,
        maximo = 4,
        cupoUnidad = "plazas",
        cupoNota = "Falta 1 jugador",
        etiqueta = "Intermedio",
        estiloEtiqueta = EstiloEtiqueta.ALERTA,
    ),
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