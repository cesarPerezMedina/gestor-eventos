package com.example.gestordeeventos.ui.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Colores de la pantalla (puedes moverlos a tu Theme / Color.kt) ---
private val RecViolet = Color(0xFF6D28D9)
private val RecInkDark = Color(0xFF0F1222)
private val RecLabel = Color(0xFF1F2333)
private val RecSubtitle = Color(0xFF3F465C)
private val RecFieldBackground = Color(0xFFEEF0FF)
private val RecIconGray = Color(0xFF7D8397)
private val RecHint = Color(0xFF6B7186)
private val RecPageBackground = Color(0xFFF7F7FD)
private val RecLogoInner = Color(0xFFD9D9DE)

@Composable
fun RecuperarScreen(
    onSendClick: (email: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSupportClick: () -> Unit = {},
) {
    var email by rememberSaveable { mutableStateOf("") }

    Surface(modifier = modifier.fillMaxSize(), color = RecPageBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(Color(0x268B5CF6), Color.Transparent),
                            center = Offset(size.width * 0.5f, size.height * 0.12f),
                            radius = size.width * 0.8f,
                        ),
                    )
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(12.dp))

            // Barra superior: botón atrás + chip "ACCESO SEGURO"
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(42.dp)
                        .background(RecFieldBackground, CircleShape)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = RecInkDark,
                    )
                }
                Row(
                    modifier = Modifier
                        .background(RecFieldBackground, RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = RecViolet,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "ACCESO SEGURO",
                        color = RecViolet,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // Logo con insignia de correo
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF8B5CF6))
                        .background(Color.White, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(RecLogoInner, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        RecuperarPinLogo(modifier = Modifier.size(48.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 10.dp, y = 10.dp)
                        .size(30.dp)
                        .background(RecViolet, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Recuperar contraseña",
                color = RecInkDark,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Escribe tu correo y te enviaremos un enlace para restablecerla.",
                color = RecSubtitle,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
            )

            Spacer(Modifier.height(24.dp))

            // Tarjeta con el formulario
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Text(
                    text = "Correo electrónico",
                    color = RecLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("ejemplo@correo.com", color = RecIconGray, fontSize = 15.sp) },
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = RecIconGray)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = RecFieldBackground,
                        unfocusedContainerColor = RecFieldBackground,
                        focusedBorderColor = RecViolet,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = RecViolet,
                    ),
                )

                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = RecHint,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(14.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "Te llegará un código y enlace temporal válido por 15 minutos.",
                        color = RecHint,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                    )
                }

                Spacer(Modifier.height(22.dp))

                // Botón
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = RecViolet,
                            spotColor = RecViolet,
                        )
                        .background(RecViolet, RoundedCornerShape(16.dp))
                        .clickable { onSendClick(email.trim()) },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Enviar enlace",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Dos tarjetas informativas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                InfoTile(
                    icon = Icons.Outlined.EmojiEvents,
                    title = "Tus torneos",
                    text = "Tus registros y fixtures seguirán intactos.",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
                InfoTile(
                    icon = Icons.Outlined.Lock,
                    title = "Cifrado total",
                    text = "Protección de datos conforme a estándares federativos.",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }

            Spacer(Modifier.height(28.dp))

            // Volver al inicio de sesión
            Row(
                modifier = Modifier.clickable(onClick = onBackClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = RecViolet,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Volver al inicio de sesión",
                    color = RecViolet,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "¿Necesitas ayuda adicional?",
                    color = RecHint,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = "Contactar soporte",
                    color = RecViolet,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onSupportClick),
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoTile(
    icon: ImageVector,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(RecFieldBackground, RoundedCornerShape(16.dp))
            .padding(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFE1E4FB), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RecViolet,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            color = RecInkDark,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = text,
            color = RecSubtitle,
            fontSize = 13.sp,
            lineHeight = 18.sp,
        )
    }
}

/** Logo (pin con cuadrícula) dibujado con Canvas, no necesita ningún recurso. */
@Composable
private fun RecuperarPinLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.width / 64f
        val pin = Path().apply {
            moveTo(32f * s, 3f * s)
            cubicTo(18.7f * s, 3f * s, 9f * s, 13f * s, 9f * s, 25.5f * s)
            cubicTo(9f * s, 41f * s, 27f * s, 56f * s, 32f * s, 61f * s)
            cubicTo(37f * s, 56f * s, 55f * s, 41f * s, 55f * s, 25.5f * s)
            cubicTo(55f * s, 13f * s, 45.3f * s, 3f * s, 32f * s, 3f * s)
            close()
        }
        drawPath(pin, RecViolet)
        drawCircle(Color.White, radius = 15f * s, center = Offset(32f * s, 25f * s))

        val cell = Size(6.5f * s, 6.5f * s)
        val corner = CornerRadius(2f * s)
        fun square(x: Float, y: Float, alpha: Float) = drawRoundRect(
            color = RecViolet.copy(alpha = alpha),
            topLeft = Offset(x * s, y * s),
            size = cell,
            cornerRadius = corner,
        )
        square(24.5f, 17.5f, 1f)
        square(33f, 17.5f, 0.5f)
        square(24.5f, 26f, 0.5f)
        square(33f, 26f, 1f)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun RecuperarScreenPreview() {
    RecuperarScreen(
        onSendClick = {},
        onBackClick = {},
    )
}
