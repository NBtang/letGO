package com.letgo.core.di.module

import android.content.Context
import com.google.gson.Gson
import com.letgo.core.di.OkHttpConfiguration
import com.letgo.core.di.RetrofitConfiguration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.letgo.core.internal.http.HttpLoggingInterceptor
import com.letgo.core.internal.json.ConverterFactory
import com.letgo.core.internal.json.GsonConverter
import com.letgo.core.internal.json.JsonConverter
import com.letgo.core.internal.rxerrorhandler.core.RxErrorHandler
import com.letgo.core.internal.rxerrorhandler.handler.listener.ResponseErrorListener
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClientModule {

    @Provides
    fun provideRetrofitBuilder(): Retrofit.Builder {
        return Retrofit.Builder()
    }

    @Provides
    fun provideOkHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder()
    }

    @Singleton
    @Provides
    fun provideRetrofit(
        @ApplicationContext context: Context,
        configuration: RetrofitConfiguration?,
        builder: Retrofit.Builder,
        client: OkHttpClient,
        httpUrl: HttpUrl,
        gson: Gson,
        jsonConverter: JsonConverter
    ): Retrofit {
        val converterFactory = if (jsonConverter is GsonConverter) {
            GsonConverterFactory.create(gson)
        } else {
            ConverterFactory.create(jsonConverter)
        }
        builder
            .baseUrl(httpUrl)//域名
            .client(client);//设置 OkHttp
        configuration?.configRetrofit(context, builder)
        builder
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//使用 RxJava
            .addConverterFactory(converterFactory)//使用封装过的json解析器
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        configuration: OkHttpConfiguration?,
        builder: OkHttpClient.Builder,
        httpLoggingInterceptor: HttpLoggingInterceptor?
    ): OkHttpClient {
        builder
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
        configuration?.configOkHttp(context, builder)
        httpLoggingInterceptor?.let { builder.addInterceptor(it) }
        return builder.build()
    }

    @Singleton
    @Provides
    fun provideRxErrorHandler(
        @ApplicationContext context: Context,
        listener: ResponseErrorListener
    ): RxErrorHandler {
        return RxErrorHandler
            .builder()
            .with(context)
            .responseErrorListener(listener)
            .build()
    }
}