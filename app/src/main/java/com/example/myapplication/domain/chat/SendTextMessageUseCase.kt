package com.example.myapplication.domain.chat

import com.example.myapplication.data.repository.MessageRepositoryFactory

class SendTextMessageUseCase(
    private val repoFactory: MessageRepositoryFactory
) {
    operator fun invoke(
        chatroomId: String,
        userId: String,
        myRole: String,
        text: String,
        replyToId: String?,
        replyToText: String?,
        onError: (String) -> Unit
    ) {
        if (text.isBlank()) return
        val messageRepo = repoFactory.create(chatroomId)
        if (replyToId != null) {
            messageRepo.sendMessageWithFields(userId, myRole, mapOf(
                "text" to text,
                "imageUrl" to "",
                "timestamp" to System.currentTimeMillis(),
                "replyToId" to replyToId,
                "replyToText" to (replyToText ?: "")
            ))
        } else {
            messageRepo.sendMessage(userId, myRole, text, onError)
        }
    }
}
