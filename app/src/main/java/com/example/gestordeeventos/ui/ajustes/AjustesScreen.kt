package com.example.gestordeeventos.ui.ajustes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.componentes.PantallaProvisional

@Composable
fun AjustesScreen(modifier: Modifier = Modifier) {
    PantallaProvisional(titulo = stringResource(R.string.nav_ajustes), modifier = modifier)
}
