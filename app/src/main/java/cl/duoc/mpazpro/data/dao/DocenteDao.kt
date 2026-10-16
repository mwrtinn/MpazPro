package cl.duoc.mpazpro.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.mpazpro.data.entity.DocenteEntity

@Dao
interface DocenteDao {
    @Query("SELECT * FROM docentes WHERE usuario = :usuario AND contrasena = :contrasena")
    suspend fun login(usuario: String, contrasena: String): DocenteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarDocente(docente: DocenteEntity)

    @Query("SELECT COUNT(id) FROM docentes")
    suspend fun contarDocentes(): Int
}