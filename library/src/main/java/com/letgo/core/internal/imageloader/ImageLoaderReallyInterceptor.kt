package com.letgo.core.internal.imageloader

import android.view.View

class ImageLoaderReallyInterceptor<T : ImageLoaderViewTarget<*>> :
    ImageLoaderInterceptor<T> {

    override fun intercept(chain: ImageLoaderInterceptor.Chain<T>, view: View, viewTarget: T) {
        chain.loadImage(view, viewTarget)
    }
}