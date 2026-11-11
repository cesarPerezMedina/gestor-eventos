package com.example.gestordeeventos.data.model

/**
 * Un usuario que aparece al buscar usuarios (Issue 31). No es una tabla: es el resultado de
 * `UsuarioDao.buscarUsuarios`. Solo lleva lo necesario para mostrarlo; nunca el correo ni la contraseña.
 *
 * @property yaEsAmigo si ya es amigo del usuario que busca, para no ofrecer añadirlo otra vez.
 */
data class UsuarioEncontrado(
    val id: Long,
    val nombreUsuario: String,
    val yaEsAmigo: Boolean
)
