package com.example.myapplication.ui.expert

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.SolutionItem
import com.example.myapplication.di.ExpertViewModel
import com.example.myapplication.domain.expert.ExpertInputValidator
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.flow.collectLatest

enum class CardStep {
    INPUT,
    CONFIRM
}

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
                QuickLogCard(
                    viewModel = viewModel,
                    historyList = uiState.solutionHistory,
                    onLog = { expertise, tags ->
                        viewModel.submitSolution(userId, "Q_ID", expertise, tags)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "📜 我的知識庫 (已轉化為配對技能)",
                    modifier = Modifier.padding(vertical = 16.dp),
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextWhite
                )

                if (uiState.solutionHistory.isEmpty()) {
                    Text("尚無知識紀錄", color = AppColors.TextGray, fontSize = 14.sp)
                }
            }

            items(uiState.solutionHistory) { solution ->
                KnowledgeItemCard(
                    solution = solution,
                    onEditClick = {
                        // TODO: 觸發編輯 ViewModel 邏輯
                    }
                )
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

@Composable
fun KnowledgeItemCard(solution: SolutionItem, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = solution.expertise,
                    fontSize = 15.sp,
                    color = AppColors.TextWhite,
                    fontWeight = FontWeight.Medium
                )

                if (solution.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        solution.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = AppColors.AccentBlue.copy(alpha = 0.2f),
                            ) {
                                Text(
                                    text = "#$tag",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    color = AppColors.AccentBlue
                                )
                            }
                        }
                    }
                }
            }

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "編輯",
                    tint = AppColors.TextGray
                )
            }
        }
    }
}

@Composable
fun QuickLogCard(
    viewModel: ExpertViewModel,
    historyList: List<SolutionItem>,
    onLog: (expertise: String, tags: List<String>) -> Unit
) {
    var expertise by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var step by remember { mutableStateOf(CardStep.INPUT) }
    val tagsList = remember { mutableStateListOf<String>() }
    var tagInputText by remember { mutableStateOf("") }

    val maxCharLimit = 20
    val maxTagLimit = 5
    val maxSingleTagCharLimit = 6

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (step == CardStep.INPUT) {
                Text("✨ 分享您能幫忙解決的事", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("越具體，越能配對到需要您的人！", fontSize = 12.sp, color = AppColors.TextGray, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = expertise,
                    onValueChange = {
                        if (it.length <= maxCharLimit && !isGenerating) {
                            expertise = it
                            errorMessage = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例如：淘寶退貨從台灣到大陸流程") },
                    singleLine = false,
                    minLines = 2,
                    enabled = !isGenerating,
                    isError = errorMessage.isNotEmpty(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = AppColors.TextWhite,
                        unfocusedTextColor = AppColors.TextWhite,
                        focusedBorderColor = AppColors.AccentBlue,
                        unfocusedBorderColor = AppColors.BorderGray
                    ),
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${expertise.length} / $maxCharLimit",
                                color = if (expertise.length == maxCharLimit) Color(0xFFEF5350) else AppColors.TextGray,
                                textAlign = TextAlign.End
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val text = expertise.trim()
                            val isDuplicate = historyList.any { it.expertise == text }

                            if (isDuplicate) {
                                errorMessage = "您已經新增過這項技能囉！"
                            } else {
                                val validationError = ExpertInputValidator.validate(text)
                                if (validationError != null) {
                                    errorMessage = validationError
                                } else {
                                    isGenerating = true
                                    viewModel.fetchTagsFromAi(text) { tags ->
                                        isGenerating = false
                                        if (tags == null) {
                                            // 系統或配額故障：直接進入確認頁面，允許用戶手動新增標籤
                                            tagsList.clear()
                                            step = CardStep.CONFIRM
                                        } else if (tags.isEmpty()) {
                                            // AI 判定為無意義內容（垃圾過濾）：報錯並留在輸入頁面
                                            errorMessage = "輸入的內容似乎不屬於合法的專業技能，請重新描述。"
                                        } else {
                                            // 正常生成標籤
                                            tagsList.clear()
                                            tagsList.addAll(tags)
                                            step = CardStep.CONFIRM
                                        }
                                    }
                                }
                            }
                        },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = expertise.isNotBlank() && !isGenerating,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentGreen)
                ) {
                    if (isGenerating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI 關鍵字生成中...", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("分析技能標籤", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // CONFIRM Step
                Text("🏷️ 確認您的技能標籤", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("技能描述：" + expertise, fontSize = 14.sp, color = AppColors.TextLightGray, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

                if (tagsList.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ AI 服務目前無法使用，請手動新增標籤。",
                            color = AppColors.AccentYellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        text = "已提取標籤 (最多 $maxTagLimit 個，點擊 × 移除)：",
                        fontSize = 12.sp,
                        color = AppColors.TextGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    tagsList.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AppColors.AccentBlue.copy(alpha = 0.2f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 12.sp,
                                    color = AppColors.AccentBlue
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(
                                    onClick = { tagsList.remove(tag) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "刪除標籤",
                                        tint = AppColors.AccentBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 手動新增標籤
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = tagInputText,
                        onValueChange = {
                            if (it.length <= maxSingleTagCharLimit) {
                                tagInputText = it
                            }
                        },
                        placeholder = { Text("手動新增標籤", fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.TextWhite,
                            unfocusedTextColor = AppColors.TextWhite,
                            focusedBorderColor = AppColors.AccentBlue,
                            unfocusedBorderColor = AppColors.BorderGray
                        ),
                        supportingText = {
                            Text(
                                text = "${tagsList.size} / $maxTagLimit 標籤 (限 ${maxSingleTagCharLimit} 字)",
                                color = AppColors.TextGray,
                                fontSize = 11.sp
                            )
                        }
                    )

                    Button(
                        onClick = {
                            val cleaned = tagInputText.trim()
                            if (cleaned.isNotEmpty()) {
                                if (!tagsList.contains(cleaned) && tagsList.size < maxTagLimit) {
                                    tagsList.add(cleaned)
                                }
                                tagInputText = ""
                            }
                        },
                        enabled = tagInputText.trim().isNotEmpty() && tagsList.size < maxTagLimit,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentBlue),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("新增", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                // 確認與返回按鈕
                Button(
                    onClick = {
                        onLog(expertise.trim(), tagsList.toList())
                        expertise = ""
                        tagsList.clear()
                        tagInputText = ""
                        step = CardStep.INPUT
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentGreen)
                ) {
                    Text("確認發布此技能", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        step = CardStep.INPUT
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.TextGray),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.TextWhite)
                ) {
                    Text("返回修改描述", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
