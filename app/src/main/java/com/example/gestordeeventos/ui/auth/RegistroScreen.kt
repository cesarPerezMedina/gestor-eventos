
package com.example.gestordeeventos.ui.auth

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
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
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
private val RegViolet = Color(0xFF5B0FBF)
private val RegGradientStart = Color(0xFF8B5CF6)
private val RegGradientEnd = Color(0xFF5B0FBF)
private val RegInkDark = Color(0xFF0F1222)
private val RegLabel = Color(0xFF1F2333)
private val RegSubtitle = Color(0xFF3F465C)
private val RegFieldBackground = Color(0xFFEEF0FF)
private val RegIconGray = Color(0xFF7D8397)
private val RegHint = Color(0xFF6B7186)
private val RegLogoTile = Color(0xFFEDEDF0)
private val RegPin = Color(0xFF6D28D9)

@Composable
fun RegistroScreen(
    onRegisterClick: (username: String, email: String, password: String, repeatPassword: String) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var repeatVisible by rememberSaveable { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxSize(), color = Color(0xFFF7F7FC)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        Brush.radialGradient(
                            colors = listOf(Color(0x1F8B5CF6), Color.Transparent),
                            center = Offset(size.width * 0.5f, 0f),
                            radius = size.width,
                        ),
                    )
                }
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(24.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(10.dp, RoundedCornerShape(18.dp), spotColor = RegGradientStart)
                    .background(RegLogoTile, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                RegistroPinLogo(modifier = Modifier.size(44.dp))
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Crear cuenta",
                color = RegInkDark,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Únete y empieza a organizar tus partidos y eventos deportivos.",
                color = RegSubtitle,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
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
                FieldLabel("Nombre de usuario")
                RegistroTextField(
                    value = username,
                    onValueChange = { username = it },
                    placeholder = "Ej: alex_martinez",
                    leadingIcon = {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = RegIconGray)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )

                Spacer(Modifier.height(18.dp))

                FieldLabel("Correo electrónico")
                RegistroTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "alex.martinez@ejemplo.com",
                    leadingIcon = {
                        Icon(Icons.Outlined.Email, contentDescription = null, tint = RegIconGray)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next,
                    ),
                )

                Spacer(Modifier.height(18.dp))

                FieldLabel("Contraseña")
                RegistroTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "••••••••",
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = RegIconGray)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = RegIconGray,
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
                )

                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                        tint = RegHint,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "Mínimo 8 caracteres, con letras y números",
                        color = RegHint,
                        fontSize = 12.sp,
                    )
                }

                Spacer(Modifier.height(18.dp))

                FieldLabel("Repetir contraseña")
                RegistroTextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    placeholder = "••••••••",
                    leadingIcon = {
                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = RegIconGray)
                    },
                    trailingIcon = {
                        IconButton(onClick = { repeatVisible = !repeatVisible }) {
                            Icon(
                                imageVector = if (repeatVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                contentDescription = if (repeatVisible) "Ocultar contraseña" else "Mostrar contraseña",
                                tint = RegIconGray,
                            )
                        }
                    },
                    visualTransformation = if (repeatVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                )

                Spacer(Modifier.height(26.dp))

                // Botón
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = RegGradientStart,
                            spotColor = RegGradientStart,
                        )
                        .background(
                            Brush.horizontalGradient(listOf(RegGradientStart, RegGradientEnd)),
                            RoundedCornerShape(16.dp),
                        )
                        .clickable {
                            onRegisterClick(username.trim(), email.trim(), password, repeatPassword)
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Crear cuenta",
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
            }

            Spacer(Modifier.height(24.dp))

            // Pie: ir al login
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "¿Ya tienes cuenta?",
                    color = RegLabel,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = "Inicia sesión",
                    color = RegViolet,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onLoginClick),
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = RegLabel,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
    )
}

@Composable
private fun RegistroTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text(placeholder, color = RegIconGray, fontSize = 15.sp) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = RegFieldBackground,
            unfocusedContainerColor = RegFieldBackground,
            focusedBorderColor = RegGradientStart,
            unfocusedBorderColor = Color.Transparent,
            cursorColor = RegViolet,
        ),
    )
}

/** Logo (pin con cuadrícula) dibujado con Canvas, no necesita ningún recurso. */
@Composable
private fun RegistroPinLogo(modifier: Modifier = Modifier) {
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
        drawPath(pin, RegPin)
        drawCircle(Color.White, radius = 15f * s, center = Offset(32f * s, 25f * s))

        val cell = Size(6.5f * s, 6.5f * s)
        val corner = CornerRadius(2f * s)
        fun square(x: Float, y: Float, alpha: Float) = drawRoundRect(
            color = RegPin.copy(alpha = alpha),
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
private fun RegistroScreenPreview() {
    RegistroScreen(
        onRegisterClick = { _, _, _, _ -> },
        onLoginClick = {},
    )
}