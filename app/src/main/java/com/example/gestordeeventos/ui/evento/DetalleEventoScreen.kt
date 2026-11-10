package com.example.gestordeeventos.ui.evento

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.gestordeeventos.R
import com.example.gestordeeventos.ui.componentes.PantallaProvisional

@Composable
fun DetalleEventoScreen(eventoId: Long, modifier: Modifier = Modifier) {
    PantallaProvisional(
        titulo = stringResource(R.string.evento_detalle_titulo, eventoId),
        modifier = modifier
    )
}
