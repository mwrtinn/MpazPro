package cl.duoc.mpazpro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.mpazpro.data.entity.EstudianteEntity

@Dao
interface EstudianteDao {
    @Query("SELECT * FROM estudiantes WHERE rut = :rut AND contrasena = :contrasena")
    suspend fun login(rut: String, contrasena: String): EstudianteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEstudiante(estudiante: EstudianteEntity)

    @Query("SELECT COUNT(id) FROM estudiantes")
    suspend fun contarEstudiantes(): Int
}