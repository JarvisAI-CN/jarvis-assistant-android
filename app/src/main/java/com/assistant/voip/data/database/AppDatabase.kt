package com.assistant.voip.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.assistant.voip.data.database.dao.CallDao
import com.assistant.voip.data.database.dao.FileTransferDao
import com.assistant.voip.data.database.dao.TaskDao
import com.assistant.voip.data.database.entity.CallEntity
import com.assistant.voip.data.database.entity.FileTransferEntity
import com.assistant.voip.data.database.entity.TaskEntity
import com.assistant.voip.data.database.converter.DateConverter
import com.assistant.voip.data.database.converter.FileTypeConverter
import com.assistant.voip.data.database.converter.TaskStatusConverter
import com.assistant.voip.data.database.converter.FileTransferStatusConverter

@Database(
    entities = [
        CallEntity::class,
        TaskEntity::class,
        FileTransferEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    DateConverter::class,
    FileTypeConverter::class,
    TaskStatusConverter::class,
    FileTransferStatusConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun callDao(): CallDao
    abstract fun taskDao(): TaskDao
    abstract fun fileTransferDao(): FileTransferDao

    companion object {
        private const val DATABASE_NAME = "jarvis_assistant.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
