package com.example.gestordeeventos.ui.componentes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Pantalla vacía con solo un título. Sirve de relleno hasta que se implemente cada pantalla real:
 * quien la haga solo tiene que sustituir el contenido de su `...Screen`.
 */
@Composable
fun PantallaProvisional(titulo: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = titulo, style = MaterialTheme.typography.titleLarge)
    }
}
