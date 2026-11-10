package com.example.gestordeeventos.ui.amigos

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.componentes.PantallaProvisional

@Composable
fun AmigosScreen(modifier: Modifier = Modifier) {
    PantallaProvisional(titulo = stringResource(R.string.nav_amigos), modifier = modifier)
}
