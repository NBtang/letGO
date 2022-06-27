package com.letgo.core.di

import android.content.Context
import android.text.TextUtils
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.letgo.core.internal.IRepositoryManager
import com.letgo.core.internal.cache.Cache
import com.letgo.core.internal.cache.CacheFactory
import com.letgo.core.internal.http.BaseUrl
import com.letgo.core.internal.http.HttpLoggingInterceptor
import com.letgo.core.internal.http.response.BaseResponseBean
import com.letgo.core.internal.imageloader.ImageLoader
import com.letgo.core.internal.imageloader.ImageLoaderStrategy
import com.letgo.core.internal.imageloader.ImageLoaderViewTarget
import com.letgo.core.internal.json.JsonConverter
import com.letgo.core.internal.rxerrorhandler.handler.listener.ResponseErrorListener
import com.letgo.core.internal.subscriber.RxProgressObservable
import com.letgo.core.internal.subscriber.RxProgressObservableImpl
import com.letgo.core.util.getDefaultCacheFile
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import timber.log.Timber
import java.io.File
import java.lang.Exception
import java.util.concurrent.ExecutorService


class GlobalConfigModule private constructor(builder: Builder) {

    private var mApiUrl: HttpUrl? = null
    private var mBaseUrl: BaseUrl? = null
    private var mCacheFile: File? = null
    private var mCacheFactory: Cache.Factory? = null
    private var mRetrofitConfiguration: RetrofitConfiguration? = null
    private var mOkHttpConfiguration: OkHttpConfiguration? = null
    private var mErrorListener: ResponseErrorListener? = null
    private var mGsonConfiguration: GsonConfiguration? = null
    private var mRxProgressConfiguration: RxProgressConfiguration? = null
    private var mLoaderStrategy: ImageLoaderStrategy<ImageLoaderViewTarget<*>>? = null
    private var mImageLoaderConfiguration: ImageLoaderConfiguration? = null
    private var mResponseDeserializer: JsonDeserializer<BaseResponseBean<*>>? = null
    private var mRepositoryManager: IRepositoryManager? = null
    private var mEnableHttpLogging: Boolean = false
    private var mJsonConverter: JsonConverter? = null
    private var mCustomConfigurationWrap: CustomConfigurationWrap? = null

    init {
        this.mApiUrl = builder.apiUrl
        this.mBaseUrl = builder.url
        this.mCacheFile = builder.cacheFile
        this.mCacheFactory = builder.cacheFactory
        this.mRetrofitConfiguration = builder.retrofitConfiguration
        this.mOkHttpConfiguration = builder.okHttpConfiguration
        this.mErrorListener = builder.responseErrorListener
        this.mGsonConfiguration = builder.gsonConfiguration
        this.mRxProgressConfiguration = builder.rxProgressConfiguration
        this.mLoaderStrategy = builder.loaderStrategy
        this.mImageLoaderConfiguration = builder.imageLoaderConfiguration
        this.mResponseDeserializer = builder.responseDeserializer
        this.mRepositoryManager = builder.repositoryManager
        this.mEnableHttpLogging = builder.enableHttpLogging
        this.mJsonConverter = builder.jsonConverter
        this.mCustomConfigurationWrap = builder.customConfigurationWrap
    }

    fun provideBaseUrl(): HttpUrl {
        val url = mBaseUrl?.url()
        return url ?: (mApiUrl ?: throw AssertionError("url empty"))
    }

    fun provideCacheFile(context: Context): File {
        return mCacheFile ?: context.getDefaultCacheFile()
    }

    fun provideCacheFactory(context: Context): Cache.Factory {
        return mCacheFactory ?: CacheFactory(
            context
        )
    }

    fun provideRetrofitConfiguration(): RetrofitConfiguration? {
        return mRetrofitConfiguration
    }

    fun provideOkHttpConfiguration(): OkHttpConfiguration? {
        return mOkHttpConfiguration
    }

    fun provideResponseErrorListener(): ResponseErrorListener {
        return mErrorListener ?: ResponseErrorListener.EMPTY
    }

    fun provideGsonConfiguration(): GsonConfiguration? {
        return mGsonConfiguration
    }

    fun provideRxProgressConfiguration(): RxProgressConfiguration {
        return mRxProgressConfiguration ?: (object : RxProgressConfiguration {
            override fun provideRxProgressObservable(
                msg: String,
                cancelable: Boolean
            ): RxProgressObservable {
                return RxProgressObservableImpl(
                    msg,
                    cancelable
                )
            }
        })
    }

    fun provideImageLoaderStrategy(): ImageLoaderStrategy<ImageLoaderViewTarget<*>>? {
        var glideImageLoaderStrategy: ImageLoaderStrategy<ImageLoaderViewTarget<*>>? = null
        try {
            val clazz = Class.forName("com.letgo.core.glide.GlideImageLoaderStrategy")
            glideImageLoaderStrategy =
                clazz.newInstance() as ImageLoaderStrategy<ImageLoaderViewTarget<*>>
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return mLoaderStrategy ?: glideImageLoaderStrategy
    }

    fun provideImageLoaderConfiguration(): ImageLoaderConfiguration? {
        return mImageLoaderConfiguration
    }

    fun provideResponseDeserializer(): JsonDeserializer<BaseResponseBean<*>>? {
        return mResponseDeserializer
    }

    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor? {
        return if (mEnableHttpLogging) HttpLoggingInterceptor { _, message ->
            Timber.tag("OkHttp")
            Timber.d(message)
        }.setLevel(
            HttpLoggingInterceptor.Level.BODY
        ) else null
    }

    fun provideRepositoryManager(): IRepositoryManager? {
        return mRepositoryManager
    }

    fun provideJsonConverter(): JsonConverter? {
        return mJsonConverter
    }

    fun provideCustomConfigurationWrap(): CustomConfigurationWrap? {
        return mCustomConfigurationWrap
    }


    class Builder {
        internal var apiUrl: HttpUrl? = null
        internal var url: BaseUrl? = null
        internal var cacheFile: File? = null
        internal var cacheFactory: Cache.Factory? = null
        internal var retrofitConfiguration: RetrofitConfiguration? = null
        internal var okHttpConfiguration: OkHttpConfiguration? = null
        internal var responseErrorListener: ResponseErrorListener? = null
        internal var executorService: ExecutorService? = null
        internal var gsonConfiguration: GsonConfiguration? = null
        internal var rxProgressConfiguration: RxProgressConfiguration? = null
        internal var loaderStrategy: ImageLoaderStrategy<ImageLoaderViewTarget<*>>? = null
        internal var imageLoaderConfiguration: ImageLoaderConfiguration? = null
        internal var responseDeserializer: JsonDeserializer<BaseResponseBean<*>>? = null
        internal var repositoryManager: IRepositoryManager? = null
        internal var enableHttpLogging: Boolean = false
        internal var jsonConverter: JsonConverter? = null
        internal var customConfigurationWrap: CustomConfigurationWrap? = null

        fun baseUrl(baseUrl: String): Builder {//基础url
            if (TextUtils.isEmpty(baseUrl)) {
                throw NullPointerException("BaseUrl can not be empty")
            }
            this.apiUrl = baseUrl.toHttpUrlOrNull()
            return this
        }

        fun baseUrl(baseUrl: BaseUrl): Builder {
            this.url = baseUrl
            return this
        }

        fun cacheFile(cacheFile: File): Builder {
            this.cacheFile = cacheFile
            return this
        }

        fun cacheFactory(cacheFactory: Cache.Factory): Builder {
            this.cacheFactory = cacheFactory
            return this
        }

        fun retrofitConfiguration(retrofitConfiguration: RetrofitConfiguration): Builder {
            if (this.retrofitConfiguration == null) {
                this.retrofitConfiguration = RetrofitConfigurationImpl()
            }
            (this.retrofitConfiguration as RetrofitConfigurationImpl)
                .addRetrofitConfiguration(retrofitConfiguration)
            return this
        }

        fun okHttpConfiguration(okHttpConfiguration: OkHttpConfiguration): Builder {
            if (this.okHttpConfiguration == null) {
                this.okHttpConfiguration = OkHttpConfigurationImpl()
            }
            (this.okHttpConfiguration as OkHttpConfigurationImpl)
                .addOkHttpConfiguration(okHttpConfiguration)
            return this
        }

        fun responseErrorListener(listener: ResponseErrorListener): Builder {//处理所有RxJava的onError逻辑
            this.responseErrorListener = listener
            return this
        }

        fun executorService(executorService: ExecutorService): Builder {
            this.executorService = executorService
            return this
        }

        fun gsonConfiguration(gsonConfiguration: GsonConfiguration): Builder {
            if (this.gsonConfiguration == null) {
                this.gsonConfiguration = GsonConfigurationImpl()
            }
            (this.gsonConfiguration as GsonConfigurationImpl)
                .addGsonConfiguration(gsonConfiguration)
            return this
        }

        fun rxProgressConfiguration(rxProgressConfiguration: RxProgressConfiguration): Builder {
            this.rxProgressConfiguration = rxProgressConfiguration
            return this
        }

        fun imageLoaderStrategy(loaderStrategy: ImageLoaderStrategy<ImageLoaderViewTarget<*>>): Builder {//用来请求网络图片
            this.loaderStrategy = loaderStrategy
            return this
        }

        fun imageLoaderConfiguration(imageLoaderConfiguration: ImageLoaderConfiguration): Builder {
            this.imageLoaderConfiguration = imageLoaderConfiguration
            return this
        }

        fun responseDeserializer(responseDeserializer: JsonDeserializer<BaseResponseBean<*>>): Builder {
            this.responseDeserializer = responseDeserializer
            return this
        }

        fun enableHttpLogging(enable: Boolean = true): Builder {
            this.enableHttpLogging = enable
            return this
        }

        fun repositoryManager(repositoryManager: IRepositoryManager): Builder {
            this.repositoryManager = repositoryManager
            return this
        }

        fun jsonConverter(jsonConverter: JsonConverter): Builder {
            this.jsonConverter = jsonConverter
            return this
        }

        fun customConfiguration(customConfiguration: CustomConfiguration): Builder {
            if (this.customConfigurationWrap == null) {
                customConfigurationWrap = CustomConfigurationWrapImpl()
            }
            (customConfigurationWrap as CustomConfigurationWrapImpl)
                .addCustomConfiguration(customConfiguration)
            return this
        }

        fun build(): GlobalConfigModule {
            return GlobalConfigModule(this)
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}

interface GsonConfiguration {
    fun configGson(context: Context, gsonBuilder: GsonBuilder)
}

interface RxProgressConfiguration {
    fun provideRxProgressObservable(msg: String, cancelable: Boolean): RxProgressObservable
}

interface RetrofitConfiguration {
    fun configRetrofit(context: Context, builder: Retrofit.Builder)
}

interface OkHttpConfiguration {
    fun configOkHttp(context: Context, builder: OkHttpClient.Builder)
}

interface ImageLoaderConfiguration {
    fun configImageLoader(context: Context, imageLoader: ImageLoader)
}

class OkHttpConfigurationImpl : OkHttpConfiguration {
    private var mOkHttpConfigurations: MutableList<OkHttpConfiguration>? = null

    fun addOkHttpConfiguration(okHttpConfiguration: OkHttpConfiguration) {
        if (mOkHttpConfigurations == null) {
            mOkHttpConfigurations = mutableListOf()
        }
        mOkHttpConfigurations!!.add(okHttpConfiguration)
    }

    override fun configOkHttp(context: Context, builder: OkHttpClient.Builder) {
        mOkHttpConfigurations?.forEach {
            it.configOkHttp(context, builder)
        }
    }
}


class RetrofitConfigurationImpl : RetrofitConfiguration {
    private var mRetrofitConfigurations: MutableList<RetrofitConfiguration>? = null

    fun addRetrofitConfiguration(retrofitConfiguration: RetrofitConfiguration) {
        if (mRetrofitConfigurations == null) {
            mRetrofitConfigurations = mutableListOf()
        }
        mRetrofitConfigurations!!.add(retrofitConfiguration)
    }

    override fun configRetrofit(context: Context, builder: Retrofit.Builder) {
        mRetrofitConfigurations?.forEach {
            it.configRetrofit(context, builder)
        }
    }
}


class GsonConfigurationImpl : GsonConfiguration {
    private var mGsonConfigurations: MutableList<GsonConfiguration>? = null

    fun addGsonConfiguration(gsonConfiguration: GsonConfiguration) {
        if (mGsonConfigurations == null) {
            mGsonConfigurations = mutableListOf()
        }
        mGsonConfigurations!!.add(gsonConfiguration)
    }

    override fun configGson(context: Context, gsonBuilder: GsonBuilder) {
        mGsonConfigurations?.forEach {
            it.configGson(context, gsonBuilder)
        }
    }
}

class CustomConfigurationWrapImpl : CustomConfigurationWrap {
    private var mCustomConfigurations: MutableList<CustomConfiguration>? = null

    fun addCustomConfiguration(customConfiguration: CustomConfiguration) {
        if (mCustomConfigurations == null) {
            mCustomConfigurations = mutableListOf()
        }
        mCustomConfigurations!!.add(customConfiguration)
    }

    override val customConfigurations: List<CustomConfiguration>?
        get() = mCustomConfigurations
}

interface CustomConfigurationWrap {
    val customConfigurations: List<CustomConfiguration>?
}

interface CustomConfiguration