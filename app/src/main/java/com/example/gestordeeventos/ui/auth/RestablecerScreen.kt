package com.example.gestordeeventos.ui.auth


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// --- Colores de la pantalla (puedes moverlos a tu Theme / Color.kt) ---
private val RestViolet = Color(0xFF5B0FBF)
private val RestVioletSoft = Color(0xFF8B5CF6)
private val RestGradientStart = Color(0xFF4A0E8F)
private val RestGradientEnd = Color(0xFF6535BB)
private val RestInkDark = Color(0xFF1B1B3A)
private val RestLabel = Color(0xFF1F2333)
private val RestSubtitle = Color(0xFF4A5068)
private val RestFieldBackground = Color(0xFFEEF0FF)
private val RestDigitFilled = Color(0xFFDFE4FB)
private val RestFieldBorder = Color(0xFFC4C7DB)
private val RestIconGray = Color(0xFF7D8397)
private val RestPageBackground = Color(0xFFF6F4FF)
private val RestLogoInner = Color(0xFFD9D9DE)
private val RestChipBackground = Color(0xFFE6DDFB)
private val RestTrack = Color(0xFFE0E3F5)

private const val CODE_LENGTH = 6
private const val RESEND_SECONDS = 60

@Composable
fun RestablecerScreen(
    onSaveClick: (codigo: String, nueva: String, repetir: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onResendClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
) {
    var code by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var newVisible by rememberSaveable { mutableStateOf(false) }
    var repeatVisible by rememberSaveable { mutableStateOf(false) }
    var seconds by rememberSaveable { mutableIntStateOf(RESEND_SECONDS) }

    // Cuenta atrás para poder reenviar el código
    LaunchedEffect(seconds) {
        if (seconds > 0) {
            delay(1000)
            seconds -= 1
        }
    }

    val codeComplete = code.length == CODE_LENGTH
    val hasLength = newPassword.length >= 8
    val hasNumberOrSymbol = newPassword.any { it.isDigit() || !it.isLetterOrDigit() }
    val hasLetter = newPassword.any { it.isLetter() }
    val strength = (listOf(hasLength, hasNumberOrSymbol, hasLetter).count { it }) / 3f

    Surface(modifier = modifier.fillMaxSize(), color = RestPageBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(Color(0x338B5CF6), Color.Transparent),
                            center = Offset(0f, size.height * 0.05f),
                            radius = size.width * 1.1f,
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

            // Barra superior: botón atrás + chip "PASO 2 DE 2"
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(RestChipBackground, CircleShape)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = RestInkDark,
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(RestChipBackground, RoundedCornerShape(50))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(RestViolet, CircleShape),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = "PASO 2 DE 2",
                        color = RestViolet,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                    )
                }
            }

            Spacer(Modifier.height(26.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .shadow(12.dp, RoundedCornerShape(22.dp), spotColor = RestVioletSoft)
                    .background(Color.White, RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(RestLogoInner, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    RestPinLogo(modifier = Modifier.size(48.dp))
                }
            }

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Restablecer contraseña",
                color = RestInkDark,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Introduce el código de 6 dígitos que enviamos a tu correo y crea tu nueva contraseña para volver a tus partidos.",
                color = RestSubtitle,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(22.dp))

            // Tarjeta 1: código de verificación
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Email,
                            contentDescription = null,
                            tint = RestViolet,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = "Código de verificación",
                            color = RestInkDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "6 DÍGITOS",
                        color = RestViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp,
                    )
                }

                Spacer(Modifier.height(16.dp))

                CodeInput(code = code, onCodeChange = { code = it })

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = RestSubtitle,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.size(6.dp))
                        Text(text = "Reenviar en", color = RestSubtitle, fontSize = 13.sp)
                        Spacer(Modifier.size(4.dp))
                        Text(
                            text = "%02d:%02d".format(seconds / 60, seconds % 60),
                            color = RestInkDark,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Text(
                        text = "¿No lo recibiste? Reenviar",
                        color = if (seconds == 0) RestViolet else RestVioletSoft.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(enabled = seconds == 0) {
                            seconds = RESEND_SECONDS
                            onResendClick()
                        },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Tarjeta 2: nueva contraseña
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x1A000000))
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .padding(16.dp),
            ) {
                if (!codeComplete) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RestFieldBackground, RoundedCornerShape(12.dp))
                            .border(1.dp, RestFieldBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                tint = RestInkDark,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.size(10.dp))
                            Text(
                                text = "Introduce los 6 dígitos para desbloquear",
                                color = RestSubtitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        Icon(
                            imageVector = Icons.Outlined.HourglassEmpty,
                            contentDescription = null,
                            tint = RestSubtitle,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                }

                Text(
                    text = "Nueva contraseña",
                    color = RestLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                RestPasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = "Introduce tu contraseña",
                    enabled = codeComplete,
                    visible = newVisible,
                    onToggleVisible = { newVisible = !newVisible },
                    imeAction = ImeAction.Next,
                )

                Spacer(Modifier.height(18.dp))

                Text(
                    text = "Repetir nueva contraseña",
                    color = RestLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                RestPasswordField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    placeholder = "Confirma tu contraseña",
                    enabled = codeComplete,
                    visible = repeatVisible,
                    onToggleVisible = { repeatVisible = !repeatVisible },
                    imeAction = ImeAction.Done,
                )

                Spacer(Modifier.height(18.dp))

                // Barra de seguridad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(RestTrack, RoundedCornerShape(50)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(strength)
                            .height(6.dp)
                            .background(RestViolet, RoundedCornerShape(50)),
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    RequirementRow("Mínimo 8 caracteres", hasLength)
                    RequirementRow("Un número o símbolo", hasNumberOrSymbol)
                }
            }

            Spacer(Modifier.height(22.dp))

            // Botón
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(18.dp),
                        ambientColor = RestViolet,
                        spotColor = RestViolet,
                    )
                    .background(
                        Brush.horizontalGradient(listOf(RestGradientStart, RestGradientEnd)),
                        RoundedCornerShape(18.dp),
                    )
                    .clickable { onSaveClick(code, newPassword, repeatPassword) },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Guardar y entrar",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.size(10.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.clickable(onClick = onSupportClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = RestSubtitle,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "¿Problemas con el código? Contactar soporte",
                    color = RestSubtitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Seis casillas para el código. Un campo invisible encima recoge lo que se escribe. */
@Composable
private fun CodeInput(code: String, onCodeChange: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            repeat(CODE_LENGTH) { index ->
                val digit = code.getOrNull(index)?.toString() ?: ""
                val active = focused && index == code.length.coerceAtMost(CODE_LENGTH - 1)
                val shape = RoundedCornerShape(14.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .background(if (digit.isNotEmpty()) RestDigitFilled else RestFieldBackground, shape)
                        .border(1.dp, if (active) RestVioletSoft else Color.Transparent, shape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = digit,
                        color = RestInkDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        BasicTextField(
            value = code,
            onValueChange = { onCodeChange(it.filter(Char::isDigit).take(CODE_LENGTH)) },
            modifier = Modifier
                .matchParentSize()
                .alpha(0f)
                .onFocusChanged { focused = it.isFocused },
            singleLine = true,
            textStyle = TextStyle(color = Color.Transparent),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Next,
            ),
        )
    }
}

@Composable
private fun RestPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    enabled: Boolean,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    imeAction: ImeAction,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        enabled = enabled,
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text(placeholder, fontSize = 16.sp) },
        trailingIcon = {
            IconButton(onClick = onToggleVisible, enabled = enabled) {
                Icon(
                    imageVector = if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = RestFieldBackground,
            unfocusedContainerColor = RestFieldBackground,
            disabledContainerColor = RestFieldBackground.copy(alpha = 0.6f),
            focusedBorderColor = RestVioletSoft,
            unfocusedBorderColor = RestFieldBorder,
            disabledBorderColor = RestFieldBorder.copy(alpha = 0.7f),
            cursorColor = RestViolet,
            focusedPlaceholderColor = RestIconGray,
            unfocusedPlaceholderColor = RestIconGray,
            disabledPlaceholderColor = RestIconGray.copy(alpha = 0.7f),
            focusedTrailingIconColor = RestInkDark,
            unfocusedTrailingIconColor = RestInkDark,
            disabledTrailingIconColor = RestIconGray.copy(alpha = 0.7f),
        ),
    )
}

@Composable
private fun RequirementRow(text: String, met: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (met) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (met) RestViolet else RestIconGray,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(8.dp))
        Text(text = text, color = RestSubtitle, fontSize = 14.sp)
    }
}

/** Logo (pin con cuadrícula) dibujado con Canvas, no necesita ningún recurso. */
@Composable
private fun RestPinLogo(modifier: Modifier = Modifier) {
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
        drawPath(pin, Color(0xFF6D28D9))
        drawCircle(Color.White, radius = 15f * s, center = Offset(32f * s, 25f * s))

        val cell = Size(6.5f * s, 6.5f * s)
        val corner = CornerRadius(2f * s)
        fun square(x: Float, y: Float, alpha: Float) = drawRoundRect(
            color = Color(0xFF6D28D9).copy(alpha = alpha),
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
private fun RestablecerScreenPreview() {
    RestablecerScreen(
        onSaveClick = { _, _, _ -> },
        onBackClick = {},
    )
}