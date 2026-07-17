package com.example.myapplication.domain.chat

import android.util.Log
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.data.repository.MessageRepositoryFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.ConcurrentHashMap

data class MessagesResult(
    val messages: List<ChatMessage>,
    val hasMore: Boolean
)

class ObserveMessagesUseCase(
    private val repoFactory: MessageRepositoryFactory
) {
    private class ChatSession(
        val loadedOlder: MutableList<ChatMessage> = mutableListOf(),
        var realTimeMessages: List<ChatMessage> = emptyList(),
        var hasMoreMessages: Boolean = true,
        var onUpdate: (() -> Unit)? = null
    )

    private val activeSessions = ConcurrentHashMap<String, ChatSession>()

    fun observeMessagesDirect(chatroomId: String): Flow<MessagesResult> = callbackFlow {
        Log.d("ChatVM", "observeMessagesDirect callbackFlow started chatroomId=$chatroomId")
        val repo = repoFactory.create(chatroomId)
        val session = ChatSession()
        activeSessions[chatroomId] = session

        val listener = repo.addMessagesListener { list ->
            session.realTimeMessages = list
            synchronized(session) {
                val merged = (session.loadedOlder + session.realTimeMessages).sortedBy { it.timestamp }
                trySend(MessagesResult(merged, session.hasMoreMessages))
            }
        }
        session.onUpdate = {
            synchronized(session) {
                val merged = (session.loadedOlder + session.realTimeMessages).sortedBy { it.timestamp }
                trySend(MessagesResult(merged, session.hasMoreMessages))
            }
        }

        awaitClose {
            Log.d("ChatVM", "observeMessagesDirect awaitClose chatroomId=$chatroomId")
            repo.removeMessagesListener(listener)
            activeSessions.remove(chatroomId)
        }
    }

    fun observeMessages(chatroomId: String): Flow<MessagesResult> = callbackFlow {
        Log.d("ChatVM", "observeMessages callbackFlow started chatroomId=$chatroomId")
        val repo = repoFactory.create(chatroomId)
        val session = ChatSession()
        activeSessions[chatroomId] = session

        val emitMerged = {
            synchronized(session) {
                val merged = (session.loadedOlder + session.realTimeMessages).sortedBy { it.timestamp }
                Log.d("ChatVM", "observeMessages emitMerged merged.size=${merged.size}")
                trySend(MessagesResult(merged, session.hasMoreMessages))
            }
        }
        session.onUpdate = { emitMerged() }

        val listener = repo.listenToRecentMessages(
            userId = "",
            onMessages = { list ->
                Log.d("ChatVM", "observeMessages onMessages list.size=${list.size}")
                session.realTimeMessages = list
                emitMerged()
            },
            onTypingChange = {},
            onHasMore = { hasMore ->
                Log.d("ChatVM", "observeMessages onHasMore hasMore=$hasMore")
                session.hasMoreMessages = hasMore
                emitMerged()
            }
        )

        awaitClose {
            Log.d("ChatVM", "observeMessages awaitClose chatroomId=$chatroomId")
            repo.removeListener(listener)
            repo.clearTypingStatus("")
            activeSessions.remove(chatroomId)
        }
    }

    fun loadMore(chatroomId: String, oldestTimestamp: Long?, onComplete: () -> Unit) {
        if (oldestTimestamp == null) { onComplete(); return }
        val session = activeSessions[chatroomId] ?: run { onComplete(); return }

        repoFactory.create(chatroomId).loadMoreMessages(
            oldestTimestamp = oldestTimestamp,
            onResult = { older, _ ->
                synchronized(session) {
                    session.loadedOlder.addAll(0, older)
                }
                session.onUpdate?.invoke()
                onComplete()
            },
            onStateChange = { _, hasMore ->
                session.hasMoreMessages = hasMore
                session.onUpdate?.invoke()
            }
        )
    }

    fun observeTypingStatus(chatroomId: String, myUserId: String): Flow<Boolean> = callbackFlow {
        Log.d("ChatVM", "observeTypingStatus callbackFlow started chatroomId=$chatroomId myUserId=$myUserId")
        val repo = repoFactory.create(chatroomId)
        val listener = repo.listenToTypingStatus(myUserId) { isTyping ->
            Log.d("ChatVM", "observeTypingStatus callback isTyping=$isTyping")
            trySend(isTyping)
        }
        awaitClose {
            Log.d("ChatVM", "observeTypingStatus awaitClose")
            repo.removeTypingListener(listener)
        }
    }

    fun observeChatStatus(chatroomId: String): Flow<Boolean> = callbackFlow {
        Log.d("ChatVM", "observeChatStatus callbackFlow started chatroomId=$chatroomId")
        val repo = repoFactory.create(chatroomId)
        val listener = repo.listenToStatus { isEnded ->
            Log.d("ChatVM", "observeChatStatus callback isEnded=$isEnded")
            trySend(isEnded)
        }
        awaitClose {
            Log.d("ChatVM", "observeChatStatus awaitClose")
            repo.removeStatusListener(listener)
        }
    }

    fun markAsRead(chatroomId: String, msgIds: List<String>, userId: String) {
        if (msgIds.isEmpty()) return
        val repo = repoFactory.create(chatroomId)
        msgIds.forEach { repo.markAsRead(it, userId) }
    }

    fun updateMyTypingStatus(chatroomId: String, userId: String, isTyping: Boolean) {
        repoFactory.create(chatroomId).updateTypingStatus(userId, isTyping)
    }

    fun markChatEnded(chatroomId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        repoFactory.create(chatroomId).markChatEnded(onSuccess, onError)
    }
}
