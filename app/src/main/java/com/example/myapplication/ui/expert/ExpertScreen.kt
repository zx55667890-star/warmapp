package com.example.myapplication.ui.expert

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
            Text(
                text = solution.expertise,
                fontSize = 15.sp,
                color = AppColors.TextWhite,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

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
    var errorMessage by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }

    val maxCharLimit = 20

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
                        errorMessage = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("例如：淘寶退貨從台灣到大陸流程") },
                singleLine = false,
                minLines = 2,
                enabled = !isChecking,
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
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${expertise.length} / $maxCharLimit",
                            color = if (expertise.length == maxCharLimit) Color(0xFFEF5350) else AppColors.TextGray,
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(start = 8.dp)
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
                            isChecking = true
                            viewModel.fetchTagsFromAi(text) { tags ->
                                isChecking = false
                                when {
                                    tags == null -> {
                                        errorMessage = "系統服務繁忙，請稍後再試"
                                    }
                                    tags.isEmpty() -> {
                                        errorMessage = "請輸入有意義的內容，避免輸入無效字詞"
                                    }
                                    else -> {
                                        onLog(text, tags)
                                        expertise = ""
                                        errorMessage = ""
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = expertise.isNotBlank() && !isChecking,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentGreen)
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.Black,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("發布技能", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
