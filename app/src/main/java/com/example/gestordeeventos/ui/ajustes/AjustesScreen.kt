package com.example.gestordeeventos.ui.ajustes

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestordeeventos.R
import com.example.gestordeeventos.data.model.Deporte
import com.example.gestordeeventos.data.model.EscalaTexto
import com.example.gestordeeventos.data.model.ModoTema
import com.example.gestordeeventos.data.repositorio.AjustesRepositorio
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme

/** Todo lo que la pantalla de Ajustes necesita para dibujarse. Los valores por defecto sirven para tests y previews. */
internal data class AjustesEstado(
    val modoTema: ModoTema = ModoTema.SISTEMA,
    val escalaTexto: EscalaTexto = EscalaTexto.NORMAL,
    val radioBusquedaKm: Int = AjustesRepositorio.RADIO_BUSQUEDA_POR_DEFECTO_KM,
    val deportesFavoritos: Set<Deporte> = emptySet(),
    val recordatorioPartidos: Boolean = true,
    val avisosPlazasVacantes: Boolean = true,
    val invitacionesDirectas: Boolean = false,
    val versionApp: String = "",
)

/** Qué hace la pantalla cuando el usuario toca algo. Por defecto no hace nada (tests y previews). */
internal class AjustesAcciones(
    val onModoTemaChange: (ModoTema) -> Unit = {},
    val onEscalaTextoChange: (EscalaTexto) -> Unit = {},
    val onRadioChange: (Int) -> Unit = {},
    val onDeporteAlternar: (Deporte) -> Unit = {},
    val onRecordatorioChange: (Boolean) -> Unit = {},
    val onAvisosPlazasChange: (Boolean) -> Unit = {},
    val onInvitacionesChange: (Boolean) -> Unit = {},
)

/**
 * Pantalla de Ajustes. Obtiene su ViewModel ella misma, así que la navegación solo le pasa
 * las acciones de volver y cerrar sesión.
 */
@Composable
fun AjustesScreen(
    onBackClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AjustesViewModel = viewModel(factory = AjustesViewModel.Factory),
) {
    val modoTema by viewModel.modoTema.collectAsStateWithLifecycle()
    val escalaTexto by viewModel.escalaTexto.collectAsStateWithLifecycle()
    val radio by viewModel.radioBusquedaKm.collectAsStateWithLifecycle()
    val deportes by viewModel.deportesFavoritos.collectAsStateWithLifecycle()
    val recordatorio by viewModel.recordatorioPartidos.collectAsStateWithLifecycle()
    val plazas by viewModel.avisosPlazasVacantes.collectAsStateWithLifecycle()
    val invitaciones by viewModel.invitacionesDirectas.collectAsStateWithLifecycle()

    AjustesContenido(
        estado = AjustesEstado(
            modoTema = modoTema,
            escalaTexto = escalaTexto,
            radioBusquedaKm = radio,
            deportesFavoritos = deportes,
            recordatorioPartidos = recordatorio,
            avisosPlazasVacantes = plazas,
            invitacionesDirectas = invitaciones,
            versionApp = rememberVersionApp(),
        ),
        acciones = AjustesAcciones(
            onModoTemaChange = viewModel::elegirModoTema,
            onEscalaTextoChange = viewModel::elegirEscalaTexto,
            onRadioChange = viewModel::elegirRadioBusquedaKm,
            onDeporteAlternar = viewModel::alternarDeporteFavorito,
            onRecordatorioChange = viewModel::cambiarRecordatorioPartidos,
            onAvisosPlazasChange = viewModel::cambiarAvisosPlazasVacantes,
            onInvitacionesChange = viewModel::cambiarInvitacionesDirectas,
        ),
        onBackClick = onBackClick,
        onCerrarSesion = onCerrarSesion,
        modifier = modifier,
    )
}

/** Dibujo de la pantalla sin ViewModel: así se puede probar y previsualizar con datos de mentira. */
@Composable
internal fun AjustesContenido(
    estado: AjustesEstado,
    acciones: AjustesAcciones,
    onBackClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))
            CabeceraAjustes(onBackClick = onBackClick)
            Spacer(Modifier.height(24.dp))

            SeccionApariencia(estado, acciones)
            Spacer(Modifier.height(24.dp))

            SeccionJuego(estado, acciones)
            Spacer(Modifier.height(24.dp))

            SeccionNotificaciones(estado, acciones)
            Spacer(Modifier.height(24.dp))

            TarjetaAjustes {
                CerrarSesionOpcion(onCerrarSesion = onCerrarSesion, modifier = Modifier.padding(12.dp))
            }

            PieAjustes(versionApp = estado.versionApp)
        }
    }
}

/** Flecha de volver, título con subtítulo y, a la derecha, un icono de ajustes (solo decorativo). */
@Composable
private fun CabeceraAjustes(onBackClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.ajustes_volver),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.ajustes_titulo),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(R.string.ajustes_subtitulo),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
            )
        }
        Spacer(Modifier.size(8.dp))
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun SeccionApariencia(estado: AjustesEstado, acciones: AjustesAcciones) {
    SeccionAjustes(
        titulo = stringResource(R.string.ajustes_seccion_apariencia_accesibilidad),
        icono = Icons.Outlined.Palette,
    ) {
        // El interruptor solo distingue claro/oscuro. "Seguir al sistema" sigue funcionando por dentro:
        // mientras no se toque, el interruptor refleja lo que tenga activado el móvil.
        FilaInterruptor(
            titulo = stringResource(R.string.ajustes_tema_oscuro_titulo),
            descripcion = stringResource(R.string.ajustes_tema_oscuro_descripcion),
            marcado = estado.modoTema.esOscuro(sistemaOscuro = isSystemInDarkTheme()),
            onCambio = { oscuro -> acciones.onModoTemaChange(if (oscuro) ModoTema.OSCURO else ModoTema.CLARO) },
        )
        SeparadorAjustes()
        SelectorTamanoTexto(escala = estado.escalaTexto, onCambio = acciones.onEscalaTextoChange)
    }
}

@Composable
private fun SeccionJuego(estado: AjustesEstado, acciones: AjustesAcciones) {
    SeccionAjustes(
        titulo = stringResource(R.string.ajustes_seccion_juego),
        icono = Icons.Outlined.SportsSoccer,
    ) {
        SelectorRadioBusqueda(
            radioKm = estado.radioBusquedaKm,
            opciones = AjustesRepositorio.RADIOS_BUSQUEDA_KM,
            onCambio = acciones.onRadioChange,
        )
        SeparadorAjustes()
        SelectorDeportesFavoritos(
            disponibles = AjustesRepositorio.DEPORTES_FAVORITOS_DISPONIBLES,
            favoritos = estado.deportesFavoritos,
            onAlternar = acciones.onDeporteAlternar,
        )
    }
}

@Composable
private fun SeccionNotificaciones(estado: AjustesEstado, acciones: AjustesAcciones) {
    SeccionAjustes(
        titulo = stringResource(R.string.ajustes_seccion_notificaciones),
        icono = Icons.Outlined.NotificationsActive,
    ) {
        FilaInterruptor(
            titulo = stringResource(R.string.ajustes_notif_recordatorio_titulo),
            descripcion = stringResource(R.string.ajustes_notif_recordatorio_descripcion),
            marcado = estado.recordatorioPartidos,
            onCambio = acciones.onRecordatorioChange,
            icono = Icons.Outlined.Alarm,
        )
        SeparadorAjustes()
        FilaInterruptor(
            titulo = stringResource(R.string.ajustes_notif_plazas_titulo),
            descripcion = stringResource(R.string.ajustes_notif_plazas_descripcion),
            marcado = estado.avisosPlazasVacantes,
            onCambio = acciones.onAvisosPlazasChange,
            icono = Icons.Outlined.PersonAdd,
        )
        SeparadorAjustes()
        FilaInterruptor(
            titulo = stringResource(R.string.ajustes_notif_invitaciones_titulo),
            descripcion = stringResource(R.string.ajustes_notif_invitaciones_descripcion),
            marcado = estado.invitacionesDirectas,
            onCambio = acciones.onInvitacionesChange,
            icono = Icons.Outlined.Groups,
        )
    }
}

/** Versión de la app y lema, centrados al final de la pantalla. */
@Composable
private fun PieAjustes(versionApp: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (versionApp.isNotEmpty()) {
            Text(
                text = stringResource(R.string.ajustes_pie_version, versionApp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
        }
        Text(
            text = stringResource(R.string.ajustes_pie_lema),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

/** Versión de la app (la de build.gradle: versionName). Si no se puede leer, devuelve texto vacío. */
@Suppress("DEPRECATION")
@Composable
private fun rememberVersionApp(): String {
    val contexto = LocalContext.current
    return remember(contexto) {
        runCatching {
            val gestor = contexto.packageManager
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gestor.getPackageInfo(contexto.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                gestor.getPackageInfo(contexto.packageName, 0)
            }
            info.versionName
        }.getOrNull().orEmpty()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1500)
@Composable
private fun AjustesScreenClaroPreview() {
    GestorDeEventosTheme(darkTheme = false) {
        AjustesContenido(
            estado = AjustesEstado(
                modoTema = ModoTema.CLARO,
                deportesFavoritos = setOf(Deporte.FUTBOL, Deporte.PADEL),
                versionApp = "1.0",
            ),
            acciones = AjustesAcciones(),
            onBackClick = {},
            onCerrarSesion = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 1500)
@Composable
private fun AjustesScreenOscuroPreview() {
    GestorDeEventosTheme(darkTheme = true) {
        AjustesContenido(
            estado = AjustesEstado(
                modoTema = ModoTema.OSCURO,
                escalaTexto = EscalaTexto.GRANDE,
                radioBusquedaKm = 25,
                deportesFavoritos = setOf(Deporte.BALONCESTO),
                invitacionesDirectas = true,
                versionApp = "1.0",
            ),
            acciones = AjustesAcciones(),
            onBackClick = {},
            onCerrarSesion = {},
        )
    }
}