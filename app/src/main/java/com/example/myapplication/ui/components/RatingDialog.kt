package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RatingDialog(
    ratingScore: Int,
    ratingComment: String,
    ratingError: String?,
    ratingSubmitting: Boolean,
    onScoreChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("請為專家評分") },
        text = {
            Column {
                Text("評分：")
                Row {
                    (1..5).forEach { star ->
                        val isSelected = star <= ratingScore
                        Text(
                            text = if (isSelected) "⭐" else "☆",
                            modifier = Modifier.clickable { onScoreChange(star) },
                            fontSize = 24.sp
                        )
                    }
                }
                if (ratingError != null) {
                    Text(ratingError, color = Color.Red, fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = ratingComment,
                    onValueChange = { onCommentChange(it) },
                    label = { Text("意見 (可選)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = onSubmit, enabled = !ratingSubmitting) {
                Text("提交", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("跳過")
            }
        }
    )
}
