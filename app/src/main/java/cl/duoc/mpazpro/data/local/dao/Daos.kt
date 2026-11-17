package cl.duoc.mpazpro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazpro.data.local.entity.ContenidoEntity
import cl.duoc.mpazpro.data.local.entity.InformeEntity
import cl.duoc.mpazpro.data.local.entity.ObjetivoEntity
import cl.duoc.mpazpro.data.local.entity.PreguntaEntity
import cl.duoc.mpazpro.data.local.entity.RespuestaEntity
import cl.duoc.mpazpro.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios WHERE rut = :rut AND contrasena = :hash AND rol = :rol LIMIT 1")
    suspend fun login(rut: String, hash: String, rol: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE rol = :rol ORDER BY nombre")
    fun porRol(rol: String): Flow<List<UsuarioEntity>>

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: UsuarioEntity)
}

@Dao
interface ObjetivoDao {
    @Query("SELECT * FROM objetivos ORDER BY asignatura, id")
    fun todos(): Flow<List<ObjetivoEntity>>

    @Query("SELECT * FROM objetivos WHERE asignado = 1 ORDER BY asignatura, id")
    fun asignados(): Flow<List<ObjetivoEntity>>

    @Query("SELECT * FROM objetivos WHERE id = :id")
    fun observar(id: Int): Flow<ObjetivoEntity?>

    @Query("SELECT COUNT(*) FROM objetivos")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(objetivo: ObjetivoEntity): Long

    @Update
    suspend fun actualizar(objetivo: ObjetivoEntity)
}

@Dao
interface ContenidoDao {
    @Query("SELECT * FROM contenidos ORDER BY id DESC")
    fun todos(): Flow<List<ContenidoEntity>>

    @Query("SELECT * FROM contenidos WHERE oaId = :oaId AND estado = 'APROBADO' ORDER BY id")
    fun aprobadosDe(oaId: Int): Flow<List<ContenidoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(contenido: ContenidoEntity)

    @Update
    suspend fun actualizar(contenido: ContenidoEntity)
}

@Dao
interface PreguntaDao {
    @Query("SELECT * FROM preguntas ORDER BY oaId, tipo, dificultad, id")
    fun todas(): Flow<List<PreguntaEntity>>

    @Query("SELECT * FROM preguntas WHERE oaId = :oaId AND tipo = :tipo ORDER BY dificultad, id")
    suspend fun de(oaId: Int, tipo: String): List<PreguntaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(pregunta: PreguntaEntity)
}

@Dao
interface RespuestaDao {
    @Query("SELECT * FROM respuestas WHERE rutAlumno = :rut")
    fun deAlumno(rut: String): Flow<List<RespuestaEntity>>

    @Query("SELECT * FROM respuestas")
    fun todas(): Flow<List<RespuestaEntity>>

    @Query("SELECT COALESCE(MAX(intento), 0) FROM respuestas WHERE rutAlumno = :rut AND oaId = :oaId AND tipo = :tipo")
    suspend fun ultimoIntento(rut: String, oaId: Int, tipo: String): Int

    @Insert
    suspend fun insertar(respuesta: RespuestaEntity)
}

@Dao
interface InformeDao {
    @Query("SELECT * FROM informes")
    fun todos(): Flow<List<InformeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(informe: InformeEntity)
}
