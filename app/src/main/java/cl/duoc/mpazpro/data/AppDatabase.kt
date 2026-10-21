package cl.duoc.mpazpro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.duoc.mpazpro.data.dao.DocenteDao
import cl.duoc.mpazpro.data.entity.DocenteEntity
import cl.duoc.mpazpro.data.dao.EstudianteDao
import cl.duoc.mpazpro.data.entity.EstudianteEntity
import cl.duoc.mpazpro.data.dao.RegistroActividadDao
import cl.duoc.mpazpro.data.entity.RegistroActividadEntity

@Database(
    entities = [
        DocenteEntity::class,
        EstudianteEntity::class,
        RegistroActividadEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun docenteDao(): DocenteDao
    abstract fun estudianteDao(): EstudianteDao
    abstract fun registroActividadDao(): RegistroActividadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpazpro_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}