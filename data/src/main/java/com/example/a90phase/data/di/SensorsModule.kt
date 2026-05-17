package com.example.a90phase.data.di

import com.example.a90phase.data.sensors.AccelerometerDataSourceImpl
import com.example.a90phase.domain.sensors.AccelerometerDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SensorsModule {

    @Binds
    @Singleton
    abstract fun bindAccelerometerDataSource(impl: AccelerometerDataSourceImpl): AccelerometerDataSource
}
