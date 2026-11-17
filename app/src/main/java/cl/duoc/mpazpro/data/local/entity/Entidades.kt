package cl.duoc.mpazpro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Usuario ficticio. Todos los perfiles ingresan con su RUT (normalizado, sin puntos ni guion). */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey val rut: String,
    val nombre: String,
    /** Hash SHA-256 de la contraseña (ver Seguridad.hash). */
    val contrasena: String,
    val rol: String,
    val curso: String = "3° Básico"
)

/** Objetivo de aprendizaje (OA) del Currículum Nacional. Máximo una unidad por asignatura. */
@Entity(tableName = "objetivos")
data class ObjetivoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val asignatura: String,
    val unidad: String,
    val codigo: String,
    val eje: String,
    val descripcion: String,
    val referencia: String,
    /** El docente decide qué objetivos quedan disponibles para los estudiantes. */
    val asignado: Boolean = false,
    /** Máximo de intentos de la prueba final de este objetivo (lo define el docente). */
    val intentosPrueba: Int = 2
)

/** Recurso didáctico. Los estudiantes solo ven los que UTP dejó en estado APROBADO. */
@Entity(tableName = "contenidos")
data class ContenidoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val oaId: Int,
    val titulo: String,
    val explicacion: String,
    val ejemplo: String,
    val icono: String,
    val estado: String = "PENDIENTE",
    val observacionUtp: String = "",
    /** Ruta de la imagen de apoyo guardada en el almacenamiento interno (cámara o galería). Opcional. */
    val imagenPath: String? = null
)

/** Pregunta de práctica (DESAFIO, dificultad progresiva) o de la prueba final (PRUEBA). */
@Entity(tableName = "preguntas")
data class PreguntaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val oaId: Int,
    val tipo: String,
    val dificultad: Int,
    val enunciado: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    /** 0 = A, 1 = B, 2 = C. */
    val correcta: Int
)

/** Cada respuesta del estudiante queda registrada: base del avance y del informe. */
@Entity(tableName = "respuestas")
data class RespuestaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rutAlumno: String,
    val oaId: Int,
    val preguntaId: Int,
    val tipo: String,
    val dificultad: Int,
    /** Número de intento (de la prueba o de la sesión de práctica). */
    val intento: Int,
    val elegida: Int,
    val esCorrecta: Boolean,
    val fecha: Long
)

/** Informe de uso exclusivo docente. [clave] = "rut-oaId", así regenerarlo reemplaza al anterior. */
@Entity(tableName = "informes")
data class InformeEntity(
    @PrimaryKey val clave: String,
    val rutAlumno: String,
    val oaId: Int,
    val rendimiento: Int,
    val fortalezas: String,
    val refuerzo: String,
    val fecha: String,
    val validado: Boolean = false
)
