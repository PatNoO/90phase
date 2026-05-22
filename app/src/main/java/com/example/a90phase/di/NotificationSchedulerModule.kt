package com.example.a90phase.di

import com.example.a90phase.domain.repositories.NotificationScheduler
import com.example.a90phase.notifications.NotificationSchedulerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationSchedulerModule {

    @Binds
    @Singleton
    abstract fun bindNotificationScheduler(impl: NotificationSchedulerImpl): NotificationScheduler
}
