package com.assistant.voip.data.database.dao

import androidx.room.*
import com.assistant.voip.data.database.entity.CallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {

    @Query("SELECT * FROM calls ORDER BY startTime DESC")
    fun getAllCalls(): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE id = :callId")
    suspend fun getCallById(callId: String): CallEntity?

    @Query("SELECT * FROM calls WHERE callStatus = :status ORDER BY startTime DESC")
    fun getCallsByStatus(status: String): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE startTime >= :startDate AND startTime <= :endDate ORDER BY startTime DESC")
    fun getCallsByDateRange(startDate: Long, endDate: Long): Flow<List<CallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity)

    @Update
    suspend fun updateCall(call: CallEntity)

    @Delete
    suspend fun deleteCall(call: CallEntity)

    @Query("DELETE FROM calls WHERE id = :callId")
    suspend fun deleteCallById(callId: String)

    @Query("DELETE FROM calls")
    suspend fun deleteAllCalls()

    @Query("SELECT COUNT(*) FROM calls")
    suspend fun getCallCount(): Int

    @Query("SELECT SUM(duration) FROM calls WHERE callStatus = 'COMPLETED'")
    suspend fun getTotalCallDuration(): Long

    @Query("SELECT * FROM calls ORDER BY startTime DESC LIMIT :limit")
    fun getRecentCalls(limit: Int): Flow<List<CallEntity>>
}
