package com.example.myapplication.data.model

data class Experience(
    var id: String = "",
    var authorId: String = "",
    var text: String = "",
    var timestamp: Long = 0L,
    var status: String = "",
    var isOnline: Boolean = false
)