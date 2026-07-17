package com.example.myapplication.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.AppColors

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
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = AppColors.SurfaceDark,
        titleContentColor = AppColors.TextWhite,
        textContentColor = AppColors.TextGray,
        title = {
            Text("請為專家評分", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "評分",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextGray
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        val isSelected = star <= ratingScore
                        val starColor by animateColorAsState(
                            targetValue = if (isSelected)
                                AppColors.StatusPending
                            else
                                AppColors.BorderGray,
                            animationSpec = tween(200),
                            label = "star$star"
                        )
                        Icon(
                            imageVector = if (isSelected) Icons.Filled.Star
                                          else Icons.Outlined.Star,
                            contentDescription = "評分 $star",
                            tint = starColor,
                            modifier = Modifier
                                .size(32.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onScoreChange(star) }
                        )
                    }

                    if (ratingScore > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$ratingScore / 5",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.StatusPending
                        )
                    }
                }

                if (ratingError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        ratingError,
                        color = AppColors.StatusError,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "意見（可選）",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.TextGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = ratingComment,
                    onValueChange = onCommentChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("說說您的體驗…", color = AppColors.TextMuted)
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.TextWhite,
                        unfocusedTextColor = AppColors.TextWhite,
                        focusedBorderColor = AppColors.AccentGreen,
                        unfocusedBorderColor = AppColors.BorderGray,
                        cursorColor = AppColors.AccentGreen,
                        focusedContainerColor = AppColors.SurfaceMedium,
                        unfocusedContainerColor = AppColors.SurfaceMedium
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSubmit,
                enabled = !ratingSubmitting && ratingScore > 0,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.AccentGreen,
                    contentColor = AppColors.DarkBackground,
                    disabledContainerColor = AppColors.AccentGreen.copy(alpha = 0.3f)
                )
            ) {
                if (ratingSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = AppColors.DarkBackground
                    )
                } else {
                    Text("提交", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text("跳過", color = AppColors.TextGray)
            }
        }
    )
}
