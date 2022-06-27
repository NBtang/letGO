package com.letgo.core.internal.json

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.stream.JsonReader
import java.io.Reader
import java.lang.reflect.Type

class GsonConverter(private val gson: Gson) : JsonConverter {

    override fun <T> toJson(any: T, type: Type): String {
        return gson.toJson(any)
    }

    override fun <T> fromJson(any: String, type: Type): T {
        return gson.fromJson(any, type)
    }

    override fun <T> fromJson(any: Any, type: Type): T {
        if (any is JsonReader) {
            return gson.fromJson(any, type)
        } else if (any is JsonElement) {
            return gson.fromJson(any, type)
        } else if (any is Reader) {
            return gson.fromJson(any, type)
        }
        throw AssertionError("gson fromJson 不支持的类型")
    }
}