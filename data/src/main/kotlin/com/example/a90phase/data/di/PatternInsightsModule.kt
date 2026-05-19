package com.example.a90phase.data.di

import com.example.a90phase.data.repositories.PatternInsightsRepositoryImpl
import com.example.a90phase.domain.repositories.PatternInsightsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PatternInsightsModule {

    @Binds
    @Singleton
    abstract fun bindPatternInsightsRepository(
        impl: PatternInsightsRepositoryImpl,
    ): PatternInsightsRepository
}
