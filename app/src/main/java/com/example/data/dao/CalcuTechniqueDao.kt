package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CalcuTechniqueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalcuTechniqueDao {
    @Query("SELECT * FROM calcu_techniques WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getTechniquesBySubject(subjectId: Long): Flow<List<CalcuTechniqueEntity>>

    @Query("SELECT * FROM calcu_techniques ORDER BY id ASC")
    fun getAllTechniques(): Flow<List<CalcuTechniqueEntity>>

    @Query("SELECT * FROM calcu_techniques WHERE questionId = :questionId ORDER BY id ASC")
    fun getTechniquesForQuestion(questionId: Long): Flow<List<CalcuTechniqueEntity>>

    @Query("SELECT * FROM calcu_techniques WHERE questionId = :questionId ORDER BY id ASC")
    suspend fun getTechniquesForQuestionOnce(questionId: Long): List<CalcuTechniqueEntity>

    @Query("""
        SELECT * FROM calcu_techniques 
        WHERE title LIKE '%' || :query || '%' 
           OR keystrokes LIKE '%' || :query || '%' 
           OR notes LIKE '%' || :query || '%'
        ORDER BY id ASC
    """)
    fun searchTechniques(query: String): Flow<List<CalcuTechniqueEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTechnique(technique: CalcuTechniqueEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTechniques(techniques: List<CalcuTechniqueEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTechniques(techniques: List<CalcuTechniqueEntity>)

    @Query("DELETE FROM calcu_techniques WHERE id = :id")
    suspend fun deleteTechniqueById(id: Long)

    @Query("DELETE FROM calcu_techniques WHERE id IN (1, 2, 3, 4, 5) OR hash IN (:hashes)")
    suspend fun deleteDemoTechniques(hashes: List<String>): Int

    @Query("DELETE FROM calcu_techniques")
    suspend fun deleteAllTechniques(): Int

    @Query("SELECT COUNT(*) FROM calcu_techniques WHERE id IN (1, 2, 3, 4, 5) OR hash IN (:hashes)")
    suspend fun getDemoTechniqueCount(hashes: List<String>): Int
}
