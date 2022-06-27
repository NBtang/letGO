package com.letgo.core.internal.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> LiveData<T>.asState(block: (() -> T)? = null): LiveDataProperty<T> {
    val default = block?.invoke()
    return LiveDataProperty(this, default)
}

fun <T> MutableLiveData<T>.asMutableState(block: (() -> T)? = null): LiveDataMutableProperty<T> {
    val default = block?.invoke()
    return LiveDataMutableProperty(this, default)
}

class LiveDataProperty<T>(private val liveData: LiveData<T>, private val default: T? = null) :
    ReadOnlyProperty<Any?, T?> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return liveData.value ?: default
    }
}

class LiveDataMutableProperty<T>(
    private val liveData: MutableLiveData<T>,
    private val default: T? = null
) : ReadWriteProperty<Any?, T?> {
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        liveData.value = value
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return liveData.value ?: default
    }
}