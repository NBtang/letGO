package com.letgo.core.internal.http.response

class BaseResponseBean<T> : ResponseBean<T>() {
    var code: Int = 0
    var message: String = ""
    var success: Boolean = false

    override fun toString(): String {
        return "BaseResponseBean(code = ${code}, message = ${message}, success = ${success}, data = ${data?.toString()})"
    }
}