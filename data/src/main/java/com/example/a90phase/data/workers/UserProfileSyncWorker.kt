package com.example.a90phase.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.a90phase.data.remote.firebase.FirebaseUserProfileDataSource
import com.example.a90phase.data.remote.firebase.toDomain
import com.example.a90phase.data.remote.firebase.toFirestoreDocument
import com.example.a90phase.domain.common.Result as DomainResult
import com.example.a90phase.domain.repositories.AuthRepository
import com.example.a90phase.domain.repositories.UserPreferencesRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class UserProfileSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UserProfileSyncWorkerEntryPoint {
        fun userPreferencesRepository(): UserPreferencesRepository
        fun authRepository(): AuthRepository
    }

    private val dataSource = FirebaseUserProfileDataSource()

    override suspend fun doWork(): Result {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            UserProfileSyncWorkerEntryPoint::class.java,
        )
        val prefsRepository = entryPoint.userPreferencesRepository()
        val authRepository = entryPoint.authRepository()

        val userId = authRepository.getCurrentUserId() ?: return Result.retry()

        val localProfile = when (val r = prefsRepository.getUserProfile()) {
            is DomainResult.Success -> r.data
            else -> return Result.retry()
        }

        if (localProfile.userId == LOCAL_USER_ID) {
            // Fresh install — attempt to restore from Firestore
            return when (val r = dataSource.downloadProfile(userId)) {
                is DomainResult.Success -> {
                    val remote = r.data ?: return Result.success()
                    val restored = remote.toDomain().copy(userId = userId)
                    when (prefsRepository.updateUserProfile(restored)) {
                        is DomainResult.Success -> Result.success()
                        else -> Result.retry()
                    }
                }
                else -> Result.retry()
            }
        }

        // Upload current Room profile to Firestore (Room is source of truth)
        val document = localProfile.copy(userId = userId).toFirestoreDocument()
        return when (dataSource.uploadProfile(userId, document)) {
            is DomainResult.Success -> Result.success()
            else -> Result.retry()
        }
    }

    companion object {
        const val WORK_TAG = "user_profile_sync"
        private const val LOCAL_USER_ID = "local_user"
    }
}
