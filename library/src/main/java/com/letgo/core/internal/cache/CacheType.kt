package com.letgo.core.internal.cache

import android.app.ActivityManager
import android.content.Context

interface CacheType {

    /**
     * 返回框架内需要缓存的模块对应的 `id`
     *
     * @return
     */
    fun getCacheTypeId(): Int

    /**
     * 计算对应模块需要的缓存大小
     *
     * @return
     */
    fun calculateCacheSize(context: Context): Int


    companion object {
        const val RETROFIT_SERVICE_CACHE_TYPE_ID = 0

        val RETROFIT_SERVICE_CACHE: CacheType = object :
            CacheType {
            private val MAX_SIZE = 150
            private val MAX_SIZE_MULTIPLIER = 0.002f

            override fun getCacheTypeId(): Int {
                return RETROFIT_SERVICE_CACHE_TYPE_ID
            }

            override fun calculateCacheSize(context: Context): Int {
                val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val targetMemoryCacheSize = (activityManager.memoryClass.toFloat() * MAX_SIZE_MULTIPLIER * 1024f).toInt()
                return if (targetMemoryCacheSize >= MAX_SIZE) {
                    MAX_SIZE
                } else targetMemoryCacheSize
            }
        }
    }


}