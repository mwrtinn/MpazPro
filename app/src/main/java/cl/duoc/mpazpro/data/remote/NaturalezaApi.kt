package cl.duoc.mpazpro.data.remote

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

// Respuesta general de iNaturalist.
data class NaturalezaResponse(
    val results: List<EspecieDto>? = null
)

// Representa una especie de la naturaleza.
data class EspecieDto(
    val id: Int,
    val name: String?,

    @SerializedName("default_photo")
    val fotoPrincipal: FotoNaturalezaDto?,

    @SerializedName("taxon_photos")
    val fotos: List<FotoTaxonDto>?
)

// Fotografías asociadas a una especie.
data class FotoTaxonDto(
    val photo: FotoNaturalezaDto?
)

// Información y licencia de una fotografía.
data class FotoNaturalezaDto(
    val id: Long?,
    val url: String?,
    val attribution: String?,

    @SerializedName("medium_url")
    val imagenMediana: String?,

    @SerializedName("license_code")
    val licencia: String?
)

// Operaciones de la API externa.
interface NaturalezaApi {

    @GET("v1/taxa/{ids}")
    suspend fun consultarEspecies(
        @Path("ids") ids: String
    ): NaturalezaResponse
}

// Cliente independiente para proteger el token JWT.
object NaturalezaCliente {

    private const val BASE_URL =
        "https://api.inaturalist.org/"

    private val cliente = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followSslRedirects(false)
        .build()

    val api: NaturalezaApi by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(NaturalezaApi::class.java)
    }
}