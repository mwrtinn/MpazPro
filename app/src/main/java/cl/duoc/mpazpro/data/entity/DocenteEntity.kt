package cl.duoc.mpazpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "docentes")
data class DocenteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuario: String,
    val contrasena: String,
    val nombreCompleto: String
)