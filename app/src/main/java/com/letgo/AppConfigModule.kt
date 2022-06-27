package com.letgo

import android.content.Context
import com.letgo.core.app.AppLifecycleCallbacks
import com.letgo.core.di.GlobalConfigModule
import com.letgo.core.di.RetrofitConfiguration
import com.letgo.core.internal.IConfigModule
import com.letgo.di.ComponentEntryPoint

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppConfigModule : IConfigModule {
    override val moduleName: String
        get() = "AppConfigModule"

    override fun applyOptions(context: Context, builder: GlobalConfigModule.Builder) {
        builder.baseUrl("https://api.github.com/")
            .retrofitConfiguration(object : RetrofitConfiguration {
                override fun configRetrofit(context: Context, builder: Retrofit.Builder) {
                    val moshi = ComponentEntryPoint.getEntryPoint(context).moshi()
                    builder.addConverterFactory(MoshiConverterFactory.create(moshi))
                }
            })
            .jsonConverter(JsonConverterImpl(context))
    }

    override fun addAppLifecycleCallback(lifecycleCallbacks: MutableList<AppLifecycleCallbacks>) {
        lifecycleCallbacks.add(AppLifecycleCallbacksImpl())
    }
}