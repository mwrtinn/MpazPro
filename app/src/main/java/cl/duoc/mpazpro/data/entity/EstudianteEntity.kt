package cl.duoc.mpazpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estudiantes")
data class EstudianteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rut: String,
    val contrasena: String,
    val nombreCompleto: String
)

