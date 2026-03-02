package com.assistant.voip.core

import android.content.Context

object SpeechManager {

    private lateinit var context: Context

    fun initialize(appContext: Context) {
        context = appContext
    }

    // 简化的语音管理功能
    fun startListening() {
        // 简化版本：不执行任何操作
    }

    fun stopListening() {
        // 简化版本：不执行任何操作
    }
}
