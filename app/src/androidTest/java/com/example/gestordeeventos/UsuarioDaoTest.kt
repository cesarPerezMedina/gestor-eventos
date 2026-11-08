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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Comprueba las consultas de UsuarioDao contra una base de datos real en memoria (necesita emulador o móvil). */
@RunWith(AndroidJUnit4::class)
class UsuarioDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        runBlocking {
            db.usuarioDao().insertar(Usuario(nombreUsuario = "Ana_23", correo = "ana@correo.com", claveHash = "hash"))
        }
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    @Test
    fun buscaPorCorreoSinDistinguirMayusculas() = runBlocking {
        assertEquals("Ana_23", db.usuarioDao().buscarPorCorreoONombreUsuario("ANA@correo.COM")?.nombreUsuario)
    }

    @Test
    fun buscaPorNombreDeUsuarioSinDistinguirMayusculas() = runBlocking {
        assertEquals("ana@correo.com", db.usuarioDao().buscarPorCorreoONombreUsuario("ana_23")?.correo)
    }

    @Test
    fun devuelveNullSiNoExiste() = runBlocking {
        assertNull(db.usuarioDao().buscarPorCorreoONombreUsuario("nadie"))
    }
}
