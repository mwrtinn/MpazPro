package cl.duoc.mpazpro.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "desafios")
data class DesafioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val limiteIntentos: Int,
    val puntajeMaximo: Int,
    val rutAlumno: String,
    val intentosRealizados: Int = 0,
    val puntajeObtenido: Int = 0,
    val informePedagogico: String = ""
)