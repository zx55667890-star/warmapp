package com.example.myapplication.data.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderRole: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val imageUrls: List<String> = emptyList(),
    val voiceUrl: String = "",
    val voiceDuration: Long = 0L,
    val timestamp: Long = 0L,
    val readBy: Map<String, Boolean> = emptyMap(),
    val replyToId: String = "",
    val replyToText: String = "",
    val localId: String = "",
    val localImageUrls: List<String> = emptyList(),
    val isCameraCapture: Boolean = false
)