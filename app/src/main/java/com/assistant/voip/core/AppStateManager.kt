package com.assistant.voip.core

import android.content.Context

object AppStateManager {

    private lateinit var context: Context

    fun initialize(appContext: Context) {
        context = appContext
    }

    // 简化的应用状态管理
    var isInitialized: Boolean = false

    fun start() {
        isInitialized = true
    }

    fun stop() {
        isInitialized = false
    }
}
