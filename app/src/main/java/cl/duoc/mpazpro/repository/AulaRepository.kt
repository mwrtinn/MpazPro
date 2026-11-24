package cl.duoc.mpazpro.repository

import cl.duoc.mpazpro.data.local.dao.ContenidoDao
import cl.duoc.mpazpro.data.local.dao.InformeDao
import cl.duoc.mpazpro.data.local.dao.ObjetivoDao
import cl.duoc.mpazpro.data.local.dao.PreguntaDao
import cl.duoc.mpazpro.data.local.dao.RespuestaDao
import cl.duoc.mpazpro.data.local.dao.UsuarioDao
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.InformeEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import cl.duoc.mpazpro.data.remote.LoginRequest
import cl.duoc.mpazpro.data.remote.LoginResponse
import cl.duoc.mpazpro.data.remote.RetrofitClient
import cl.duoc.mpazpro.model.Rol
import kotlinx.coroutines.flow.Flow

// Centraliza el acceso a los datos locales y remotos de la aplicación.
class AulaRepository(
    private val usuarios: UsuarioDao,
    private val objetivos: ObjetivoDao,
    private val contenidos: ContenidoDao,
    private val preguntas: PreguntaDao,
    private val respuestas: RespuestaDao,
    private val informes: InformeDao
) {

    // ---------- Usuarios ----------

    // Autentica al usuario mediante el microservicio Spring Boot.
    suspend fun login(
        rut: String,
        password: String,
        rol: String
    ): LoginResponse {
        return RetrofitClient.usuarios.login(
            LoginRequest(
                rut = rut,
                password = password,
                rol = rol
            )
        )
    }

    fun estudiantes(): Flow<List<UsuarioEntity>> =
        usuarios.porRol(Rol.ESTUDIANTE)

    // ---------- Objetivos ----------

    fun objetivos(): Flow<List<ObjetivoEntity>> =
        objetivos.todos()

    fun objetivosAsignados(): Flow<List<ObjetivoEntity>> =
        objetivos.asignados()

    fun observarObjetivo(id: Int): Flow<ObjetivoEntity?> =
        objetivos.observar(id)

    suspend fun actualizarObjetivo(o: ObjetivoEntity) =
        objetivos.actualizar(o)

    // ---------- Contenidos ----------

    fun contenidos(): Flow<List<ContenidoEntity>> =
        contenidos.todos()

    fun contenidosAprobados(oaId: Int): Flow<List<ContenidoEntity>> =
        contenidos.aprobadosDe(oaId)

    suspend fun crearContenido(c: ContenidoEntity) =
        contenidos.insertar(c)

    suspend fun actualizarContenido(c: ContenidoEntity) =
        contenidos.actualizar(c)

    // ---------- Preguntas ----------

    fun preguntas(): Flow<List<PreguntaEntity>> =
        preguntas.todas()

    suspend fun preguntasDe(
        oaId: Int,
        tipo: String
    ): List<PreguntaEntity> =
        preguntas.de(oaId, tipo)

    suspend fun crearPregunta(p: PreguntaEntity) =
        preguntas.insertar(p)

    // ---------- Respuestas e informes ----------

    fun respuestasDeAlumno(rut: String): Flow<List<RespuestaEntity>> =
        respuestas.deAlumno(rut)

    fun respuestas(): Flow<List<RespuestaEntity>> =
        respuestas.todas()

    suspend fun ultimoIntento(
        rut: String,
        oaId: Int,
        tipo: String
    ): Int = respuestas.ultimoIntento(rut, oaId, tipo)

    suspend fun guardarRespuesta(r: RespuestaEntity) =
        respuestas.insertar(r)

    fun informes(): Flow<List<InformeEntity>> =
        informes.todos()

    suspend fun guardarInforme(i: InformeEntity) =
        informes.guardar(i)

    // ---------- Datos de prueba ----------

    // Mantiene los datos locales durante la transición a Retrofit.
    suspend fun sembrarSiVacio() {

        if (usuarios.contar() > 0) return

        DatosDePrueba.usuarios().forEach {
            usuarios.insertar(it)
        }

        DatosDePrueba.objetivos().forEach {
                (oa, contenidosOa, preguntasOa) ->

            val id = objetivos.insertar(oa).toInt()

            contenidosOa.forEach {
                contenidos.insertar(it.copy(oaId = id))
            }

            preguntasOa.forEach {
                preguntas.insertar(it.copy(oaId = id))
            }
        }
    }
}