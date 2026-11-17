package com.example.gestordeeventos.ui.auth



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Colores de la pantalla (puedes moverlos a tu Theme / Color.kt) ---
private val Violet = Color(0xFF6D3FE8)
private val GradientStart = Color(0xFF7C3AED)
private val GradientEnd = Color(0xFF4F46E5)
private val InkDark = Color(0xFF0F1222)
private val LabelColor = Color(0xFF1F2333)
private val FieldBackground = Color(0xFFF5F6FB)
private val FieldBorder = Color(0xFFE2E5F0)
private val IconGray = Color(0xFF8A90A6)
private val LogoTile = Color(0xFFEDEDF0)
private val PinPurple = Color(0xFF6D28D9)

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
    estado: LoginUiState = LoginUiState(),
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Resplandor suave lavanda del fondo
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(Color(0x267C3AED), Color.Transparent),
                            center = Offset(size.width, size.height * 0.05f),
                            radius = size.width * 0.9f,
                        ),
                    )
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(Color(0x1A4F46E5), Color.Transparent),
                            center = Offset(0f, size.height * 0.45f),
                            radius = size.width * 0.9f,
                        ),
                    )
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(32.dp))

                // Logo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x33000000))
                        .background(LogoTile, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    EventPinLogo(modifier = Modifier.size(48.dp))
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Gestor de Eventos",
                    color = InkDark,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Deportivos",
                    color = Violet,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(40.dp))

                // Correo
                Text(
                    text = "Correo electrónico",
                    color = LabelColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                LoginTextField(
                    value = email,
                    onValueChange = { email = it },
                    isError = estado.errorIdentificador != null,
                    placeholder = "correo@ejemplo.com",
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = IconGray)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )
                ErrorAuth(estado.errorIdentificador)

                Spacer(Modifier.height(20.dp))

                // Contraseña
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Contraseña",
                        color = LabelColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "¿Has olvidado tu contraseña?",
                        color = Violet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onForgotPasswordClick),
                    )
                }
                Spacer(Modifier.height(8.dp))
                LoginTextField(
                    value = password,
                    onValueChange = { password = it },
                    isError = estado.errorClave != null,
                    placeholder = "••••••••",
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = IconGray)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = IconGray,
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                )
                ErrorAuth(estado.errorClave)
                ErrorAuth(estado.errorGeneral)

                Spacer(Modifier.height(28.dp))

                // Botón
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(18.dp),
                            ambientColor = GradientStart,
                            spotColor = GradientStart,
                        )
                        .background(
                            Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
                            RoundedCornerShape(18.dp),
                        )
                        .alpha(if (estado.cargando) 0.6f else 1f)
                        .clickable(enabled = !estado.cargando) { onLoginClick(email.trim(), password) },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (estado.cargando) "Iniciando sesión…" else "Iniciar sesión",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Spacer(Modifier.size(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // Pie: registro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "¿No tienes una cuenta?",
                    color = Color(0xFF4A5068),
                    fontSize = 13.sp,
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = "Regístrate",
                    color = Violet,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onRegisterClick),
                )
            }
        }
    }
}

@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        isError = isError,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        placeholder = { Text(placeholder, color = IconGray, fontSize = 15.sp) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FieldBackground,
            unfocusedContainerColor = FieldBackground,
            focusedBorderColor = Violet,
            unfocusedBorderColor = FieldBorder,
            cursorColor = Violet,
        ),
    )
}

/** Logo (pin con cuadrícula) dibujado con Canvas, no necesita ningún recurso. */
@Composable
private fun EventPinLogo(modifier: Modifier = Modifier) {
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
        drawPath(pin, PinPurple)
        drawCircle(Color.White, radius = 15f * s, center = Offset(32f * s, 25f * s))

        val cell = Size(6.5f * s, 6.5f * s)
        val corner = CornerRadius(2f * s)
        fun square(x: Float, y: Float, alpha: Float) = drawRoundRect(
            color = PinPurple.copy(alpha = alpha),
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
private fun LoginScreenPreview() {
    LoginScreen(
        onLoginClick = { _, _ -> },
        onForgotPasswordClick = {},
        onRegisterClick = {},
    )
}
