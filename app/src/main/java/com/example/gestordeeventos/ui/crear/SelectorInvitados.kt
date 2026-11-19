package com.example.gestordeeventos.ui.crear

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import com.example.gestordeeventos.ui.theme.SuperficieApp

/** Cuántos amigos se enseñan antes de pulsar "Ver lista completa". */
private const val AMIGOS_VISIBLES = 3

/**
 * Apartado "Invitar amigos" de Crear evento (Issue 48): se eligen usuarios concretos de entre los amigos.
 *
 * - Buscador por nombre de usuario o ID.
 * - Cada amigo se marca y se desmarca con su botón "Invitar" / "Invitado".
 * - Los elegidos aparecen arriba como chips, que se pueden quitar con la X.
 * - "Ver lista completa" despliega todos los amigos (al buscar se enseñan todos los que coinciden).
 * - En un evento privado avisa de que solo lo verán los invitados.
 *
 * Es un componente sin estado de selección: quien lo usa guarda los elegidos y los cambia en [onAlternar].
 *
 * @param seleccionados ids de los amigos invitados.
 * @param onAlternar se llama con el id del amigo pulsado, para invitarlo o quitarlo.
 */
@Composable
fun SelectorInvitados(
    amigos: List<AmigoInvitable>,
    seleccionados: Set<String>,
    onAlternar: (String) -> Unit,
    esPublico: Boolean,
    modifier: Modifier = Modifier,
) {
    var busqueda by rememberSaveable { mutableStateOf("") }
    var verTodos by rememberSaveable { mutableStateOf(false) }

    val consulta = busqueda.trim().removePrefix("@").removePrefix("#")
    val coinciden = amigos.filter {
        consulta.isEmpty() || it.nombre.contains(consulta, ignoreCase = true) || it.detalle.contains(consulta, ignoreCase = true)
    }
    val buscando = consulta.isNotEmpty()
    val visibles = if (verTodos || buscando) coinciden else coinciden.take(AMIGOS_VISIBLES)
    val elegidos = amigos.filter { it.id in seleccionados }

    Column(modifier = modifier.fillMaxWidth()) {
        // Título, "Opcional" y contador de invitados
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
            if (elegidos.isNotEmpty()) {
                Text(
                    text = if (elegidos.size == 1) "1 invitado" else "${elegidos.size} invitados",
                    color = CreVioleta,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        if (!esPublico && elegidos.isEmpty()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Es un evento privado: solo lo verán las personas que invites.",
                color = CreVioleta,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }

        // Elegidos: chips que se quitan con la X
        if (elegidos.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                elegidos.forEach { amigo -> ChipInvitado(amigo.nombre) { onAlternar(amigo.id) } }
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(18.dp), spotColor = Color(0x1A000000))
                .clip(RoundedCornerShape(18.dp))
                .background(SuperficieApp)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (amigos.isEmpty()) {
                EstadoSinAmigos()
            } else {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Buscar por usuario o ID...", color = CreGris, fontSize = 13.sp, maxLines = 1) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = CreSubtitulo) },
                    trailingIcon = if (busqueda.isNotEmpty()) {
                        {
                            Icon(
                                Icons.Outlined.Close,
                                contentDescription = "Borrar búsqueda",
                                tint = CreSubtitulo,
                                modifier = Modifier.clickable { busqueda = "" },
                            )
                        }
                    } else {
                        null
                    },
                    colors = coloresCampoEvento(),
                )

                visibles.forEach { amigo ->
                    FilaAmigoInvitable(
                        amigo = amigo,
                        invitado = amigo.id in seleccionados,
                        onClick = { onAlternar(amigo.id) },
                    )
                }

                if (coinciden.isEmpty()) {
                    Text(
                        text = "Ningún amigo coincide con tu búsqueda",
                        color = CreGris,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                    )
                }

                if (!buscando && coinciden.size > AMIGOS_VISIBLES) {
                    Text(
                        text = if (verTodos) "Ver menos" else "Ver lista completa (${coinciden.size})",
                        color = CreVioleta,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { verTodos = !verTodos }
                            .padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipInvitado(nombre: String, onQuitar: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(CreChip)
            .clickable(onClick = onQuitar)
            .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(nombre, color = CreVioleta, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Spacer(Modifier.size(4.dp))
        Icon(Icons.Outlined.Close, contentDescription = "Quitar a $nombre", tint = CreVioleta, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun EstadoSinAmigos() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(CreChip),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Group, contentDescription = null, tint = CreVioleta, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text("Todavía no tienes amigos agregados", color = CreTinta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Añádelos desde la pestaña Amigos y podrás invitarlos aquí.",
            color = CreSubtitulo,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
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

@Preview(showBackground = true, widthDp = 390, name = "Con amigos")
@Composable
private fun SelectorInvitadosPreview() {
    GestorDeEventosTheme {
        var elegidos by remember { mutableStateOf(setOf("1")) }
        val amigos = amigosInvitablesDeEjemplo + AmigoInvitable("4", "luis_g", "#1849")
        Column(Modifier.padding(16.dp)) {
            SelectorInvitados(
                amigos = amigos,
                seleccionados = elegidos,
                onAlternar = { id -> elegidos = if (id in elegidos) elegidos - id else elegidos + id },
                esPublico = false,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, name = "Sin amigos")
@Composable
private fun SelectorInvitadosVacioPreview() {
    GestorDeEventosTheme {
        Column(Modifier.padding(16.dp)) {
            SelectorInvitados(amigos = emptyList(), seleccionados = emptySet(), onAlternar = {}, esPublico = true)
        }
    }
}
