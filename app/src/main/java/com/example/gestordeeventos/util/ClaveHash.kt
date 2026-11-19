package com.example.gestordeeventos.util

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Convierte contraseñas en un hash seguro y comprueba si una contraseña coincide con un hash guardado.
 *
 * La contraseña nunca se guarda: se guarda el resultado de PBKDF2 (una función hecha para esto, lenta a propósito)
 * junto con una "sal" aleatoria, de modo que dos usuarios con la misma contraseña tienen hashes distintos.
 *
 * Formato guardado en `Usuario.claveHash`: `pbkdf2:<iteraciones>:<sal en Base64>:<hash en Base64>`
 *
 * Kotlin y Java puros (sin dependencias de Android) para poder probarlo con tests unitarios JVM.
 * Es una operación costosa: desde una corrutina hay que llamarla fuera del hilo principal.
 */
object ClaveHash {
    private const val ALGORITMO = "PBKDF2WithHmacSHA256"
    private const val PREFIJO = "pbkdf2"
    private const val SEPARADOR = ":"
    private const val ITERACIONES = 100_000
    private const val BYTES_SAL = 16
    private const val BITS_HASH = 256
    private const val INDICE_ITERACIONES = 1
    private const val INDICE_SAL = 2
    private const val INDICE_HASH = 3

    /** Devuelve el texto que se guarda en la base de datos para esa contraseña. */
    fun generar(clave: String): String {
        val sal = ByteArray(BYTES_SAL).also { SecureRandom().nextBytes(it) }
        val hash = derivar(clave, sal, ITERACIONES)
        return listOf(PREFIJO, ITERACIONES.toString(), codificar(sal), codificar(hash)).joinToString(SEPARADOR)
    }

    /** true si [clave] corresponde al [almacenado] generado antes con [generar]. Un formato roto devuelve false. */
    fun verificar(clave: String, almacenado: String): Boolean {
        val datos = leer(almacenado)
        return datos != null && MessageDigest.isEqual(derivar(clave, datos.sal, datos.iteraciones), datos.hash)
    }

    private class DatosHash(val iteraciones: Int, val sal: ByteArray, val hash: ByteArray)

    private fun leer(almacenado: String): DatosHash? {
        val partes = almacenado.split(SEPARADOR)
        if (partes.firstOrNull() != PREFIJO) return null
        val iteraciones = partes.getOrNull(INDICE_ITERACIONES)?.toIntOrNull()?.takeIf { it > 0 }
        val sal = partes.getOrNull(INDICE_SAL)?.let(::decodificar)
        val hash = partes.getOrNull(INDICE_HASH)?.let(::decodificar)
        return if (iteraciones != null && sal != null && hash != null) DatosHash(iteraciones, sal, hash) else null
    }

    private fun derivar(clave: String, sal: ByteArray, iteraciones: Int): ByteArray {
        val especificacion = PBEKeySpec(clave.toCharArray(), sal, iteraciones, BITS_HASH)
        return try {
            SecretKeyFactory.getInstance(ALGORITMO).generateSecret(especificacion).encoded
        } finally {
            especificacion.clearPassword()
        }
    }

    private fun codificar(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)

    private fun decodificar(texto: String): ByteArray? = runCatching { Base64.getDecoder().decode(texto) }.getOrNull()
}
