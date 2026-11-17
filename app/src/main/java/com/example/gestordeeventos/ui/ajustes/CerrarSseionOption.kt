package com.example.gestordeeventos.ui.ajustes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Rojo = Color(0xFFD32F2F)

@Composable
fun CerrarSesionOpcion(
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { mostrarDialogo = true }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null, tint = Rojo)
        Spacer(Modifier.size(12.dp))
        Text("Cerrar sesión", color = Rojo, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }

    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres salir de tu cuenta?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogo = false
                    onCerrarSesion()
                }) { Text("Cerrar sesión", color = Rojo) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CerrarSesionOpcionPreview() {
    CerrarSesionOpcion(onCerrarSesion = {})
}