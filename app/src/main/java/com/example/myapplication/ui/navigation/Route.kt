package com.example.myapplication.ui.navigation

import android.net.Uri

object Routes {
    const val AUTH = "auth"
    const val ROLE_SELECT = "role_select"
    const val ASK = "ask"
    const val EXPERT = "expert"

    const val CHAT_CHATROOM_ID = "chatroomId"
    const val CHAT_MY_ROLE = "myRole"
    const val CHAT_EXPERT_ID = "expertId"
    const val CHAT_EXPERT_TEXT = "expertText"
    const val CHAT_EXPERT_DATE = "expertDate"

    const val CHAT = "chat/{$CHAT_CHATROOM_ID}/{$CHAT_MY_ROLE}/{$CHAT_EXPERT_ID}/{$CHAT_EXPERT_TEXT}/{$CHAT_EXPERT_DATE}"

    fun chat(chatroomId: String, myRole: String, expertId: String, expertText: String, expertDate: String): String =
        "chat/$chatroomId/$myRole/$expertId/${Uri.encode(expertText)}/${Uri.encode(expertDate)}"

    /** Intent extras for deep-link navigation from FCM notifications. */
    const val EXTRA_CHATROOM_ID = "navigate_chatroom_id"
    const val EXTRA_MY_ROLE = "navigate_my_role"
    const val EXTRA_EXPERT_ID = "navigate_expert_id"
    const val EXTRA_EXPERT_TEXT = "navigate_expert_text"
    const val EXTRA_EXPERT_DATE = "navigate_expert_date"
}
