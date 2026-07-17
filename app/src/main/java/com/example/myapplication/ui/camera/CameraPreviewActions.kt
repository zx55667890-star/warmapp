package com.example.myapplication.ui.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.AppColors

@Composable
fun CameraPreviewActions(
    onDiscard: () -> Unit,
    onSend: () -> Unit,
    bottomPadding: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier.padding(
                bottom = if (bottomPadding) 100.dp else 32.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        AppColors.StatusError.copy(alpha = 0.15f),
                        CircleShape
                    )
                    .border(
                        2.dp,
                        AppColors.StatusError.copy(alpha = 0.6f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onDiscard,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "丟棄",
                        tint = AppColors.StatusError,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        AppColors.AccentGreen.copy(alpha = 0.15f),
                        CircleShape
                    )
                    .border(
                        2.dp,
                        AppColors.AccentGreen.copy(alpha = 0.6f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "傳送",
                        tint = AppColors.AccentGreen,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
