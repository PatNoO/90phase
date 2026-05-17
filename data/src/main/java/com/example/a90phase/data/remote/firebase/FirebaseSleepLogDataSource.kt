package com.example.a90phase.data.remote.firebase

import com.example.a90phase.domain.common.DomainError
import com.example.a90phase.domain.common.Result
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseSleepLogDataSource {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadSleepLog(userId: String, document: FirestoreSleepLogDocument): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            firestore
                .collection(FirestoreSchema.USERS)
                .document(userId)
                .collection(FirestoreSchema.SleepLogs.COLLECTION)
                .document(document.id)
                .set(document)
                .addOnSuccessListener { continuation.resume(Result.Success(Unit)) }
                .addOnFailureListener { e ->
                    continuation.resume(Result.Error(DomainError.SyncError(e.message)))
                }
        }

    suspend fun downloadSleepLogsUpdatedAfter(
        userId: String,
        since: Instant,
    ): Result<List<FirestoreSleepLogDocument>> =
        suspendCancellableCoroutine { continuation ->
            firestore
                .collection(FirestoreSchema.USERS)
                .document(userId)
                .collection(FirestoreSchema.SleepLogs.COLLECTION)
                .whereGreaterThan(FirestoreSchema.SleepLogs.UPDATED_AT, Timestamp(since.epochSecond, since.nano))
                .get()
                .addOnSuccessListener { snapshot ->
                    val docs = snapshot.documents.mapNotNull {
                        it.toObject(FirestoreSleepLogDocument::class.java)
                    }
                    continuation.resume(Result.Success(docs))
                }
                .addOnFailureListener { e ->
                    continuation.resume(Result.Error(DomainError.SyncError(e.message)))
                }
        }
}
