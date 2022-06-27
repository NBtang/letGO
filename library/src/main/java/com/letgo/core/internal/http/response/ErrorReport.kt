package com.letgo.core.internal.http.response

class ErrorReport(message: String?, val code: Int = 0) : Throwable(message) {
}