package cl.duoc.mpazpro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.mpazpro.data.entity.RegistroActividadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroActividadDao {
    @Query("SELECT * FROM registros_actividad WHERE rutEstudiante = :rut")
    fun getRegistrosPorAlumno(rut: String): Flow<List<RegistroActividadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistro(registro: RegistroActividadEntity)
}