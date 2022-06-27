package com.letgo.core.internal


interface IRepositoryManager {
    /**
     * 根据传入的 Class 获取对应的 Retrofit service
     *
     * @param service
     * @param <T>
     * @return
    </T> */
    fun <T> obtainRetrofitService(service: Class<T>): T

    /**
     * 清理所有缓存
     */
    fun clearAllCache()
}