package com.example.myapplication.data.repository

import com.example.myapplication.data.model.ChatMessage
import com.google.firebase.database.ValueEventListener

interface MessageRepositoryInterface {
    fun listenToRecentMessages(
        userId: String,
        onMessages: (List<ChatMessage>) -> Unit,
        onTypingChange: (Boolean) -> Unit,
        onHasMore: (Boolean) -> Unit
    ): ValueEventListener

    fun listenToTypingStatus(userId: String, onTypingChange: (Boolean) -> Unit): ValueEventListener

    fun loadMoreMessages(
        oldestTimestamp: Long?,
        onResult: (List<ChatMessage>, Boolean) -> Unit,
        onStateChange: (Boolean, Boolean) -> Unit
    )

    fun markAsRead(msgId: String, userId: String)

    fun sendMessage(userId: String, myRole: String, text: String, onError: (String) -> Unit)

    fun sendMessageWithFields(userId: String, myRole: String, fields: Map<String, Any>)

    fun listenToStatus(onStatusChanged: (Boolean) -> Unit): ValueEventListener

    fun markChatEnded(onSuccess: () -> Unit, onError: (String) -> Unit)

    fun updateTypingStatus(userId: String, isTyping: Boolean)

    fun addMessagesListener(onMessages: (List<ChatMessage>) -> Unit): ValueEventListener
    fun removeMessagesListener(listener: ValueEventListener)
    fun removeListener(listener: ValueEventListener)

    fun removeStatusListener(listener: ValueEventListener)

    fun recallMessage(msgId: String)

    fun removeTypingListener(listener: ValueEventListener)

    fun clearTypingStatus(userId: String)

    fun fetchOpponentProfile(userId: String, onSuccess: (rating: Double, helpCount: Long) -> Unit, onFailure: () -> Unit)
}
