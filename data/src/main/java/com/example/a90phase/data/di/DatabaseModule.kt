package com.example.a90phase.data.di

import android.content.Context
import androidx.room.Room
import com.example.a90phase.data.local.room.SleepOptimizerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): SleepOptimizerDatabase =
        Room.databaseBuilder(
            context,
            SleepOptimizerDatabase::class.java,
            SleepOptimizerDatabase.DATABASE_NAME,
        ).build()
}
