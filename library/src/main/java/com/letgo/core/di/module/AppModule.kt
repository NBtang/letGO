package com.letgo.core.di.module

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.letgo.core.AppManager
import com.letgo.core.di.EmbedJsonConverter
import com.letgo.core.di.EmbedRepositoryManager
import com.letgo.core.di.GsonConfiguration
import com.letgo.core.di.ImageLoaderConfiguration
import com.letgo.core.internal.IRepositoryManager
import com.letgo.core.internal.RepositoryManager
import com.letgo.core.internal.cache.Cache
import com.letgo.core.internal.http.response.BaseResponseBean
import com.letgo.core.internal.imageloader.ImageLoader
import com.letgo.core.internal.imageloader.ImageLoaderStrategy
import com.letgo.core.internal.imageloader.ImageLoaderViewTarget
import com.letgo.core.internal.json.GsonConverter
import com.letgo.core.internal.json.JsonConverter

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

import retrofit2.Retrofit
import java.lang.reflect.Type
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideGson(
        @ApplicationContext context: Context,
        configuration: GsonConfiguration?,
        responseDeserializer: JsonDeserializer<BaseResponseBean<*>>
    ): Gson {
        val builder = GsonBuilder()
        configuration?.configGson(context, builder)
        builder
            .registerTypeAdapter(
                BaseResponseBean::class.java,
                responseDeserializer
            ).serializeNulls()
        return builder.create()
    }

    @Singleton
    @Provides
    fun provideAppManager(): AppManager {
        return AppManager.instance
    }

    @Singleton
    @Provides
    @EmbedRepositoryManager
    fun provideRepositoryManager(
        retrofit: Retrofit,
        cacheFactory: Cache.Factory
    ): IRepositoryManager {
        return RepositoryManager(
            retrofit,
            cacheFactory
        )
    }

    @Singleton
    @Provides
    fun provideImageLoader(
        @ApplicationContext context: Context,
        strategy: ImageLoaderStrategy<ImageLoaderViewTarget<*>>?,
        configuration: ImageLoaderConfiguration?
    ): ImageLoader {
        val imageLoader =
            ImageLoader(strategy)
        configuration?.configImageLoader(context, imageLoader)
        return imageLoader
    }

    @Singleton
    @Provides
    @EmbedJsonConverter
    fun provideJsonConverter(
        gson: Gson
    ): JsonConverter {
        return GsonConverter(gson)
    }
}
