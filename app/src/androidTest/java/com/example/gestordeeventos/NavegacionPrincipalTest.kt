package com.example.gestordeeventos

import android.content.Context
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.repositorio.AuthRepositorio
import com.example.gestordeeventos.data.repositorio.ResultadoRegistro
import com.example.gestordeeventos.data.repositorio.SesionRepositorio
import com.example.gestordeeventos.util.SesionManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Pruebas de integración de la navegación principal (Issue 27). Necesitan emulador o móvil.
 *
 * Arrancan la app de verdad (`MainActivity` + `AppNavHost`) sobre la base de datos local, con un usuario
 * de prueba, y recorren el flujo como lo haría una persona: login, menú inferior, botón "+", Ajustes y
 * cerrar sesión. La lógica de cada pantalla se prueba aparte (`LoginViewModelTest`, `LoginScreenTest`...).
 *
 * Para no romperse cuando otras Issues rediseñen las pantallas, solo se comprueban textos que ya son
 * estables (el saludo de Inicio, el título de Amigos, el de Crear evento y los de Ajustes). En Buscar y
 * Eventos solo se comprueba que se sale de Inicio y que el menú sigue visible.
 *
 * Antes de cada prueba se vacía la base de datos y se borra la sesión, así que no dependen unas de otras.
 */
@RunWith(AndroidJUnit4::class)
class NavegacionPrincipalTest {

    @get:Rule
    val regla = createEmptyComposeRule()

    private val contexto: Context = ApplicationProvider.getApplicationContext()
    private var escenario: ActivityScenario<MainActivity>? = null

    @Before
    fun prepararEstadoLimpio() {
        // El archivo "sesion" lo usan SesionManager (correo) y SesionRepositorio (id de usuario)
        contexto.getSharedPreferences("sesion", Context.MODE_PRIVATE).edit().clear().commit()
        AppDatabase.obtener(contexto).clearAllTables()
    }

    @After
    fun cerrarApp() {
        escenario?.close()
        escenario = null
    }

    // ---------- Arranque y sesión ----------

    @Test
    fun sinSesionLaAppArrancaEnElLoginSinMenuInferior() {
        crearUsuario(conSesion = false)
        abrirApp()

        esperarTexto("Iniciar sesión")
        regla.onNodeWithText("Regístrate").assertIsDisplayed()
        assertFalse(hayMenuInferior())
    }

    @Test
    fun conSesionGuardadaLaAppArrancaEnInicioConElNombreReal() {
        crearUsuario(conSesion = true)
        abrirApp()

        esperarInicio() // incluye el nombre del usuario de la sesión, no uno provisional
        assertTrue(hayMenuInferior())
    }

    @Test
    fun loginCorrectoLlevaAInicioYAtrasYaNoVuelveAlLogin() {
        crearUsuario(conSesion = false)
        abrirApp()

        iniciarSesionPorLaPantalla()
        assertTrue(hayMenuInferior())

        // Inicio es ahora la primera pantalla: "atrás" sale de la app en lugar de volver al Login
        comprobarQueAtrasSaleDeLaApp()
    }

    // ---------- Menú inferior ----------

    @Test
    fun elMenuRecorreLasCuatroSeccionesYVuelveAInicio() {
        crearUsuario(conSesion = true)
        abrirApp()
        esperarInicio()

        pulsarMenu("Buscar")
        comprobarQueSeHaSalidoDeInicio()
        assertTrue(hayMenuInferior())

        pulsarMenu("Eventos")
        comprobarQueSeHaSalidoDeInicio()
        assertTrue(hayMenuInferior())

        pulsarMenu("Amigos")
        regla.onNodeWithText(TITULO_AMIGOS).assertIsDisplayed()

        pulsarMenu("Inicio")
        comprobarQueEstaEnInicio()
    }

    @Test
    fun atrasDesdeCualquierSeccionVuelveAInicio() {
        crearUsuario(conSesion = true)
        abrirApp()
        esperarInicio()

        // Las secciones no se apilan: tras Buscar, Eventos y Amigos, "atrás" va a Inicio y no a Eventos
        pulsarMenu("Buscar")
        pulsarMenu("Eventos")
        pulsarMenu("Amigos")
        regla.onNodeWithText(TITULO_AMIGOS).assertIsDisplayed()

        Espresso.pressBackUnconditionally()
        regla.waitForIdle()

        comprobarQueEstaEnInicio()
    }

    @Test
    fun pulsarDosVecesLaMismaSeccionNoLaApila() {
        crearUsuario(conSesion = true)
        abrirApp()
        esperarInicio()

        pulsarMenu("Amigos")
        pulsarMenu("Amigos")
        regla.onNodeWithText(TITULO_AMIGOS).assertIsDisplayed()

        Espresso.pressBackUnconditionally()
        regla.waitForIdle()

        comprobarQueEstaEnInicio()
    }

    // ---------- Botón "+" (crear evento) ----------

    @Test
    fun elBotonCrearAbreCrearEventoSinMenuYVolverRegresaAInicio() {
        crearUsuario(conSesion = true)
        abrirApp()
        esperarInicio()

        pulsarCrear()
        regla.onNodeWithText(TITULO_CREAR).assertIsDisplayed()
        assertFalse(hayMenuInferior())

        regla.onNodeWithContentDescription("Volver").performClick()
        comprobarQueEstaEnInicio()
    }

    @Test
    fun crearDesdeAmigosYVolverRegresaAAmigos() {
        crearUsuario(conSesion = true)
        abrirApp()
        esperarInicio()

        pulsarMenu("Amigos")
        regla.onNodeWithText(TITULO_AMIGOS).assertIsDisplayed()

        pulsarCrear()
        regla.onNodeWithText(TITULO_CREAR).assertIsDisplayed()

        regla.onNodeWithContentDescription("Volver").performClick()
        regla.onNodeWithText(TITULO_AMIGOS).assertIsDisplayed()
        assertTrue(hayMenuInferior())
    }

    // ---------- Ajustes y cerrar sesión ----------

    @Test
    fun ajustesSeAbreDesdeInicioSinMenuYVolverRegresa() {
        crearUsuario(conSesion = true)
        abrirApp()
        esperarInicio()

        regla.onNodeWithContentDescription("Ajustes").performClick()
        regla.onNodeWithText("Ajustes").assertIsDisplayed()
        regla.onNodeWithText("Cerrar sesión").assertIsDisplayed()
        assertFalse(hayMenuInferior())

        regla.onNodeWithContentDescription("Volver").performClick()
        comprobarQueEstaEnInicio()
    }

    @Test
    fun cancelarElDialogoDeCerrarSesionMantieneLaSesion() {
        crearUsuario(conSesion = true)
        abrirApp()
        esperarInicio()

        regla.onNodeWithContentDescription("Ajustes").performClick()
        regla.onNodeWithText("Cerrar sesión").performClick()
        regla.onNodeWithText("Cancelar").performClick()

        regla.onNodeWithText("Cerrar sesión").assertIsDisplayed() // sigue en Ajustes
        assertTrue(SesionRepositorio.crear(contexto).haySesionGuardada())
    }

    @Test
    fun cerrarSesionLlevaAlLoginOlvidaLaSesionYAtrasNoVuelveALaApp() {
        crearUsuario(conSesion = true)
        abrirApp()
        esperarInicio()

        cerrarSesionDesdeAjustes()

        esperarTexto("Iniciar sesión")
        assertFalse(hayMenuInferior())
        assertFalse(SesionRepositorio.crear(contexto).haySesionGuardada())
        assertFalse(SesionManager(contexto).haySesion())

        // El historial se ha borrado: "atrás" no puede volver a Inicio
        comprobarQueAtrasSaleDeLaApp()
    }

    @Test
    fun trasCerrarSesionLaAppReabiertaArrancaEnElLogin() {
        crearUsuario(conSesion = true)
        abrirApp()
        esperarInicio()
        cerrarSesionDesdeAjustes()
        esperarTexto("Iniciar sesión")

        cerrarApp()
        abrirApp()

        esperarTexto("Iniciar sesión")
        assertFalse(hayMenuInferior())
    }

    // ---------- Preparación ----------

    /** Crea el usuario de prueba en la base de datos y, si se pide, deja su sesión ya iniciada. */
    private fun crearUsuario(conSesion: Boolean) {
        val usuarioDao = AppDatabase.obtener(contexto).usuarioDao()
        val resultado = runBlocking { AuthRepositorio(usuarioDao).registrar(NOMBRE, CORREO, CLAVE) }
        check(resultado is ResultadoRegistro.Exito) { "No se pudo crear el usuario de prueba" }
        if (conSesion) {
            SesionRepositorio.crear(contexto).iniciar(resultado.usuario.id)
            SesionManager(contexto).guardarSesion(resultado.usuario.correo)
        }
    }

    private fun abrirApp() {
        escenario = ActivityScenario.launch(MainActivity::class.java)
    }

    // ---------- Acciones ----------

    private fun iniciarSesionPorLaPantalla() {
        esperarTexto("Iniciar sesión")
        regla.onAllNodes(hasSetTextAction())[0].performTextInput(CORREO)
        regla.onAllNodes(hasSetTextAction())[1].performTextInput(CLAVE)
        regla.onNodeWithText("Iniciar sesión").performClick()
        esperarInicio()
    }

    /**
     * Pulsa una opción del menú inferior de la app. En Inicio puede haber otro menú dibujado dentro de la
     * propia pantalla, así que entre las coincidencias se elige la más baja, que es la de `AppNavHost`.
     */
    private fun pulsarMenu(etiqueta: String) {
        regla.onAllNodes(hasText(etiqueta) and hasClickAction()).laDeMasAbajo().performClick()
        regla.waitForIdle()
    }

    private fun pulsarCrear() {
        regla.onAllNodesWithContentDescription("Crear evento").laDeMasAbajo().performClick()
        regla.waitForIdle()
    }

    private fun cerrarSesionDesdeAjustes() {
        regla.onNodeWithContentDescription("Ajustes").performClick()
        regla.onNodeWithText("Cerrar sesión").performClick()
        // Confirmar en el diálogo (el botón de confirmar, no la fila de Ajustes que queda detrás)
        regla.onNode(hasText("Cerrar sesión") and hasClickAction() and hasAnyAncestor(isDialog())).performClick()
    }

    // ---------- Comprobaciones ----------

    /** true si se ve el menú inferior (la opción "Amigos" solo existe como botón en el menú). */
    private fun hayMenuInferior(): Boolean =
        regla.onAllNodes(hasText("Amigos") and hasClickAction()).fetchSemanticsNodes().isNotEmpty()

    private fun comprobarQueEstaEnInicio() {
        regla.onNode(hasText(SALUDO_INICIO, substring = true)).assertIsDisplayed()
    }

    private fun comprobarQueSeHaSalidoDeInicio() {
        regla.onAllNodes(hasText("Hola,", substring = true)).assertCountEquals(0)
    }

    /**
     * Pulsa "atrás" y comprueba que la app deja de estar en primer plano (se cierra o pasa a segundo plano).
     * Si "atrás" volviera a otra pantalla de la app, la actividad seguiría activa y la prueba fallaría.
     */
    private fun comprobarQueAtrasSaleDeLaApp() {
        val actividad = checkNotNull(escenario) { "La app no está abierta" }
        Espresso.pressBackUnconditionally()
        val limite = System.currentTimeMillis() + TIEMPO_ESPERA_MS
        while (actividad.state == Lifecycle.State.RESUMED) {
            check(System.currentTimeMillis() < limite) { "\"Atrás\" no ha sacado al usuario de la app" }
            Thread.sleep(100)
        }
    }

    // ---------- Esperas y utilidades ----------

    private fun esperarTexto(texto: String, subcadena: Boolean = false) {
        regla.waitUntil(TIEMPO_ESPERA_MS) {
            regla.onAllNodes(hasText(texto, substring = subcadena)).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /** Espera a Inicio: el saludo lleva el nombre del usuario, así que también prueba la sesión real. */
    private fun esperarInicio() = esperarTexto(SALUDO_INICIO, subcadena = true)

    /** Entre varios nodos iguales, el que está más abajo en pantalla. */
    private fun SemanticsNodeInteractionCollection.laDeMasAbajo(): SemanticsNodeInteraction {
        val nodos = fetchSemanticsNodes()
        val indice = nodos.indices.maxByOrNull { nodos[it].boundsInRoot.top }
            ?: error("No se ha encontrado ningún elemento")
        return get(indice)
    }

    private companion object {
        const val NOMBRE = "ana_23"
        const val CORREO = "ana@correo.com"
        const val CLAVE = "Clave#2026"
        const val TIEMPO_ESPERA_MS = 10_000L

        // Textos que identifican cada pantalla. Si alguna Issue los cambia, se actualizan aquí.
        const val SALUDO_INICIO = "Hola, $NOMBRE"
        const val TITULO_AMIGOS = "Comunidad & Amigos"
        const val TITULO_CREAR = "Crear evento"
    }
}
