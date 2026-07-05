package com.example.myapplication.domain.chat

import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.data.repository.MessageRepositoryFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RecallMessageUseCase(
    private val repoFactory: MessageRepositoryFactory,
    private val sendMediaUseCase: SendMediaUseCase
) {
    fun recallConfirmed(
        chatroomId: String,
        msg: ChatMessage,
        scope: CoroutineScope,
        onUpdateDisplay: () -> Unit
    ) {
        val messageRepo = repoFactory.create(chatroomId)
        messageRepo.recallMessage(msg.id)
        onUpdateDisplay()
        val mediaUrls = listOfNotNull(
            msg.imageUrl.takeIf { it.isNotBlank() },
            msg.videoUrl.takeIf { it.isNotBlank() },
            msg.voiceUrl.takeIf { it.isNotBlank() }
        ) + msg.imageUrls.filter { it.isNotBlank() }
        if (mediaUrls.isNotEmpty()) {
            scope.launch {
                sendMediaUseCase.deleteMessageMedia(mediaUrls)
            }
        }
    }
}
