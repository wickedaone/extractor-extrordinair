package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FlashLog
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashLogDao {
    @Query("SELECT * FROM flash_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<FlashLog>>

    @Query("SELECT * FROM flash_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): FlashLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FlashLog): Long

    @Update
    suspend fun updateLog(log: FlashLog)

    @Query("DELETE FROM flash_logs WHERE id = :id")
    suspend fun deleteLogById(id: Long)

    @Query("DELETE FROM flash_logs")
    suspend fun deleteAllLogs()
}
