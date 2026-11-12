package com.example.gestordeeventos.ui.inicio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Colores de la pantalla (puedes moverlos a tu Theme / Color.kt) ---
private val IniPagina = Color(0xFFF6F4FF)
private val IniVioleta = Color(0xFF5B0FBF)
private val IniVioletaClaro = Color(0xFF7C3AED)
private val IniBoton = Color(0xFF6D28D9)
private val IniTinta = Color(0xFF1B1B3A)
private val IniSubtitulo = Color(0xFF4A5068)
private val IniGris = Color(0xFF7D8397)
private val IniChip = Color(0xFFE6DDFB)
private val IniCampo = Color(0xFFEEF0FF)
private val IniPie = Color(0xFFF6F5FF)
private val IniPista = Color(0xFFE0E3F5)
private val IniUrgente = Color(0xFFC2185B)

// --- Modelos de datos (más adelante se rellenarán desde Room) ---

data class EventoResumen(
    val id: String,
    val titulo: String,
    val deporte: String,
    val detalle: String,
    val inscritos: Int,
    val plazas: Int,
    val aviso: String,
    val avisoIcono: ImageVector,
    val urgente: Boolean = false,
)

data class ProximoEvento(
    val titulo: String,
    val lugar: String,
    val hora: String,
)

enum class SeccionNav { INICIO, BUSCAR, EVENTOS, AMIGOS }

val eventosDeEjemplo = listOf(
    EventoResumen(
        id = "1", titulo = "Fútbol 7 en el parque", deporte = "Fútbol",
        detalle = "Sáb 18:00 · a 1,2 km · 8/14 inscritos",
        inscritos = 8, plazas = 14,
        aviso = "6 plazas libres", avisoIcono = Icons.Outlined.Groups,
    ),
    EventoResumen(
        id = "2", titulo = "Pádel dobles", deporte = "Pádel",
        detalle = "Dom 11:00 · a 2,8 km · 3/4 inscritos",
        inscritos = 3, plazas = 4,
        aviso = "¡Última plaza disponible!", avisoIcono = Icons.Outlined.FlashOn, urgente = true,
    ),
    EventoResumen(
        id = "3", titulo = "Carrera 5 km", deporte = "Running",
        detalle = "Dom 09:30 · a 0,5 km · 12/30 inscritos",
        inscritos = 12, plazas = 30,
        aviso = "Ruta ribereña parque norte", avisoIcono = Icons.Outlined.Place,
    ),
    EventoResumen(
        id = "4", titulo = "Baloncesto 3×3", deporte = "Baloncesto",
        detalle = "Hoy 20:00 · a 0,8 km · 5/6 inscritos",
        inscritos = 5, plazas = 6,
        aviso = "Empieza en 2 horas", avisoIcono = Icons.Outlined.Schedule,
    ),
)

val proximoEjemplo = ProximoEvento(
    titulo = "Fútbol 7: Amistoso Los Pinos",
    lugar = "Polideportivo Central · Campo B",
    hora = "Hoy, 19:30",
)

private data class DeporteChip(val nombre: String, val icono: ImageVector)

private val deportes = listOf(
    DeporteChip("Todos", Icons.Outlined.GridView),
    DeporteChip("Fútbol", Icons.Outlined.SportsSoccer),
    DeporteChip("Pádel", Icons.Outlined.SportsTennis),
    DeporteChip("Baloncesto", Icons.Outlined.SportsBasketball),
)

private fun iconoDeporte(deporte: String): ImageVector = when (deporte) {
    "Fútbol" -> Icons.Outlined.SportsSoccer
    "Pádel" -> Icons.Outlined.SportsTennis
    "Baloncesto" -> Icons.Outlined.SportsBasketball
    else -> Icons.Outlined.EmojiEvents
}

@Composable
fun InicioScreen(
    nombreUsuario: String,
    modifier: Modifier = Modifier,
    eventos: List<EventoResumen> = eventosDeEjemplo,
    proximo: ProximoEvento? = proximoEjemplo,
    seccion: SeccionNav = SeccionNav.INICIO,
    onUnirseClick: (EventoResumen) -> Unit = {},
    onCrearClick: () -> Unit = {},
    onVerMapaClick: () -> Unit = {},
    onNotificacionesClick: () -> Unit = {},
    onMensajesClick: () -> Unit = {},
    onAjustesClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {},
    onNavegar: (SeccionNav) -> Unit = {},
) {
    var deporteSeleccionado by rememberSaveable { mutableStateOf("Todos") }
    val visibles = if (deporteSeleccionado == "Todos") {
        eventos
    } else {
        eventos.filter { it.deporte == deporteSeleccionado }
    }

    Surface(modifier = modifier.fillMaxSize(), color = IniPagina) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                // Barra superior
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
                                .background(Brush.linearGradient(listOf(IniVioleta, IniVioletaClaro))),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.size(10.dp))
                        Column {
                            Text("Eventia", color = IniTinta, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                            Text("INICIO", color = IniVioleta, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onNotificacionesClick),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Notifications, contentDescription = "Notificaciones", tint = IniTinta)
                        }
                        Spacer(Modifier.size(6.dp))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(IniChip)
                                .clickable(onClick = onPerfilClick),
                            contentAlignment = Alignment.Center,
                        ) {
                            // Cuando tengas foto de perfil, sustituye este icono por la imagen
                            Icon(Icons.Outlined.Person, contentDescription = "Perfil", tint = IniVioleta)
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Saludo + botones de mensajes y ajustes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hola, $nombreUsuario 👋",
                            color = IniTinta,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.NearMe, contentDescription = null, tint = IniVioleta, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Eventos públicos cerca de ti", color = IniSubtitulo, fontSize = 14.sp)
                        }
                    }
                    Spacer(Modifier.size(8.dp))
                    BotonCuadrado(Icons.Outlined.Mail, "Mensajes", onMensajesClick, conAviso = true)
                    Spacer(Modifier.size(8.dp))
                    BotonCuadrado(Icons.Outlined.Settings, "Ajustes", onAjustesClick)
                }

                Spacer(Modifier.height(18.dp))

                // Tarjeta de la próxima agenda
                if (proximo != null) {
                    TarjetaAgenda(proximo)
                    Spacer(Modifier.height(22.dp))
                }

                // Explorar deportes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Explorar deportes", color = IniTinta, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "VER MAPA",
                        color = IniVioleta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.clickable(onClick = onVerMapaClick),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    deportes.forEach { deporte ->
                        ChipDeporte(
                            deporte = deporte,
                            seleccionado = deporte.nombre == deporteSeleccionado,
                            onClick = { deporteSeleccionado = deporte.nombre },
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))

                // Eventos disponibles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Eventos disponibles", color = IniTinta, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = "${visibles.size} sugerencias",
                        color = IniVioleta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(12.dp))

                visibles.forEach { evento ->
                    TarjetaEvento(evento = evento, onUnirse = { onUnirseClick(evento) })
                    Spacer(Modifier.height(14.dp))
                }

                BannerCrear(onCrearClick = onCrearClick)

                Spacer(Modifier.height(24.dp))
            }

            BarraNavegacion(
                seccion = seccion,
                onNavegar = onNavegar,
                onCrearClick = onCrearClick,
            )
        }
    }
}

@Composable
private fun BotonCuadrado(
    icono: ImageVector,
    descripcion: String,
    onClick: () -> Unit,
    conAviso: Boolean = false,
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(IniChip)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icono, contentDescription = descripcion, tint = IniVioleta, modifier = Modifier.size(22.dp))
        if (conAviso) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(9.dp)
                    .background(Color(0xFFE5251F), CircleShape),
            )
        }
    }
}

@Composable
private fun TarjetaAgenda(proximo: ProximoEvento) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), spotColor = IniVioletaClaro)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(IniVioleta, IniVioletaClaro)))
            .padding(18.dp),
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(6.dp).background(Color(0xFFB8F5C8), CircleShape))
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "TU AGENDA DEPORTIVA",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                    )
                }
                Text(proximo.hora, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text = proximo.titulo,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 27.sp,
                modifier = Modifier.padding(end = 56.dp),
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text(proximo.lugar, color = Color.White.copy(alpha = 0.92f), fontSize = 14.sp)
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.SportsSoccer, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun ChipDeporte(deporte: DeporteChip, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (seleccionado) IniBoton else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = deporte.icono,
            contentDescription = null,
            tint = if (seleccionado) Color.White else IniTinta,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(6.dp))
        Text(
            text = deporte.nombre,
            color = if (seleccionado) Color.White else IniTinta,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TarjetaEvento(evento: EventoResumen, onUnirse: () -> Unit) {
    val colorAviso = if (evento.urgente) IniUrgente else IniBoton
    val fraccion = if (evento.plazas > 0) {
        (evento.inscritos.toFloat() / evento.plazas).coerceIn(0f, 1f)
    } else {
        0f
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(IniCampo),
                contentAlignment = Alignment.Center,
            ) {
                Icon(iconoDeporte(evento.deporte), contentDescription = null, tint = IniBoton, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.titulo,
                    color = IniTinta,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = evento.detalle,
                    color = IniSubtitulo,
                    fontSize = 12.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.size(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(IniBoton)
                    .clickable(onClick = onUnirse)
                    .padding(horizontal = 18.dp, vertical = 11.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Unirme", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(IniPie)
                .padding(horizontal = 14.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(evento.avisoIcono, contentDescription = null, tint = colorAviso, modifier = Modifier.size(15.dp))
                Spacer(Modifier.size(6.dp))
                Text(
                    text = evento.aviso,
                    color = colorAviso,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.size(10.dp))
            Box(
                modifier = Modifier
                    .width(92.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(IniPista),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraccion)
                        .fillMaxHeight()
                        .background(colorAviso),
                )
            }
        }
    }
}

@Composable
private fun BannerCrear(onCrearClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(IniCampo)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, tint = IniBoton)
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("¿No encuentras partido?", color = IniTinta, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text("Crea tu propio evento en 1 minuto", color = IniSubtitulo, fontSize = 12.5.sp)
        }
        Spacer(Modifier.size(10.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFDCE1FB))
                .clickable(onClick = onCrearClick)
                .padding(horizontal = 18.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Crear", color = IniVioleta, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BarraNavegacion(
    seccion: SeccionNav,
    onNavegar: (SeccionNav) -> Unit,
    onCrearClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(IniPista))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ItemNavegacion(Icons.Outlined.Home, "Inicio", seccion == SeccionNav.INICIO, Modifier.weight(1f)) {
                    onNavegar(SeccionNav.INICIO)
                }
                ItemNavegacion(Icons.Outlined.Search, "Buscar", seccion == SeccionNav.BUSCAR, Modifier.weight(1f)) {
                    onNavegar(SeccionNav.BUSCAR)
                }
                Spacer(Modifier.weight(1f)) // hueco para el botón central
                ItemNavegacion(Icons.Outlined.Event, "Eventos", seccion == SeccionNav.EVENTOS, Modifier.weight(1f)) {
                    onNavegar(SeccionNav.EVENTOS)
                }
                ItemNavegacion(Icons.Outlined.Group, "Amigos", seccion == SeccionNav.AMIGOS, Modifier.weight(1f)) {
                    onNavegar(SeccionNav.AMIGOS)
                }
            }
        }

        // Botón central elevado "+"
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-22).dp)
                .size(58.dp)
                .shadow(10.dp, CircleShape, spotColor = IniVioleta)
                .background(Brush.linearGradient(listOf(IniVioletaClaro, IniVioleta)), CircleShape)
                .clickable(onClick = onCrearClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Crear evento", tint = Color.White, modifier = Modifier.size(30.dp))
        }
    }
}

@Composable
private fun ItemNavegacion(
    icono: ImageVector,
    etiqueta: String,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val color = if (seleccionado) IniBoton else IniGris
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(2.dp))
        Text(etiqueta, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InicioScreenPreview() {
    InicioScreen(nombreUsuario = "ana_23")
}
