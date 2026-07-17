package com.example.myapplication.ui.chat.bubble

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.ChatMessage
import com.example.myapplication.ui.theme.AppColors

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
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(14.dp),
        containerColor = AppColors.SurfaceMedium,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp
    ) {
        if (msg.text.isNotBlank()) {
            DropdownMenuItem(
                text = {
                    Text(
                        "複製",
                        color = AppColors.TextWhite,
                        fontSize = 14.sp
                    )
                },
                onClick = {
                    onDismiss()
                    val clipboard = context.getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText("chat_message", msg.text)
                    )
                }
            )
        }
        if (!isMine && msg.text.isNotBlank()) {
            DropdownMenuItem(
                text = {
                    Text(
                        "回覆",
                        color = AppColors.TextWhite,
                        fontSize = 14.sp
                    )
                },
                onClick = {
                    onDismiss()
                    onReply()
                }
            )
        }
        if (isMine) {
            DropdownMenuItem(
                text = {
                    Text(
                        "收回",
                        color = AppColors.StatusError,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                onClick = {
                    onDismiss()
                    onRecall()
                }
            )
        }
    }
}
