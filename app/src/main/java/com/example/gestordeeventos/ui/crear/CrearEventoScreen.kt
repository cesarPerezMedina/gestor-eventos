package com.example.gestordeeventos.ui.crear

import android.widget.Toast
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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestordeeventos.data.localizacion.GeocodificadorProvider
import com.example.gestordeeventos.data.localizacion.LocalizacionProvider
import com.example.gestordeeventos.data.localizacion.ResultadoUbicacion
import com.example.gestordeeventos.data.localizacion.Ubicacion
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.ui.componentes.rememberSolicitudPermiso
import com.example.gestordeeventos.util.EstadoPermiso
import com.example.gestordeeventos.util.PermisoApp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import com.example.gestordeeventos.ui.componentes.SelectorDeporte
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import com.example.gestordeeventos.ui.theme.PaletaApp
import com.example.gestordeeventos.ui.theme.SuperficieApp

// --- Colores de la pantalla (mismos tonos que la pantalla de Amigos) ---
internal val CrePagina get() = PaletaApp.elegir(Color(0xFFF6F4FF), Color(0xFF15131F))
internal val CreVioleta get() = PaletaApp.elegir(Color(0xFF5B0FBF), Color(0xFF8A5CEB))
internal val CreVioletaClaro get() = PaletaApp.elegir(Color(0xFF7C3AED), Color(0xFFA78BFA))
internal val CreBoton get() = PaletaApp.elegir(Color(0xFF6D28D9), Color(0xFF7C3AED))
internal val CreTinta get() = PaletaApp.elegir(Color(0xFF1B1B3A), Color(0xFFECEAFB))
internal val CreSubtitulo get() = PaletaApp.elegir(Color(0xFF4A5068), Color(0xFFB9BDD3))
internal val CreGris get() = PaletaApp.elegir(Color(0xFF7D8397), Color(0xFF9AA0B5))
internal val CreChip get() = PaletaApp.elegir(Color(0xFFE6DDFB), Color(0xFF332A52))
internal val CreCampo get() = PaletaApp.elegir(Color(0xFFEEF0FF), Color(0xFF25223A))
internal val CreLinea get() = PaletaApp.elegir(Color(0xFFE3E1F2), Color(0xFF363350))

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
    amigos: List<AmigoInvitable> = emptyList(),
    onBackClick: () -> Unit = {},
    onCrearEventoClick: (NuevoEvento) -> Unit = {},
) {
    var deporte by rememberSaveable { mutableStateOf<Deporte?>(Deporte.FUTBOL) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    // Pasa a true al pulsar "Crear": entonces se enseñan los errores de los campos
    var intentoCrear by rememberSaveable { mutableStateOf(false) }
    var esPublico by rememberSaveable { mutableStateOf(true) }
    var minJugadores by rememberSaveable { mutableStateOf(10) }
    var maxJugadores by rememberSaveable { mutableStateOf(14) }
    var tipoUbicacion by rememberSaveable { mutableStateOf(TipoUbicacion.POLIDEPORTIVO) }
    var ubicacion by rememberSaveable { mutableStateOf("") }
    var indicaciones by rememberSaveable { mutableStateOf("") }
    // Fecha y hora (Issue 47): se guardan como números porque LocalDate y LocalTime no sobreviven a girar la pantalla.
    // Por defecto, mañana de 18:30 a 20:00.
    var fechaDia by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(1).toEpochDay()) }
    var inicioMin by rememberSaveable { mutableStateOf(18 * 60 + 30) }
    var finMin by rememberSaveable { mutableStateOf(20 * 60) }
    val fecha = LocalDate.ofEpochDay(fechaDia)
    val horaInicio = LocalTime.of(inicioMin / 60, inicioMin % 60)
    val horaFin = LocalTime.of(finMin / 60, finMin % 60)
    // Ids de los amigos invitados; se guardan para no perderlos al girar la pantalla
    val invitados = rememberSaveable(saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })) {
        mutableStateListOf<String>()
    }

    // ---------- Mapa y ubicación (Issue 46) ----------
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val localizacion = remember { LocalizacionProvider(context) }
    val geocodificador = remember { GeocodificadorProvider(context) }
    var latitud by rememberSaveable { mutableStateOf<Double?>(null) }
    var longitud by rememberSaveable { mutableStateOf<Double?>(null) }
    val lat = latitud
    val lon = longitud
    val coordenadas = if (lat != null && lon != null) Ubicacion(lat, lon) else null

    fun aviso(texto: String) = Toast.makeText(context, texto, Toast.LENGTH_SHORT).show()

    // Marca el punto en el mapa y rellena el campo con su dirección
    fun marcar(punto: Ubicacion) {
        latitud = punto.latitud
        longitud = punto.longitud
        scope.launch {
            val direccion = geocodificador.direccionDe(punto.latitud, punto.longitud)
            if (direccion != null) {
                ubicacion = direccion.take(UBICACION_MAX)
            } else {
                aviso("No se pudo obtener la dirección de ese punto: escríbela a mano")
            }
        }
    }

    fun usarMiUbicacion() {
        scope.launch {
            when (val resultado = localizacion.obtenerUbicacionActual()) {
                is ResultadoUbicacion.Obtenida -> marcar(resultado.ubicacion)
                ResultadoUbicacion.SinPermiso -> aviso("Falta el permiso de ubicación")
                ResultadoUbicacion.UbicacionDesactivada -> aviso("Activa la ubicación del móvil")
                ResultadoUbicacion.NoDisponible -> aviso("No se pudo obtener tu ubicación")
                is ResultadoUbicacion.Fallo -> aviso("Error al obtener tu ubicación")
            }
        }
    }

    val permisoUbicacion = rememberSolicitudPermiso(PermisoApp.UBICACION) { concedido ->
        if (concedido) usarMiUbicacion()
    }

    // Escribe una dirección y pulsa "buscar" en el teclado: el mapa va a ese sitio
    fun buscarDireccion() {
        if (errorUbicacionEvento(ubicacion) != null) return
        scope.launch {
            val punto = geocodificador.buscarDireccion(ubicacion)
            if (punto != null) {
                latitud = punto.latitud
                longitud = punto.longitud
            } else {
                aviso("No se encontró esa dirección")
            }
        }
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
                .background(SuperficieApp)
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
        SelectorUbicacionEvento(
            tipo = tipoUbicacion,
            onTipoChange = { tipoUbicacion = it },
            ubicacion = ubicacion,
            onUbicacionChange = { ubicacion = it },
            indicaciones = indicaciones,
            onIndicacionesChange = { indicaciones = it },
            mostrarError = intentoCrear,
            coordenadas = coordenadas,
            onCoordenadasElegidas = { marcar(it) },
            onUbicacionActualClick = {
                when {
                    permisoUbicacion.concedido -> usarMiUbicacion()
                    permisoUbicacion.estado == EstadoPermiso.BLOQUEADO -> permisoUbicacion.abrirAjustes()
                    else -> permisoUbicacion.solicitar()
                }
            },
            onBuscarDireccion = { buscarDireccion() },
        )

        Spacer(Modifier.height(18.dp))

        // ---------- Fecha y hora ----------
        SelectorFechaHoraEvento(
            fecha = fecha,
            onFechaChange = { fechaDia = it.toEpochDay() },
            horaInicio = horaInicio,
            horaFin = horaFin,
            onHorasChange = { inicio, fin ->
                inicioMin = inicio.hour * 60 + inicio.minute
                finMin = fin.hour * 60 + fin.minute
            },
        )

        Spacer(Modifier.height(20.dp))

        // ---------- Invitar amigos ----------
        SelectorInvitados(
            amigos = amigos,
            seleccionados = invitados.toSet(),
            onAlternar = { id -> if (id in invitados) invitados.remove(id) else invitados.add(id) },
            esPublico = esPublico,
        )

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
                    // Solo se crea si el nombre, la ubicación y la fecha son válidos; la descripción es opcional
                    if (errorNombreEvento(nombre) == null &&
                        errorUbicacionEvento(ubicacion) == null &&
                        errorFechaHoraEvento(fecha, horaInicio, horaFin) == null
                    ) {
                        onCrearEventoClick(
                            NuevoEvento(
                                nombre = nombre.trim(),
                                descripcion = descripcion.trim(),
                                deporte = deporte,
                                publico = esPublico,
                                minJugadores = minJugadores,
                                maxJugadores = maxJugadores,
                                tipoUbicacion = tipoUbicacion,
                                ubicacion = ubicacion.trim(),
                                indicaciones = indicaciones.trim(),
                                latitud = latitud,
                                longitud = longitud,
                                inicioMillis = milisegundosDe(fecha, horaInicio),
                                finMillis = milisegundosDe(fecha, horaFin),
                                invitadosIds = invitados.filter { id -> amigos.any { it.id == id } },
                            )
                        )
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

/** Campo que no se escribe, se pulsa (fecha y hora). */
@Composable
internal fun CampoSelector(icono: androidx.compose.ui.graphics.vector.ImageVector, texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SuperficieApp)
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
            .background(SuperficieApp)
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

@Preview(showBackground = true, widthDp = 390, heightDp = 1900)
@Composable
private fun CrearEventoScreenPreview() {
    GestorDeEventosTheme {
        CrearEventoScreen(amigos = amigosInvitablesDeEjemplo)
    }
}