package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ImportBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportBatchDao {
    @Query("SELECT * FROM import_batches ORDER BY createdAt DESC")
    fun getAllBatches(): Flow<List<ImportBatchEntity>>

    @Query("SELECT * FROM import_batches WHERE subjectId = :subjectId ORDER BY createdAt DESC")
    fun getBatchesBySubject(subjectId: Long): Flow<List<ImportBatchEntity>>

    @Query("SELECT * FROM import_batches WHERE id = :id LIMIT 1")
    fun getBatchById(id: Long): Flow<ImportBatchEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: ImportBatchEntity): Long

    @Query("DELETE FROM import_batches WHERE id = :id")
    suspend fun deleteBatchById(id: Long)

    @Delete
    suspend fun deleteBatch(batch: ImportBatchEntity)

    @Query("DELETE FROM import_batches")
    suspend fun deleteAllBatches(): Int

    @Query("SELECT COUNT(*) FROM import_batches WHERE subjectId = :subjectId")
    fun getBatchCountBySubject(subjectId: Long): Flow<Int>
}
