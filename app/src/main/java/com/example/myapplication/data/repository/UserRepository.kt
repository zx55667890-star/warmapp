package com.example.myapplication.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class UserRepository(private val firebaseDb: FirebaseDatabase) {

    suspend fun getNickname(uid: String): String =
        suspendCancellableCoroutine { cont ->
            firebaseDb.getReference("users").child(uid).child("nickname").get()
                .addOnSuccessListener { snap ->
                    if (cont.isActive) cont.resume(snap.value?.toString() ?: "使用者")
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume("使用者")
                }
        }

    fun setNickname(uid: String, nickname: String) {
        firebaseDb.getReference("users").child(uid).child("nickname").setValue(nickname)
    }

    suspend fun getFcmToken(uid: String): String? =
        suspendCancellableCoroutine { cont ->
            firebaseDb.getReference("users").child(uid).child("fcmToken").get()
                .addOnSuccessListener { if (cont.isActive) cont.resume(it.value?.toString()) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
        }
}
