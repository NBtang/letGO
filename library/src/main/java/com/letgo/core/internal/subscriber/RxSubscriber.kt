package com.letgo.core.internal.subscriber


import com.letgo.core.AppManager
import com.letgo.core.di.RxProgressConfiguration
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import com.letgo.core.internal.rxerrorhandler.core.RxErrorHandler
import com.letgo.core.internal.rxerrorhandler.handler.ErrorHandlerFactory


abstract class RxSubscriber<T>(
    msg: String = "",
    showProgress: Boolean = true,
    cancelable: Boolean = true
) : DisposableObserver<T>() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface RxSubscriberEntryPoint {
        fun rxErrorHandler(): RxErrorHandler
        fun rxProgressConfiguration(): RxProgressConfiguration
    }

    private var progressObservable: RxProgressObservable? = null
    private var cancelDisposable: Disposable? = null

    private val mHandlerFactory: ErrorHandlerFactory

    init {
        val rxSubscriberEntryPoint = EntryPointAccessors.fromApplication(
            AppManager.instance.getApplicationContext(),
            RxSubscriberEntryPoint::class.java)
        val rxProgressConfiguration = rxSubscriberEntryPoint.rxProgressConfiguration()
        mHandlerFactory = rxSubscriberEntryPoint.rxErrorHandler().handlerFactory
        if (showProgress && msg.isNotEmpty()) {
            progressObservable = rxProgressConfiguration.provideRxProgressObservable(msg, cancelable)
        }
    }

    override fun onStart() {
        super.onStart()
        val instance = this
        val activity = AppManager.instance.getTopActivity()
        if (activity != null) {
            val cancelObservable = progressObservable?.showProgress(activity)
            cancelDisposable = cancelObservable?.let { observable ->
                observable.observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it) {
                            instance.dispose()
                        }
                    }
            }
        }
    }

    override fun onNext(t: T) {
        dismissLoadingDialog()
        _onNext(t)
    }

    override fun onComplete() {
        dismissLoadingDialog()
    }

    final override fun onError(e: Throwable) {
        dismissLoadingDialog()
        e.printStackTrace()
        _onError(e)
    }

    open fun _onError(e: Throwable) {
        //如果你某个地方不想使用全局错误处理,则重写 _onError(Throwable) 并将 super._onError(e); 删掉
        //如果你不仅想使用全局错误处理,还想加入自己的逻辑,则重写 _onError(Throwable) 并在 super._onError(e); 后面加入自己的逻辑
        mHandlerFactory.handleError(e)
    }

    private fun dismissLoadingDialog() {
        cancelDisposable?.apply {
            if (!isDisposed) {
                dispose()
            }
        }
        progressObservable?.apply {
            if (isShowing()) {
                dismiss()
            }
        }
    }

    abstract fun _onNext(t: T)

}