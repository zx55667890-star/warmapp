package com.example.myapplication.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.AppColors

@Composable
fun FullScreenImageDialog(
    imageUrls: List<String>,
    startIndex: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    isCameraCaptureList: List<Boolean> = emptyList()
) {
    if (imageUrls.isEmpty()) {
        onDismiss()
        return
    }

    var currentIndex by remember {
        mutableIntStateOf(startIndex.coerceIn(0, imageUrls.lastIndex))
    }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var totalPanX by remember { mutableFloatStateOf(0f) }

    fun resetZoom() {
        scale = 1f; offsetX = 0f; offsetY = 0f
    }

    fun goNext() {
        if (currentIndex < imageUrls.lastIndex) { currentIndex++; resetZoom() }
    }

    fun goPrev() {
        if (currentIndex > 0) { currentIndex--; resetZoom() }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.DarkBackground)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── 圖片本體 ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (zoom != 1f) {
                                totalPanX = 0f
                                val newScale = (scale * zoom).coerceIn(1f, 5f)
                                if (newScale <= 1f) {
                                    resetZoom()
                                } else {
                                    val ratio = newScale / scale
                                    offsetX += (pan.x - offsetX) * (1f - ratio)
                                    offsetY += (pan.y - offsetY) * (1f - ratio)
                                    scale = newScale
                                }
                            } else if (scale > 1f) {
                                totalPanX = 0f
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                totalPanX += pan.x
                                val threshold = 150f
                                if (totalPanX > threshold) { goPrev(); totalPanX = 0f }
                                else if (totalPanX < -threshold) { goNext(); totalPanX = 0f }
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onDismiss() },
                            onDoubleTap = { tapOffset ->
                                if (scale > 1f) {
                                    resetZoom()
                                } else {
                                    val newScale = 2.5f
                                    offsetX = tapOffset.x * (1f - newScale)
                                    offsetY = tapOffset.y * (1f - newScale)
                                    scale = newScale
                                }
                            }
                        )
                    }
            ) {
                val isCamera = isCameraCaptureList.getOrElse(currentIndex) { false }
                val contentScaleType = if (isCamera) ContentScale.Crop else ContentScale.Fit
                AsyncImage(
                    model = imageUrls[currentIndex],
                    contentDescription = null,
                    contentScale = contentScaleType,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY,
                            transformOrigin = TransformOrigin(0f, 0f)
                        )
                )
            }

            // ── 頂部工具列 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(
                        AppColors.DarkBackground.copy(alpha = 0.5f)
                    )
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (imageUrls.size > 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (imageUrls.size <= 7) {
                            repeat(imageUrls.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (index == currentIndex) 8.dp else 6.dp)
                                        .background(
                                            color = if (index == currentIndex)
                                                AppColors.TextWhite
                                            else
                                                AppColors.TextGray.copy(alpha = 0.5f),
                                            shape = CircleShape
                                        )
                                )
                            }
                        } else {
                            Text(
                                text = "${currentIndex + 1} / ${imageUrls.size}",
                                color = AppColors.TextWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
