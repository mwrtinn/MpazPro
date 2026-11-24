package cl.duoc.mpazpro.repository

import cl.duoc.mpazpro.data.local.entity.*
import cl.duoc.mpazpro.data.remote.*
import cl.duoc.mpazpro.model.Rol

// Centraliza las operaciones remotas de ambos microservicios.
class AulaRemotaRepository(
    private val usuarios: UsuariosApi = RetrofitClient.usuarios,
    private val aprendizaje: AprendizajeApi = RetrofitClient.aprendizaje
) {

    // ---------- Usuarios ----------

    suspend fun estudiantes(): List<UsuarioEntity> {

        return usuarios.listarUsuarios(Rol.ESTUDIANTE)
            .map { it.aEntidad() }
    }

    suspend fun listarUsuarios(): List<UsuarioEntity> {

        return usuarios.listarUsuarios()
            .map { it.aEntidad() }
    }

    // ---------- Objetivos ----------

    suspend fun objetivos(): List<ObjetivoEntity> {

        return aprendizaje.listarObjetivos()
            .map { it.aEntidad() }
    }

    suspend fun objetivosAsignados(): List<ObjetivoEntity> {

        return objetivos().filter { it.asignado }
    }

    suspend fun buscarObjetivo(id: Int): ObjetivoEntity {

        return aprendizaje.buscarObjetivo(id)
            .aEntidad()
    }

    suspend fun crearObjetivo(
        objetivo: ObjetivoEntity
    ): ObjetivoEntity {

        return aprendizaje.crearObjetivo(objetivo.aDto())
            .aEntidad()
    }

    suspend fun actualizarObjetivo(
        objetivo: ObjetivoEntity
    ): ObjetivoEntity {

        return aprendizaje.actualizarObjetivo(
            objetivo.id,
            objetivo.aDto()
        ).aEntidad()
    }

    suspend fun eliminarObjetivo(id: Int) {

        aprendizaje.eliminarObjetivo(id)
    }

    // ---------- Contenidos ----------

    suspend fun contenidos(): List<ContenidoEntity> {

        return aprendizaje.listarContenidos()
            .map { it.aEntidad() }
    }

    suspend fun contenidosAprobados(
        oaId: Int
    ): List<ContenidoEntity> {

        return aprendizaje.listarContenidos(oaId)
            .filter { it.estado == "APROBADO" }
            .map { it.aEntidad() }
    }

    suspend fun crearContenido(
        contenido: ContenidoEntity
    ): ContenidoEntity {

        return aprendizaje.crearContenido(
            contenido.aDto()
        ).aEntidad()
    }

    suspend fun actualizarContenido(
        contenido: ContenidoEntity
    ): ContenidoEntity {

        return aprendizaje.actualizarContenido(
            contenido.id,
            contenido.aDto()
        ).aEntidad()
    }

    // La revisión de contenidos utiliza el endpoint exclusivo de UTP.
    suspend fun revisarContenido(
        id: Int,
        estado: String,
        observacion: String
    ): ContenidoEntity {

        return aprendizaje.revisarContenido(
            id,
            RevisionRequest(
                estado = estado,
                observacionUtp = observacion
            )
        ).aEntidad()
    }

    suspend fun eliminarContenido(id: Int) {

        aprendizaje.eliminarContenido(id)
    }

    // ---------- Preguntas ----------

    suspend fun preguntas(): List<PreguntaEntity> {

        return aprendizaje.listarPreguntas()
            .map { it.aEntidad() }
    }

    suspend fun preguntasDe(
        oaId: Int,
        tipo: String
    ): List<PreguntaEntity> {

        return aprendizaje.listarPreguntas(
            oaId = oaId,
            tipo = tipo
        ).map { it.aEntidad() }
            .sortedWith(
                compareBy<PreguntaEntity> { it.dificultad }
                    .thenBy { it.id }
            )
    }

    suspend fun crearPregunta(
        pregunta: PreguntaEntity
    ): PreguntaEntity {

        return aprendizaje.crearPregunta(
            pregunta.aDto()
        ).aEntidad()
    }

    suspend fun actualizarPregunta(
        pregunta: PreguntaEntity
    ): PreguntaEntity {

        return aprendizaje.actualizarPregunta(
            pregunta.id,
            pregunta.aDto()
        ).aEntidad()
    }

    suspend fun eliminarPregunta(id: Int) {

        aprendizaje.eliminarPregunta(id)
    }

    // ---------- Respuestas ----------

    suspend fun respuestas(): List<RespuestaEntity> {

        return aprendizaje.listarRespuestas()
            .map { it.aEntidad() }
    }

    suspend fun respuestasDeAlumno(
        rut: String
    ): List<RespuestaEntity> {

        return aprendizaje.respuestasDeAlumno(rut)
            .map { it.aEntidad() }
    }

    // El servidor determina si la respuesta es correcta.
    suspend fun registrarRespuesta(
        preguntaId: Int,
        intento: Int,
        elegida: Int
    ): RespuestaDto {

        return aprendizaje.registrarRespuesta(
            RespuestaRequest(
                preguntaId = preguntaId,
                intento = intento,
                elegida = elegida
            )
        )
    }

    // ---------- Intentos ----------

    suspend fun consultarIntentos(
        oaId: Int,
        tipo: String,
        rut: String? = null
    ): IntentosDto {

        return aprendizaje.consultarIntentos(
            oaId = oaId,
            tipo = tipo,
            rut = rut
        )
    }

    suspend fun ultimoIntento(
        rut: String,
        oaId: Int,
        tipo: String
    ): Int {

        return consultarIntentos(
            oaId,
            tipo,
            rut
        ).ultimoIntento
    }

    // ---------- Informes ----------

    suspend fun informes(): List<InformeEntity> {

        return aprendizaje.listarInformes()
            .map { it.aEntidad() }
    }

    // El informe se calcula a partir de los resultados del servidor.
    suspend fun generarInforme(
        rutAlumno: String,
        oaId: Int
    ): InformeEntity {

        return aprendizaje.generarInforme(
            clave = "$rutAlumno-$oaId",
            informe = InformeRequest(
                rutAlumno = rutAlumno,
                oaId = oaId
            )
        ).aEntidad()
    }

    suspend fun validarInforme(
        clave: String
    ): InformeEntity {

        return aprendizaje.validarInforme(clave)
            .aEntidad()
    }
}