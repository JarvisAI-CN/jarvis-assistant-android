package com.assistant.voip.domain.model

import java.util.Date

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val progress: Int,
    val priority: TaskPriority,
    val category: String,
    val createdAt: Date,
    val updatedAt: Date,
    val completedAt: Date?,
    val tags: List<String>,
    val metadata: Map<String, Any>
)

enum class TaskStatus {
    Pending,
    InProgress,
    Completed,
    Failed
}

enum class TaskPriority {
    Low,
    Medium,
    High,
    Critical
}

data class TaskProgress(
    val taskId: String,
    val progress: Int,
    val status: TaskStatus,
    val timestamp: Date,
    val message: String?
)
