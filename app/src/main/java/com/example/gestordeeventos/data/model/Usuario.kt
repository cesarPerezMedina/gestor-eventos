package com.example.gestordeeventos.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "usuarios". Correo y nombre de usuario son únicos.
 * claveHash guarda la contraseña ya transformada (nunca en texto plano), con el formato que genera
 * `ClaveHash.generar` (util/ClaveHash.kt). Se comprueba con `ClaveHash.verificar`.
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true), Index(value = ["nombreUsuario"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombreUsuario: String,
    val correo: String,
    val password: String
)
