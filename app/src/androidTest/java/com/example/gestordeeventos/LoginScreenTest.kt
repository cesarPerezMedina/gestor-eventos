package com.example.gestordeeventos

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.ui.auth.LoginScreen
import com.example.gestordeeventos.ui.theme.GestorDeEventosTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de interfaz de la pantalla de Login (Issue 19). Necesitan emulador o móvil.
 * Comprueban lo que ve y hace el usuario; la lógica del login (validación, credenciales) se prueba en
 * `LoginViewModelTest` y `AuthRepositorioTest`.
 */
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val regla = createComposeRule()

    private fun mostrarPantalla(
        onLoginClick: (String, String) -> Unit = { _, _ -> },
        onForgotPasswordClick: () -> Unit = {},
        onRegisterClick: () -> Unit = {}
    ) {
        regla.setContent {
            GestorDeEventosTheme {
                LoginScreen(
                    onLoginClick = onLoginClick,
                    onForgotPasswordClick = onForgotPasswordClick,
                    onRegisterClick = onRegisterClick
                )
            }
        }
    }

    /** Los dos campos de texto de la pantalla, en orden: correo y contraseña. */
    private fun escribirCorreo(texto: String) = regla.onAllNodes(hasSetTextAction())[0].performTextInput(texto)

    private fun escribirContrasena(texto: String) = regla.onAllNodes(hasSetTextAction())[1].performTextInput(texto)

    @Test
    fun muestraTitulosCamposYEnlaces() {
        mostrarPantalla()

        regla.onNodeWithText("Gestor de Eventos").assertIsDisplayed()
        regla.onNodeWithText("Correo electrónico").assertIsDisplayed()
        regla.onNodeWithText("Contraseña").assertIsDisplayed()
        regla.onNodeWithText("¿Has olvidado tu contraseña?").assertIsDisplayed()
        regla.onNodeWithText("Iniciar sesión").assertIsDisplayed()
        regla.onNodeWithText("¿No tienes una cuenta?").assertIsDisplayed()
        regla.onNodeWithText("Regístrate").assertIsDisplayed()
    }

    @Test
    fun tieneDosCamposDeTexto() {
        mostrarPantalla()

        regla.onAllNodes(hasSetTextAction()).assertCountEquals(2)
    }

    @Test
    fun alPulsarIniciarSesionEntregaElCorreoYLaContrasenaEscritos() {
        var recibido: Pair<String, String>? = null
        mostrarPantalla(onLoginClick = { correo, clave -> recibido = correo to clave })

        escribirCorreo("ana@correo.com")
        escribirContrasena("Clave#2026")
        regla.onNodeWithText("Iniciar sesión").performClick()

        assertEquals("ana@correo.com" to "Clave#2026", recibido)
    }

    @Test
    fun elCorreoSeEntregaSinEspaciosAlrededor() {
        var correoRecibido: String? = null
        mostrarPantalla(onLoginClick = { correo, _ -> correoRecibido = correo })

        escribirCorreo("  ana@correo.com  ")
        escribirContrasena("Clave#2026")
        regla.onNodeWithText("Iniciar sesión").performClick()

        assertEquals("ana@correo.com", correoRecibido)
    }

    @Test
    fun conLosCamposVaciosEntregaTextosVacios() {
        var recibido: Pair<String, String>? = null
        mostrarPantalla(onLoginClick = { correo, clave -> recibido = correo to clave })

        regla.onNodeWithText("Iniciar sesión").performClick()

        // La pantalla no valida: eso lo hace LoginViewModel (ver LoginViewModelTest).
        assertEquals("" to "", recibido)
    }

    @Test
    fun noLlamaAlLoginHastaQueSePulsaElBoton() {
        var recibido: Pair<String, String>? = null
        mostrarPantalla(onLoginClick = { correo, clave -> recibido = correo to clave })

        escribirCorreo("ana@correo.com")
        escribirContrasena("Clave#2026")

        assertNull(recibido)
    }

    @Test
    fun elEnlaceDeContrasenaOlvidadaAvisa() {
        var pulsado = false
        mostrarPantalla(onForgotPasswordClick = { pulsado = true })

        regla.onNodeWithText("¿Has olvidado tu contraseña?").performClick()

        assertTrue(pulsado)
    }

    @Test
    fun elEnlaceDeRegistroAvisa() {
        var pulsado = false
        mostrarPantalla(onRegisterClick = { pulsado = true })

        regla.onNodeWithText("Regístrate").performClick()

        assertTrue(pulsado)
    }

    @Test
    fun elOjoMuestraYOcultaLaContrasena() {
        mostrarPantalla()

        regla.onNodeWithContentDescription("Mostrar contraseña").assertIsDisplayed()
        regla.onNodeWithContentDescription("Mostrar contraseña").performClick()

        regla.onNodeWithContentDescription("Ocultar contraseña").assertIsDisplayed()
        regla.onNodeWithContentDescription("Ocultar contraseña").performClick()

        regla.onNodeWithContentDescription("Mostrar contraseña").assertIsDisplayed()
    }

    @Test
    fun alRotarLaPantallaSeConservaLoEscrito() {
        val restauracion = StateRestorationTester(regla)
        restauracion.setContent {
            GestorDeEventosTheme {
                LoginScreen(onLoginClick = { _, _ -> }, onForgotPasswordClick = {}, onRegisterClick = {})
            }
        }

        escribirCorreo("ana@correo.com")
        restauracion.emulateSavedInstanceStateRestore()

        regla.onNodeWithText("ana@correo.com").assertIsDisplayed()
    }
}
