package com.assistant.voip.data.task

import android.util.Log
import com.assistant.voip.domain.model.Task
import com.assistant.voip.domain.repository.TaskRepository
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class TaskScheduler private constructor() {

    companion object {
        private const val TAG = "TaskScheduler"
        @Volatile
        private var instance: TaskScheduler? = null

        fun getInstance(): TaskScheduler {
            return instance ?: synchronized(this) {
                instance ?: TaskScheduler().also { instance = it }
            }
        }
    }

    private val executor = Executors.newScheduledThreadPool(3)
    private val taskRepository: TaskRepository = TaskRepositoryImpl()

    fun scheduleTask(task: Task, delay: Long = 0): String {
        val taskId = System.currentTimeMillis().toString()
        val scheduledTask = task.copy(id = taskId)

        executor.schedule({
            try {
                Log.d(TAG, "Executing task: ${scheduledTask.title}")
                taskRepository.saveTask(scheduledTask)
                executeTask(scheduledTask)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute task: ${scheduledTask.title}", e)
                taskRepository.updateTaskStatus(taskId, TaskStatus.FAILED)
            }
        }, delay, TimeUnit.MILLISECONDS)

        Log.d(TAG, "Task scheduled: ${scheduledTask.title}")
        return taskId
    }

    fun scheduleRepeatingTask(task: Task, initialDelay: Long, period: Long): String {
        val taskId = System.currentTimeMillis().toString()
        val scheduledTask = task.copy(id = taskId)

        executor.scheduleAtFixedRate({
            try {
                Log.d(TAG, "Executing repeating task: ${scheduledTask.title}")
                executeTask(scheduledTask)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to execute repeating task: ${scheduledTask.title}", e)
            }
        }, initialDelay, period, TimeUnit.MILLISECONDS)

        Log.d(TAG, "Repeating task scheduled: ${scheduledTask.title}")
        return taskId
    }

    fun cancelTask(taskId: String): Boolean {
        return try {
            Log.d(TAG, "Canceling task: $taskId")
            executor.shutdown()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel task: $taskId", e)
            false
        }
    }

    fun executeTask(task: Task) {
        when (task.type) {
            TaskType.CALL -> executeCallTask(task)
            TaskType.MESSAGE -> executeMessageTask(task)
            TaskType.FILE_TRANSFER -> executeFileTransferTask(task)
            TaskType.SETTING -> executeSettingTask(task)
            TaskType.CUSTOM -> executeCustomTask(task)
        }
    }

    private fun executeCallTask(task: Task) {
        // 执行通话任务
        Log.d(TAG, "Executing call task: ${task.title}")
        taskRepository.updateTaskStatus(task.id, TaskStatus.IN_PROGRESS)
        // TODO: 实现通话任务执行逻辑
        Log.d(TAG, "Call task completed")
        taskRepository.updateTaskStatus(task.id, TaskStatus.COMPLETED)
    }

    private fun executeMessageTask(task: Task) {
        // 执行消息任务
        Log.d(TAG, "Executing message task: ${task.title}")
        taskRepository.updateTaskStatus(task.id, TaskStatus.IN_PROGRESS)
        // TODO: 实现消息任务执行逻辑
        Log.d(TAG, "Message task completed")
        taskRepository.updateTaskStatus(task.id, TaskStatus.COMPLETED)
    }

    private fun executeFileTransferTask(task: Task) {
        // 执行文件传输任务
        Log.d(TAG, "Executing file transfer task: ${task.title}")
        taskRepository.updateTaskStatus(task.id, TaskStatus.IN_PROGRESS)
        // TODO: 实现文件传输任务执行逻辑
        Log.d(TAG, "File transfer task completed")
        taskRepository.updateTaskStatus(task.id, TaskStatus.COMPLETED)
    }

    private fun executeSettingTask(task: Task) {
        // 执行设置任务
        Log.d(TAG, "Executing setting task: ${task.title}")
        taskRepository.updateTaskStatus(task.id, TaskStatus.IN_PROGRESS)
        // TODO: 实现设置任务执行逻辑
        Log.d(TAG, "Setting task completed")
        taskRepository.updateTaskStatus(task.id, TaskStatus.COMPLETED)
    }

    private fun executeCustomTask(task: Task) {
        // 执行自定义任务
        Log.d(TAG, "Executing custom task: ${task.title}")
        taskRepository.updateTaskStatus(task.id, TaskStatus.IN_PROGRESS)
        // TODO: 实现自定义任务执行逻辑
        Log.d(TAG, "Custom task completed")
        taskRepository.updateTaskStatus(task.id, TaskStatus.COMPLETED)
    }

    fun getTaskStatus(taskId: String): TaskStatus {
        return taskRepository.getTaskStatus(taskId)
    }

    fun getAllTasks(): List<Task> {
        return taskRepository.getAllTasks()
    }

    fun getPendingTasks(): List<Task> {
        return taskRepository.getTasksByStatus(TaskStatus.PENDING)
    }

    fun getInProgressTasks(): List<Task> {
        return taskRepository.getTasksByStatus(TaskStatus.IN_PROGRESS)
    }

    fun getCompletedTasks(): List<Task> {
        return taskRepository.getTasksByStatus(TaskStatus.COMPLETED)
    }

    fun getFailedTasks(): List<Task> {
        return taskRepository.getTasksByStatus(TaskStatus.FAILED)
    }

    fun addTaskListener(listener: TaskEventListener) {
        taskRepository.addTaskListener(listener)
    }

    fun removeTaskListener(listener: TaskEventListener) {
        taskRepository.removeTaskListener(listener)
    }

    interface TaskEventListener {
        fun onTaskCreated(task: Task)
        fun onTaskUpdated(task: Task)
        fun onTaskDeleted(task: Task)
        fun onTaskStatusChanged(taskId: String, status: TaskStatus)
        fun onTaskCompleted(task: Task)
        fun onTaskFailed(task: Task)
        fun onTaskStarted(task: Task)
        fun onTaskPaused(task: Task)
        fun onTaskResumed(task: Task)
        fun onTaskCanceled(task: Task)
    }

    class TaskRepositoryImpl : TaskRepository {
        private val tasks = mutableListOf<Task>()
        private val listeners = mutableListOf<TaskEventListener>()

        override fun saveTask(task: Task) {
            tasks.add(task)
            listeners.forEach { it.onTaskCreated(task) }
        }

        override fun updateTask(task: Task) {
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index != -1) {
                tasks[index] = task
                listeners.forEach { it.onTaskUpdated(task) }
            }
        }

        override fun deleteTask(taskId: String) {
            val task = tasks.find { it.id == taskId }
            task?.let {
                tasks.remove(it)
                listeners.forEach { listener -> listener.onTaskDeleted(it) }
            }
        }

        override fun getTaskById(taskId: String): Task? {
            return tasks.find { it.id == taskId }
        }

        override fun getAllTasks(): List<Task> {
            return tasks.toList()
        }

        override fun getTasksByStatus(status: TaskStatus): List<Task> {
            return tasks.filter { it.status == status }
        }

        override fun updateTaskStatus(taskId: String, status: TaskStatus) {
            val task = getTaskById(taskId)
            task?.let {
                updateTask(it.copy(status = status))
                listeners.forEach { listener -> listener.onTaskStatusChanged(taskId, status) }
                when (status) {
                    TaskStatus.COMPLETED -> listeners.forEach { listener -> listener.onTaskCompleted(it) }
                    TaskStatus.FAILED -> listeners.forEach { listener -> listener.onTaskFailed(it) }
                    TaskStatus.IN_PROGRESS -> listeners.forEach { listener -> listener.onTaskStarted(it) }
                    else -> {}
                }
            }
        }

        override fun getTaskStatus(taskId: String): TaskStatus {
            return getTaskById(taskId)?.status ?: TaskStatus.PENDING
        }

        override fun addTaskListener(listener: TaskEventListener) {
            listeners.add(listener)
        }

        override fun removeTaskListener(listener: TaskEventListener) {
            listeners.remove(listener)
        }
    }
}
