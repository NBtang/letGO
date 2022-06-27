package com.letgo.core.internal.imageloader

import android.view.View

interface ImageLoaderInterceptor<K : ImageLoaderViewTarget<*>> {

    fun intercept(chain: Chain<K>, view: View, viewTarget: K)

    interface Chain<in T : ImageLoaderViewTarget<*>> {
        fun proceed(view: View, viewTarget: T)
        fun loadImage(view: View, viewTarget: T)
    }
}