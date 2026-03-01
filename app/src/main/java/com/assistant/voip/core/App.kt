package com.assistant.voip.core

import android.app.Application
import android.util.Log
import com.assistant.voip.BuildConfig
import com.assistant.voip.di.AppComponent
import com.assistant.voip.di.DaggerAppComponent
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class App : Application() {

    lateinit var appComponent: AppComponent
        private set

    override fun onCreate() {
        super.onCreate()
        initializeTimber()
        initializeDependencies()
        initializeCrashHandler()
        initializeAppState()
    }

    private fun initializeTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(object : Timber.Tree() {
                override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                    if (priority >= Log.WARN) {
                        // 生产环境只记录警告和错误
                        // 可以在这里集成Crashlytics或其他日志服务
                    }
                }
            })
        }
        Timber.d("Application onCreate")
    }

    private fun initializeDependencies() {
        appComponent = DaggerAppComponent.factory().create(applicationContext)
        Timber.d("Dependencies initialized")
    }

    private fun initializeCrashHandler() {
        if (BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
                Timber.e(exception, "Uncaught exception in thread: ${thread.name}")
            }
        }
    }

    private fun initializeAppState() {
        // 初始化应用状态
        AppStateManager.initialize(this)

        // 检查权限
        PermissionManager.checkRequiredPermissions(this)

        // 初始化网络状态监听
        NetworkManager.initialize(this)

        // 初始化语音服务
        SpeechManager.initialize(this)

        Timber.d("App state initialized")
    }

    override fun onTerminate() {
        super.onTerminate()
        Timber.d("Application onTerminate")
    }
}
