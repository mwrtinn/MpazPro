package cl.duoc.mpazpro.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

// Datos enviados al microservicio para iniciar sesión.
data class LoginRequest(
    val rut: String,
    val password: String,
    val rol: String
)

// Datos recibidos después de una autenticación correcta.
data class LoginResponse(
    val rut: String,
    val nombre: String,
    val rol: String,
    val curso: String,
    val token: String
)

// Endpoints del microservicio de usuarios.
interface UsuariosApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body datos: LoginRequest
    ): LoginResponse
}