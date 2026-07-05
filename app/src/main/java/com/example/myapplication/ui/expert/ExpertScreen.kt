package com.example.myapplication.ui.expert

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
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
            QuickLogCard(onLog = { viewModel.submitSolution(userId, "Q_ID", it) })
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("📜 我的知識庫", modifier = Modifier.padding(vertical = 16.dp), fontWeight = FontWeight.Bold, color = AppColors.TextWhite)
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
                modifier = Modifier.fillMaxWidth().height(48.dp),
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
fun QuickLogCard(onLog: (String) -> Unit) {
    var expText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("📝 記錄今日解法", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = expText,
                onValueChange = { expText = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("我解決了什麼問題？") },
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

            Button(
                onClick = {
                    if (expText.isNotBlank()) {
                        onLog(expText.trim())
                        expText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = expText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentGreen)
            ) {
                Text("儲存至知識庫", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
