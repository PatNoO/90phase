package com.example.a90phase.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseAuthWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) return Result.success()

        return suspendCancellableCoroutine { continuation ->
            auth.signInAnonymously()
                .addOnSuccessListener { continuation.resume(Result.success()) }
                .addOnFailureListener { continuation.resume(Result.retry()) }
        }
    }

    companion object {
        const val WORK_NAME = "firebase_anonymous_auth"
    }
}
