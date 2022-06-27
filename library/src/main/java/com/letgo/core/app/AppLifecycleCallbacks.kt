package com.letgo.core.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration

interface AppLifecycleCallbacks {
    fun attachBaseContext(base: Context?, application: Application)
    fun onCreate(application: Application)
    fun onLowMemory(application: Application)
    fun onTrimMemory(level: Int, application: Application)
    fun onConfigurationChanged(newConfig: Configuration, application: Application)
    fun onTerminate(application: Application)
}

open class DummyAppLifecycleCallbacks :
    AppLifecycleCallbacks {
    override fun attachBaseContext(base: Context?, application: Application) {
    }

    override fun onCreate(application: Application) {
    }

    override fun onLowMemory(application: Application) {
    }

    override fun onTrimMemory(level: Int, application: Application) {
    }

    override fun onConfigurationChanged(newConfig: Configuration, application: Application) {
    }

    override fun onTerminate(application: Application) {
    }
}