package cl.duoc.mpazpro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.duoc.mpazpro.data.dao.DocenteDao
import cl.duoc.mpazpro.data.entity.DocenteEntity

@Database(entities = [DocenteEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun docenteDao(): DocenteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpazpro_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}