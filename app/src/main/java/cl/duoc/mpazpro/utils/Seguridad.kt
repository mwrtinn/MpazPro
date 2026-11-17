package cl.duoc.mpazpro.utils

import java.security.MessageDigest

/** Las contraseñas no se guardan en texto plano en Room: se guarda su hash SHA-256. */
object Seguridad {
    fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(texto.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
