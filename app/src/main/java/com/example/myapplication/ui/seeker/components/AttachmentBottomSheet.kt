package com.example.myapplication.ui.seeker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit,
    onVoiceClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isSystemInDarkTheme()) Color(0xFF22222E) else Color(0xFFF8F9FA),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AttachmentCardItem(icon = Icons.Default.Photo, label = "相簿", onClick = onGalleryClick)
            AttachmentCardItem(icon = Icons.Default.CameraAlt, label = "相機", onClick = onCameraClick)
            AttachmentCardItem(icon = Icons.Default.Mic, label = "錄音", onClick = onVoiceClick)
        }
    }
}

@Composable
private fun RowScope.AttachmentCardItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    Column(
        modifier = Modifier
            .weight(1f)
            .background(
                color = if (isDark) Color(0xFF31313F) else Color(0xFFEEEEF2),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = if (isDark) Color.White else Color(0xFF333333), modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = if (isDark) Color(0xFFE0E0E0) else Color(0xFF333333))
    }
}
