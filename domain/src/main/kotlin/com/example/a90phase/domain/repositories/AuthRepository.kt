package com.example.a90phase.domain.repositories

import com.example.a90phase.domain.common.Result

interface AuthRepository {
    suspend fun signInAnonymously(): Result<String>

    fun getCurrentUserId(): String?

    fun isSignedIn(): Boolean
}
