package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.Usuario
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Comprueba la consulta real de búsqueda de usuarios (Issue 31) sobre una base de datos en memoria. */
@RunWith(AndroidJUnit4::class)
class BusquedaUsuariosDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    private suspend fun crear(nombre: String) {
        db.usuarioDao().insertar(Usuario(nombreUsuario = nombre, correo = "$nombre@correo.com", claveHash = "hash"))
    }

    private suspend fun buscar(texto: String, miCorreo: String = "yo@correo.com", limite: Int = 20) =
        db.usuarioDao().buscarPorNombreUsuarioParcial(texto, miCorreo, limite).map { it.nombreUsuario }

    @Test
    fun encuentraPorParteDelNombreSinDistinguirMayusculas() = runBlocking {
        crear("ana_23")
        crear("juana")
        crear("luis_7")
        assertEquals(listOf("ana_23", "juana"), buscar("ANA"))
    }

    @Test
    fun elGuionBajoSeBuscaTalCualYNoComoComodin() = runBlocking {
        crear("ana_23")
        crear("juana")
        assertEquals(listOf("ana_23"), buscar("a_"))
    }

    @Test
    fun noDevuelveAlUsuarioQueBusca() = runBlocking {
        crear("ana_23")
        crear("juana")
        assertEquals(listOf("juana"), buscar("ana", miCorreo = "ana_23@correo.com"))
    }

    @Test
    fun respetaElLimiteDeResultados() = runBlocking {
        (1..5).forEach { crear("dep_0$it") }
        assertEquals(3, buscar("dep", limite = 3).size)
    }
}
