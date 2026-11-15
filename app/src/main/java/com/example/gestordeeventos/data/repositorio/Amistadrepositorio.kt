package com.example.gestordeeventos.data.repositorio

import com.example.gestordeeventos.data.local.dao.AmistadDao
import com.example.gestordeeventos.data.local.dao.UsuarioDao
import com.example.gestordeeventos.data.model.Amistad
import com.example.gestordeeventos.data.model.EstadoAmistad
import kotlinx.coroutines.flow.Flow
import com.example.gestordeeventos.data.model.Amigo

data class ResultadoAmistad(val ok: Boolean, val mensaje: String)

/**
 * Reglas de las solicitudes de amistad (con aceptación).
 * Los usuarios se identifican por su correo.
 */
class AmistadRepositorio(
    private val usuarioDao: UsuarioDao,
    private val amistadDao: AmistadDao,
) {

    /** Envía una solicitud a quien coincida con el ID o nombre de usuario escrito. */
    suspend fun enviarSolicitud(miCorreo: String, consulta: String): ResultadoAmistad {
        val texto = consulta.trim().removePrefix("@")
        if (texto.isEmpty()) {
            return ResultadoAmistad(false, "Escribe un ID o un nombre de usuario")
        }

        val destino = usuarioDao.buscarPorUsuarioOCorreo(texto)
            ?: return ResultadoAmistad(false, "No existe ningún usuario con ese ID o nombre")

        if (destino.correo.equals(miCorreo, ignoreCase = true)) {
            return ResultadoAmistad(false, "No puedes añadirte a ti mismo")
        }

        val existente = amistadDao.entre(miCorreo, destino.correo)
        if (existente != null) {
            return when {
                existente.estado == EstadoAmistad.ACEPTADA ->
                    ResultadoAmistad(false, "Ya sois amigos")
                existente.solicitante.equals(miCorreo, ignoreCase = true) ->
                    ResultadoAmistad(false, "Ya le has enviado una solicitud")
                else ->
                    ResultadoAmistad(false, "Esta persona ya te envió una solicitud: acéptala en Solicitudes")
            }
        }

        amistadDao.insertar(Amistad(solicitante = miCorreo, destinatario = destino.correo))
        return ResultadoAmistad(true, "Solicitud enviada a $texto")
    }

    /** Acepta una solicitud que me ha enviado otra persona. */
    suspend fun aceptarSolicitud(miCorreo: String, correoSolicitante: String): ResultadoAmistad {
        val filas = amistadDao.aceptar(solicitante = correoSolicitante, destinatario = miCorreo)
        return if (filas > 0) {
            ResultadoAmistad(true, "Solicitud aceptada")
        } else {
            ResultadoAmistad(false, "La solicitud ya no existe")
        }
    }

    /** Rechaza (borra) una solicitud que me ha enviado otra persona. */
    suspend fun rechazarSolicitud(miCorreo: String, correoSolicitante: String): ResultadoAmistad {
        val filas = amistadDao.eliminarPendiente(solicitante = correoSolicitante, destinatario = miCorreo)
        return if (filas > 0) {
            ResultadoAmistad(true, "Solicitud rechazada")
        } else {
            ResultadoAmistad(false, "La solicitud ya no existe")
        }
    }
    /** Quita a un amigo de mi lista. */
    suspend fun eliminarAmigo(miCorreo: String, idAmigo: Long): ResultadoAmistad {
        val yo = amistadDao.obtenerIdPorCorreo(miCorreo)
            ?: return ResultadoAmistad(false, "No se encontró tu usuario")
        return if (amistadDao.eliminar(yo, idAmigo) > 0) {
            ResultadoAmistad(true, "Amigo eliminado")
        } else {
            ResultadoAmistad(false, "Esa persona ya no está en tu lista")
        }
    }

    /** Bloquea a un amigo: desaparece de la lista y ya no puede enviarte solicitudes. */
    suspend fun bloquearAmigo(miCorreo: String, idAmigo: Long): ResultadoAmistad {
        val yo = amistadDao.obtenerIdPorCorreo(miCorreo)
            ?: return ResultadoAmistad(false, "No se encontró tu usuario")
        return if (amistadDao.bloquear(yo, idAmigo) > 0) {
            ResultadoAmistad(true, "Usuario bloqueado")
        } else {
            ResultadoAmistad(false, "No se pudo bloquear a este usuario")
        }
    }

    /** Solicitudes pendientes que he recibido. */
    suspend fun solicitudesRecibidas(miCorreo: String): List<Amistad> =
        amistadDao.solicitudesRecibidas(miCorreo)

    /** Amigos aceptados, siempre actualizados. */
    fun observarAmigos(miCorreo: String): Flow<List<Amigo>> =
        amistadDao.observarAmigos(miCorreo)
}
