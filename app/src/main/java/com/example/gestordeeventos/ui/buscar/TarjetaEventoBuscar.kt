package com.example.gestordeeventos.ui.buscar

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import java.util.Locale
import kotlin.math.roundToInt
import com.example.gestordeeventos.ui.theme.PaletaApp
import com.example.gestordeeventos.ui.theme.SuperficieApp

// --- Colores (mismos tonos que Amigos y Crear evento) ---
internal val BusPagina get() = PaletaApp.elegir(Color(0xFFF6F4FF), Color(0xFF15131F))
internal val BusVioleta get() = PaletaApp.elegir(Color(0xFF5B0FBF), Color(0xFF8A5CEB))
internal val BusVioletaClaro get() = PaletaApp.elegir(Color(0xFF7C3AED), Color(0xFFA78BFA))
internal val BusBoton get() = PaletaApp.elegir(Color(0xFF6D28D9), Color(0xFF7C3AED))
internal val BusTinta get() = PaletaApp.elegir(Color(0xFF1B1B3A), Color(0xFFECEAFB))
internal val BusSubtitulo get() = PaletaApp.elegir(Color(0xFF4A5068), Color(0xFFB9BDD3))
internal val BusGris get() = PaletaApp.elegir(Color(0xFF7D8397), Color(0xFF9AA0B5))
internal val BusChip get() = PaletaApp.elegir(Color(0xFFE6DDFB), Color(0xFF332A52))
internal val BusCampo get() = PaletaApp.elegir(Color(0xFFEEF0FF), Color(0xFF25223A))
internal val BusLinea get() = PaletaApp.elegir(Color(0xFFE3E1F2), Color(0xFF363350))
internal val BusAlerta get() = PaletaApp.elegir(Color(0xFFC2185B), Color(0xFFF06292))
internal val BusAlertaFondo get() = PaletaApp.elegir(Color(0xFFFFE4EC), Color(0xFF3D2230))
private val BusAlertaOscuro = Color(0xFF9F1239)
private val BusNeutroFondo get() = PaletaApp.elegir(Color(0xFFEFEFF4), Color(0xFF2A2838))

// --- Modelos de la tarjeta (solo lo que se pinta; se rellenan desde Room en el ViewModel) ---

/** Los cuatro aspectos de la tarjeta de evento. */
enum class VarianteTarjeta {
    /** Evento público normal. */
    ESTANDAR,

    /** Quedan una o dos plazas o empieza pronto: se destaca en magenta. */
    URGENTE,

    /** Torneo o evento con foto y cuadro. */
    TORNEO,

    /** Invitación a un evento privado ("Solo amigos"). */
    PRIVADO,
}

enum class EstiloEtiqueta { NEUTRA, INFO, FUERTE, OSCURA, ALERTA }

/** Chip pequeño de la parte de arriba: "PÚBLICO", "INTERMEDIO", "Torneo Express"... */
data class EtiquetaTarjeta(
    val texto: String,
    val estilo: EstiloEtiqueta = EstiloEtiqueta.NEUTRA,
    val icono: ImageVector? = null,
)

/**
 * Un evento tal como lo pinta [TarjetaEventoBuscar]. Casi todo es opcional: la tarjeta solo enseña
 * los bloques de los que tiene datos, así que puede rellenarse poco a poco desde la base de datos.
 */
data class EventoBuscarItem(
    val id: String,
    val titulo: String,
    /** Nombre del enum `Deporte` ("FUTBOL", "PADEL"...). Sirve para filtrar por deporte; null si no se conoce. */
    val deporteId: String? = null,
    val icono: ImageVector,
    val cuando: String,
    val lugar: String,
    val apuntados: Int,
    /** 0 = sin límite de jugadores. */
    val maximo: Int,
    val variante: VarianteTarjeta = VarianteTarjeta.ESTANDAR,
    val etiquetas: List<EtiquetaTarjeta> = emptyList(),
    val duracion: String? = null, // "90 min"
    val distanciaKm: Double? = null, // null mientras no se conozca la ubicación del usuario
    val organizador: String? = null, // "@carlos_m"
    val organizadorDetalle: String? = null, // "★ 4.9 (28 partidos creados)"
    val unidadCupo: String = "plazas ocupadas", // "8 de 14 plazas ocupadas", "5 de 6 confirmados"...
    val minimo: Int? = null,
    val avatares: List<String> = emptyList(), // iniciales de quienes ya están apuntados
    val notaFaltan: String? = null, // "Faltan 6 jugadores"
    val avisoUrgente: String? = null, // "¡ÚLTIMA PLAZA DISPONIBLE! (5/6)"
    val empiezaEn: String? = null, // "Empieza en 2h"
    @DrawableRes val imagenRes: Int? = null,
    val imagenTitulo: String? = null, // "Pádel Club Premium • Pista 4"
    val imagenBadge: String? = null, // "Césped Azul"
    val invitacionDe: String? = null, // "Invitación de @ines_p"
    val invitacionDetalle: String? = null, // "19 partidos jugados juntos • Nivel 3.5"
    val guardado: Boolean = false,
    /** true si el usuario ya está apuntado: el botón principal pasa a "Ya apuntado" y no hace nada. */
    val yoApuntado: Boolean = false,
    val textoPrincipal: String? = null, // cambia el texto del botón principal
    val textoSecundario: String? = null, // cambia el texto del botón secundario
)

/**
 * Tarjeta de un evento en los resultados de Buscar (Issue 58). Tiene cuatro aspectos ([VarianteTarjeta]).
 *
 * Es un componente sin estado: solo pinta [evento] y avisa con los callbacks.
 * - [onClick]: tocar la tarjeta (abre el detalle).
 * - [onPrincipalClick]: botón de la derecha ("Unirme", "Reservar", "Solicitar plaza", "Aceptar").
 * - [onSecundarioClick]: botón de la izquierda ("Ver detalle", "Ver cuadro", "Rechazar").
 * - [onGuardarClick]: marcador de la esquina del aspecto estándar.
 * Si el evento está completo, el botón principal se apaga y pone "Completo".
 */
@Composable
fun TarjetaEventoBuscar(
    evento: EventoBuscarItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onPrincipalClick: () -> Unit = {},
    onSecundarioClick: () -> Unit = {},
    onGuardarClick: () -> Unit = {},
) {
    val completo = evento.variante != VarianteTarjeta.PRIVADO &&
        evento.maximo > 0 && evento.apuntados >= evento.maximo

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(24.dp))
            .background(SuperficieApp)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (evento.variante) {
            VarianteTarjeta.ESTANDAR -> CuerpoEstandar(evento, completo, onPrincipalClick, onSecundarioClick, onGuardarClick)
            VarianteTarjeta.URGENTE -> CuerpoUrgente(evento, completo, onPrincipalClick)
            VarianteTarjeta.TORNEO -> CuerpoTorneo(evento, completo, onPrincipalClick, onSecundarioClick)
            VarianteTarjeta.PRIVADO -> CuerpoPrivado(evento, onPrincipalClick, onSecundarioClick)
        }
    }
}

// ---------- Aspecto estándar ----------

@Composable
private fun CuerpoEstandar(
    evento: EventoBuscarItem,
    completo: Boolean,
    onPrincipalClick: () -> Unit,
    onSecundarioClick: () -> Unit,
    onGuardarClick: () -> Unit,
) {
    CabeceraTitulo(
        evento = evento,
        fondoIcono = BusChip,
        derecha = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SuperficieApp)
                    .border(1.dp, BusLinea, RoundedCornerShape(12.dp))
                    .clickable(onClick = onGuardarClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (evento.guardado) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = if (evento.guardado) "Quitar de guardados" else "Guardar evento",
                    tint = BusVioleta,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
    )

    // Cuándo y dónde
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BusCampo)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        FilaInfo(Icons.Outlined.CalendarToday, evento.cuando, negrita = true, insignia = evento.duracion)
        FilaInfo(Icons.Outlined.Place, evento.lugar, insignia = evento.distanciaKm?.let { textoDistancia(it) })
    }

    // Organizador
    if (evento.organizador != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(evento.organizador.removePrefix("@"), tamano = 36)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = BusTinta)) { append(evento.organizador) }
                        append(" • Org.")
                    },
                    color = BusSubtitulo,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (evento.organizadorDetalle != null) {
                    Text(evento.organizadorDetalle, color = BusGris, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }

    // Plazas
    BloquePlazas(evento = evento, color = BusBoton, mostrarMinimo = true)

    if (evento.avatares.isNotEmpty() || evento.notaFaltan != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (evento.avatares.isNotEmpty()) {
                Avatares(evento.avatares, extra = (evento.apuntados - evento.avatares.size).coerceAtLeast(0))
            }
            Spacer(Modifier.weight(1f))
            if (evento.notaFaltan != null) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(BusAlerta))
                Spacer(Modifier.width(6.dp))
                Text(evento.notaFaltan, color = BusAlerta, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    Acciones(
        textoSecundario = evento.textoSecundario ?: "Ver detalle",
        iconoSecundario = null,
        textoPrincipal = evento.textoPrincipal ?: "Unirme al evento",
        iconoPrincipal = Icons.Outlined.PersonAdd,
        completo = completo,
        apuntado = evento.yoApuntado,
        fondoPrincipal = BusBoton,
        onSecundarioClick = onSecundarioClick,
        onPrincipalClick = onPrincipalClick,
    )
}

// ---------- Aspecto urgente ----------

@Composable
private fun CuerpoUrgente(
    evento: EventoBuscarItem,
    completo: Boolean,
    onPrincipalClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (evento.avisoUrgente != null) {
            Row(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(RoundedCornerShape(50))
                    .background(BusAlertaFondo)
                    .border(1.dp, PaletaApp.elegir(Color(0xFFF8BBD0), Color(0xFF7A2E4A)), RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Bolt, contentDescription = null, tint = BusAlerta, modifier = Modifier.size(13.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    evento.avisoUrgente,
                    color = BusAlerta,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.weight(1f))
        if (evento.empiezaEn != null) {
            Text(evento.empiezaEn, color = BusVioleta, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }

    CabeceraTitulo(evento = evento, fondoIcono = BusAlertaFondo, tintaIcono = BusAlerta)

    // Plazas en magenta
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BusCampo)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BloquePlazas(evento = evento, color = BusAlerta, mostrarMinimo = false, mostrarPorcentaje = true)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Place, contentDescription = null, tint = BusGris, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = evento.lugar + (evento.distanciaKm?.let { " (${textoDistancia(it)})" } ?: ""),
                color = BusSubtitulo,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }

    // Botón grande
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                when {
                    evento.yoApuntado -> Brush.horizontalGradient(listOf(BusChip, BusChip))
                    completo -> Brush.horizontalGradient(listOf(BusLinea, BusLinea))
                    else -> Brush.horizontalGradient(listOf(BusAlertaOscuro, BusAlerta))
                },
            )
            .clickable(enabled = !completo && !evento.yoApuntado, onClick = onPrincipalClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (evento.yoApuntado) {
            Icon(Icons.Outlined.Check, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        } else if (!completo) {
            Icon(Icons.Outlined.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = when {
                evento.yoApuntado -> "Ya estás apuntado"
                completo -> "Completo"
                else -> evento.textoPrincipal ?: "¡Reservar mi plaza ahora!"
            },
            color = when {
                evento.yoApuntado -> BusVioleta
                completo -> BusGris
                else -> Color.White
            },
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

// ---------- Aspecto torneo ----------

@Composable
private fun CuerpoTorneo(
    evento: EventoBuscarItem,
    completo: Boolean,
    onPrincipalClick: () -> Unit,
    onSecundarioClick: () -> Unit,
) {
    CabeceraTitulo(evento = evento, fondoIcono = BusChip)

    // Foto con el sitio encima (si no hay foto, un degradado azul)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1E40AF)))),
    ) {
        if (evento.imagenRes != null) {
            Image(
                painter = painterResource(evento.imagenRes),
                contentDescription = evento.imagenTitulo,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x99000000)))),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = evento.imagenTitulo ?: evento.lugar,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (evento.imagenBadge != null) {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = evento.imagenBadge,
                    color = BusTinta,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xE6FFFFFF))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
        }
    }

    // Inscritos
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BusCampo)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Groups, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(textoCupo(evento), color = BusTinta, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        if (evento.notaFaltan != null) {
            Text(
                text = evento.notaFaltan,
                color = BusAlerta,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(BusAlertaFondo)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }

    Acciones(
        textoSecundario = evento.textoSecundario ?: "Ver cuadro",
        iconoSecundario = null,
        textoPrincipal = evento.textoPrincipal ?: "Solicitar plaza",
        iconoPrincipal = Icons.Outlined.PersonAdd,
        completo = completo,
        apuntado = evento.yoApuntado,
        fondoPrincipal = BusVioleta,
        onSecundarioClick = onSecundarioClick,
        onPrincipalClick = onPrincipalClick,
    )
}

// ---------- Aspecto privado (invitación) ----------

@Composable
private fun CuerpoPrivado(
    evento: EventoBuscarItem,
    onPrincipalClick: () -> Unit,
    onSecundarioClick: () -> Unit,
) {
    CabeceraTitulo(
        evento = evento,
        fondoIcono = BusChip,
        derecha = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SuperficieApp)
                    .border(1.dp, BusLinea, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.MailOutline, contentDescription = "Invitación", tint = BusVioleta, modifier = Modifier.size(18.dp))
            }
        },
    )

    if (evento.invitacionDe != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BusCampo)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(evento.invitacionDe.substringAfter("de ").removePrefix("@"), tamano = 36)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(evento.invitacionDe, color = BusTinta, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (evento.invitacionDetalle != null) {
                    Text(evento.invitacionDetalle, color = BusGris, fontSize = 11.sp, lineHeight = 14.sp, maxLines = 2)
                }
            }
            Spacer(Modifier.width(8.dp))
            Insignia(evento.cuando)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(BusCampo)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilaInfo(Icons.Outlined.CalendarToday, evento.cuando, negrita = true)
            FilaInfo(Icons.Outlined.Place, evento.lugar)
        }
    }

    Acciones(
        textoSecundario = evento.textoSecundario ?: "Rechazar",
        iconoSecundario = Icons.Outlined.Close,
        textoPrincipal = evento.textoPrincipal ?: "Aceptar",
        iconoPrincipal = Icons.Outlined.Check,
        completo = false,
        apuntado = false,
        fondoPrincipal = BusVioleta,
        onSecundarioClick = onSecundarioClick,
        onPrincipalClick = onPrincipalClick,
    )
}

// ---------- Piezas compartidas ----------

/** Icono del deporte + etiquetas + título, y a la derecha lo que pida cada aspecto ([derecha]). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CabeceraTitulo(
    evento: EventoBuscarItem,
    fondoIcono: Color,
    tintaIcono: Color = BusVioleta,
    derecha: (@Composable () -> Unit)? = null,
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(fondoIcono),
            contentAlignment = Alignment.Center,
        ) {
            Icon(evento.icono, contentDescription = null, tint = tintaIcono, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            if (evento.etiquetas.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    evento.etiquetas.forEach { Etiqueta(it) }
                }
            }
            Text(
                text = evento.titulo,
                color = BusTinta,
                fontSize = 17.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (derecha != null) {
            Spacer(Modifier.width(10.dp))
            derecha()
        }
    }
}

@Composable
private fun Etiqueta(etiqueta: EtiquetaTarjeta) {
    val (fondo, tinta) = when (etiqueta.estilo) {
        EstiloEtiqueta.NEUTRA -> BusNeutroFondo to BusSubtitulo
        EstiloEtiqueta.INFO -> BusChip to BusVioleta
        EstiloEtiqueta.FUERTE -> BusVioleta to Color.White
        EstiloEtiqueta.OSCURA -> BusTinta to Color.White
        EstiloEtiqueta.ALERTA -> BusAlertaFondo to BusAlerta
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(fondo)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (etiqueta.icono != null) {
            Icon(etiqueta.icono, contentDescription = null, tint = tinta, modifier = Modifier.size(10.dp))
            Spacer(Modifier.width(3.dp))
        }
        Text(etiqueta.texto, color = tinta, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

/** Una línea con icono, texto y, a la derecha, una insignia opcional ("90 min", "a 1.2 km"). */
@Composable
private fun FilaInfo(icono: ImageVector, texto: String, negrita: Boolean = false, insignia: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icono, contentDescription = null, tint = if (negrita) BusVioleta else BusGris, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            text = texto,
            color = BusTinta,
            fontSize = 12.sp,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (insignia != null) {
            Spacer(Modifier.width(8.dp))
            Insignia(insignia)
        }
    }
}

@Composable
private fun Insignia(texto: String) {
    Text(
        text = texto,
        color = BusSubtitulo,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SuperficieApp)
            .border(1.dp, BusLinea, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

/** "8 de 14 plazas ocupadas" + (mínimo / % lleno) y la barra de progreso. */
@Composable
private fun BloquePlazas(
    evento: EventoBuscarItem,
    color: Color,
    mostrarMinimo: Boolean,
    mostrarPorcentaje: Boolean = false,
) {
    val tieneLimite = evento.maximo > 0
    val progreso = if (tieneLimite) (evento.apuntados.toFloat() / evento.maximo).coerceIn(0f, 1f) else 0f

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = textoCupo(evento),
                color = BusTinta,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            if (mostrarPorcentaje && tieneLimite) {
                Text("${(progreso * 100).roundToInt()}% lleno", color = color, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            } else if (mostrarMinimo && evento.minimo != null) {
                Insignia("Mínimo ${evento.minimo}")
            }
        }
        if (tieneLimite) {
            Box(
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
                        .background(color),
                )
            }
        }
    }
}

/** Botón secundario (claro) y principal (de color) de abajo. */
@Composable
private fun Acciones(
    textoSecundario: String,
    iconoSecundario: ImageVector?,
    textoPrincipal: String,
    iconoPrincipal: ImageVector,
    completo: Boolean,
    apuntado: Boolean,
    fondoPrincipal: Color,
    onSecundarioClick: () -> Unit,
    onPrincipalClick: () -> Unit,
) {
    val principalActivo = !completo && !apuntado
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(BusCampo)
                .clickable(onClick = onSecundarioClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (iconoSecundario != null) {
                Icon(iconoSecundario, contentDescription = null, tint = BusSubtitulo, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(textoSecundario, color = BusSubtitulo, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Row(
            modifier = Modifier
                .weight(1.25f)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    when {
                        apuntado -> BusChip
                        completo -> BusLinea
                        else -> fondoPrincipal
                    },
                )
                .clickable(enabled = principalActivo, onClick = onPrincipalClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when {
                    apuntado -> "Ya apuntado"
                    completo -> "Completo"
                    else -> textoPrincipal
                },
                color = when {
                    apuntado -> BusVioleta
                    completo -> BusGris
                    else -> Color.White
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
            if (apuntado) {
                Spacer(Modifier.width(6.dp))
                Icon(Icons.Outlined.Check, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(16.dp))
            } else if (!completo) {
                Spacer(Modifier.width(6.dp))
                Icon(iconoPrincipal, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** "8 de 14 plazas ocupadas"; si no hay máximo, "7 apuntados". */
private fun textoCupo(evento: EventoBuscarItem): String =
    if (evento.maximo > 0) "${evento.apuntados} de ${evento.maximo} ${evento.unidadCupo}" else "${evento.apuntados} apuntados"

/** "a 800 m" por debajo de 1 km, "a 1.2 km" a partir de ahí. */
private fun textoDistancia(km: Double): String =
    if (km < 1.0) "a ${(km * 1000).roundToInt()} m" else "a ${String.format(Locale.US, "%.1f", km)} km"

/** Círculo con la inicial de un usuario (más adelante podrá ser su foto). */
@Composable
private fun Avatar(nombre: String, tamano: Int) {
    Box(
        modifier = Modifier
            .size(tamano.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(BusVioletaClaro, Color(0xFFB794F4)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = nombre.take(1).uppercase(),
            color = Color.White,
            fontSize = (tamano * 0.4f).sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

/** Hasta cuatro círculos con las iniciales de quienes ya se han apuntado, solapados, y "+N" si hay más. */
@Composable
private fun Avatares(iniciales: List<String>, extra: Int) {
    val colores = listOf(Color(0xFFB794F4), Color(0xFFF6AD55), Color(0xFF63B3ED), Color(0xFF68D391))
    Row(horizontalArrangement = Arrangement.spacedBy((-8).dp), verticalAlignment = Alignment.CenterVertically) {
        iniciales.take(4).forEachIndexed { indice, texto ->
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(colores[indice % colores.size])
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(texto.take(1).uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (extra > 0) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(BusChip)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("+$extra", color = BusVioleta, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------- Datos de ejemplo (solo para ver el diseño) ----------

val eventosBuscarDeEjemplo = listOf(
    EventoBuscarItem(
        id = "1",
        titulo = "Fútbol 7 en el Parque",
        deporteId = "FUTBOL",
        icono = Icons.Outlined.SportsSoccer,
        cuando = "Sáb 14 Nov • 18:00 - 19:30",
        duracion = "90 min",
        lugar = "Parque Municipal, Campo 2",
        distanciaKm = 1.2,
        apuntados = 8,
        maximo = 14,
        minimo = 10,
        etiquetas = listOf(
            EtiquetaTarjeta("PÚBLICO", EstiloEtiqueta.NEUTRA),
            EtiquetaTarjeta("INTERMEDIO", EstiloEtiqueta.INFO),
        ),
        organizador = "@carlos_m",
        organizadorDetalle = "★ 4.9 (28 partidos creados)",
        avatares = listOf("A", "B", "C", "D"),
        notaFaltan = "Faltan 6 jugadores",
    ),
    EventoBuscarItem(
        id = "2",
        titulo = "Basket 3x3 Callejero Nocturno",
        deporteId = "BALONCESTO",
        icono = Icons.Outlined.SportsBasketball,
        cuando = "Hoy 20:00",
        lugar = "Polideportivo Central",
        distanciaKm = 0.8,
        apuntados = 5,
        maximo = 6,
        unidadCupo = "confirmados",
        variante = VarianteTarjeta.URGENTE,
        etiquetas = listOf(
            EtiquetaTarjeta("Cancha Abierta", EstiloEtiqueta.NEUTRA),
            EtiquetaTarjeta("Casual / Mixto", EstiloEtiqueta.INFO),
        ),
        avisoUrgente = "¡ÚLTIMA PLAZA DISPONIBLE! (5/6)",
        empiezaEn = "Empieza en 2h",
    ),
    EventoBuscarItem(
        id = "3",
        titulo = "Pádel Oro Dobles Mixto",
        deporteId = "PADEL",
        icono = Icons.Outlined.SportsTennis,
        cuando = "Dom 11:00",
        lugar = "Pádel Club Premium",
        apuntados = 3,
        maximo = 4,
        unidadCupo = "parejas inscritas",
        variante = VarianteTarjeta.TORNEO,
        etiquetas = listOf(
            EtiquetaTarjeta("NIVEL 4.0 - AVANZADO", EstiloEtiqueta.FUERTE),
            EtiquetaTarjeta("Torneo Express", EstiloEtiqueta.INFO),
        ),
        imagenTitulo = "Pádel Club Premium • Pista 4 (Panorámica)",
        imagenBadge = "Césped Azul",
        notaFaltan = "Falta 1 pareja mixta",
    ),
    EventoBuscarItem(
        id = "4",
        titulo = "Duelo Tenis Individual 1 vs 1",
        deporteId = "TENIS",
        icono = Icons.Outlined.SportsTennis,
        cuando = "Mañana 10:00",
        lugar = "Club Deportivo Retiro",
        apuntados = 1,
        maximo = 2,
        variante = VarianteTarjeta.PRIVADO,
        etiquetas = listOf(EtiquetaTarjeta("Privado / Solo amigos", EstiloEtiqueta.OSCURA, Icons.Outlined.Lock)),
        invitacionDe = "Invitación de @ines_p",
        invitacionDetalle = "19 partidos jugados juntos • Nivel 3.5",
    ),
)

// --- Vistas previas (solo Android Studio) ---

@Preview(showBackground = true, widthDp = 390, heightDp = 1900, name = "Las cuatro variantes")
@Composable
private fun TarjetaEventoBuscarPreview() {
    GestorDeEventosTheme {
        Column(
            modifier = Modifier
                .background(BusPagina)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            eventosBuscarDeEjemplo.forEach { TarjetaEventoBuscar(evento = it) }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, name = "Completo, título largo y sin límite")
@Composable
private fun TarjetaEventoBuscarCasosLimitePreview() {
    GestorDeEventosTheme {
        Column(
            modifier = Modifier
                .background(BusPagina)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TarjetaEventoBuscar(
                evento = eventosBuscarDeEjemplo[0].copy(
                    titulo = "Torneo de fútbol sala benéfico de los barrios del sur de la ciudad",
                    lugar = "Polideportivo Municipal Vicente del Bosque, pista exterior 3",
                    apuntados = 14,
                    notaFaltan = null,
                ),
            )
            TarjetaEventoBuscar(
                evento = EventoBuscarItem(
                    id = "z",
                    titulo = "Quedada de running",
                    icono = Icons.Outlined.SportsSoccer,
                    cuando = "Mañana 09:00",
                    lugar = "Parque del Retiro",
                    apuntados = 7,
                    maximo = 0,
                    etiquetas = listOf(EtiquetaTarjeta("PÚBLICO")),
                ),
            )
        }
    }
}