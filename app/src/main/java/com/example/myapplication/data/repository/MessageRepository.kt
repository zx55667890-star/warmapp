package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.ChatMessage
import com.google.firebase.database.*

class MessageRepository(
    private val firebaseDb: FirebaseDatabase,
    private val chatroomId: String
) : MessageRepositoryInterface {
    companion object {
        private const val PAGE_SIZE = 100
    }

    val messagesRef: DatabaseReference
        get() = firebaseDb.getReference("chatrooms").child(chatroomId).child("messages")

    val statusRef: DatabaseReference
        get() = firebaseDb.getReference("questions").child(chatroomId).child("status")

    private fun parseMessage(child: DataSnapshot): ChatMessage? {
        val readByMap = mutableMapOf<String, Boolean>()
        child.child("readBy").children.forEach { rb ->
            rb.key?.let { k -> readByMap[k] = true }
        }

        val urlsList = mutableListOf<String>()
        child.child("imageUrls").children.forEach { urlSnap ->
            urlSnap.value?.toString()?.let { urlsList.add(it) }
        }

        val msg = ChatMessage(
            id = child.key ?: "",
            senderId = child.child("senderId").value?.toString() ?: "",
            senderRole = child.child("sender").value?.toString() ?: "",
            text = child.child("text").value?.toString() ?: "",
            imageUrl = child.child("imageUrl").value?.toString() ?: "",
            imageUrls = urlsList,
            videoUrl = child.child("videoUrl").value?.toString() ?: "",
            voiceUrl = child.child("voiceUrl").value?.toString() ?: "",
            voiceDuration = (child.child("voiceDuration").value as? Number)?.toLong() ?: 0L,
            timestamp = (child.child("timestamp").value as? Number)?.toLong() ?: 0L,
            readBy = readByMap,
            replyToId = child.child("replyToId").value?.toString() ?: "",
            replyToText = child.child("replyToText").value?.toString() ?: ""
        )
        if (msg.text.isNotBlank() || msg.imageUrl.isNotBlank() || msg.imageUrls.isNotEmpty() || msg.videoUrl.isNotBlank() || msg.voiceUrl.isNotBlank()) {
            return msg
        }
        return null
    }

    override fun listenToRecentMessages(
        userId: String,
        onMessages: (List<ChatMessage>) -> Unit,
        onTypingChange: (Boolean) -> Unit,
        onHasMore: (Boolean) -> Unit
    ): ValueEventListener {
        val query = messagesRef.orderByChild("timestamp").limitToLast(PAGE_SIZE)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val realTimeMessages = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    if (child.key == "typing_status") continue
                    val msg = parseMessage(child)
                    if (msg != null) realTimeMessages.add(msg)
                }
                realTimeMessages.sortBy { it.timestamp }
                onHasMore(realTimeMessages.size >= PAGE_SIZE)
                onMessages(realTimeMessages)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MessageRepo", "Recent messages listener cancelled", error.toException())
            }
        }
        query.addValueEventListener(listener)
        return listener
    }

    override fun listenToTypingStatus(userId: String, onTypingChange: (Boolean) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var otherTyping = false
                for (child in snapshot.children) {
                    if (child.key != userId && child.value?.toString() == "true") {
                        otherTyping = true
                        break
                    }
                }
                onTypingChange(otherTyping)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MessageRepo", "Typing status listener cancelled", error.toException())
            }
        }
        messagesRef.child("typing_status").addValueEventListener(listener)
        return listener
    }

    override fun loadMoreMessages(
        oldestTimestamp: Long?,
        onResult: (List<ChatMessage>, Boolean) -> Unit,
        onStateChange: (Boolean, Boolean) -> Unit
    ) {
        if (oldestTimestamp == null) {
            onStateChange(false, false)
            return
        }
        onStateChange(true, true)
        val query = messagesRef.orderByChild("timestamp")
            .endBefore(oldestTimestamp.toDouble())
            .limitToLast(PAGE_SIZE)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val older = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    if (child.key == "typing_status") continue
                    val msg = parseMessage(child)
                    if (msg != null) older.add(msg)
                }
                val hasMore = older.size >= PAGE_SIZE
                onResult(older.sortedBy { it.timestamp }, hasMore)
                onStateChange(false, hasMore)
            }

            override fun onCancelled(error: DatabaseError) {
                onStateChange(false, true)
                Log.w("MessageRepo", "Load more cancelled", error.toException())
            }
        })
    }

    override fun markAsRead(msgId: String, userId: String) {
        messagesRef.child(msgId).child("readBy").child(userId).setValue(true)
    }

    override fun sendMessage(userId: String, myRole: String, text: String, onError: (String) -> Unit) {
        if (text.isBlank()) return
        val newMsgRef = messagesRef.push()
        newMsgRef.setValue(
            mapOf(
                "senderId" to userId,
                "sender" to myRole,
                "text" to text,
                "imageUrl" to "",
                "timestamp" to System.currentTimeMillis()
            )
        ).addOnFailureListener { e ->
            onError(e.message ?: "訊息發送失敗，請檢查網路連線")
        }
    }

    override fun sendMessageWithFields(userId: String, myRole: String, fields: Map<String, Any>) {
        val newMsgRef = messagesRef.push()
        val data = fields.toMutableMap()
        data.putAll(mapOf("senderId" to userId, "sender" to myRole))
        newMsgRef.setValue(data)
    }

    override fun listenToStatus(onStatusChanged: (Boolean) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.value?.toString()
                onStatusChanged(status == "completed" || status == "cancelled")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MessageRepo", "Status listener cancelled", error.toException())
            }
        }
        statusRef.addValueEventListener(listener)
        return listener
    }

    override fun markChatEnded(onSuccess: () -> Unit, onError: (String) -> Unit) {
        statusRef.setValue("completed")
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "寫入失敗，請再試一次") }
    }

    override fun updateTypingStatus(userId: String, isTyping: Boolean) {
        messagesRef.child("typing_status").child(userId).setValue(isTyping)
    }

    override fun removeListener(listener: ValueEventListener) {
        messagesRef.orderByChild("timestamp").limitToLast(PAGE_SIZE).removeEventListener(listener)
    }

    override fun removeStatusListener(listener: ValueEventListener) {
        statusRef.removeEventListener(listener)
    }

    override fun recallMessage(msgId: String) {
        messagesRef.child(msgId).removeValue()
    }

    override fun removeTypingListener(listener: ValueEventListener) {
        messagesRef.child("typing_status").removeEventListener(listener)
    }

    override fun clearTypingStatus(userId: String) {
        messagesRef.child("typing_status").child(userId).setValue(false)
    }

    override fun fetchOpponentProfile(userId: String, onSuccess: (rating: Double, helpCount: Long) -> Unit, onFailure: () -> Unit) {
        firebaseDb.getReference("experts").child(userId).get()
            .addOnSuccessListener { snap ->
                onSuccess(
                    (snap.child("rating").value as? Number)?.toDouble() ?: 5.0,
                    (snap.child("ratingCount").value as? Number)?.toLong() ?: 0L
                )
            }
            .addOnFailureListener { onFailure() }
    }
}
