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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AjPagina = Color(0xFFF6F4FF)
private val AjTinta = Color(0xFF1B1B3A)
private val AjVioleta = Color(0xFF5B0FBF)
private val AjChip = Color(0xFFE6DDFB)

@Composable
fun AjustesScreen(
    onBackClick: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = AjPagina) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            // Cabecera: flecha de volver + título
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(AjChip, CircleShape)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = AjTinta,
                    )
                }
                Spacer(Modifier.size(14.dp))
                Text(
                    text = "Ajustes",
                    color = AjTinta,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "CUENTA",
                color = AjVioleta,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )

            // Tarjeta con las opciones de cuenta
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White),
            ) {
                CerrarSesionOpcion(onCerrarSesion = onCerrarSesion)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AjustesScreenPreview() {
    AjustesScreen(onBackClick = {}, onCerrarSesion = {})
}
