package com.example.myapplication.ui.chat.dialog

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors

@Composable
fun EndChatConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = AppColors.SurfaceDark,
        titleContentColor = AppColors.TextWhite,
        textContentColor = AppColors.TextGray,
        title = {
            Text("結束對話？", fontWeight = FontWeight.Bold)
        },
        text = {
            Text("確定要結束這次對話嗎？")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.StatusError,
                    contentColor = AppColors.TextWhite
                )
            ) {
                Text("結束", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = AppColors.TextGray)
            }
        }
    )
}
