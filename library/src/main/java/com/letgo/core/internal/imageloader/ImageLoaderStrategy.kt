package com.letgo.core.internal.imageloader

import android.view.View

interface ImageLoaderStrategy<T : ImageLoaderViewTarget<*>> {
    fun load(view: View, viewTarget: T)
    fun clear(view: View)
    fun pause()
    fun resume()
}

