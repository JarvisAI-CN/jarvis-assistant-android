package com.assistant.voip.core

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeTimber()
        initializeAppState()
    }

    private fun initializeTimber() {
        if (true) { // 暂时强制使用DEBUG模式
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    if (priority >= Log.WARN) {
                        // 生产环境只记录警告和错误
                    }
                }
            })
        }
        Timber.d("Application onCreate")
    }

    private fun initializeAppState() {
        // 简化初始化，移除未实现的功能
        Timber.d("App state initialized")
    }

    override fun onTerminate() {
        super.onTerminate()
        Timber.d("Application onTerminate")
    }
}
