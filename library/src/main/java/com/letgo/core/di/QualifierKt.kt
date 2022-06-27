package com.letgo.core.di

import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheFile


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class EmbedRepositoryManager


@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class EmbedJsonConverter