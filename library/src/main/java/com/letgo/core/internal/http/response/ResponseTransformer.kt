package com.letgo.core.internal.http.response

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

open class ResponseTransformer<T>(private val default: T? = null) :
    ObservableTransformer<BaseResponseBean<T>, T> {

    override fun apply(upstream: Observable<BaseResponseBean<T>>): ObservableSource<T> {
        return upstream.flatMap { t ->
            if (t.success) {
                if (t.data == null) {
                    if (default != null) {
                        return@flatMap Observable.just(default)
                    } else {
                        return@flatMap Observable.error<T>(
                            ErrorReport(
                                message = "Response success but data is null",
                                code = t.code
                            )
                        )
                    }
                }
                return@flatMap Observable.just(t.data)
            } else {
                return@flatMap Observable.error<T>(
                    ErrorReport(
                        message = t.message,
                        code = t.code
                    )
                )
            }
        }
    }
}