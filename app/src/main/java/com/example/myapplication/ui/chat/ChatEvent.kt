package com.example.myapplication.ui.chat

sealed class ChatEvent {
    data object ScrollToBottom : ChatEvent()
    data class ShowSnackbar(val message: String) : ChatEvent()
    data object ChatEndedByOther : ChatEvent()
    data object OpenCamera : ChatEvent()
    data object OpenVoiceRecorder : ChatEvent()
}
