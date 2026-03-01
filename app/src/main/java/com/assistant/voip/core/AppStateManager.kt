package com.assistant.voip.core

import android.content.Context
import android.content.SharedPreferences
import com.assistant.voip.utils.extensions.toast

object AppStateManager {

    private const val PREFS_NAME = "app_state"
    private const val KEY_APP_INITIALIZED = "app_initialized"
    private const val KEY_LAST_ACTIVE_TIME = "last_active_time"
    private const val KEY_APP_VERSION = "app_version"
    private const val KEY_CALL_COUNT = "call_count"
    private const val KEY_TASK_COUNT = "task_count"

    private lateinit var sharedPreferences: SharedPreferences

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        checkInitialization()
        updateLastActiveTime()
        checkVersionUpdate()
    }

    private fun checkInitialization() {
        if (!isInitialized()) {
            // 应用首次启动
            initializeApp()
        }
    }

    private fun initializeApp() {
        // 设置默认配置
        with(sharedPreferences.edit()) {
            putBoolean(KEY_APP_INITIALIZED, true)
            putLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis())
            putInt(KEY_CALL_COUNT, 0)
            putInt(KEY_TASK_COUNT, 0)
            apply()
        }
    }

    fun isInitialized(): Boolean {
        return sharedPreferences.getBoolean(KEY_APP_INITIALIZED, false)
    }

    fun updateLastActiveTime() {
        with(sharedPreferences.edit()) {
            putLong(KEY_LAST_ACTIVE_TIME, System.currentTimeMillis())
            apply()
        }
    }

    fun getLastActiveTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_ACTIVE_TIME, 0)
    }

    fun incrementCallCount() {
        val currentCount = sharedPreferences.getInt(KEY_CALL_COUNT, 0)
        with(sharedPreferences.edit()) {
            putInt(KEY_CALL_COUNT, currentCount + 1)
            apply()
        }
    }

    fun getCallCount(): Int {
        return sharedPreferences.getInt(KEY_CALL_COUNT, 0)
    }

    fun incrementTaskCount() {
        val currentCount = sharedPreferences.getInt(KEY_TASK_COUNT, 0)
        with(sharedPreferences.edit()) {
            putInt(KEY_TASK_COUNT, currentCount + 1)
            apply()
        }
    }

    fun getTaskCount(): Int {
        return sharedPreferences.getInt(KEY_TASK_COUNT, 0)
    }

    private fun checkVersionUpdate() {
        val currentVersion = BuildConfig.VERSION_CODE
        val lastVersion = sharedPreferences.getInt(KEY_APP_VERSION, 0)

        if (lastVersion < currentVersion) {
            // 应用版本更新
            handleVersionUpdate(lastVersion, currentVersion)
            with(sharedPreferences.edit()) {
                putInt(KEY_APP_VERSION, currentVersion)
                apply()
            }
        }
    }

    private fun handleVersionUpdate(oldVersion: Int, newVersion: Int) {
        when (oldVersion) {
            0 -> {
                // 从无版本到v1.0.0
            }
            1 -> {
                // 从v1.0.0到v1.0.1
            }
            // 更多版本升级逻辑
        }
    }

    fun isCallActive(): Boolean {
        return CallStateManager.isCallActive()
    }

    fun isTaskRunning(): Boolean {
        return TaskStateManager.isTaskRunning()
    }

    fun isFileTransmitting(): Boolean {
        return FileStateManager.isFileTransmitting()
    }

    fun getAppUsageStats(): AppUsageStats {
        return AppUsageStats(
            callCount = getCallCount(),
            taskCount = getTaskCount(),
            lastActiveTime = getLastActiveTime(),
            isInitialized = isInitialized(),
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME
        )
    }

    fun showUsageStats(context: Context) {
        val stats = getAppUsageStats()
        context.toast(
            "使用统计:\n" +
            "通话次数: ${stats.callCount}\n" +
            "任务数量: ${stats.taskCount}\n" +
            "版本: ${stats.versionName}"
        )
    }

    data class AppUsageStats(
        val callCount: Int,
        val taskCount: Int,
        val lastActiveTime: Long,
        val isInitialized: Boolean,
        val versionCode: Int,
        val versionName: String
    )
}
