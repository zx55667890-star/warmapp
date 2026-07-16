package com.example.myapplication.ui.expert.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.model.SkillStatus
import com.example.myapplication.data.model.SolutionItem
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay

@Composable
fun KnowledgeItemCard(solution: SolutionItem, onEditClick: () -> Unit, animDelay: Long = 0L) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(animDelay); appeared = true }

    val statusColor = when (solution.status) {
        SkillStatus.ACTIVE   -> AppColors.AccentGreen
        SkillStatus.REJECTED -> AppColors.StatusError
        SkillStatus.PENDING  -> AppColors.StatusPending
    }
    val statusBg = when (solution.status) {
        SkillStatus.ACTIVE   -> AppColors.StatusSuccessBg
        SkillStatus.REJECTED -> AppColors.StatusErrorBg
        SkillStatus.PENDING  -> AppColors.StatusPendingBg
    }

    AnimatedVisibility(
        visible = appeared,
        enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceDark),
            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = solution.expertise,
                        fontSize = 15.sp,
                        color = AppColors.TextWhite,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    when (solution.status) {
                        SkillStatus.REJECTED -> {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusBg
                                ) {
                                    Text(
                                        text = stringResource(R.string.expert_content_unrecognized),
                                        fontSize = 11.sp,
                                        color = statusColor,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        SkillStatus.ACTIVE -> {
                            if (solution.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    solution.tags.take(5).forEach { tag ->
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(tag, fontSize = 11.sp) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = AppColors.AccentGreen.copy(alpha = 0.08f),
                                                labelColor = AppColors.AccentGreen
                                            ),
                                            border = BorderStroke(1.dp, AppColors.AccentGreen.copy(alpha = 0.2f))
                                        )
                                    }
                                    if (solution.tags.size > 5) {
                                        Text(
                                            "+${solution.tags.size - 5}",
                                            fontSize = 11.sp,
                                            color = AppColors.TextMuted,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                    }
                                }
                            }
                        }
                        SkillStatus.PENDING -> { }
                    }
                }

                if (solution.status != SkillStatus.PENDING) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.expert_edit_content_desc),
                            tint = AppColors.TextGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun EmptyKnowledgeCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.SurfaceDark),
        border = BorderStroke(1.dp, AppColors.BorderGray.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = AppColors.TextMuted,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "尚無知識紀錄",
                fontSize = 15.sp,
                color = AppColors.TextGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "發布您的第一個技能，開始建立影響力",
                fontSize = 12.sp,
                color = AppColors.TextMuted
            )
        }
    }
}
