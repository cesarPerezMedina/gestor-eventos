package com.example.gestordeeventos

import com.example.gestordeeventos.data.local.dao.AmistadDao
import com.example.gestordeeventos.data.model.Amigo
import com.example.gestordeeventos.data.model.Amistad
import com.example.gestordeeventos.data.model.EstadoAmistad
import com.example.gestordeeventos.data.model.SolicitudRecibida
import com.example.gestordeeventos.data.model.Usuario
import com.example.gestordeeventos.data.repositorio.AmistadRepositorio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FakeAmistadDao : AmistadDao {
    val amistades = mutableListOf<Amistad>()
    var idPorCorreoMap = mapOf<String, Long>()

    override suspend fun insertarRaw(amistad: Amistad): Long {
        amistades.add(amistad)
        return amistades.size.toLong()
    }

    override suspend fun obtenerIdPorCorreo(correo: String): Long? {
        return idPorCorreoMap[correo]
    }

    override suspend fun insertar(amistad: Amistad): Long {
        amistades.add(amistad)
        return amistades.size.toLong()
    }

    override suspend fun sonAmigos(a: Long, b: Long): Boolean {
        return amistades.any { (it.usuarioId == a && it.amigoId == b) || (it.usuarioId == b && it.amigoId == a) }
    }

    override suspend fun entre(c1: String, c2: String): Amistad? {
        return amistades.firstOrNull {
            (it.solicitante.equals(c1, true) && it.destinatario.equals(c2, true)) ||
            (it.solicitante.equals(c2, true) && it.destinatario.equals(c1, true))
        }
    }

    override suspend fun aceptar(solicitante: String, destinatario: String): Int {
        val index = amistades.indexOfFirst { it.solicitante.equals(solicitante, true) && it.destinatario.equals(destinatario, true) && it.estado == EstadoAmistad.PENDIENTE }
        if (index != -1) {
            amistades[index] = amistades[index].copy(estado = EstadoAmistad.ACEPTADA)
            return 1
        }
        return 0
    }

    override suspend fun eliminarPendiente(solicitante: String, destinatario: String): Int {
        val removed = amistades.removeIf { it.solicitante.equals(solicitante, true) && it.destinatario.equals(destinatario, true) && it.estado == EstadoAmistad.PENDIENTE }
        return if (removed) 1 else 0
    }

    override suspend fun solicitudesRecibidas(miCorreo: String): List<Amistad> {
        return amistades.filter { it.destinatario.equals(miCorreo, true) && it.estado == EstadoAmistad.PENDIENTE }
    }

    override suspend fun listarAmigos(usuarioId: Long): List<Amigo> = emptyList()

    override suspend fun contarAmigos(usuarioId: Long): Int = 0

    override suspend fun eliminar(a: Long, b: Long): Int {
        val removed = amistades.removeIf { (it.usuarioId == a && it.amigoId == b) || (it.usuarioId == b && it.amigoId == a) }
        return if (removed) 1 else 0
    }

    override suspend fun bloquear(yo: Long, amigo: Long): Int {
        val index = amistades.indexOfFirst { (it.usuarioId == yo && it.amigoId == amigo) || (it.usuarioId == amigo && it.amigoId == yo) }
        if (index != -1) {
            amistades[index] = amistades[index].copy(estado = EstadoAmistad.BLOQUEADA)
            return 1
        }
        return 0
    }

    override fun observarAmigos(miCorreo: String): Flow<List<Amigo>> = flowOf(emptyList())

    override fun observarSolicitudesRecibidas(miCorreo: String): Flow<List<SolicitudRecibida>> = flowOf(emptyList())
}

class AmistadRepositorioTest {
    private lateinit var usuarioDao: FakeUsuarioDao
    private lateinit var amistadDao: FakeAmistadDao
    private lateinit var repositorio: AmistadRepositorio

    @Before
    fun setUp() {
        usuarioDao = FakeUsuarioDao()
        amistadDao = FakeAmistadDao()
        repositorio = AmistadRepositorio(usuarioDao, amistadDao)
    }

    @Test
    fun enviarSolicitudConExito() = runBlocking {
        val yo = Usuario(correo = "yo@test.com", nombreUsuario = "yo", claveHash = "hash")
        val destino = Usuario(correo = "amigo@test.com", nombreUsuario = "amigo", claveHash = "hash")
        usuarioDao.insertar(yo)
        usuarioDao.insertar(destino)
        amistadDao.idPorCorreoMap = mapOf("yo@test.com" to 1L, "amigo@test.com" to 2L)

        val resultado = repositorio.enviarSolicitud("yo@test.com", "amigo")
        assertTrue(resultado.ok)
        assertEquals(1, amistadDao.amistades.size)
        assertEquals(EstadoAmistad.PENDIENTE, amistadDao.amistades[0].estado)
    }

    @Test
    fun noPuedesAñadirteATiMismo() = runBlocking {
        val yo = Usuario(correo = "yo@test.com", nombreUsuario = "yo", claveHash = "hash")
        usuarioDao.insertar(yo)
        amistadDao.idPorCorreoMap = mapOf("yo@test.com" to 1L)

        val resultado = repositorio.enviarSolicitud("yo@test.com", "yo")
        assertFalse(resultado.ok)
        assertEquals("No puedes añadirte a ti mismo", resultado.mensaje)
    }

    @Test
    fun aceptarSolicitudExistente() = runBlocking {
        val solicitud = Amistad(solicitante = "amigo@test.com", destinatario = "yo@test.com", estado = EstadoAmistad.PENDIENTE)
        amistadDao.amistades.add(solicitud)

        val resultado = repositorio.aceptarSolicitud("yo@test.com", "amigo@test.com")
        assertTrue(resultado.ok)
        assertEquals(EstadoAmistad.ACEPTADA, amistadDao.amistades[0].estado)
    }
}
