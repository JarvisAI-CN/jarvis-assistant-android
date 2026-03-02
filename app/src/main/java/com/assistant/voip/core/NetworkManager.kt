package com.assistant.voip.core

import android.content.Context

object NetworkManager {

    private lateinit var context: Context

    fun initialize(appContext: Context) {
        context = appContext
    }

    // 简化的网络状态管理
    fun isNetworkAvailable(): Boolean {
        // 简化版本：返回true表示网络可用
        return true
    }
}
