package cl.duoc.mpazpro.data.remote

import cl.duoc.mpazpro.data.local.entity.*

// ---------- Usuarios ----------

// Convierte el usuario remoto sin almacenar contraseñas.
fun UsuarioDto.aEntidad() = UsuarioEntity(
    rut = rut,
    nombre = nombre,
    contrasena = "",
    rol = rol,
    curso = curso
)

// ---------- Objetivos ----------

fun ObjetivoDto.aEntidad() = ObjetivoEntity(
    id = requireNotNull(id),
    asignatura = asignatura,
    unidad = unidad,
    codigo = codigo,
    eje = eje,
    descripcion = descripcion,
    referencia = referencia,
    asignado = asignado,
    intentosPrueba = intentosPrueba
)

fun ObjetivoEntity.aDto() = ObjetivoDto(
    id = id.takeIf { it > 0 },
    asignatura = asignatura,
    unidad = unidad,
    codigo = codigo,
    eje = eje,
    descripcion = descripcion,
    referencia = referencia,
    asignado = asignado,
    intentosPrueba = intentosPrueba
)

// ---------- Contenidos ----------

fun ContenidoDto.aEntidad() = ContenidoEntity(
    id = requireNotNull(id),
    oaId = oaId,
    titulo = titulo,
    explicacion = explicacion,
    ejemplo = ejemplo,
    icono = icono,
    estado = estado,
    observacionUtp = observacionUtp,
    imagenPath = imagenPath
)

fun ContenidoEntity.aDto() = ContenidoDto(
    id = id.takeIf { it > 0 },
    oaId = oaId,
    titulo = titulo,
    explicacion = explicacion,
    ejemplo = ejemplo,
    icono = icono,
    estado = estado,
    observacionUtp = observacionUtp,

    // Las rutas de imágenes del celular no se comparten con el servidor.
    imagenPath = null
)

// ---------- Preguntas ----------

// La alternativa correcta puede estar oculta para el estudiante.
fun PreguntaDto.aEntidad() = PreguntaEntity(
    id = requireNotNull(id),
    oaId = oaId,
    tipo = tipo,
    dificultad = dificultad,
    enunciado = enunciado,
    opcionA = opcionA,
    opcionB = opcionB,
    opcionC = opcionC,
    correcta = correcta ?: -1
)

fun PreguntaEntity.aDto() = PreguntaDto(
    id = id.takeIf { it > 0 },
    oaId = oaId,
    tipo = tipo,
    dificultad = dificultad,
    enunciado = enunciado,
    opcionA = opcionA,
    opcionB = opcionB,
    opcionC = opcionC,
    correcta = correcta
)

// ---------- Respuestas ----------

fun RespuestaDto.aEntidad() = RespuestaEntity(
    id = id,
    rutAlumno = rutAlumno,
    oaId = oaId,
    preguntaId = preguntaId,
    tipo = tipo,
    dificultad = dificultad,
    intento = intento,
    elegida = elegida,
    esCorrecta = esCorrecta,
    fecha = fecha
)

// ---------- Informes ----------

fun InformeDto.aEntidad() = InformeEntity(
    clave = clave,
    rutAlumno = rutAlumno,
    oaId = oaId,
    rendimiento = rendimiento,
    fortalezas = fortalezas,
    refuerzo = refuerzo,
    fecha = fecha,
    validado = validado
)