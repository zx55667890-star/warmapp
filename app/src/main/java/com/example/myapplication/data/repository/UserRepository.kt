package com.example.myapplication.data.repository

import com.google.firebase.database.FirebaseDatabase

class UserRepository(private val firebaseDb: FirebaseDatabase) {

    fun getNickname(uid: String, onResult: (String) -> Unit) {
        firebaseDb.getReference("users").child(uid).child("nickname").get()
            .addOnSuccessListener { snap ->
                onResult(snap.value?.toString() ?: "使用者")
            }
            .addOnFailureListener { onResult("使用者") }
    }

    fun setNickname(uid: String, nickname: String) {
        firebaseDb.getReference("users").child(uid).child("nickname").setValue(nickname)
    }

    fun getFcmToken(uid: String, onResult: (String?) -> Unit) {
        firebaseDb.getReference("users").child(uid).child("fcmToken").get()
            .addOnSuccessListener { onResult(it.value?.toString()) }
            .addOnFailureListener { onResult(null) }
    }
}
