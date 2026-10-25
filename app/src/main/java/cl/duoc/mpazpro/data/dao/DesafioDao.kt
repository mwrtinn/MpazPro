package cl.duoc.mpazpro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.entity.DesafioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DesafioDao {
    @Query("SELECT * FROM desafios WHERE rutAlumno = :rut")
    fun getDesafiosPorAlumno(rut: String): Flow<List<DesafioEntity>>

    @Query("SELECT * FROM desafios")
    fun getAllDesafios(): Flow<List<DesafioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDesafio(desafio: DesafioEntity)

    @Update
    suspend fun actualizarDesafio(desafio: DesafioEntity)
}