package com.example.gestordeeventos.ui.invitaciones

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.SportsBasketball
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.SportsTennis
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.R
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.InvitacionPendiente
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Colores de la pantalla (los mismos que Inicio y el resto de la app, en violeta)
private val InvPagina = Color(0xFFF6F4FF)
private val InvVioleta = Color(0xFF5B0FBF)
private val InvBoton = Color(0xFF6D28D9)
private val InvTinta = Color(0xFF1B1B3A)
private val InvSubtitulo = Color(0xFF4A5068)
private val InvBorde = Color(0xFFE0E3F5)
private val InvChip = Color(0xFFE6DDFB)

/**
 * Pantalla "Invitaciones" (Issue 71): las invitaciones a eventos que has recibido y todavía no has
 * respondido, cada una con los botones Aceptar y Rechazar.
 *
 * No guarda estado ni toca la base de datos: recibe la lista y avisa con [onAceptarClick] y
 * [onRechazarClick]. De dónde sale la lista (la consulta de la Issue 72) y qué pasa al responder
 * (Issues 73 y 74) se conecta fuera, en el ViewModel. Con la lista vacía enseña un mensaje.
 *
 * Todos los parámetros tienen valor por defecto, así que `InvitacionesScreen()` compila y enseña
 * datos de ejemplo. [snackbarHostState] sirve para mostrar avisos ("Te has apuntado a...") desde fuera.
 */
@Composable
fun InvitacionesScreen(
    modifier: Modifier = Modifier,
    invitaciones: List<InvitacionPendiente> = invitacionesDeEjemplo,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onAceptarClick: (InvitacionPendiente) -> Unit = {},
    onRechazarClick: (InvitacionPendiente) -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = InvPagina,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { relleno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(relleno)
                .padding(horizontal = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.invitaciones_volver),
                        tint = InvTinta,
                    )
                }
                Text(
                    text = stringResource(R.string.invitaciones_titulo),
                    color = InvTinta,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.semantics { heading() },
                )
            }

            if (invitaciones.isEmpty()) {
                EstadoVacio(modifier = Modifier.weight(1f).fillMaxWidth())
            } else {
                Text(
                    text = if (invitaciones.size == 1) {
                        stringResource(R.string.invitaciones_contador_una)
                    } else {
                        stringResource(R.string.invitaciones_contador_varias, invitaciones.size)
                    },
                    color = InvSubtitulo,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 12.dp),
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    items(invitaciones, key = { it.invitacionId }) { invitacion ->
                        TarjetaInvitacion(
                            invitacion = invitacion,
                            onAceptarClick = { onAceptarClick(invitacion) },
                            onRechazarClick = { onRechazarClick(invitacion) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaInvitacion(
    invitacion: InvitacionPendiente,
    onAceptarClick: () -> Unit,
    onRechazarClick: () -> Unit,
) {
    val deporte = stringResource(invitacion.deporte.etiqueta)
    val fecha = remember(invitacion.fechaHora) {
        SimpleDateFormat("EEE d MMM · HH:mm", Locale.getDefault()).format(Date(invitacion.fechaHora))
    }
    val descripcionAceptar = stringResource(R.string.invitaciones_aceptar_descripcion, invitacion.eventoNombre)
    val descripcionRechazar = stringResource(R.string.invitaciones_rechazar_descripcion, invitacion.eventoNombre)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, InvBorde),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(InvChip),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = iconoDeporte(invitacion.deporte),
                        contentDescription = null,
                        tint = InvVioleta,
                        modifier = Modifier.size(26.dp),
                    )
                }
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invitacion.eventoNombre,
                        color = InvTinta,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "$deporte · $fecha",
                        color = InvSubtitulo,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = stringResource(R.string.invitaciones_de, invitacion.deNombreUsuario),
                        color = InvSubtitulo,
                        fontSize = 14.sp,
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onRechazarClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = descripcionRechazar },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, InvBoton),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = InvBoton),
                ) {
                    Text(stringResource(R.string.invitaciones_rechazar), fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAceptarClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = descripcionAceptar },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InvBoton, contentColor = Color.White),
                ) {
                    Text(stringResource(R.string.invitaciones_aceptar), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EstadoVacio(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(CircleShape).background(InvChip),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Mail, contentDescription = null, tint = InvVioleta, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.invitaciones_vacio_titulo),
            color = InvTinta,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.invitaciones_vacio_texto),
            color = InvSubtitulo,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

private fun iconoDeporte(deporte: Deporte): ImageVector = when (deporte) {
    Deporte.FUTBOL -> Icons.Outlined.SportsSoccer
    Deporte.BALONCESTO -> Icons.Outlined.SportsBasketball
    Deporte.TENIS, Deporte.PADEL -> Icons.Outlined.SportsTennis
    Deporte.ATLETISMO -> Icons.Outlined.Flag
    Deporte.OTROS -> Icons.Outlined.EmojiEvents
}

/** Datos de ejemplo para ver el diseño (previsualización y valor por defecto de la pantalla). */
val invitacionesDeEjemplo = listOf(
    InvitacionPendiente(
        invitacionId = 1, eventoId = 10, eventoNombre = "Fútbol 7 en el parque", deporte = Deporte.FUTBOL,
        fechaHora = 1_795_300_000_000L, deUsuarioId = 2, deNombreUsuario = "luis_9",
    ),
    InvitacionPendiente(
        invitacionId = 2, eventoId = 11, eventoNombre = "Tenis dobles", deporte = Deporte.TENIS,
        fechaHora = 1_795_400_000_000L, deUsuarioId = 3, deNombreUsuario = "marta",
    ),
    InvitacionPendiente(
        invitacionId = 3, eventoId = 12, eventoNombre = "Partido de baloncesto 3x3", deporte = Deporte.BALONCESTO,
        fechaHora = 1_795_500_000_000L, deUsuarioId = 2, deNombreUsuario = "luis_9",
    ),
)

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InvitacionesScreenPreview() {
    InvitacionesScreen()
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InvitacionesScreenVaciaPreview() {
    InvitacionesScreen(invitaciones = emptyList())
}
