package com.example.myapplication.ui.chat.bubble

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.model.ChatMessage

@Composable
fun BubbleContextMenu(
    expanded: Boolean,
    msg: ChatMessage,
    isMine: Boolean,
    onDismiss: () -> Unit,
    onReply: () -> Unit,
    onRecall: () -> Unit
) {
    val context = LocalContext.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        if (msg.text.isNotBlank()) {
            DropdownMenuItem(
                text = { Text("複製") },
                onClick = {
                    onDismiss()
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("chat_message", msg.text))
                }
            )
        }
        if (!isMine && msg.text.isNotBlank()) {
            DropdownMenuItem(
                text = { Text("回覆") },
                onClick = {
                    onDismiss()
                    onReply()
                }
            )
        }
        if (isMine) {
            DropdownMenuItem(
                text = { Text("收回") },
                onClick = {
                    onDismiss()
                    onRecall()
                }
            )
        }
    }
}
