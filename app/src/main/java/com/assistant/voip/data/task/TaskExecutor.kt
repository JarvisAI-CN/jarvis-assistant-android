package com.assistant.voip.data.task

import android.util.Log
import com.assistant.voip.domain.model.Task
import com.assistant.voip.domain.model.TaskStatus
import com.assistant.voip.domain.model.TaskType
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class TaskExecutor private constructor() {

    companion object {
        private const val TAG = "TaskExecutor"
        private const val MAX_CONCURRENT_TASKS = 5
        private const val TASK_TIMEOUT_MS = 30000L

        @Volatile
        private var instance: TaskExecutor? = null

        fun getInstance(): TaskExecutor {
            return instance ?: synchronized(this) {
                instance ?: TaskExecutor().also { instance = it }
            }
        }
    }

    private val executor = Executors.newFixedThreadPool(MAX_CONCURRENT_TASKS)
    private val runningTasks = ConcurrentHashMap<String, Task>()
    private val taskCallbacks = ConcurrentHashMap<String, TaskCallback>()

    interface TaskCallback {
        fun onTaskStarted(task: Task)
        fun onTaskProgress(task: Task, progress: Int)
        fun onTaskCompleted(task: Task)
        fun onTaskFailed(task: Task, error: String)
        fun onTaskCanceled(task: Task)
    }

    fun executeTask(task: Task, callback: TaskCallback? = null): Observable<Task> {
        return Observable.create { emitter ->
            if (runningTasks.size >= MAX_CONCURRENT_TASKS) {
                emitter.onError(IllegalStateException("Max concurrent tasks reached"))
                return@create
            }

            executor.submit {
                try {
                    runningTasks[task.id] = task
                    callback?.let { taskCallbacks[task.id] = it }

                    Log.d(TAG, "Executing task: ${task.title} (${task.id})")

                    val startedTask = task.copy(status = TaskStatus.IN_PROGRESS)
                    runningTasks[task.id] = startedTask
                    callback?.onTaskStarted(startedTask)
                    emitter.onNext(startedTask)

                    when (task.type) {
                        TaskType.CALL -> executeCallTask(startedTask, callback, emitter)
                        TaskType.MESSAGE -> executeMessageTask(startedTask, callback, emitter)
                        TaskType.FILE_TRANSFER -> executeFileTransferTask(startedTask, callback, emitter)
                        TaskType.SETTING -> executeSettingTask(startedTask, callback, emitter)
                        TaskType.CUSTOM -> executeCustomTask(startedTask, callback, emitter)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to execute task: ${task.title}", e)
                    val failedTask = task.copy(
                        status = TaskStatus.FAILED,
                        error = e.message
                    )
                    runningTasks[task.id] = failedTask
                    callback?.onTaskFailed(failedTask, e.message ?: "Unknown error")
                    emitter.onNext(failedTask)
                    emitter.onError(e)
                } finally {
                    runningTasks.remove(task.id)
                    taskCallbacks.remove(task.id)
                }
            }
        }.subscribeOn(Schedulers.io())
    }

    private fun executeCallTask(
        task: Task,
        callback: TaskCallback?,
        emitter: Observable emitter
    ) {
        Log.d(TAG, "Executing call task: ${task.title}")

        // 模拟任务执行进度
        for (i in 0..100 step 10) {
            Thread.sleep(500)
            val progressTask = task.copy(progress = i)
            runningTasks[task.id] = progressTask
            callback?.onTaskProgress(progressTask, i)
            emitter.onNext(progressTask)
        }

        val completedTask = task.copy(
            status = TaskStatus.COMPLETED,
            progress = 100
        )
        runningTasks[task.id] = completedTask
        callback?.onTaskCompleted(completedTask)
        emitter.onNext(completedTask)
        emitter.onComplete()
    }

    private fun executeMessageTask(
        task: Task,
        callback: TaskCallback?,
        emitter: Observable emitter
    ) {
        Log.d(TAG, "Executing message task: ${task.title}")

        // 模拟任务执行进度
        for (i in 0..100 step 20) {
            Thread.sleep(300)
            val progressTask = task.copy(progress = i)
            runningTasks[task.id] = progressTask
            callback?.onTaskProgress(progressTask, i)
            emitter.onNext(progressTask)
        }

        val completedTask = task.copy(
            status = TaskStatus.COMPLETED,
            progress = 100
        )
        runningTasks[task.id] = completedTask
        callback?.onTaskCompleted(completedTask)
        emitter.onNext(completedTask)
        emitter.onComplete()
    }

    private fun executeFileTransferTask(
        task: Task,
        callback: TaskCallback?,
        emitter: Observable emitter
    ) {
        Log.d(TAG, "Executing file transfer task: ${task.title}")

        // 模拟任务执行进度
        for (i in 0..100 step 5) {
            Thread.sleep(200)
            val progressTask = task.copy(progress = i)
            runningTasks[task.id] = progressTask
            callback?.onTaskProgress(progressTask, i)
            emitter.onNext(progressTask)
        }

        val completedTask = task.copy(
            status = TaskStatus.COMPLETED,
            progress = 100
        )
        runningTasks[task.id] = completedTask
        callback?.onTaskCompleted(completedTask)
        emitter.onNext(completedTask)
        emitter.onComplete()
    }

    private fun executeSettingTask(
        task: Task,
        callback: TaskCallback?,
        emitter: Observable emitter
    ) {
        Log.d(TAG, "Executing setting task: ${task.title}")

        // 模拟任务执行进度
        for (i in 0..100 step 25) {
            Thread.sleep(200)
            val progressTask = task.copy(progress = i)
            runningTasks[task.id] = progressTask
            callback?.onTaskProgress(progressTask, i)
            emitter.onNext(progressTask)
        }

        val completedTask = task.copy(
            status = TaskStatus.COMPLETED,
            progress = 100
        )
        runningTasks[task.id] = completedTask
        callback?.onTaskCompleted(completedTask)
        emitter.onNext(completedTask)
        emitter.onComplete()
    }

    private fun executeCustomTask(
        task: Task,
        callback: TaskCallback?,
        emitter: Observable emitter
    ) {
        Log.d(TAG, "Executing custom task: ${task.title}")

        // 模拟任务执行进度
        for (i in 0..100 step 15) {
            Thread.sleep(400)
            val progressTask = task.copy(progress = i)
            runningTasks[task.id] = progressTask
            callback?.onTaskProgress(progressTask, i)
            emitter.onNext(progressTask)
        }

        val completedTask = task.copy(
            status = TaskStatus.COMPLETED,
            progress = 100
        )
        runningTasks[task.id] = completedTask
        callback?.onTaskCompleted(completedTask)
        emitter.onNext(completedTask)
        emitter.onComplete()
    }

    fun cancelTask(taskId: String): Boolean {
        val task = runningTasks[taskId]
        return if (task != null) {
            runningTasks.remove(taskId)
            taskCallbacks[taskId]?.onTaskCanceled(task)
            taskCallbacks.remove(taskId)
            Log.d(TAG, "Task canceled: ${task.title} ($taskId)")
            true
        } else {
            Log.w(TAG, "Task not found: $taskId")
            false
        }
    }

    fun getRunningTasks(): List<Task> {
        return runningTasks.values.toList()
    }

    fun getTaskById(taskId: String): Task? {
        return runningTasks[taskId]
    }

    fun isTaskRunning(taskId: String): Boolean {
        return runningTasks.containsKey(taskId)
    }

    fun clearAllTasks() {
        runningTasks.clear()
        taskCallbacks.clear()
        Log.d(TAG, "All tasks cleared")
    }
}
