package cl.duoc.mpazpro.data.remote

import retrofit2.http.*

// ---------- Modelos de comunicación remota ----------

// Representa un objetivo de aprendizaje.
data class ObjetivoDto(
    val id: Int? = null,
    val asignatura: String,
    val unidad: String,
    val codigo: String,
    val eje: String,
    val descripcion: String,
    val referencia: String,
    val asignado: Boolean,
    val intentosPrueba: Int
)

// Representa un contenido educativo.
data class ContenidoDto(
    val id: Int? = null,
    val oaId: Int,
    val titulo: String,
    val explicacion: String,
    val ejemplo: String,
    val icono: String,
    val estado: String = "PENDIENTE",
    val observacionUtp: String = "",
    val imagenPath: String? = null
)

// Datos enviados por UTP al revisar un contenido.
data class RevisionRequest(
    val estado: String,
    val observacionUtp: String
)

// Representa una pregunta de desafío o evaluación.
data class PreguntaDto(
    val id: Int? = null,
    val oaId: Int,
    val tipo: String,
    val dificultad: Int,
    val enunciado: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val correcta: Int? = null
)

// Datos enviados por el estudiante al responder.
data class RespuestaRequest(
    val preguntaId: Int,
    val intento: Int,
    val elegida: Int
)

// Resultado calculado por el servidor.
data class RespuestaDto(
    val id: Int,
    val rutAlumno: String,
    val oaId: Int,
    val preguntaId: Int,
    val tipo: String,
    val dificultad: Int,
    val intento: Int,
    val elegida: Int,
    val esCorrecta: Boolean,
    val fecha: Long,
    val correcta: Int? = null
)

// Información sobre los intentos de evaluación.
data class IntentosDto(
    val rutAlumno: String,
    val oaId: Int,
    val tipo: String,
    val ultimoIntento: Int,
    val intentosPermitidos: Int,
    val intentosRestantes: Int,
    val disponible: Boolean
)

// Representa un informe pedagógico.
data class InformeDto(
    val clave: String,
    val rutAlumno: String,
    val oaId: Int,
    val rendimiento: Int,
    val fortalezas: String,
    val refuerzo: String,
    val fecha: String,
    val validado: Boolean
)

// Datos necesarios para generar un informe.
data class InformeRequest(
    val rutAlumno: String,
    val oaId: Int
)

// ---------- Microservicio de aprendizaje ----------

// Define las operaciones REST del currículo y seguimiento.
interface AprendizajeApi {

    // ---------- Objetivos ----------

    @GET("api/objetivos")
    suspend fun listarObjetivos(): List<ObjetivoDto>

    @GET("api/objetivos/{id}")
    suspend fun buscarObjetivo(
        @Path("id") id: Int
    ): ObjetivoDto

    @POST("api/objetivos")
    suspend fun crearObjetivo(
        @Body objetivo: ObjetivoDto
    ): ObjetivoDto

    @PUT("api/objetivos/{id}")
    suspend fun actualizarObjetivo(
        @Path("id") id: Int,
        @Body objetivo: ObjetivoDto
    ): ObjetivoDto

    @DELETE("api/objetivos/{id}")
    suspend fun eliminarObjetivo(
        @Path("id") id: Int
    )

    // ---------- Contenidos ----------

    @GET("api/contenidos")
    suspend fun listarContenidos(
        @Query("oaId") oaId: Int? = null
    ): List<ContenidoDto>

    @GET("api/contenidos/{id}")
    suspend fun buscarContenido(
        @Path("id") id: Int
    ): ContenidoDto

    @POST("api/contenidos")
    suspend fun crearContenido(
        @Body contenido: ContenidoDto
    ): ContenidoDto

    @PUT("api/contenidos/{id}")
    suspend fun actualizarContenido(
        @Path("id") id: Int,
        @Body contenido: ContenidoDto
    ): ContenidoDto

    @PATCH("api/contenidos/{id}/estado")
    suspend fun revisarContenido(
        @Path("id") id: Int,
        @Body revision: RevisionRequest
    ): ContenidoDto

    @DELETE("api/contenidos/{id}")
    suspend fun eliminarContenido(
        @Path("id") id: Int
    )

    // ---------- Preguntas ----------

    @GET("api/preguntas")
    suspend fun listarPreguntas(
        @Query("oaId") oaId: Int? = null,
        @Query("tipo") tipo: String? = null
    ): List<PreguntaDto>

    @GET("api/preguntas/{id}")
    suspend fun buscarPregunta(
        @Path("id") id: Int
    ): PreguntaDto

    @POST("api/preguntas")
    suspend fun crearPregunta(
        @Body pregunta: PreguntaDto
    ): PreguntaDto

    @PUT("api/preguntas/{id}")
    suspend fun actualizarPregunta(
        @Path("id") id: Int,
        @Body pregunta: PreguntaDto
    ): PreguntaDto

    @DELETE("api/preguntas/{id}")
    suspend fun eliminarPregunta(
        @Path("id") id: Int
    )

    // ---------- Respuestas ----------

    @GET("api/respuestas")
    suspend fun listarRespuestas(): List<RespuestaDto>

    @GET("api/respuestas/alumno/{rut}")
    suspend fun respuestasDeAlumno(
        @Path("rut") rut: String
    ): List<RespuestaDto>

    @POST("api/respuestas")
    suspend fun registrarRespuesta(
        @Body respuesta: RespuestaRequest
    ): RespuestaDto

    // ---------- Intentos ----------

    @GET("api/intentos")
    suspend fun consultarIntentos(
        @Query("oaId") oaId: Int,
        @Query("tipo") tipo: String,
        @Query("rut") rut: String? = null
    ): IntentosDto

    // ---------- Informes pedagógicos ----------

    @GET("api/informes")
    suspend fun listarInformes(): List<InformeDto>

    @GET("api/informes/{clave}")
    suspend fun buscarInforme(
        @Path("clave") clave: String
    ): InformeDto

    @PUT("api/informes/{clave}")
    suspend fun generarInforme(
        @Path("clave") clave: String,
        @Body informe: InformeRequest
    ): InformeDto

    @PATCH("api/informes/{clave}/validar")
    suspend fun validarInforme(
        @Path("clave") clave: String
    ): InformeDto
}