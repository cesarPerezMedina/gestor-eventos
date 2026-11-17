package com.example.gestordeeventos.ui.ajustes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestordeeventos.R
import com.example.gestordeeventos.data.model.EscalaTexto
import com.example.gestordeeventos.data.model.ModoTema
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme

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

    AjustesContenido(
        modoTema = modoTema,
        onModoTemaChange = viewModel::elegirModoTema,
        escalaTexto = escalaTexto,
        onEscalaTextoChange = viewModel::elegirEscalaTexto,
        onBackClick = onBackClick,
        onCerrarSesion = onCerrarSesion,
        modifier = modifier,
    )
}

/** Dibujo de la pantalla sin ViewModel: así se puede probar y previsualizar con datos de mentira. */
@Composable
internal fun AjustesContenido(
    modoTema: ModoTema,
    onModoTemaChange: (ModoTema) -> Unit,
    escalaTexto: EscalaTexto,
    onEscalaTextoChange: (EscalaTexto) -> Unit,
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

            // Cabecera: flecha de volver + título
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
                Text(
                    text = stringResource(R.string.ajustes_titulo),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            Spacer(Modifier.height(28.dp))

            SeccionAjustes(titulo = stringResource(R.string.ajustes_seccion_apariencia)) {
                OpcionesSeleccion(
                    opciones = ModoTema.entries,
                    seleccionada = modoTema,
                    etiqueta = { stringResource(it.etiqueta) },
                    onSeleccion = onModoTemaChange,
                )
            }

            Spacer(Modifier.height(24.dp))

            SeccionAjustes(titulo = stringResource(R.string.ajustes_seccion_texto)) {
                OpcionesSeleccion(
                    opciones = EscalaTexto.entries,
                    seleccionada = escalaTexto,
                    etiqueta = { stringResource(it.etiqueta) },
                    onSeleccion = onEscalaTextoChange,
                )
            }

            Spacer(Modifier.height(24.dp))

            SeccionAjustes(titulo = stringResource(R.string.ajustes_seccion_cuenta)) {
                CerrarSesionOpcion(onCerrarSesion = onCerrarSesion)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Título violeta en mayúsculas y, debajo, una tarjeta con las opciones de esa sección. */
@Composable
private fun SeccionAjustes(titulo: String, contenido: @Composable () -> Unit) {
    Text(
        text = titulo,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface),
    ) {
        contenido()
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AjustesScreenClaroPreview() {
    GestorDeEventosTheme(darkTheme = false) {
        AjustesContenido(
            modoTema = ModoTema.SISTEMA,
            onModoTemaChange = {},
            escalaTexto = EscalaTexto.NORMAL,
            onEscalaTextoChange = {},
            onBackClick = {},
            onCerrarSesion = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AjustesScreenOscuroPreview() {
    GestorDeEventosTheme(darkTheme = true) {
        AjustesContenido(
            modoTema = ModoTema.OSCURO,
            onModoTemaChange = {},
            escalaTexto = EscalaTexto.GRANDE,
            onEscalaTextoChange = {},
            onBackClick = {},
            onCerrarSesion = {},
        )
    }
}
