package com.letgo.core.internal

import android.content.Context
import com.letgo.core.app.AppLifecycleCallbacks
import com.letgo.core.di.GlobalConfigModule


interface IConfigModule {
    val moduleName: String

    fun applyOptions(
        context: Context,
        builder: GlobalConfigModule.Builder
    )

    fun addAppLifecycleCallback(lifecycleCallbacks: MutableList<AppLifecycleCallbacks>) {}

}