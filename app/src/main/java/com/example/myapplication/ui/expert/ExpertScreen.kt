package com.example.myapplication.ui.expert

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.di.ExpertViewModel
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

            // 升級：將 QuickLogCard 修改為結構化輸入，並將組合後的結構資料丟給 ViewModel
            item {
                QuickLogCard(viewModel = viewModel, onLog = { domain, subDomain, problem, tags ->
                    // 為了配合你目前後端 submitSolution 的單一字串設計，暫時將結構化資料轉為格式化文字儲存
                    // 未來你的後端資料庫升級後，可以直接將這四個欄位分開寫入 Firebase 欄位
                    val formattedSolution = "[$domain][$subDomain] $problem | 標籤: ${tags.joinToString(", ")}"
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

@Composable
fun QuickLogCard(viewModel: ExpertViewModel, onLog: (domain: String, subDomain: String, problem: String, tags: List<String>) -> Unit) {
    var domain by remember { mutableStateOf("") }
    var subDomain by remember { mutableStateOf("") }
    var concreteProblem by remember { mutableStateOf("") }
    var aiTags by remember { mutableStateOf(listOf<String>()) }
    var isAiGenerating by remember { mutableStateOf(false) }

    LaunchedEffect(concreteProblem) {
        val trimmedProblem = concreteProblem.trim()
        if (trimmedProblem.length > 5 && domain.isNotBlank()) {
            isAiGenerating = true
            viewModel.fetchTagsFromAi(domain, subDomain, trimmedProblem) { generatedTags ->
                aiTags = generatedTags
                isAiGenerating = false
            }
        } else {
            aiTags = emptyList()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("✨ 設定您能解決的專業問題 (插旗解題池)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            // 欄位 1：大領域篩選器 (作為配對系統的第一層資料分池過濾)
            OutlinedTextField(
                value = domain,
                onValueChange = { domain = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("1. 核心專業領域 (如: Android, iOS, 項目管理)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.TextWhite,
                    unfocusedTextColor = AppColors.TextWhite,
                    focusedBorderColor = AppColors.AccentBlue,
                    unfocusedBorderColor = AppColors.BorderGray,
                    focusedLabelColor = AppColors.AccentBlue,
                    unfocusedLabelColor = AppColors.TextGray
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 欄位 2：技術子項目 (精準命中 Bigram 拆詞核心項目)
            OutlinedTextField(
                value = subDomain,
                onValueChange = { subDomain = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("2. 技術子項目 / 框架 (如: CameraX, Koin, Compose)") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.TextWhite,
                    unfocusedTextColor = AppColors.TextWhite,
                    focusedBorderColor = AppColors.AccentBlue,
                    unfocusedBorderColor = AppColors.BorderGray,
                    focusedLabelColor = AppColors.AccentBlue,
                    unfocusedLabelColor = AppColors.TextGray
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 欄位 3：具體痛點或錯誤情境 (使用者發問時的比對核心句)
            OutlinedTextField(
                value = concreteProblem,
                onValueChange = { concreteProblem = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("3. 您最能解決什麼具體的錯誤或痛點情靜？") },
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.TextWhite,
                    unfocusedTextColor = AppColors.TextWhite,
                    focusedBorderColor = AppColors.AccentBlue,
                    unfocusedBorderColor = AppColors.BorderGray,
                    focusedLabelColor = AppColors.AccentBlue,
                    unfocusedLabelColor = AppColors.TextGray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 欄位 4：AI 自動生成的延伸特徵標籤 (消除字串盲區)
            Text("4. AI 關聯特徵標籤 (配對核心防禦圈)", fontSize = 12.sp, color = AppColors.TextGray)
            Spacer(modifier = Modifier.height(6.dp))
            
            if (isAiGenerating) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = AppColors.AccentBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 正在提煉專業特徵...", fontSize = 12.sp, color = AppColors.TextGray)
                }
            } else if (aiTags.isNotEmpty()) {
                // 模擬橫向流式標籤，點擊可刪除不需要的標籤
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    aiTags.forEach { tag ->
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
                Text("於上方輸入具體問題後自動生成標籤", fontSize = 12.sp, color = AppColors.TextGray.copy(alpha = 0.6f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 發布解題卡按鈕
            Button(
                onClick = {
                    if (concreteProblem.isNotBlank() && domain.isNotBlank()) {
                        onLog(domain.trim(), subDomain.trim(), concreteProblem.trim(), aiTags)
                        // 提交後重置輸入狀態
                        subDomain = ""
                        concreteProblem = ""
                        aiTags = emptyList()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = concreteProblem.isNotBlank() && domain.isNotBlank() && !isAiGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentGreen)
            ) {
                Text("發布此解題技能卡", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
