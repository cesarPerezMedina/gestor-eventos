package com.example.gestordeeventos.ui.crear

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.ui.componentes.SelectorDeporte
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme

// --- Colores de la pantalla (mismos tonos que la pantalla de Amigos) ---
internal val CrePagina = Color(0xFFF6F4FF)
internal val CreVioleta = Color(0xFF5B0FBF)
internal val CreVioletaClaro = Color(0xFF7C3AED)
internal val CreBoton = Color(0xFF6D28D9)
internal val CreTinta = Color(0xFF1B1B3A)
internal val CreSubtitulo = Color(0xFF4A5068)
internal val CreGris = Color(0xFF7D8397)
internal val CreChip = Color(0xFFE6DDFB)
internal val CreCampo = Color(0xFFEEF0FF)
internal val CreLinea = Color(0xFFE3E1F2)

/** Un amigo al que se puede invitar al evento (más adelante saldrá de la lista de amigos de Room). */
data class AmigoInvitable(
    val id: String,
    val nombre: String,
    val detalle: String,
)

val amigosInvitablesDeEjemplo = listOf(
    AmigoInvitable("1", "carlos_m", "Delantero • 4.8★"),
    AmigoInvitable("2", "ines_p", "Centrocampista"),
    AmigoInvitable("3", "marta_r", "Defensa"),
)

/**
 * Pantalla "Crear evento" (Issue 39). De momento es solo la interfaz: guarda lo que el usuario
 * escribe en variables locales y avisa con los callbacks. Guardar el evento llegará después.
 */
@Composable
fun CrearEventoScreen(
    modifier: Modifier = Modifier,
    amigos: List<AmigoInvitable> = amigosInvitablesDeEjemplo,
    onBackClick: () -> Unit = {},
    onCrearEventoClick: (nombre: String, descripcion: String) -> Unit = { _, _ -> },
    onFechaClick: () -> Unit = {},
    onHoraClick: () -> Unit = {},
    onUbicacionActualClick: () -> Unit = {},
    onAmpliarMapaClick: () -> Unit = {},
    onVerListaCompletaClick: () -> Unit = {},
) {
    var deporte by rememberSaveable { mutableStateOf<Deporte?>(Deporte.FUTBOL) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    // Pasa a true al pulsar "Crear": entonces se enseñan los errores de los campos
    var intentoCrear by rememberSaveable { mutableStateOf(false) }
    var esPublico by rememberSaveable { mutableStateOf(true) }
    var minJugadores by rememberSaveable { mutableStateOf(10) }
    var maxJugadores by rememberSaveable { mutableStateOf(14) }
    var ubicacion by rememberSaveable { mutableStateOf("") }
    var busquedaAmigo by rememberSaveable { mutableStateOf("") }
    val invitados = remember { mutableStateListOf<String>() }

    val amigosFiltrados = amigos.filter {
        busquedaAmigo.isBlank() || it.nombre.contains(busquedaAmigo.trim().removePrefix("@"), ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrePagina)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        BarraPaso(onBackClick)

        Spacer(Modifier.height(16.dp))

        Text("Crear evento", color = CreTinta, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Organiza un partido o actividad con tu comunidad deportiva",
            color = CreSubtitulo,
            fontSize = 14.sp,
            lineHeight = 19.sp,
        )

        Spacer(Modifier.height(20.dp))

        // ---------- Deporte ----------
        Etiqueta(
            texto = "SELECCIONA DEPORTE",
            derecha = if (deporte != null) "1 seleccionado" else "Elige uno",
        )
        Spacer(Modifier.height(8.dp))
        SelectorDeporte(seleccionado = deporte, onSeleccionar = { deporte = it })

        Spacer(Modifier.height(18.dp))

        // ---------- Nombre y descripción ----------
        CampoNombreEvento(
            valor = nombre,
            onCambio = { nombre = it },
            mostrarError = intentoCrear,
        )

        Spacer(Modifier.height(14.dp))

        CampoDescripcionEvento(
            valor = descripcion,
            onCambio = { descripcion = it },
        )

        Spacer(Modifier.height(18.dp))

        // ---------- Visibilidad ----------
        Etiqueta("Visibilidad del evento")
        Spacer(Modifier.height(6.dp))
        SelectorVisibilidad(esPublico = esPublico, onCambio = { esPublico = it })
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = CreVioletaClaro, modifier = Modifier.size(15.dp))
            Spacer(Modifier.size(6.dp))
            Text(
                text = if (esPublico) {
                    "Abierto a cualquier jugador cercano en la app para apuntarse."
                } else {
                    "Solo podrán apuntarse las personas que invites."
                },
                color = CreVioleta,
                fontSize = 12.sp,
            )
        }

        Spacer(Modifier.height(16.dp))

        // ---------- Número de jugadores ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x1A000000))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ContadorJugadores(
                etiqueta = "MÍN. JUGADORES",
                valor = minJugadores,
                onMenos = { if (minJugadores > 1) minJugadores-- },
                onMas = { if (minJugadores < maxJugadores) minJugadores++ },
                modifier = Modifier.weight(1f),
            )
            ContadorJugadores(
                etiqueta = "MÁX. JUGADORES",
                valor = maxJugadores,
                onMenos = { if (maxJugadores > minJugadores) maxJugadores-- },
                onMas = { if (maxJugadores < 99) maxJugadores++ },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(18.dp))

        // ---------- Ubicación ----------
        Etiqueta("Ubicación de la pista o campo")
        Spacer(Modifier.height(6.dp))
        CampoTexto(
            valor = ubicacion,
            onCambio = { ubicacion = it },
            placeholder = "Ej: Polideportivo Retiro - Pista Central",
            icono = Icons.Outlined.Place,
            unaLinea = true,
            accionFinal = {
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
        )
        Spacer(Modifier.height(10.dp))
        VistaPreviaMapa(onAmpliarMapaClick)

        Spacer(Modifier.height(18.dp))

        // ---------- Fecha y hora ----------
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Etiqueta("Fecha")
                Spacer(Modifier.height(6.dp))
                CampoSelector(Icons.Outlined.CalendarMonth, "Sáb, 24 Oct", onFechaClick)
            }
            Column(modifier = Modifier.weight(1f)) {
                Etiqueta("Hora")
                Spacer(Modifier.height(6.dp))
                CampoSelector(Icons.Outlined.Schedule, "18:30 - 20:00", onHoraClick)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ---------- Invitar amigos ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Invitar amigos", color = CreTinta, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Opcional",
                    color = CreVioleta,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(CreChip)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
            Text(
                text = "Ver lista completa",
                color = CreVioleta,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onVerListaCompletaClick),
            )
        }
        Spacer(Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x1A000000))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CampoTexto(
                valor = busquedaAmigo,
                onCambio = { busquedaAmigo = it },
                placeholder = "Buscar por apodo o nombre...",
                icono = Icons.Outlined.Search,
                unaLinea = true,
            )
            amigosFiltrados.forEach { amigo ->
                FilaAmigoInvitable(
                    amigo = amigo,
                    invitado = amigo.id in invitados,
                    onClick = {
                        if (amigo.id in invitados) invitados.remove(amigo.id) else invitados.add(amigo.id)
                    },
                )
            }
            if (amigosFiltrados.isEmpty()) {
                Text(
                    text = "Ningún amigo coincide con tu búsqueda",
                    color = CreGris,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
        }

        Spacer(Modifier.height(22.dp))

        // ---------- Botón crear ----------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(CreVioleta, CreVioletaClaro)))
                .clickable {
                    intentoCrear = true
                    // Solo se crea si el nombre es válido; la descripción es opcional
                    if (errorNombreEvento(nombre) == null) {
                        onCrearEventoClick(nombre.trim(), descripcion.trim())
                    }
                },
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(8.dp))
            Text("Crear evento deportivo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.NotificationsActive, contentDescription = null, tint = CreVioletaClaro, modifier = Modifier.size(14.dp))
            Spacer(Modifier.size(6.dp))
            Text(
                text = "Notificaremos al instante a los invitados y jugadores locales",
                color = CreSubtitulo,
                fontSize = 11.sp,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ───────────────────────── Componentes ─────────────────────────

@Composable
private fun BarraPaso(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CreChip)
                    .clickable(onClick = onBackClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver", tint = CreVioleta, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.size(10.dp))
            Text(
                text = "PASO 1 DE 2",
                color = CreVioleta,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.6.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(CreChip)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
        Text(
            text = "Cancelar",
            color = CreVioleta,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(onClick = onBackClick),
        )
    }
}

/** Título pequeño de cada bloque, con un texto opcional a la derecha. */
@Composable
internal fun Etiqueta(texto: String, derecha: String? = null, derechaGris: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(texto, color = CreTinta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        if (derecha != null) {
            Text(
                text = derecha,
                color = if (derechaGris) CreGris else CreVioleta,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    unaLinea: Boolean,
    icono: androidx.compose.ui.graphics.vector.ImageVector? = null,
    accionFinal: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth(),
        singleLine = unaLinea,
        minLines = if (unaLinea) 1 else 3,
        shape = RoundedCornerShape(14.dp),
        placeholder = { Text(placeholder, color = CreGris, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        leadingIcon = icono?.let { { Icon(it, contentDescription = null, tint = CreSubtitulo) } },
        trailingIcon = accionFinal,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = CreVioletaClaro,
            unfocusedBorderColor = CreLinea,
            cursorColor = CreVioleta,
        ),
    )
}

/** Campo que no se escribe, se pulsa (fecha y hora). */
@Composable
private fun CampoSelector(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, CreLinea, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = null, tint = CreVioleta, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(8.dp))
        Text(texto, color = CreTinta, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** Interruptor de dos opciones: Público / Privado. */
@Composable
private fun SelectorVisibilidad(esPublico: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, CreLinea, RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        OpcionVisibilidad("Público", Icons.Outlined.Public, esPublico, Modifier.weight(1f)) { onCambio(true) }
        OpcionVisibilidad("Privado", Icons.Outlined.Lock, !esPublico, Modifier.weight(1f)) { onCambio(false) }
    }
}

@Composable
private fun OpcionVisibilidad(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    seleccionada: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (seleccionada) CreChip else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = null, tint = if (seleccionada) CreVioleta else CreGris, modifier = Modifier.size(17.dp))
        Spacer(Modifier.size(6.dp))
        Text(
            text = texto,
            color = if (seleccionada) CreVioleta else CreSubtitulo,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Contador con botones − y + (mínimo y máximo de jugadores). */
@Composable
private fun ContadorJugadores(
    etiqueta: String,
    valor: Int,
    onMenos: () -> Unit,
    onMas: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(etiqueta, color = CreSubtitulo, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BotonRedondo(Icons.Outlined.Remove, "Menos", onMenos)
            Text(valor.toString(), color = CreTinta, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            BotonRedondo(Icons.Outlined.Add, "Más", onMas)
        }
    }
}

@Composable
private fun BotonRedondo(icono: androidx.compose.ui.graphics.vector.ImageVector, descripcion: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(CreCampo)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icono, contentDescription = descripcion, tint = CreVioleta, modifier = Modifier.size(18.dp))
    }
}

/** Recuadro del mapa. Es un dibujo de relleno: el mapa de verdad se enchufa más adelante. */
@Composable
private fun VistaPreviaMapa(onAmpliarClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(Color(0xFFE7EAFB), Color(0xFFDCD7F5))))
            .border(1.dp, CreLinea, RoundedCornerShape(16.dp)),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CreBoton),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.SportsSoccer, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Pista 2 reservada",
                color = CreTinta,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .padding(horizontal = 9.dp, vertical = 3.dp),
            )
        }
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .clip(RoundedCornerShape(50))
                .background(CreChip)
                .clickable(onClick = onAmpliarClick)
                .padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.OpenInFull, contentDescription = null, tint = CreVioleta, modifier = Modifier.size(12.dp))
            Spacer(Modifier.size(4.dp))
            Text("Ampliar", color = CreVioleta, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilaAmigoInvitable(amigo: AmigoInvitable, invitado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(CreChip),
            contentAlignment = Alignment.Center,
        ) {
            Text(amigo.nombre.take(1).uppercase(), color = CreVioleta, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(amigo.nombre, color = CreTinta, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(amigo.detalle, color = CreSubtitulo, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.size(8.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (invitado) CreBoton else CreCampo)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (invitado) Icons.Outlined.Check else Icons.Outlined.Add,
                contentDescription = null,
                tint = if (invitado) Color.White else CreVioleta,
                modifier = Modifier.size(14.dp),
            )
            Spacer(Modifier.size(4.dp))
            Text(
                text = if (invitado) "Invitado" else "Invitar",
                color = if (invitado) Color.White else CreVioleta,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1900)
@Composable
private fun CrearEventoScreenPreview() {
    GestorDeEventosTheme {
        CrearEventoScreen()
    }
}