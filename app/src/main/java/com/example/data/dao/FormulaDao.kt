package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.FormulaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FormulaDao {
    @Query("SELECT * FROM formulas WHERE subjectId = :subjectId ORDER BY name ASC")
    fun getFormulasBySubject(subjectId: Long): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas ORDER BY name ASC")
    fun getAllFormulas(): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas WHERE questionId = :questionId")
    fun getFormulasForQuestion(questionId: Long): Flow<List<FormulaEntity>>

    @Query("SELECT * FROM formulas WHERE questionId = :questionId")
    suspend fun getFormulasForQuestionOnce(questionId: Long): List<FormulaEntity>

    @Query("""
        SELECT * FROM formulas 
        WHERE name LIKE '%' || :query || '%' 
           OR formula LIKE '%' || :query || '%' 
           OR variables LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchFormulas(query: String): Flow<List<FormulaEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFormula(formula: FormulaEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFormulas(formulas: List<FormulaEntity>): List<Long>

    @Query("DELETE FROM formulas WHERE id = :id")
    suspend fun deleteFormulaById(id: Long)

    @Query("DELETE FROM formulas WHERE id IN (1, 2, 3, 4, 5, 6, 7, 8) OR hash IN (:hashes)")
    suspend fun deleteDemoFormulas(hashes: List<String>): Int

    @Query("DELETE FROM formulas WHERE subjectId = :subjectId")
    suspend fun deleteFormulasBySubject(subjectId: Long): Int

    @Query("DELETE FROM formulas")
    suspend fun deleteAllFormulas(): Int

    @Query("SELECT COUNT(*) FROM formulas WHERE id IN (1, 2, 3, 4, 5, 6, 7, 8) OR hash IN (:hashes)")
    suspend fun getDemoFormulaCount(hashes: List<String>): Int
}
