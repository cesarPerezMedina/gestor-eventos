package com.example.gestordeeventos

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestordeeventos.data.local.AppDatabase
import com.example.gestordeeventos.data.model.Amistad
import com.example.gestordeeventos.data.model.Usuario
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Comprueba la consulta `UsuarioDao.buscarUsuarios` contra una base de datos real en memoria (necesita emulador). */
@RunWith(AndroidJUnit4::class)
class BusquedaUsuariosDaoTest {

    private lateinit var db: AppDatabase
    private var yo = 0L

    @Before
    fun crearBaseDeDatos() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        yo = crear("yo_mismo")
    }

    @After
    fun cerrarBaseDeDatos() {
        db.close()
    }

    private fun crear(nombre: String): Long = runBlocking {
        db.usuarioDao().insertar(Usuario(nombreUsuario = nombre, correo = "$nombre@correo.com", claveHash = "hash"))
    }

    private fun buscar(patron: String, id: Long? = null, limite: Int = 20) = runBlocking {
        db.usuarioDao().buscarUsuarios(yo, id, patron, limite)
    }

    @Test
    fun encuentraPorParteDelNombreSinDistinguirMayusculas() {
        crear("marta_padel")
        crear("Marta99")
        crear("carlos")
        assertEquals(listOf("marta_padel", "Marta99"), buscar("MARTA").map { it.nombreUsuario })
    }

    @Test
    fun encuentraPorIdExacto() {
        val ana = crear("ana_23")
        crear("luis_7")
        val encontrados = buscar(patron = "no_coincide_con_ningun_nombre", id = ana)
        assertEquals(listOf("ana_23"), encontrados.map { it.nombreUsuario })
    }

    @Test
    fun nuncaDevuelveAlUsuarioQueBusca() {
        crear("yo_otro")
        assertEquals(listOf("yo_otro"), buscar("yo").map { it.nombreUsuario })
        assertTrue(buscar(patron = "zzz", id = yo).isEmpty())
    }

    @Test
    fun indicaQuienYaEsAmigo() = runBlocking {
        val marta = crear("marta_padel")
        crear("marta_run")
        db.amistadDao().insertar(Amistad.entre(yo, marta))

        val resultado = buscar("marta").associate { it.nombreUsuario to it.yaEsAmigo }
        assertTrue(resultado.getValue("marta_padel"))
        assertFalse(resultado.getValue("marta_run"))
    }

    @Test
    fun elGuionBajoEscapadoSeBuscaTalCual() {
        crear("pa_ra")
        crear("paXra")
        assertEquals(listOf("pa_ra"), buscar("pa\\_ra").map { it.nombreUsuario })
    }

    @Test
    fun ordenaPrimeroElIdLuegoLosQueEmpiezanPorElTextoYLuegoElResto() {
        val zeta = crear("zeta")
        crear("a${zeta}x")
        crear("${zeta}b")
        assertEquals(
            listOf("zeta", "${zeta}b", "a${zeta}x"),
            buscar(patron = zeta.toString(), id = zeta).map { it.nombreUsuario }
        )
    }

    @Test
    fun respetaElLimiteDeResultados() {
        repeat(5) { crear("jugador_$it") }
        assertEquals(3, buscar("jugador", limite = 3).size)
    }
}
