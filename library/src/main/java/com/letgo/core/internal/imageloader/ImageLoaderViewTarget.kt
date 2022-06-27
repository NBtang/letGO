package com.letgo.core.internal.imageloader

import android.graphics.drawable.Drawable


open class ImageLoaderViewTarget<T>(
    val scr: T,
    val placeholder: Int = 0,
    val errorPic: Int = 0,
    val isCenterCrop: Boolean = false,
    val isCircle: Boolean = false,
    val imageRadius: Int = 0,
    val targetWidth: Int = 0,
    val targetHeight: Int = 0,
    val cacheStrategy: Int = 0,
    val crossFade: Boolean = false
) {

    fun isImageRadius(): Boolean {
        return imageRadius > 0
    }

    open fun onLoadStarted(placeholder: Drawable?) {

    }

    open fun onLoadFailed(errorDrawable: Drawable?) {

    }

    open fun onResourceReady(resource: Drawable) {

    }
}

open class UrlImageLoaderViewTarget(
    url: String,
    placeholder: Int = 0,
    errorPic: Int = 0,
    isCenterCrop: Boolean = false,
    isCircle: Boolean = false,
    imageRadius: Int = 0,
    targetWidth: Int = 0,
    targetHeight: Int = 0,
    cacheStrategy: Int = 0,
    crossFade: Boolean = false
) : ImageLoaderViewTarget<String>(
    url,
    placeholder,
    errorPic,
    isCenterCrop,
    isCircle,
    imageRadius,
    targetWidth,
    targetHeight,
    cacheStrategy,
    crossFade
) {
    open fun copy(
        scr: String? = null,
        placeholder: Int? = null,
        errorPic: Int? = null,
        isCenterCrop: Boolean? = null,
        isCircle: Boolean? = null,
        imageRadius: Int? = null,
        targetWidth: Int? = null,
        targetHeight: Int? = null,
        cacheStrategy: Int? = null,
        crossFade: Boolean? = null
    ): UrlImageLoaderViewTarget {
        return UrlImageLoaderViewTarget(
            url = scr ?: this.scr,
            placeholder = placeholder ?: this.placeholder,
            errorPic = errorPic ?: this.errorPic,
            isCenterCrop = isCenterCrop ?: this.isCenterCrop,
            isCircle = isCircle ?: this.isCircle,
            imageRadius = imageRadius ?: this.imageRadius,
            targetWidth = targetWidth ?: this.targetWidth,
            targetHeight = targetHeight ?: this.targetHeight,
            cacheStrategy = cacheStrategy ?: this.cacheStrategy,
            crossFade = crossFade ?: this.crossFade,
        )
    }
}

fun String.toImageLoaderViewTarget(): ImageLoaderViewTarget<String> {
    return UrlImageLoaderViewTarget(this)
}