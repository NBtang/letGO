package com.letgo.core.util

import android.app.Activity
import android.content.Context
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.jakewharton.rxbinding3.view.clicks
import com.letgo.core.AppManager
import com.letgo.core.di.GlobalEntryPoint
import com.letgo.core.internal.imageloader.ImageLoaderViewTarget
import es.dmoral.toasty.Toasty
import io.reactivex.ObservableTransformer
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * File
 */
fun File.makeDirs(): File {
    if (!this.exists()) {
        this.mkdirs()
    }
    return this
}

fun Context.getDefaultCacheFile(): File {
    return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
        var file: File? = null
        file = this.externalCacheDir//获取系统管理的sd卡缓存文件
        if (file == null) {//如果获取的文件为空,就使用自己定义的缓存文件夹做缓存路径
            file = File(getCacheFilePath(this))
            file.makeDirs()
        }
        file
    } else {
        this.cacheDir
    }
}


fun getCacheFilePath(context: Context): String {
    val packageName = context.packageName
    return Environment.getExternalStorageDirectory().path + packageName
}


/**
 * Toasty
 */
fun String.toasty(duration: Int = Toast.LENGTH_SHORT) {
    Toasty.normal(AppManager.instance.getApplicationContext(), this, duration).show()
}

fun String.errorToasty(duration: Int = Toast.LENGTH_SHORT) {
    Toasty.error(AppManager.instance.getApplicationContext(), this, duration).show()
}


/**
 * loadImage
 */
fun View.loadImage(viewTarget: ImageLoaderViewTarget<*>) {
    GlobalEntryPoint.getImageLoader(this.context).loadImage(this, viewTarget)
}

fun View.clearLoadImageRequest() {
    GlobalEntryPoint.getImageLoader(this.context).clear(this)
}

/**
 * post
 */
fun Context.post(runnable: Runnable) {
    ContextCompat.getMainExecutor(this).execute(runnable)
}

fun Fragment.post(runnable: Runnable) {
    ContextCompat.getMainExecutor(requireContext()).execute(runnable)
}

/**
 * cacheFile
 */
fun Context.cacheFile(): File {
    return GlobalEntryPoint.getCacheFile(this)
}

fun Fragment.cacheFile(): File {
    return GlobalEntryPoint.getCacheFile(requireContext())
}


/**
 * click
 */
fun View.clickObserver(skipDuration: Long = 1, block: (View) -> Unit): Disposable {
    val context = this.context as Activity
    return if (context is LifecycleOwner) {
        this.clicks()
            .throttleFirst(skipDuration, TimeUnit.SECONDS)
            .autoDisposable(context.onDestroyScope())
            .subscribe {
                block(this)
            }
    } else {
        this.clicks()
            .throttleFirst(skipDuration, TimeUnit.SECONDS)
            .subscribe {
                if (context.isFinishing || context.isDestroyed) {
                    return@subscribe
                }
                block(this)
            }
    }
}

fun <T> View.clickObserver(
    skipDuration: Long = 1,
    transformer: ObservableTransformer<Unit, T>,
    block: (T) -> Unit
): Disposable {
    val context = this.context as Activity
    return if (context is LifecycleOwner) {
        this.clicks()
            .throttleFirst(skipDuration, TimeUnit.SECONDS)
            .compose(transformer)
            .autoDisposable(context.onDestroyScope())
            .subscribe {
                block(it)
            }
    } else {
        this.clicks()
            .throttleFirst(skipDuration, TimeUnit.SECONDS)
            .compose(transformer)
            .subscribe {
                if (context.isFinishing || context.isDestroyed) {
                    return@subscribe
                }
                block(it)
            }
    }
}

fun View.clickObserver(skipDuration: Long = 1, consumer: Consumer<Unit>): Disposable {
    val context = this.context as Activity
    return if (context is LifecycleOwner) {
        this.clicks()
            .throttleFirst(skipDuration, TimeUnit.SECONDS)
            .autoDisposable(context.onDestroyScope())
            .subscribe(consumer)
    } else {
        this.clicks()
            .throttleFirst(skipDuration, TimeUnit.SECONDS)
            .subscribe(object : Consumer<Unit> {
                override fun accept(t: Unit?) {
                    if (context.isFinishing || context.isDestroyed) {
                        return
                    }
                    consumer.accept(Unit)
                }
            })
    }
}

fun <T> View.clickMapObserver(
    skipDuration: Long = 1,
    map: () -> T,
    consumer: Consumer<T>
): Disposable {
    val context = this.context as Activity
    return if (context is LifecycleOwner) {
        this.clicks()
            .throttleFirst(skipDuration, TimeUnit.SECONDS)
            .map {
                map.invoke()
            }
            .autoDisposable(context.onDestroyScope())
            .subscribe(consumer)
    } else {
        this.clicks()
            .throttleFirst(skipDuration, TimeUnit.SECONDS)
            .map {
                map.invoke()
            }
            .subscribe(object : Consumer<T> {
                override fun accept(t: T?) {
                    if (context.isFinishing || context.isDestroyed) {
                        return
                    }
                    consumer.accept(t)
                }
            })
    }
}

val Context.globalEntryPoint: GlobalEntryPoint.IGlobalEntryPoint
    get() {
        return GlobalEntryPoint.getEntryPoint(this)
    }
