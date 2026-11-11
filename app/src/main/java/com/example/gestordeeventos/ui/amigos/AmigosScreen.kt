package com.example.gestordeeventos.ui.amigos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material.icons.outlined.PersonSearch
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Colores de la pantalla (puedes moverlos a tu Theme / Color.kt) ---
private val AmiPagina = Color(0xFFF6F4FF)
private val AmiVioleta = Color(0xFF5B0FBF)
private val AmiVioletaClaro = Color(0xFF7C3AED)
private val AmiBoton = Color(0xFF6D28D9)
private val AmiBotonOscuro = Color(0xFF4A0E8F)
private val AmiTinta = Color(0xFF1B1B3A)
private val AmiSubtitulo = Color(0xFF4A5068)
private val AmiGris = Color(0xFF7D8397)
private val AmiChip = Color(0xFFE6DDFB)
private val AmiCampo = Color(0xFFEEF0FF)
private val AmiLinea = Color(0xFFE8E8F5)
private val AmiRojo = Color(0xFFD32F2F)
private val AmiRojoClaro = Color(0xFFFDECEE)
private val AmiVerde = Color(0xFF22C55E)

// --- Modelos de datos (más adelante se rellenarán desde Room) ---

/** Tipo de búsqueda que se elige con los chips de la tarjeta "Añadir por ID o Usuario". */
enum class FiltroBusqueda(val etiqueta: String, val ejemplo: String) {
    TODOS("Todos", "Ej: carlos_m o #USI"),
    NOMBRE("@Nombre", "Ej: carlos_m"),
    ID_TAG("#ID Tag", "Ej: #EVA-0281"),
    DEPORTE("Deporte", "Ej: pádel"),
}

data class InvitacionRecibida(
    val id: String,
    val titulo: String,
    val etiqueta: String,
    val de: String,
    val fecha: String,
    val lugar: String,
)

data class DeportistaSugerido(
    val id: String,
    val nombre: String,
    val usuario: String,
    val detalle: String,
    val insignia: String,
)

data class AmigoItem(
    val id: String,
    val usuario: String,
    val deportes: String,
    val idTag: String,
    val conectado: Boolean = false,
    val etiqueta: String? = null,
)

val invitacionesDeEjemplo = listOf(
    InvitacionRecibida("1", "Pádel dobles semifinal", "Privado", "@carlos_m", "Domingo 11:00", "Club Moraleja"),
    InvitacionRecibida("2", "Partido amistoso tenis", "Amateur", "@ines_p", "Lunes 19:00", "Pistas Canal"),
)

val sugeridosDeEjemplo = listOf(
    DeportistaSugerido("1", "Elena Vega", "@elena_run", "2 amigos en común", "94% afín"),
    DeportistaSugerido("2", "David Romero", "@david_basket", "Liga Chamberí", "Capitán"),
)

val amigosDeEjemplo = listOf(
    AmigoItem("1", "carlos_m", "Pádel / Fútbol", "#4802", conectado = true),
    AmigoItem("2", "ines_p", "Tenis Individual", "#5512"),
    AmigoItem("3", "marta_r", "Atletismo / Crossfit", "#8821", etiqueta = "Entreno"),
    AmigoItem("4", "luis_g", "Baloncesto 3×3", "#1849"),
)

@Composable
fun AmigosScreen(
    modifier: Modifier = Modifier,
    miId: String = "#EVA-0281",
    invitaciones: List<InvitacionRecibida> = invitacionesDeEjemplo,
    sugeridos: List<DeportistaSugerido> = sugeridosDeEjemplo,
    amigos: List<AmigoItem> = amigosDeEjemplo,
    onBuscarClick: (consulta: String, filtro: FiltroBusqueda) -> Unit = { _, _ -> },
    onEscanearQrClick: () -> Unit = {},
    onVerMiCodigoClick: () -> Unit = {},
    onAnadirAmigoClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {},
    onVerTodasInvitacionesClick: () -> Unit = {},
    onAceptarInvitacion: (InvitacionRecibida) -> Unit = {},
    onRechazarInvitacion: (InvitacionRecibida) -> Unit = {},
    onConectarClick: (DeportistaSugerido) -> Unit = {},
    onMensajeClick: (AmigoItem) -> Unit = {},
    onEliminarClick: (AmigoItem) -> Unit = {},
) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    var consulta by rememberSaveable { mutableStateOf("") }
    var filtroIndice by rememberSaveable { mutableIntStateOf(0) }
    val filtro = FiltroBusqueda.values()[filtroIndice]

    Surface(modifier = modifier.fillMaxSize(), color = AmiPagina) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            BarraSuperior(onNotificacionesClick, onPerfilClick)

            Spacer(Modifier.height(8.dp))

            // Título + botón de añadir amigo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Comunidad & Amigos", color = AmiTinta, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Conecta con deportistas, rivales y compañeros de equipo",
                        color = AmiSubtitulo,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.size(10.dp))
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AmiChip)
                        .clickable(onClick = onAnadirAmigoClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.PersonAdd, contentDescription = "Añadir amigo", tint = AmiVioleta)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Pestañas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Pestana("Mis Amigos", Icons.Outlined.Group, pestana == 0, Modifier.weight(1f), contador = amigos.size.toString()) { pestana = 0 }
                Pestana("Buscar / ID", Icons.Outlined.PersonSearch, pestana == 1, Modifier.weight(1f)) { pestana = 1 }
                Pestana("Solicitudes", Icons.Outlined.Mail, pestana == 2, Modifier.weight(1f), aviso = true) { pestana = 2 }
            }

            Spacer(Modifier.height(16.dp))

            // ---------- Issue 30: buscar usuarios por ID o nombre de usuario ----------
            TarjetaBuscarUsuario(
                miId = miId,
                consulta = consulta,
                onConsultaChange = { consulta = it },
                filtro = filtro,
                onFiltroChange = { filtroIndice = it.ordinal },
                onBuscar = { onBuscarClick(consulta.trim(), filtro) },
                onEscanearQrClick = onEscanearQrClick,
                onVerMiCodigoClick = onVerMiCodigoClick,
            )

            Spacer(Modifier.height(22.dp))

            // Invitaciones recibidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Invitaciones recibidas", color = AmiTinta, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.size(8.dp))
                    Box(
                        modifier = Modifier.size(20.dp).background(AmiRojo, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(invitaciones.size.toString(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    text = "Ver todas",
                    color = AmiVioleta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onVerTodasInvitacionesClick),
                )
            }
            Spacer(Modifier.height(12.dp))
            invitaciones.forEach { invitacion ->
                TarjetaInvitacion(
                    invitacion = invitacion,
                    onAceptar = { onAceptarInvitacion(invitacion) },
                    onRechazar = { onRechazarInvitacion(invitacion) },
                )
                Spacer(Modifier.height(12.dp))
            }

            Spacer(Modifier.height(10.dp))

            // Deportistas sugeridos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Deportistas sugeridos", color = AmiTinta, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Basado en tus deportes favoritos y nivel", color = AmiSubtitulo, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmiChip),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Tune, contentDescription = "Filtrar", tint = AmiVioleta, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                sugeridos.forEach { sugerido ->
                    TarjetaSugerido(sugerido) { onConectarClick(sugerido) }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Amigos agregados
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("Amigos agregados", color = AmiTinta, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.size(6.dp))
                    Text("(${amigos.size})", color = AmiGris, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ordenar por actividad", color = AmiSubtitulo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Outlined.KeyboardArrowDown, contentDescription = null, tint = AmiSubtitulo, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White),
            ) {
                amigos.forEachIndexed { indice, amigo ->
                    FilaAmigo(amigo, onMensaje = { onMensajeClick(amigo) }, onEliminar = { onEliminarClick(amigo) })
                    if (indice < amigos.lastIndex) {
                        Box(Modifier.fillMaxWidth().padding(horizontal = 14.dp).height(1.dp).background(AmiLinea))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BarraSuperior(onNotificacionesClick: () -> Unit, onPerfilClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(AmiVioleta, AmiVioletaClaro))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.size(10.dp))
            Column {
                Text("Eventia", color = AmiTinta, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text("INICIO", color = AmiVioleta, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onNotificacionesClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones", tint = AmiTinta)
            }
            Spacer(Modifier.size(6.dp))
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(AmiChip).clickable(onClick = onPerfilClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = AmiVioleta)
            }
        }
    }
}

@Composable
private fun Pestana(
    texto: String,
    icono: ImageVector,
    seleccionada: Boolean,
    modifier: Modifier = Modifier,
    contador: String? = null,
    aviso: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (seleccionada) AmiChip else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icono, contentDescription = null, tint = if (seleccionada) AmiVioleta else AmiGris, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(4.dp))
        Text(
            text = texto,
            color = if (seleccionada) AmiVioleta else AmiSubtitulo,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (contador != null) {
            Spacer(Modifier.size(4.dp))
            Box(
                modifier = Modifier.clip(RoundedCornerShape(50)).background(AmiBoton).padding(horizontal = 6.dp, vertical = 1.dp),
            ) {
                Text(contador, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (aviso) {
            Spacer(Modifier.size(4.dp))
            Box(Modifier.size(7.dp).background(AmiRojo, CircleShape))
        }
    }
}

/** Tarjeta "Añadir por ID o Usuario": el campo de búsqueda, los filtros y el botón Añadir. */
@Composable
private fun TarjetaBuscarUsuario(
    miId: String,
    consulta: String,
    onConsultaChange: (String) -> Unit,
    filtro: FiltroBusqueda,
    onFiltroChange: (FiltroBusqueda) -> Unit,
    onBuscar: () -> Unit,
    onEscanearQrClick: () -> Unit,
    onVerMiCodigoClick: () -> Unit,
) {
    val puedeBuscar = consulta.isNotBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = null, tint = AmiVioleta, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(8.dp))
                Text("Añadir por ID o Usuario", color = AmiTinta, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "Tu ID: $miId",
                color = AmiVioleta,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(AmiChip)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        Spacer(Modifier.height(12.dp))

        // Filtros de búsqueda
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FiltroBusqueda.values().forEach { opcion ->
                val activo = opcion == filtro
                Text(
                    text = opcion.etiqueta,
                    color = if (activo) Color.White else AmiVioleta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (activo) AmiBoton else AmiChip)
                        .clickable { onFiltroChange(opcion) }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Campo + botón
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = consulta,
                onValueChange = onConsultaChange,
                modifier = Modifier.weight(1f).height(54.dp),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                placeholder = { Text(filtro.ejemplo, color = AmiGris, fontSize = 13.sp, maxLines = 1) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = AmiGris) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { if (puedeBuscar) onBuscar() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AmiCampo,
                    unfocusedContainerColor = AmiCampo,
                    focusedBorderColor = AmiVioletaClaro,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = AmiVioleta,
                ),
            )
            Spacer(Modifier.size(8.dp))
            Row(
                modifier = Modifier
                    .height(54.dp)
                    .alpha(if (puedeBuscar) 1f else 0.55f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AmiBotonOscuro)
                    .clickable(enabled = puedeBuscar, onClick = onBuscar)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text("Añadir", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.clickable(onClick = onEscanearQrClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.QrCodeScanner, contentDescription = null, tint = AmiVioleta, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text("Escanear código QR en pista", color = AmiSubtitulo, fontSize = 12.sp)
            }
            Text(
                text = "Ver mi código",
                color = AmiVioleta,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onVerMiCodigoClick),
            )
        }
    }
}

@Composable
private fun TarjetaInvitacion(
    invitacion: InvitacionRecibida,
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(14.dp),
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(AmiCampo),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.SportsTennis, contentDescription = null, tint = AmiBoton, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = invitacion.titulo,
                        color = AmiTinta,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = invitacion.etiqueta,
                        color = AmiVioleta,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AmiChip)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "De ${invitacion.de} • ${invitacion.fecha} • ${invitacion.lugar}",
                    color = AmiSubtitulo,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmiCampo)
                    .clickable(onClick = onRechazar)
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Rechazar", color = AmiSubtitulo, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AmiBoton)
                    .clickable(onClick = onAceptar)
                    .padding(vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Aceptar", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TarjetaSugerido(sugerido: DeportistaSugerido, onConectar: () -> Unit) {
    Column(
        modifier = Modifier
            .width(176.dp)
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AmiChip),
                contentAlignment = Alignment.Center,
            ) {
                // Cuando haya foto de perfil, se sustituye la inicial por la imagen
                Text(
                    text = sugerido.nombre.take(1),
                    color = AmiVioleta,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Text(
                text = sugerido.insignia,
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(50))
                    .background(AmiBoton)
                    .padding(horizontal = 7.dp, vertical = 3.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(sugerido.nombre, color = AmiTinta, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(sugerido.usuario, color = AmiVioleta, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(2.dp))
        Text(sugerido.detalle, color = AmiSubtitulo, fontSize = 11.5.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(AmiCampo)
                .clickable(onClick = onConectar)
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = AmiVioleta, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(4.dp))
            Text("Conectar", color = AmiVioleta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun FilaAmigo(amigo: AmigoItem, onMensaje: () -> Unit, onEliminar: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(44.dp)) {
            Box(
                modifier = Modifier.fillMaxSize().clip(CircleShape).background(AmiChip),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = amigo.usuario.take(1).uppercase(),
                    color = AmiVioleta,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            if (amigo.conectado) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(13.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .background(AmiVerde, CircleShape),
                )
            }
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = amigo.usuario,
                    color = AmiTinta,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (amigo.etiqueta != null) {
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = amigo.etiqueta,
                        color = AmiVioleta,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(AmiChip)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${amigo.deportes} • ${amigo.idTag}",
                color = AmiSubtitulo,
                fontSize = 11.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(AmiCampo)
                .clickable(onClick = onMensaje),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Enviar mensaje", tint = AmiVioleta, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.size(8.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AmiRojoClaro)
                .clickable(onClick = onEliminar)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.PersonRemove, contentDescription = null, tint = AmiRojo, modifier = Modifier.size(16.dp))
            Spacer(Modifier.size(4.dp))
            Text("Eliminar", color = AmiRojo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun AmigosScreenPreview() {
    AmigosScreen()
}