package com.letgo.core.internal


import com.letgo.core.internal.cache.Cache
import com.letgo.core.internal.cache.CacheType
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import retrofit2.Retrofit
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.lang.reflect.Type

class RepositoryManager(
    private val retrofit: Retrofit,
    cacheFactory: Cache.Factory
) :
    IRepositoryManager {

    private val mRetrofitServiceCache: Cache<String, Any> =
        cacheFactory.build(CacheType.RETROFIT_SERVICE_CACHE)

    @Synchronized
    override fun <T> obtainRetrofitService(service: Class<T>): T {
        return getRetrofitService(service)
    }


    @Synchronized
    override fun clearAllCache() {
    }

    private fun <T> createWrapperService(serviceClass: Class<T>): T {
        return Proxy.newProxyInstance(
            serviceClass.classLoader,
            arrayOf<Class<*>>(serviceClass),
            RetrofitServiceProxyHandler(
                retrofit = retrofit,
                serviceClass = serviceClass
            )
        ) as T
    }

    private fun <T> getRetrofitService(serviceClass: Class<T>): T {
        var retrofitService: T? = mRetrofitServiceCache[serviceClass.name] as T
        if (retrofitService == null) {
            retrofitService = createWrapperService(serviceClass)
            mRetrofitServiceCache.put(serviceClass.name, retrofitService!!)
        }
        return retrofitService
    }
}

class RetrofitServiceProxyHandler(
    private val retrofit: Retrofit,
    private val serviceClass: Class<*>
) : InvocationHandler {

    private var mRetrofitService: Any? = null

    override fun invoke(proxy: Any, method: Method, args: Array<out Any>): Any? {
        return when (method.returnType) {
            Observable::class.java -> {
                Observable.defer { method.invoke(getRetrofitService(), *args) as Observable<*> }
            }
            Single::class.java -> {
                Single.defer { method.invoke(getRetrofitService(), *args) as Single<*> }
            }
            Flowable::class.java -> {
                Flowable.defer { method.invoke(getRetrofitService(), *args) as Flowable<*> }
            }
            else -> method.invoke(getRetrofitService(), *args)
        }
    }

    private fun getRetrofitService(): Any {
        if (mRetrofitService == null) {
            mRetrofitService = retrofit.create(serviceClass)
        }
        return mRetrofitService!!
    }
}