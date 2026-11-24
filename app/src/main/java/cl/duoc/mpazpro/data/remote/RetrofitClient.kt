package cl.duoc.mpazpro.data.remote

import cl.duoc.mpazpro.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Configuración central de los microservicios.
object RetrofitClient {

    // Recupera las direcciones configuradas en Gradle.
    private val URL_USUARIOS = BuildConfig.URL_USUARIOS
    private val URL_APRENDIZAJE = BuildConfig.URL_APRENDIZAJE

    // Agrega el token JWT a las solicitudes protegidas.
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(false)
        .followSslRedirects(false)
        .addInterceptor { chain ->

            val solicitud = chain.request()
            val token = SesionRemota.token

            val autenticada = if (
                !token.isNullOrBlank() &&
                solicitud.url.encodedPath != "/api/auth/login"
            ) {
                solicitud.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
            } else {
                solicitud
            }

            chain.proceed(autenticada)
        }
        .build()

    // Construye una conexión Retrofit para cada microservicio.
    private fun crearRetrofit(url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retrofitUsuarios by lazy {
        crearRetrofit(URL_USUARIOS)
    }

    private val retrofitAprendizaje by lazy {
        crearRetrofit(URL_APRENDIZAJE)
    }

    // Servicio remoto de usuarios.
    val usuarios: UsuariosApi by lazy {
        retrofitUsuarios.create(UsuariosApi::class.java)
    }

    // Servicio remoto de currículo y seguimiento.
    val aprendizaje: AprendizajeApi by lazy {
        retrofitAprendizaje.create(AprendizajeApi::class.java)
    }
}