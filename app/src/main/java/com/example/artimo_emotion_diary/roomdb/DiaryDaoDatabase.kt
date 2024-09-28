package com.example.artimo_emotion_diary.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DiaryTable::class], version = 1, exportSchema = false)
abstract class DiaryDaoDatabase : RoomDatabase() {

    abstract fun diaryDAO(): DiaryDAO

    companion object {
        @Volatile
        private var INSTANCE: DiaryDaoDatabase? = null

        fun getDatabase(context: Context): DiaryDaoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDaoDatabase::class.java,
                    "diary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
