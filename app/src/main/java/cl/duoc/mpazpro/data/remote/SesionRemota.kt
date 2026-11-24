package cl.duoc.mpazpro.data.remote

// Mantiene los datos del usuario autenticado durante la sesión.
object SesionRemota {

    @Volatile
    var usuario: LoginResponse? = null
        private set

    val token: String?
        get() = usuario?.token

    val rut: String?
        get() = usuario?.rut

    val rol: String?
        get() = usuario?.rol

    // Guarda los datos recibidos desde el microservicio.
    fun iniciar(datos: LoginResponse) {
        usuario = datos
    }

    // Elimina los datos al cerrar la sesión.
    fun cerrar() {
        usuario = null
    }
}