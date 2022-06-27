package com.letgo.core.internal.imageloader

import android.view.View

class ImageLoaderReallyInterceptorChain<in T : ImageLoaderViewTarget<*>>(
    var mStrategy: ImageLoaderStrategy<ImageLoaderViewTarget<*>>,
    var interceptors: List<ImageLoaderInterceptor<ImageLoaderViewTarget<*>>>,
    var index: Int = 0
) : ImageLoaderInterceptor.Chain<T> {

    override fun proceed(view: View, viewTarget: T) {
        if (index >= interceptors.size) throw AssertionError()
        val reallyInterceptorChain = ImageLoaderReallyInterceptorChain<T>(
            mStrategy = mStrategy,
            interceptors = interceptors,
            index = index + 1
        ) as ImageLoaderInterceptor.Chain<ImageLoaderViewTarget<*>>
        val interceptor = interceptors[index]
        interceptor.intercept(reallyInterceptorChain, view, viewTarget)
    }

    override fun loadImage(view: View, viewTarget: T) {
        mStrategy.load(view, viewTarget)
    }

}