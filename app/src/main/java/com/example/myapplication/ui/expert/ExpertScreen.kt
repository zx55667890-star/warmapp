package com.example.myapplication.ui.expert

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.di.ExpertViewModel
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.delay

@Composable
fun ExpertScreen(viewModel: ExpertViewModel, userId: String, onNavigateToInput: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is com.example.myapplication.di.ExpertUiEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = AppColors.DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("我的專業影響力", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "已解決問題: ${uiState.helpCount}  |  評分: ${"%.1f".format(uiState.rating)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                QuickLogCard(viewModel = viewModel, onLog = { expertise, tags ->
                    val formattedSolution = "$expertise | 標籤: ${tags.joinToString(", ")}"
                    viewModel.submitSolution(userId, "Q_ID", formattedSolution)
                })
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("📜 我的知識庫 (已轉化為配對技能)", modifier = Modifier.padding(vertical = 16.dp), fontWeight = FontWeight.Bold, color = AppColors.TextWhite)
                uiState.solutionHistory.forEach { solution ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("•  ", color = AppColors.AccentGreen)
                        Text(solution, fontSize = 15.sp, color = AppColors.TextLightGray)
                    }
                }
                if (uiState.solutionHistory.isEmpty()) {
                    Text("尚無知識紀錄", color = AppColors.TextGray, fontSize = 14.sp)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { onNavigateToInput() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.AccentBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.AccentBlue)
                ) {
                    Text("返回", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickLogCard(viewModel: ExpertViewModel, onLog: (expertise: String, tags: List<String>) -> Unit) {
    var expertise by remember { mutableStateOf("") }
    var aiTags by remember { mutableStateOf(listOf<String>()) }
    var isAiGenerating by remember { mutableStateOf(false) }

    val maxCharLimit = 20

    LaunchedEffect(expertise) {
        val trimmed = expertise.trim()
        if (trimmed.length > 2) {
            isAiGenerating = true
            delay(400)

            viewModel.extractTagsLocally(trimmed) { generatedTags ->
                aiTags = generatedTags
                isAiGenerating = false
            }
        } else {
            aiTags = emptyList()
            isAiGenerating = false
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("✨ 分享您能幫忙解決的事", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("越具體，越能配對到需要您的人！", fontSize = 12.sp, color = AppColors.TextGray, modifier = Modifier.padding(top = 4.dp))

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = expertise,
                onValueChange = {
                    if (it.length <= maxCharLimit) {
                        expertise = it
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如：淘寶退貨從台灣到大陸流程") },
                singleLine = false,
                minLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.TextWhite,
                    unfocusedTextColor = AppColors.TextWhite,
                    focusedBorderColor = AppColors.AccentBlue,
                    unfocusedBorderColor = AppColors.BorderGray
                ),
                supportingText = {
                    Text(
                        text = "${expertise.length} / $maxCharLimit",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        color = if (expertise.length == maxCharLimit) Color(0xFFEF5350) else AppColors.TextGray
                    )
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("系統自動提取的配對關鍵字", fontSize = 12.sp, color = AppColors.TextGray)
            Spacer(modifier = Modifier.height(6.dp))

            if (isAiGenerating) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AppColors.AccentBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("系統分析中...", fontSize = 12.sp, color = AppColors.TextGray)
                }
            } else if (aiTags.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    aiTags.take(4).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(AppColors.AccentBlue.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
                                .border(1.dp, AppColors.AccentBlue, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable { aiTags = aiTags.filter { it != tag } }
                        ) {
                            Text("#$tag ✕", color = AppColors.TextWhite, fontSize = 11.sp)
                        }
                    }
                }
            } else {
                Text("輸入完成後將自動生成", fontSize = 12.sp, color = AppColors.TextGray.copy(alpha = 0.6f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (expertise.isNotBlank()) {
                        onLog(expertise.trim(), aiTags)
                        expertise = ""
                        aiTags = emptyList()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = expertise.isNotBlank() && !isAiGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentGreen)
            ) {
                Text("發布此技能", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
