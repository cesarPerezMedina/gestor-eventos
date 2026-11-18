package com.example.gestordeeventos.ui.evento

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.R
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EstadoEvento
import com.example.gestordeeventos.ui.buscar.BusAlerta
import com.example.gestordeeventos.ui.buscar.BusCampo
import com.example.gestordeeventos.ui.buscar.BusChip
import com.example.gestordeeventos.ui.buscar.BusGris
import com.example.gestordeeventos.ui.buscar.BusPagina
import com.example.gestordeeventos.ui.buscar.BusSubtitulo
import com.example.gestordeeventos.ui.buscar.BusTinta
import com.example.gestordeeventos.ui.buscar.BusVioleta
import com.example.gestordeeventos.ui.buscar.BusVioletaClaro
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme

/**
 * Sección "Mis eventos" (Issue 63): pestañas Creados / Unidos, buscador por deporte o lugar,
 * chips (Próximos, Historial, Borradores, Cancelados) y una tarjeta por evento.
 *
 * Es solo interfaz: recibe las listas en [estado] y avisa de lo que pulsa el usuario.
 * El estado de la pantalla (pestaña, chip, texto buscado) se conserva al girar el móvil.
 */
@Composable
fun MisEventosScreen(
    estado: EstadoMisEventos,
    modifier: Modifier = Modifier,
    nombreUsuario: String = "",
    onNotificacionesClick: () -> Unit = {},
    onPerfilClick: () -> Unit = {},
    onVerClick: (MiEventoItem) -> Unit = {},
    /** Se llama cuando el usuario confirma en el diálogo: eliminar (si es suyo) o salir (si se unió). */
    onEliminarConfirmado: (MiEventoItem) -> Unit = {},
) {
    var pestana by rememberSaveable { mutableStateOf(PestanaMisEventos.CREADOS) }
    var filtro by rememberSaveable { mutableStateOf(FiltroMisEventos.PROXIMOS) }
    var consulta by rememberSaveable { mutableStateOf("") }
    var pendiente by remember { mutableStateOf<MiEventoItem?>(null) }

    val ahora = remember(estado) { System.currentTimeMillis() }
    val etiquetasDeporte = Deporte.entries.associateWith { stringResource(it.etiqueta) }
    val lista = if (pestana == PestanaMisEventos.CREADOS) estado.creados else estado.unidos
    val visibles = remember(lista, filtro, consulta, ahora, etiquetasDeporte) {
        filtrarMisEventos(lista, filtro, consulta, ahora) { etiquetasDeporte.getValue(it) }
    }
    val filtros = remember(pestana) {
        // Los borradores solo los tiene quien crea eventos
        FiltroMisEventos.entries.filter { pestana == PestanaMisEventos.CREADOS || it != FiltroMisEventos.BORRADORES }
    }

    Surface(modifier = modifier.fillMaxSize(), color = BusPagina) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            CabeceraEventia(nombreUsuario, onNotificacionesClick, onPerfilClick)
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { TituloSeccion() }
                item { CampoBusqueda(consulta = consulta, onConsultaChange = { consulta = it }) }
                item {
                    SelectorPestana(
                        pestana = pestana,
                        creados = estado.creados.contar(FiltroMisEventos.PROXIMOS, ahora),
                        unidos = estado.unidos.contar(FiltroMisEventos.PROXIMOS, ahora),
                        onPestanaClick = { nueva ->
                            pestana = nueva
                            if (nueva == PestanaMisEventos.UNIDOS && filtro == FiltroMisEventos.BORRADORES) {
                                filtro = FiltroMisEventos.PROXIMOS
                            }
                        },
                    )
                }
                item {
                    FilaFiltros(
                        filtros = filtros,
                        seleccionado = filtro,
                        cantidad = { lista.contar(it, ahora) },
                        onFiltroClick = { filtro = it },
                    )
                }
                when {
                    estado.cargando -> item { Cargando() }
                    visibles.isEmpty() -> item {
                        EstadoVacio(filtro = filtro, hayBusqueda = consulta.isNotBlank())
                    }
                    else -> items(visibles, key = { it.id }) { evento ->
                        TarjetaMiEvento(
                            evento = evento,
                            mostrarNota = filtro == FiltroMisEventos.PROXIMOS,
                            onVerClick = { onVerClick(evento) },
                            onEliminarClick = { pendiente = evento },
                        )
                    }
                }
                if (filtro != FiltroMisEventos.HISTORIAL) {
                    item { AvisoArchivado(onVerHistorialClick = { filtro = FiltroMisEventos.HISTORIAL }) }
                }
            }
        }
    }

    pendiente?.let { evento ->
        DialogoConfirmar(
            evento = evento,
            onConfirmar = {
                pendiente = null
                onEliminarConfirmado(evento)
            },
            onCancelar = { pendiente = null },
        )
    }
}

// ---------- Cabecera ----------

@Composable
private fun CabeceraEventia(nombreUsuario: String, onNotificacionesClick: () -> Unit, onPerfilClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.6f))
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.linearGradient(listOf(BusVioleta, BusVioletaClaro))),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Eventia", color = BusTinta, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = stringResource(R.string.mis_eventos_seccion),
                    color = BusVioleta,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).clickable(role = Role.Button, onClick = onNotificacionesClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Notifications,
                    contentDescription = stringResource(R.string.mis_eventos_notificaciones),
                    tint = BusTinta,
                )
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(BusChip)
                    .clickable(role = Role.Button, onClick = onPerfilClick),
                contentAlignment = Alignment.Center,
            ) {
                val inicial = nombreUsuario.firstOrNull()?.uppercase()
                if (inicial != null) {
                    Text(inicial, color = BusVioleta, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                } else {
                    Icon(Icons.Outlined.Person, contentDescription = stringResource(R.string.mis_eventos_perfil), tint = BusVioleta)
                }
            }
        }
    }
}

@Composable
private fun TituloSeccion() {
    Column {
        Text(
            text = stringResource(R.string.mis_eventos_titulo),
            color = BusTinta,
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(2.dp))
        Text(text = stringResource(R.string.mis_eventos_subtitulo), color = BusSubtitulo, fontSize = 15.sp)
    }
}

// ---------- Buscador, pestañas y chips ----------

@Composable
private fun CampoBusqueda(consulta: String, onConsultaChange: (String) -> Unit) {
    val foco = LocalFocusManager.current
    val forma = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(1.dp, forma, ambientColor = BusVioleta.copy(alpha = 0.10f), spotColor = BusVioleta.copy(alpha = 0.10f))
            .background(Color.White, forma)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = BusSubtitulo)
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (consulta.isEmpty()) {
                Text(stringResource(R.string.mis_eventos_buscar_placeholder), color = BusGris, fontSize = 16.sp)
            }
            BasicTextField(
                value = consulta,
                onValueChange = onConsultaChange,
                singleLine = true,
                textStyle = LocalTextStyle.current.merge(TextStyle(color = BusTinta, fontSize = 16.sp)),
                cursorBrush = SolidColor(BusVioleta),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { foco.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (consulta.isNotEmpty()) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).clickable(role = Role.Button) { onConsultaChange("") },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.mis_eventos_buscar_limpiar),
                    tint = BusSubtitulo,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun SelectorPestana(pestana: PestanaMisEventos, creados: Int, unidos: Int, onPestanaClick: (PestanaMisEventos) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(BusCampo).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Segmento(
            texto = stringResource(R.string.mis_eventos_pestana_creados),
            cantidad = creados,
            seleccionado = pestana == PestanaMisEventos.CREADOS,
            onClick = { onPestanaClick(PestanaMisEventos.CREADOS) },
            modifier = Modifier.weight(1f),
        )
        Segmento(
            texto = stringResource(R.string.mis_eventos_pestana_unidos),
            cantidad = unidos,
            seleccionado = pestana == PestanaMisEventos.UNIDOS,
            onClick = { onPestanaClick(PestanaMisEventos.UNIDOS) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun Segmento(texto: String, cantidad: Int, seleccionado: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (seleccionado) BusVioleta else Color.Transparent)
            .selectable(selected = seleccionado, role = Role.Tab, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(texto, color = if (seleccionado) Color.White else BusSubtitulo, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(8.dp))
        Text(
            text = cantidad.toString(),
            color = if (seleccionado) BusVioleta else BusSubtitulo,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clip(CircleShape)
                .background(if (seleccionado) Color.White else Color(0xFFDADDF0))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun FilaFiltros(
    filtros: List<FiltroMisEventos>,
    seleccionado: FiltroMisEventos,
    cantidad: (FiltroMisEventos) -> Int,
    onFiltroClick: (FiltroMisEventos) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(filtros, key = { it.name }) { filtro ->
            ChipFiltro(
                texto = textoFiltro(filtro, cantidad(filtro)),
                seleccionado = filtro == seleccionado,
                onClick = { onFiltroClick(filtro) },
            )
        }
    }
}

/** "Próximos (3)", "Historial"... El número solo sale en Próximos y, si hay alguno, en Borradores y Cancelados. */
@Composable
private fun textoFiltro(filtro: FiltroMisEventos, cantidad: Int): String {
    val nombre = stringResource(
        when (filtro) {
            FiltroMisEventos.PROXIMOS -> R.string.mis_eventos_filtro_proximos
            FiltroMisEventos.HISTORIAL -> R.string.mis_eventos_filtro_historial
            FiltroMisEventos.BORRADORES -> R.string.mis_eventos_filtro_borradores
            FiltroMisEventos.CANCELADOS -> R.string.mis_eventos_filtro_cancelados
        },
    )
    val conNumero = filtro == FiltroMisEventos.PROXIMOS ||
        (filtro != FiltroMisEventos.HISTORIAL && cantidad > 0)
    return if (conNumero) "$nombre ($cantidad)" else nombre
}

@Composable
private fun ChipFiltro(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(if (seleccionado) BusVioletaClaro else BusCampo)
            .selectable(selected = seleccionado, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (seleccionado) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f)))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = texto,
            color = if (seleccionado) Color.White else BusSubtitulo,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

// ---------- Estados de la lista ----------

@Composable
private fun Cargando() {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BusVioleta)
    }
}

@Composable
private fun EstadoVacio(filtro: FiltroMisEventos, hayBusqueda: Boolean) {
    val titulo = when {
        hayBusqueda -> R.string.mis_eventos_vacio_busqueda
        filtro == FiltroMisEventos.PROXIMOS -> R.string.mis_eventos_vacio_proximos
        filtro == FiltroMisEventos.HISTORIAL -> R.string.mis_eventos_vacio_historial
        filtro == FiltroMisEventos.BORRADORES -> R.string.mis_eventos_vacio_borradores
        else -> R.string.mis_eventos_vacio_cancelados
    }
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(BusChip),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.EventBusy, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(titulo),
            color = BusTinta,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (!hayBusqueda && filtro == FiltroMisEventos.PROXIMOS) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.mis_eventos_vacio_proximos_ayuda),
                color = BusSubtitulo,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AvisoArchivado(onVerHistorialClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BusCampo)
            .padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(BusChip),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = stringResource(R.string.mis_eventos_aviso_archivado),
                color = BusSubtitulo,
                fontSize = 14.sp,
                lineHeight = 21.sp,
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.clickable(role = Role.Button, onClick = onVerHistorialClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.mis_eventos_aviso_historial),
                    color = BusVioleta,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = BusVioleta, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ---------- Confirmación ----------

@Composable
private fun DialogoConfirmar(evento: MiEventoItem, onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    val titulo = if (evento.esCreador) R.string.mis_eventos_eliminar_titulo else R.string.mis_eventos_salir_titulo
    val mensaje = if (evento.esCreador) R.string.mis_eventos_eliminar_mensaje else R.string.mis_eventos_salir_mensaje
    val accion = if (evento.esCreador) R.string.mis_eventos_eliminar else R.string.mis_eventos_salir
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(stringResource(titulo)) },
        text = { Text(stringResource(mensaje, evento.nombre)) },
        confirmButton = {
            TextButton(onClick = onConfirmar) {
                Text(stringResource(accion), color = BusAlerta, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) { Text(stringResource(R.string.mis_eventos_cancelar)) }
        },
    )
}

// ---------- Vista previa (los mismos datos del boceto) ----------

private fun ejemplo(
    id: Long, nombre: String, deporte: Deporte, dias: Long, lugar: String,
    apuntados: Int, minimo: Int, maximo: Int, publico: Boolean,
    estado: EstadoEvento = EstadoEvento.PUBLICADO,
) = MiEventoItem(
    id = id, nombre = nombre, deporte = deporte,
    fechaHora = System.currentTimeMillis() + dias * 86_400_000L,
    lugar = lugar, apuntados = apuntados, minimo = minimo, maximo = maximo,
    publico = publico, estado = estado, esCreador = true,
)

@Preview(showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun MisEventosScreenPreview() {
    GestorDeEventosTheme {
        MisEventosScreen(
            estado = EstadoMisEventos(
                creados = listOf(
                    ejemplo(1, "Fútbol 7 en el parque", Deporte.FUTBOL, 1, "Parque Central", 8, 10, 14, true),
                    ejemplo(2, "Pádel dobles mixto", Deporte.PADEL, 2, "Padel Center pista 4", 3, 4, 4, false),
                    ejemplo(3, "Torneo Basket 3x3", Deporte.BALONCESTO, 7, "Polideportivo Norte", 6, 6, 6, true),
                    ejemplo(4, "Running domingo", Deporte.ATLETISMO, 9, "Casa de Campo", 0, 0, 0, true, EstadoEvento.BORRADOR),
                ),
                cargando = false,
            ),
            nombreUsuario = "Carlos",
        )
    }
}