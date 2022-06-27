package com.letgo.di

import android.content.Context
import com.squareup.moshi.Moshi
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

object ComponentEntryPoint {

    private var entryPoint: IComponentEntryPoint? = null

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface IComponentEntryPoint {
        fun moshi(): Moshi
    }

    fun getEntryPoint(context: Context): IComponentEntryPoint {
        if (entryPoint == null) {
            entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                IComponentEntryPoint::class.java
            )
        }
        return entryPoint!!
    }
}