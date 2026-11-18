package com.example.gestordeeventos.ui.evento

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.HighlightOff
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsScore
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.R
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EstadoEvento
import com.example.gestordeeventos.ui.buscar.BusAlerta
import com.example.gestordeeventos.ui.buscar.BusAlertaFondo
import com.example.gestordeeventos.ui.buscar.BusCampo
import com.example.gestordeeventos.ui.buscar.BusChip
import com.example.gestordeeventos.ui.buscar.BusGris
import com.example.gestordeeventos.ui.buscar.BusLinea
import com.example.gestordeeventos.ui.buscar.BusSubtitulo
import com.example.gestordeeventos.ui.buscar.BusTinta
import com.example.gestordeeventos.ui.buscar.BusVioleta
import com.example.gestordeeventos.ui.buscar.BusVioletaClaro

private val FormaTarjeta = RoundedCornerShape(20.dp)
private val NeutroFondo = Color(0xFFEFEFF4)
private val ConfirmadoFondo = Color(0xFFDCE1F5)
private val ConfirmadoTexto = Color(0xFF3E4A7D)
private val BorradorFondo = Color(0xFFFFF3D6)
private val BorradorTexto = Color(0xFF8A5A00)

/**
 * Tarjeta de un evento en "Mis eventos" (Issue 63): icono del deporte, nombre, etiqueta, fecha y lugar,
 * barra de plazas y los botones "Eliminar" (o "Salir" si me uní) y "Ver".
 *
 * [mostrarNota] activa el texto de la derecha de la barra ("Faltan 2 para jugar", "Completo"...);
 * solo tiene sentido en eventos que todavía no han pasado.
 */
@Composable
fun TarjetaMiEvento(
    evento: MiEventoItem,
    mostrarNota: Boolean,
    onVerClick: () -> Unit,
    onEliminarClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, FormaTarjeta, ambientColor = BusVioleta.copy(alpha = 0.10f), spotColor = BusVioleta.copy(alpha = 0.10f))
            .background(Color.White, FormaTarjeta)
            .padding(16.dp),
    ) {
        CabeceraTarjeta(evento)
        Spacer(Modifier.height(14.dp))
        CupoTarjeta(evento, mostrarNota)
        Spacer(Modifier.height(14.dp))
        AccionesTarjeta(evento, onVerClick, onEliminarClick)
    }
}

@Composable
private fun CabeceraTarjeta(evento: MiEventoItem) {
    val fechaHora = remember(evento.fechaHora) { textoFechaHora(evento.fechaHora) }
    val lugar = evento.lugar.ifBlank { stringResource(R.string.mis_eventos_sin_ubicacion) }
    val iconoFecha = if (evento.etiqueta == EtiquetaEvento.CONFIRMADO) Icons.Outlined.EventAvailable else Icons.Outlined.CalendarToday

    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(BusChip),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = iconoDeEvento(evento.deporte),
                contentDescription = stringResource(evento.deporte.etiqueta),
                tint = BusVioleta,
                modifier = Modifier.size(30.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = evento.nombre,
                    color = BusTinta,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(Modifier.width(8.dp))
                EtiquetaDeEvento(evento.etiqueta)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(iconoFecha, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "$fechaHora • $lugar",
                    color = BusSubtitulo,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EtiquetaDeEvento(etiqueta: EtiquetaEvento) {
    val (texto, fondo, color) = when (etiqueta) {
        EtiquetaEvento.PUBLICO -> Triple(R.string.mis_eventos_etiqueta_publico, NeutroFondo, BusSubtitulo)
        EtiquetaEvento.PRIVADO -> Triple(R.string.mis_eventos_etiqueta_privado, BusAlertaFondo, BusAlerta)
        EtiquetaEvento.CONFIRMADO -> Triple(R.string.mis_eventos_etiqueta_confirmado, ConfirmadoFondo, ConfirmadoTexto)
        EtiquetaEvento.BORRADOR -> Triple(R.string.mis_eventos_etiqueta_borrador, BorradorFondo, BorradorTexto)
        EtiquetaEvento.CANCELADO -> Triple(R.string.mis_eventos_etiqueta_cancelado, NeutroFondo, BusGris)
    }
    Text(
        text = stringResource(texto),
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        modifier = Modifier.clip(CircleShape).background(fondo).padding(horizontal = 12.dp, vertical = 5.dp),
    )
}

@Composable
private fun CupoTarjeta(evento: MiEventoItem, mostrarNota: Boolean) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = textoPlazas(evento),
                color = BusVioleta,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            if (mostrarNota) NotaCupo(evento)
        }
        if (evento.maximo > 0) {
            Spacer(Modifier.height(8.dp))
            BarraCupo(
                fraccion = evento.apuntados.toFloat() / evento.maximo,
                completo = evento.completo,
                apagada = evento.estado == EstadoEvento.CANCELADO,
            )
        }
    }
}

@Composable
private fun textoPlazas(evento: MiEventoItem): String = when {
    evento.maximo <= 0 -> pluralStringResource(R.plurals.mis_eventos_apuntados, evento.apuntados, evento.apuntados)
    evento.hablaDeInscritos -> stringResource(R.string.mis_eventos_inscritos, evento.apuntados, evento.maximo)
    else -> stringResource(R.string.mis_eventos_plazas, evento.apuntados, evento.maximo)
}

/** Lo que falta o sobra: "Faltan 2 para jugar" (magenta), "Quedan 3 plazas" o "✓ Completo". */
@Composable
private fun NotaCupo(evento: MiEventoItem) {
    when {
        evento.completo -> Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(R.string.mis_eventos_completo),
                color = BusVioleta,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        evento.faltanParaJugar > 0 -> Text(
            text = pluralStringResource(R.plurals.mis_eventos_faltan_para_jugar, evento.faltanParaJugar, evento.faltanParaJugar),
            color = BusAlerta,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        evento.maximo > 0 -> Text(
            text = pluralStringResource(R.plurals.mis_eventos_plazas_libres, evento.plazasLibres, evento.plazasLibres),
            color = BusSubtitulo,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun BarraCupo(fraccion: Float, completo: Boolean, apagada: Boolean) {
    val relleno = when {
        apagada -> Brush.horizontalGradient(listOf(BusGris, BusGris))
        completo -> Brush.horizontalGradient(listOf(BusVioleta, BusVioleta))
        else -> Brush.horizontalGradient(listOf(BusVioleta, BusVioletaClaro))
    }
    Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(BusLinea)) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .clip(CircleShape)
                .background(relleno),
        )
    }
}

@Composable
private fun AccionesTarjeta(evento: MiEventoItem, onVerClick: () -> Unit, onEliminarClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BotonTarjeta(
            texto = stringResource(if (evento.esCreador) R.string.mis_eventos_eliminar else R.string.mis_eventos_salir),
            icono = Icons.Outlined.HighlightOff,
            fondo = BusCampo,
            contenido = BusSubtitulo,
            iconoAlFinal = false,
            onClick = onEliminarClick,
        )
        BotonTarjeta(
            texto = stringResource(R.string.mis_eventos_ver),
            icono = Icons.Outlined.ChevronRight,
            fondo = BusVioleta,
            contenido = Color.White,
            iconoAlFinal = true,
            onClick = onVerClick,
            descripcion = stringResource(R.string.mis_eventos_ver_descripcion, evento.nombre),
        )
    }
}

@Composable
private fun BotonTarjeta(
    texto: String,
    icono: ImageVector,
    fondo: Color,
    contenido: Color,
    iconoAlFinal: Boolean,
    onClick: () -> Unit,
    descripcion: String? = null,
) {
    val modificadorBase = Modifier
        .height(44.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(fondo)
        .clickable(role = Role.Button, onClick = onClick)
        .padding(horizontal = 18.dp)
    Row(
        modifier = if (descripcion != null) modificadorBase.semantics { contentDescription = descripcion } else modificadorBase,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!iconoAlFinal) {
            Icon(icono, contentDescription = null, tint = contenido, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text = texto, color = contenido, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        if (iconoAlFinal) {
            Spacer(Modifier.width(4.dp))
            Icon(icono, contentDescription = null, tint = contenido, modifier = Modifier.size(20.dp))
        }
    }
}

/** Icono del deporte (los mismos que usa Buscar). */
internal fun iconoDeEvento(deporte: Deporte): ImageVector = when (deporte) {
    Deporte.FUTBOL -> Icons.Outlined.SportsSoccer
    Deporte.BALONCESTO -> Icons.Outlined.SportsBasketball
    Deporte.TENIS, Deporte.PADEL -> Icons.Outlined.SportsTennis
    Deporte.ATLETISMO, Deporte.OTROS -> Icons.Outlined.SportsScore
}
