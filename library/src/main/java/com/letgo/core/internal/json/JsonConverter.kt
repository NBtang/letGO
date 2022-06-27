package com.letgo.core.internal.json

import java.lang.reflect.Type

interface JsonConverter {
    fun <T> toJson(any: T, type: Type): String
    fun <T> fromJson(any: String, type: Type): T
    fun <T> fromJson(any: Any, type: Type): T
}

