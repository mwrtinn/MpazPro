package cl.duoc.mpazpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registros_actividad")
data class RegistroActividadEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val rutEstudiante: String,
    val asignatura: String,
    val observacion: String,
    val fecha: String,
    val estado: String // "Presente", "Ausente", "Aprobado", etc.
)