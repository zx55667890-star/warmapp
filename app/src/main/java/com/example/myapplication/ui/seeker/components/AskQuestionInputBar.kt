package com.example.myapplication.ui.seeker.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.myapplication.ui.seeker.SelectedMedia

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AskQuestionInputBar(
    question: String,
    onQuestionChange: (String) -> Unit,
    selectedMediaList: List<SelectedMedia>,
    focusRequester: FocusRequester,
    showSentFeedback: Boolean,
    onAttachClick: () -> Unit,
    onSendClick: () -> Unit,
    onRemoveMedia: (SelectedMedia) -> Unit
) {
    val isKeyboardVisible = WindowInsets.isImeVisible
    val bottomPadding = if (isKeyboardVisible) 16.dp else 28.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 30.dp, bottom = bottomPadding)
            .border(0.5.dp, Color(0xFF333333), RoundedCornerShape(32.dp)),
        color = Color(0xFF1A1A1E),
        shape = RoundedCornerShape(32.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (selectedMediaList.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(selectedMediaList, key = { it.uri.toString() }) { media ->
                        MediaPreviewItem(
                            uri = media.uri,
                            isVideo = media.isVideo,
                            isVoice = media.isVoice,
                            onRemove = { onRemoveMedia(media) }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = question,
                onValueChange = onQuestionChange,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                maxLines = 5,
                minLines = 1,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color(0xFF888888),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                leadingIcon = {
                    IconButton(
                        onClick = onAttachClick,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Text(
                            "+",
                            fontSize = 32.sp,
                            color = if (isSystemInDarkTheme()) Color(0xFFCCCCCC) else Color(0xFF666666),
                            fontWeight = FontWeight.Normal
                        )
                    }
                },
                trailingIcon = {
                    IconButton(
                        onClick = onSendClick,
                        enabled = selectedMediaList.isNotEmpty() || question.isNotBlank(),
                        modifier = Modifier.size(52.dp)
                    ) {
                        val isDark = isSystemInDarkTheme()
                        val iconColor = when {
                            showSentFeedback -> Color(0xFF04C9A0)
                            question.isNotBlank() -> Color(0xFFD4A853)
                            else -> if (isDark) Color(0xFF555555) else Color(0xFFBBBBBB)
                        }
                        Icon(
                            imageVector = if (showSentFeedback) Icons.Default.Check else Icons.AutoMirrored.Filled.Send,
                            contentDescription = "傳送",
                            tint = iconColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun MediaPreviewItem(uri: Uri, isVideo: Boolean, isVoice: Boolean, onRemove: () -> Unit) {
    val context = LocalContext.current
    var videoBitmap by remember { mutableStateOf<Bitmap?>(null) }

    if (isVideo) {
        LaunchedEffect(uri) {
            videoBitmap = withContext(Dispatchers.IO) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    retriever.getFrameAtTime()
                } catch (e: Exception) {
                    null
                } finally {
                    try { retriever.release() } catch (_: Exception) {}
                }
            }
        }
    }

    Box(modifier = Modifier.size(80.dp)) {
        if (isVoice) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFF2D2D3A), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Mic, contentDescription = "語音", tint = Color(0xFFD4A853), modifier = Modifier.size(32.dp))
            }
        } else if (isVideo && videoBitmap != null) {
            Image(
                bitmap = videoBitmap!!.asImageBitmap(),
                contentDescription = "預覽影片",
                modifier = Modifier.fillMaxSize().background(Color(0xFF2D2D3A), RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            AsyncImage(
                model = uri,
                contentDescription = "預覽圖片",
                modifier = Modifier.fillMaxSize().background(Color(0xFF2D2D3A), RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp).align(Alignment.TopEnd)
        ) {
            Icon(Icons.Default.Close, contentDescription = "移除", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
