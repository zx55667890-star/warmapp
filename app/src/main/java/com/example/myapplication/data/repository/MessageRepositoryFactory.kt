package com.example.myapplication.data.repository

import com.google.firebase.database.FirebaseDatabase

class MessageRepositoryFactory(
    private val firebaseDb: FirebaseDatabase
) {
    fun create(chatroomId: String): MessageRepository = MessageRepository(firebaseDb, chatroomId)
}
