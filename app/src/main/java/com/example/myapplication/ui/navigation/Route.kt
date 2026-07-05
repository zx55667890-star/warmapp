package com.example.myapplication.ui.navigation

object Routes {
    const val AUTH = "auth"
    const val ROLE_SELECT = "role_select"
    const val ASK = "ask"
    const val EXPERT = "expert"
    const val CHAT = "chat/{chatroomId}/{myRole}/{expertId}/{expertText}/{expertDate}"

    fun chat(chatroomId: String, myRole: String, expertId: String, expertText: String, expertDate: String): String =
        "chat/$chatroomId/$myRole/$expertId/${android.net.Uri.encode(expertText)}/${android.net.Uri.encode(expertDate)}"

    const val CHAT_CHATROOM_ID = "chatroomId"
    const val CHAT_MY_ROLE = "myRole"
    const val CHAT_EXPERT_ID = "expertId"
    const val CHAT_EXPERT_TEXT = "expertText"
    const val CHAT_EXPERT_DATE = "expertDate"
}
