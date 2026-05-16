package com.example.a90phase.data.di

import com.example.a90phase.data.repositories.SleepRepositoryImpl
import com.example.a90phase.domain.repositories.SleepRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SleepRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSleepRepository(impl: SleepRepositoryImpl): SleepRepository
}
