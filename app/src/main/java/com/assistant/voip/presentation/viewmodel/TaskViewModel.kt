package com.assistant.voip.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assistant.voip.data.task.TaskExecutor
import com.assistant.voip.domain.model.Task
import com.assistant.voip.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskExecutor: TaskExecutor,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask: StateFlow<Task?> = _selectedTask

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success

    fun loadTasks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val tasks = taskRepository.getAllTasks()
                _tasks.value = tasks
            } catch (e: Exception) {
                _error.value = "加载任务失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                taskRepository.addTask(task)
                val tasks = taskRepository.getAllTasks()
                _tasks.value = tasks
                _success.value = "任务添加成功"
            } catch (e: Exception) {
                _error.value = "添加任务失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                taskRepository.updateTask(task)
                val tasks = taskRepository.getAllTasks()
                _tasks.value = tasks
                _success.value = "任务更新成功"
            } catch (e: Exception) {
                _error.value = "更新任务失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                taskRepository.deleteTask(task)
                val tasks = taskRepository.getAllTasks()
                _tasks.value = tasks
                _success.value = "任务删除成功"
            } catch (e: Exception) {
                _error.value = "删除任务失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startTask(taskId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                taskExecutor.startTask(taskId)
                val tasks = taskRepository.getAllTasks()
                _tasks.value = tasks
                _success.value = "任务启动成功"
            } catch (e: Exception) {
                _error.value = "启动任务失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun pauseTask(taskId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                taskExecutor.pauseTask(taskId)
                val tasks = taskRepository.getAllTasks()
                _tasks.value = tasks
                _success.value = "任务暂停成功"
            } catch (e: Exception) {
                _error.value = "暂停任务失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun stopTask(taskId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                taskExecutor.stopTask(taskId)
                val tasks = taskRepository.getAllTasks()
                _tasks.value = tasks
                _success.value = "任务停止成功"
            } catch (e: Exception) {
                _error.value = "停止任务失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSuccess() {
        _success.value = null
    }

    fun selectTask(task: Task) {
        _selectedTask.value = task
    }

    fun clearSelectedTask() {
        _selectedTask.value = null
    }
}
