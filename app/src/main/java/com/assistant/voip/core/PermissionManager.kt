package com.assistant.voip.core

import android.content.Context

object PermissionManager {

    private lateinit var context: Context

    fun initialize(appContext: Context) {
        context = appContext
    }

    fun checkRequiredPermissions(context: Context) {
        // 简化版本：不检查权限
    }
}
