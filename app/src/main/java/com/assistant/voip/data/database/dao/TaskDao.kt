package com.assistant.voip.data.database.dao

import androidx.room.*
import com.assistant.voip.data.database.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY createdAt DESC")
    fun getTasksByStatus(status: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY createdAt DESC")
    fun getTasksByCategory(category: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE priority >= :minPriority ORDER BY priority DESC, createdAt DESC")
    fun getTasksByPriority(minPriority: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE tags LIKE '%' || :tag || '%' ORDER BY createdAt DESC")
    fun getTasksByTag(tag: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("SELECT COUNT(*) FROM tasks WHERE status = :status")
    suspend fun getTaskCountByStatus(status: String): Int

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTasks(limit: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status IN ('PENDING', 'IN_PROGRESS') ORDER BY priority DESC, createdAt ASC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Transaction
    suspend fun updateTaskProgress(taskId: String, progress: Int, status: String) {
        val task = getTaskById(taskId) ?: return
        val updatedTask = task.copy(
            progress = progress,
            status = when (status.uppercase()) {
                "COMPLETED" -> com.assistant.voip.domain.model.TaskStatus.Completed
                "IN_PROGRESS" -> com.assistant.voip.domain.model.TaskStatus.InProgress
                else -> com.assistant.voip.domain.model.TaskStatus.Pending
            },
            updatedAt = java.util.Date()
        )
        updateTask(updatedTask)
    }
}
