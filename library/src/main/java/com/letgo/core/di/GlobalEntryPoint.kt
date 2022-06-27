package com.letgo.core.di

import android.content.Context
import com.google.gson.Gson
import com.letgo.core.internal.imageloader.ImageLoader
import com.letgo.core.internal.json.JsonConverter
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.io.File

object GlobalEntryPoint {
    private var entryPoint: IGlobalEntryPoint? = null

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface IGlobalEntryPoint {
        fun imageLoader(): ImageLoader

        fun okHttpClient(): OkHttpClient

        fun gson(): Gson

        fun jsonConverter(): JsonConverter

        @CacheFile
        fun cacheFile(): File

    }

    fun getImageLoader(context: Context): ImageLoader {
        return getEntryPoint(context)
            .imageLoader()
    }

    fun getOkHttpClient(context: Context): OkHttpClient {
        return getEntryPoint(context)
            .okHttpClient()
    }

    fun getCacheFile(context: Context): File {
        return getEntryPoint(context)
            .cacheFile()
    }

    fun getGson(context: Context): Gson {
        return getEntryPoint(context)
            .gson()
    }

    fun getJsonConverter(context: Context): JsonConverter {
        return getEntryPoint(context)
            .jsonConverter()
    }


    internal fun getEntryPoint(context: Context): IGlobalEntryPoint {
        if (entryPoint == null) {
            entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                IGlobalEntryPoint::class.java
            )
        }
        return entryPoint!!
    }
}