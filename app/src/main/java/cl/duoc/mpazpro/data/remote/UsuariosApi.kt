package cl.duoc.mpazpro.data.remote

import retrofit2.http.*

// Datos enviados para iniciar sesión.
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

// Datos públicos de un usuario registrado.
data class UsuarioDto(
    val rut: String,
    val nombre: String,
    val rol: String,
    val curso: String
)

// Datos necesarios para crear un usuario.
data class UsuarioRequest(
    val rut: String,
    val nombre: String,
    val password: String,
    val rol: String,
    val curso: String = "3° Básico"
)

// Datos necesarios para actualizar un usuario.
data class UsuarioUpdateRequest(
    val nombre: String,
    val password: String? = null,
    val rol: String,
    val curso: String = "3° Básico"
)

// Operaciones REST del microservicio de usuarios.
interface UsuariosApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body datos: LoginRequest
    ): LoginResponse

    @GET("api/usuarios")
    suspend fun listarUsuarios(
        @Query("rol") rol: String? = null
    ): List<UsuarioDto>

    @GET("api/usuarios/{rut}")
    suspend fun buscarUsuario(
        @Path("rut") rut: String
    ): UsuarioDto

    @POST("api/usuarios")
    suspend fun crearUsuario(
        @Body usuario: UsuarioRequest
    ): UsuarioDto

    @PUT("api/usuarios/{rut}")
    suspend fun actualizarUsuario(
        @Path("rut") rut: String,
        @Body usuario: UsuarioUpdateRequest
    ): UsuarioDto

    @DELETE("api/usuarios/{rut}")
    suspend fun eliminarUsuario(
        @Path("rut") rut: String
    )
}