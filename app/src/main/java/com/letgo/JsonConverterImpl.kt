package com.letgo

import android.content.Context
import com.letgo.core.internal.json.JsonConverter
import com.letgo.di.ComponentEntryPoint
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.BufferedSource
import java.lang.reflect.Type

class JsonConverterImpl(context: Context) : JsonConverter {

    private val moshi: Moshi by lazy {
        ComponentEntryPoint.getEntryPoint(context).moshi()
    }

    override fun <T> toJson(any: T, type: Type): String {
        return moshi.adapter<T>(type).toJson(any)
    }

    override fun <T> fromJson(any: String, type: Type): T {
        return moshi.adapter<T>(type).fromJson(any)!!
    }

    override fun <T> fromJson(any: Any, type: Type): T {
        if (any is JsonReader) {
            return moshi.adapter<T>(type).fromJson(any)!!
        } else if (any is BufferedSource) {
            return moshi.adapter<T>(type).fromJson(any)!!
        }
        throw AssertionError("moshi fromJson 不支持的类型")
    }
}