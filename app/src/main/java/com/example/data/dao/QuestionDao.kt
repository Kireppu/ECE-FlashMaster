package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getQuestionsBySubject(subjectId: Long): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions ORDER BY id ASC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    fun getQuestionById(id: Long): Flow<QuestionEntity?>

    @Query("SELECT * FROM questions WHERE id = :id LIMIT 1")
    suspend fun getQuestionByIdOnce(id: Long): QuestionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertQuestions(questions: List<QuestionEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestions(questions: List<QuestionEntity>)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Query("UPDATE questions SET isMastered = :isMastered WHERE id = :id")
    suspend fun setMasteredStatus(id: Long, isMastered: Boolean)

    @Query("DELETE FROM questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Long)

    @Query("DELETE FROM questions WHERE batchId IS NULL OR id IN (1, 2, 3, 4)")
    suspend fun deleteDemoQuestions(): Int

    @Query("DELETE FROM questions WHERE subjectId = :subjectId")
    suspend fun deleteQuestionsBySubject(subjectId: Long): Int

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions(): Int

    @Query("SELECT COUNT(*) FROM questions WHERE batchId IS NULL OR id IN (1, 2, 3, 4)")
    suspend fun getDemoQuestionCount(): Int

    @Query("SELECT COUNT(*) FROM questions WHERE subjectId = :subjectId")
    fun getQuestionCountBySubject(subjectId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE subjectId = :subjectId AND isMastered = 1")
    fun getMasteredCountBySubject(subjectId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions")
    fun getTotalQuestionCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE isMastered = 1")
    fun getTotalMasteredCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM questions WHERE subjectId = :subjectId AND stemHash = :hash")
    suspend fun countDuplicate(subjectId: Long, hash: String): Int
}
