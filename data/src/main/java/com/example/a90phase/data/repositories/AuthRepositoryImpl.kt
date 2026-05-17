package com.example.a90phase.data.repositories

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.example.a90phase.domain.repositories.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun signInAnonymously(): Result<String> {
        val existing = firebaseAuth.currentUser
        if (existing != null) return Result.Success(existing.uid)

        return suspendCancellableCoroutine { continuation ->
            firebaseAuth.signInAnonymously()
                .addOnSuccessListener { authResult ->
                    val uid = authResult.user?.uid
                    if (uid != null) {
                        continuation.resume(Result.Success(uid))
                    } else {
                        continuation.resume(
                            Result.Error(DomainError.AuthFailed("No userId returned after sign-in")),
                        )
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.Error(DomainError.AuthFailed(e.message)))
                }
        }
    }

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun isSignedIn(): Boolean = firebaseAuth.currentUser != null
}
