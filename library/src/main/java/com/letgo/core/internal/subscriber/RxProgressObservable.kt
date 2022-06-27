package com.letgo.core.internal.subscriber

import android.content.Context
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class RxProgressObservable(val msg:String,val cancelable:Boolean){
    protected val cancelObservable = PublishSubject.create<Boolean>()

    private fun getCancelObservable(): Observable<Boolean> {
        return cancelObservable
    }

    internal fun showProgress(context: Context):Observable<Boolean>{
        show(context,msg)
        return getCancelObservable()
    }

    abstract fun show(context: Context,msg:String)
    abstract fun isShowing():Boolean
    abstract fun dismiss()


    interface ProvideObservable{
        fun provide(msg:String,cancelable:Boolean): RxProgressObservable
    }
}