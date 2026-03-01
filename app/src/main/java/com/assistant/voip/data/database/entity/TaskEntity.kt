package com.assistant.voip.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.assistant.voip.domain.model.TaskStatus
import java.util.Date

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val progress: Int,
    val priority: Int,
    val category: String,
    val createdAt: Date,
    val updatedAt: Date,
    val completedAt: Date?,
    val tags: String,
    val metadata: String
)
