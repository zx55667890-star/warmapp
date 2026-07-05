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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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
            modifier = Modifier.padding(bottom = if (bottomPadding) 100.dp else 32.dp),
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0x33FF4444), CircleShape)
                    .border(2.dp, Color(0xFFFF4444), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onDiscard,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "丟棄",
                        tint = Color(0xFFFF4444),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0x3304C9A0), CircleShape)
                    .border(2.dp, Color(0xFF04C9A0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onSend,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "傳送",
                        tint = Color(0xFF04C9A0),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
