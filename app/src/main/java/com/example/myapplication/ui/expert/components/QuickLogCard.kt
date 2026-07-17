package com.example.myapplication.ui.expert.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.domain.expert.ExpertInputValidator
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun QuickLogCard(
    onPublish: (String) -> Unit,
    onClearFeedback: () -> Unit,
    onButtonLayoutChanged: (LayoutCoordinates) -> Unit,
    clearInputSignal: Int
) {
    var expertise by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val maxCharLimit = ExpertInputValidator.MAX_CHAR_LIMIT

    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) AppColors.AccentGreen else AppColors.BorderGray,
        animationSpec = tween(300), label = "border"
    )

    LaunchedEffect(clearInputSignal) {
        if (clearInputSignal > 0) { expertise = ""; errorMessage = "" }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceDark),
        border = BorderStroke(1.dp, AppColors.GlassStroke)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "發布技能",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = AppColors.TextWhite
            )
            Text(
                text = "越具體，越能配對到需要您的人！",
                fontSize = 12.sp,
                color = AppColors.TextGray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = expertise,
                onValueChange = {
                    if (it.length <= maxCharLimit) {
                        expertise = it
                        errorMessage = ""
                        onClearFeedback()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        "例如：淘寶退貨從台灣到大陸流程",
                        color = AppColors.TextMuted, fontSize = 14.sp
                    )
                },
                singleLine = false,
                minLines = 3,
                isError = errorMessage.isNotEmpty(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.TextWhite,
                    unfocusedTextColor = AppColors.TextWhite,
                    focusedBorderColor = AppColors.AccentGreen,
                    unfocusedBorderColor = AppColors.BorderGray,
                    cursorColor = AppColors.AccentGreen,
                    focusedContainerColor = AppColors.SurfaceMedium,
                    unfocusedContainerColor = AppColors.SurfaceMedium
                ),
                supportingText = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${expertise.length} / $maxCharLimit",
                            color = if (expertise.length == maxCharLimit)
                                AppColors.StatusError else AppColors.TextMuted,
                            textAlign = TextAlign.End,
                            fontSize = 12.sp
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val trimmed = expertise.trim()
                    errorMessage = ""
                    onClearFeedback()
                    onPublish(trimmed)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .onGloballyPositioned { onButtonLayoutChanged(it) },
                enabled = expertise.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (expertise.isNotBlank())
                                Brush.horizontalGradient(
                                    listOf(AppColors.AccentGradientStart, AppColors.AccentGradientEnd)
                                )
                            else
                                Brush.horizontalGradient(
                                    listOf(AppColors.TextMuted, AppColors.TextMuted)
                                ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.expert_publish_button),
                        color = if (expertise.isNotBlank()) AppColors.DarkBackground else AppColors.TextGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
internal fun FeedbackBanner(message: String, isError: Boolean, offsetY: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .offset { IntOffset(0, offsetY) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) AppColors.StatusErrorBg else AppColors.StatusSuccessBg
        ),
        border = BorderStroke(
            1.dp,
            if (isError) AppColors.StatusError.copy(alpha = 0.2f)
            else AppColors.StatusSuccess.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Text(
            text = message,
            color = if (isError) AppColors.StatusError else AppColors.StatusSuccess,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(14.dp)
        )
    }
}
