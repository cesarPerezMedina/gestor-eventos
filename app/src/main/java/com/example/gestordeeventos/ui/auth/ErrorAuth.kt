package com.example.gestordeeventos.ui.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ColorError = Color(0xFFD32F2F)

/** Mensaje de error bajo un campo (o general). No dibuja nada si [mensaje] es null. */
@Composable
internal fun ErrorAuth(mensaje: String?, modifier: Modifier = Modifier) {
    if (mensaje != null) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = mensaje,
            color = ColorError,
            fontSize = 12.sp,
            modifier = modifier.fillMaxWidth()
        )
    }
}
