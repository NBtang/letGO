package com.letgo.core.di

import android.content.Context
import android.text.TextUtils
import androidx.annotation.Keep
import com.google.gson.JsonDeserializer
import com.letgo.core.app.AppLifecycleCallbacks
import com.letgo.core.internal.cache.Cache
import com.letgo.core.internal.http.response.BaseResponseBean
import com.letgo.core.internal.http.response.BaseResponseBeanDeserializer
import com.letgo.core.internal.imageloader.ImageLoaderStrategy
import com.letgo.core.internal.imageloader.ImageLoaderViewTarget
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.letgo.core.internal.IConfigModule
import com.letgo.core.internal.IRepositoryManager
import com.letgo.core.internal.ManifestParser
import com.letgo.core.internal.http.HttpLoggingInterceptor
import com.letgo.core.internal.json.JsonConverter
import com.letgo.core.internal.rxerrorhandler.handler.listener.ResponseErrorListener
import okhttp3.HttpUrl
import java.io.File
import java.lang.reflect.InvocationTargetException
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlobalComponent {

    private var mGlobalConfigModule: GlobalConfigModule? = null
    private var mAppLifecycleCallbacks: MutableList<AppLifecycleCallbacks> = mutableListOf()

    private var mAutoRegisterModules: MutableList<IConfigModule>? = null

    private var initialized: Boolean = false

    @Synchronized
    fun init(context: Context, registerModules: List<IConfigModule>) {
        if (initialized) {
            return
        }
        initialized = true
        loadAutoRegister()
        val builder =
            GlobalConfigModule.builder()
        val configModules = mAutoRegisterModules ?: ManifestParser(context).parse()

        val filterConfigModules = HashMap<String, IConfigModule>()
        configModules.addAll(registerModules)
        configModules.forEach {
            filterConfigModules[it.moduleName] = it
        }
        filterConfigModules.values.forEach {
            it.applyOptions(context, builder)
            it.addAppLifecycleCallback(mAppLifecycleCallbacks)
        }
        mGlobalConfigModule = builder.build()
        mAutoRegisterModules = null
        configModules.clear()
        filterConfigModules.clear()
    }

    @Keep
    private fun loadAutoRegister() {

    }

    @Keep
    private fun register(className: String) {
        if (!TextUtils.isEmpty(className)) {
            try {
                val clazz = Class.forName(className)
                val obj = clazz.getConstructor().newInstance()
                if (obj is IConfigModule) {
                    mAutoRegisterModules = (mAutoRegisterModules ?: mutableListOf())
                    mAutoRegisterModules?.add(obj)
                }
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }

        }
    }

    internal fun getAppLifecycleCallbacks(): List<AppLifecycleCallbacks> {
        return mAppLifecycleCallbacks
    }

    internal fun addAppLifecycleCallback(vararg lifecycleCallbacks: AppLifecycleCallbacks) {
        mAppLifecycleCallbacks.addAll(lifecycleCallbacks)
    }

    @Singleton
    @Provides
    fun provideConfigModuleManage(): GlobalConfigModule {
        if (mGlobalConfigModule == null) {
            mGlobalConfigModule = GlobalConfigModule.builder()
                .build()
        }
        return mGlobalConfigModule!!
    }

    @Singleton
    @Provides
    fun provideBaseUrl(
        globalConfigModule: GlobalConfigModule
    ): HttpUrl {
        return globalConfigModule.provideBaseUrl()
    }

    @Singleton
    @Provides
    @CacheFile
    fun provideCacheFile(
        @ApplicationContext context: Context,
        globalConfigModule: GlobalConfigModule
    ): File {
        return globalConfigModule.provideCacheFile(context)
    }

    @Singleton
    @Provides
    fun provideCacheFactory(
        @ApplicationContext context: Context,
        globalConfigModule: GlobalConfigModule
    ): Cache.Factory {
        return globalConfigModule.provideCacheFactory(context)
    }

    @Singleton
    @Provides
    fun provideRetrofitConfigurations(
        globalConfigModule: GlobalConfigModule
    ): RetrofitConfiguration? {
        return globalConfigModule.provideRetrofitConfiguration()
    }

    @Singleton
    @Provides
    fun provideOkHttpConfigurations(
        globalConfigModule: GlobalConfigModule
    ): OkHttpConfiguration? {
        return globalConfigModule.provideOkHttpConfiguration()
    }

    @Singleton
    @Provides
    fun provideResponseErrorListener(
        globalConfigModule: GlobalConfigModule
    ): ResponseErrorListener {
        return globalConfigModule.provideResponseErrorListener()
    }

    @Singleton
    @Provides
    fun provideGsonConfiguration(
        globalConfigModule: GlobalConfigModule
    ): GsonConfiguration? {
        return globalConfigModule.provideGsonConfiguration()
    }


    @Singleton
    @Provides
    fun provideRxProgressConfiguration(
        globalConfigModule: GlobalConfigModule
    ): RxProgressConfiguration {
        return globalConfigModule.provideRxProgressConfiguration()
    }

    @Singleton
    @Provides
    fun provideImageLoaderStrategy(
        globalConfigModule: GlobalConfigModule
    ): ImageLoaderStrategy<ImageLoaderViewTarget<*>>? {
        return globalConfigModule.provideImageLoaderStrategy()
    }

    @Singleton
    @Provides
    fun provideImageLoaderConfiguration(
        globalConfigModule: GlobalConfigModule
    ): ImageLoaderConfiguration? {
        return globalConfigModule.provideImageLoaderConfiguration()
    }

    @Singleton
    @Provides
    fun provideResponseDeserializer(
        globalConfigModule: GlobalConfigModule
    ): JsonDeserializer<BaseResponseBean<*>> {
        return globalConfigModule.provideResponseDeserializer() ?: BaseResponseBeanDeserializer()
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(
        globalConfigModule: GlobalConfigModule
    ): HttpLoggingInterceptor? {
        return globalConfigModule.provideHttpLoggingInterceptor()
    }

    @Singleton
    @Provides
    fun provideRepositoryManager(
        @EmbedRepositoryManager repositoryManager: IRepositoryManager,
        globalConfigModule: GlobalConfigModule
    ): IRepositoryManager {
        return globalConfigModule.provideRepositoryManager() ?: repositoryManager
    }

    @Singleton
    @Provides
    fun provideJsonConverter(
        @EmbedJsonConverter jsonConverter: JsonConverter,
        globalConfigModule: GlobalConfigModule
    ): JsonConverter {
        return globalConfigModule.provideJsonConverter() ?: jsonConverter
    }

}