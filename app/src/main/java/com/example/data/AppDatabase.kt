package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CalcuTechniqueDao
import com.example.data.dao.FormulaDao
import com.example.data.dao.ImportBatchDao
import com.example.data.dao.QuestionDao
import com.example.data.dao.SubjectDao
import com.example.data.model.CalcuTechniqueEntity
import com.example.data.model.FormulaEntity
import com.example.data.model.ImportBatchEntity
import com.example.data.model.QuestionEntity
import com.example.data.model.SubjectEntity

@Database(
    entities = [
        SubjectEntity::class,
        ImportBatchEntity::class,
        QuestionEntity::class,
        FormulaEntity::class,
        CalcuTechniqueEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun importBatchDao(): ImportBatchDao
    abstract fun questionDao(): QuestionDao
    abstract fun formulaDao(): FormulaDao
    abstract fun calcuTechniqueDao(): CalcuTechniqueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ece_flashmaster.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
