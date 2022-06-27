package com.letgo.core.internal.http

import okhttp3.HttpUrl

interface BaseUrl {
    fun url(): HttpUrl?
}