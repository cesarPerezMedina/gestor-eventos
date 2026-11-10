package com.example.gestordeeventos.ui.buscar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.componentes.PantallaProvisional

@Composable
fun BuscarEventoScreen(modifier: Modifier = Modifier) {
    PantallaProvisional(titulo = stringResource(R.string.nav_buscar), modifier = modifier)
}
