package com.example.a90phase.data.remote.firebase

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseUserProfileDataSource {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadProfile(userId: String, document: FirestoreUserProfileDocument): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            firestore
                .collection(FirestoreSchema.USERS)
                .document(userId)
                .set(document)
                .addOnSuccessListener { continuation.resume(Result.Success(Unit)) }
                .addOnFailureListener { e ->
                    continuation.resume(Result.Error(DomainError.SyncError(e.message)))
                }
        }

    suspend fun downloadProfile(userId: String): Result<FirestoreUserProfileDocument?> =
        suspendCancellableCoroutine { continuation ->
            firestore
                .collection(FirestoreSchema.USERS)
                .document(userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    val doc = if (snapshot.exists()) {
                        snapshot.toObject(FirestoreUserProfileDocument::class.java)
                    } else {
                        null
                    }
                    continuation.resume(Result.Success(doc))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.Error(DomainError.SyncError(e.message)))
                }
        }
}
