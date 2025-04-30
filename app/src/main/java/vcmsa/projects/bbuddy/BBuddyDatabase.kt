package vcmsa.projects.bbuddy

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [userEntity::class, categoryEntity::class, expenseEntity::class],
    version = 2, // <-- updated to version 2
    exportSchema = false
)

abstract class BBuddyDatabase : RoomDatabase() {

    abstract fun bbuddyDAO(): bbuddyDAO

    companion object {
        @Volatile
        private var INSTANCE: BBuddyDatabase? = null

        fun getDatabase(context: Context): BBuddyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BBuddyDatabase::class.java,
                    "bbuddy_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

    }
}

