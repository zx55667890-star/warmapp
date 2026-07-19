package com.example.myapplication.ui.expert

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.model.SkillStatus
import com.example.myapplication.data.model.SolutionItem
import com.example.myapplication.ui.expert.components.KnowledgeItemCard
import com.example.myapplication.ui.expert.components.QuickLogCard
import com.example.myapplication.ui.expert.components.SkillEditDialog
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ExpertScreen(viewModel: ExpertViewModel, userId: String, onNavigateToInput: () -> Unit = {}) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEvent.collectLatest { event ->
                when (event) {
                    is ExpertUiEvent.ShowToast ->
                        snackbarHostState.showSnackbar(context.getString(event.resId))
                    is ExpertUiEvent.ShowToastRaw ->
                        snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    ExpertScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onPublishSkill = { viewModel.publishSkill(userId, it) },
        onClearPublishFeedback = { viewModel.clearPublishFeedback() },
        onStartSkillEdit = { viewModel.startSkillEdit(it) },
        onEditSkillConfirm = { viewModel.submitSkillEdit(userId) },
        onEditSkillTextChange = { viewModel.updateSkillEditText(it) },
        onEditSkillDismiss = { viewModel.cancelSkillEdit() },
        onNavigateToInput = onNavigateToInput
    )
}

@Composable
fun ExpertScreenContent(
    uiState: ExpertUiState,
    snackbarHostState: SnackbarHostState,
    onPublishSkill: (String) -> Unit,
    onClearPublishFeedback: () -> Unit,
    onStartSkillEdit: (SolutionItem) -> Unit,
    onEditSkillConfirm: () -> Unit,
    onEditSkillTextChange: (String) -> Unit,
    onEditSkillDismiss: () -> Unit,
    onNavigateToInput: () -> Unit
) {
    var successVersion by remember { mutableIntStateOf(0) }
    val feedbackMsg = uiState.publishFeedbackRes?.let { stringResource(it) }

    LaunchedEffect(uiState.publishFeedbackRes, uiState.publishFeedbackIsError) {
        if (uiState.publishFeedbackRes != null && !uiState.publishFeedbackIsError) {
            successVersion++
        }
    }

    LaunchedEffect(feedbackMsg) {
        if (feedbackMsg != null) { delay(3000); onClearPublishFeedback() }
    }

    if (uiState.skillEditTarget != null) {
        SkillEditDialog(
            currentText = uiState.editText,
            errorMessage = uiState.editErrorRes?.let { stringResource(it) },
            isSubmitting = uiState.isSubmitting,
            onTextChange = onEditSkillTextChange,
            onConfirm = onEditSkillConfirm,
            onDismiss = onEditSkillDismiss
        )
    }

    var buttonYPositionPx by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = AppColors.DarkBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    item { HeaderSection() }

                    item {
                        StatsCard(helpCount = uiState.helpCount, rating = uiState.rating)
                    }

                    item {
                        QuickLogCard(
                            onPublish = onPublishSkill,
                            onClearFeedback = onClearPublishFeedback,
                            onButtonLayoutChanged = { coordinates ->
                                buttonYPositionPx = coordinates.positionInRoot().y
                            },
                            clearInputSignal = successVersion
                        )
                    }

                    item {
                        SectionHeader(
                            title = stringResource(R.string.expert_knowledge_title),
                            icon = Icons.Outlined.Lightbulb
                        )
                    }

                    val visibleHistory = uiState.solutionHistory.filter { it.status != SkillStatus.PENDING }

                    if (visibleHistory.isEmpty()) {
                        item {
                            com.example.myapplication.ui.expert.components.EmptyKnowledgeCard()
                        }
                    } else {
                        itemsIndexed(visibleHistory) { index, solution ->
                            KnowledgeItemCard(
                                solution = solution,
                                onEditClick = { onStartSkillEdit(solution) },
                                animDelay = index * 60L
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        ActionButton(
                            text = stringResource(R.string.expert_back_button),
                            onClick = onNavigateToInput,
                            style = ActionButtonStyle.OUTLINED
                        )
                    }

                    item {
                        ActionButton(
                            text = "批次測試（20筆）",
                            onClick = {
                                val skills = listOf(
                                    "Mojo 高效能 AI 原生程式語言模組開發",
                                    "Quantum CI 分散式圖形化 CI/CD 執行引擎",
                                    "Backstage 內部開發者入口平台外掛整合",
                                    "OpenTofu 開源 Terraform 分支遷移與管理",
                                    "eBPF Linux 核心動態追蹤與安全監控",
                                    "Zig 零成本抽象系統程式語言開發",
                                    "Tekton Kubernetes 原生 CI/CD 管線建置",
                                    "Kyverno Kubernetes 准入控制策略管理",
                                    "CycloneDX 軟體物料清單自動生成與稽核",
                                    "Pulumi 基礎設施即程式碼多雲編排",
                                    "Dagger CI/CD 管線容器化圖形執行",
                                    "WasmEdge WebAssembly 輕量級邊緣運算",
                                    "教你如何用 OPENCLAW 使用電腦操控",
                                    "HashiCorp Boundary 零信任動態憑證代理",
                                    "NixOS 宣告式系統配置與可重現建置",
                                    "Temporal 持久化分散式工作流程編排",
                                    "RisingWave 串流資料庫即時物化視圖",
                                    "Cilium eBPF 容器網路安全與可觀測性",
                                    "Carbon Google 實驗性系統程式語言",
                                    "教你如何使用 GPT 5.6 提示詞工程"
                                )
                                skills.forEach { onPublishSkill(it) }
                            },
                            style = ActionButtonStyle.GRADIENT
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = feedbackMsg != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 20.dp)
        ) {
            val offsetYPx = with(density) {
                val spacingPx = 210.dp.toPx()
                (buttonYPositionPx - spacingPx).toInt()
            }

            com.example.myapplication.ui.expert.components.FeedbackBanner(
                message = feedbackMsg ?: "",
                isError = uiState.publishFeedbackIsError,
                offsetY = offsetYPx.coerceAtLeast(0)
            )
        }
    }
}

@Composable
private fun HeaderSection() {
    Column(modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)) {
        Text(
            text = "我的專業影響力",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextWhite
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "分享您能幫忙解決的事，越具體越能配對到需要您的人",
            fontSize = 13.sp,
            color = AppColors.TextGray,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun StatsCard(helpCount: Long, rating: Double) {
    val infiniteTransition = rememberInfiniteTransition(label = "stats")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.12f,
        animationSpec = infiniteRepeatable(tween(2000, easing = EaseInOut), RepeatMode.Reverse),
        label = "glow"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, AppColors.GlassStroke)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AppColors.AccentGreen.copy(alpha = glowAlpha),
                            AppColors.SurfaceMedium,
                            AppColors.AccentBlue.copy(alpha = glowAlpha * 0.5f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.AutoMirrored.Outlined.TrendingUp,
                    label = "已解決問題",
                    value = "$helpCount",
                    accentColor = AppColors.AccentGreen
                )
                VerticalDivider(
                    modifier = Modifier.height(56.dp),
                    color = AppColors.BorderGray
                )
                StatItem(
                    icon = Icons.Outlined.Star,
                    label = "評分",
                    value = "%.1f".format(rating),
                    accentColor = AppColors.AccentBlue
                )
            }
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, label: String, value: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon, contentDescription = null,
            tint = accentColor, modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value, fontSize = 28.sp,
            fontWeight = FontWeight.Bold, color = AppColors.TextWhite
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 12.sp, color = AppColors.TextGray)
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon, contentDescription = null,
            tint = AppColors.AccentGreen, modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title, fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold, color = AppColors.TextWhite
        )
    }
}

private enum class ActionButtonStyle { OUTLINED, GRADIENT }

@Composable
private fun ActionButton(text: String, onClick: () -> Unit, style: ActionButtonStyle) {
    when (style) {
        ActionButtonStyle.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AppColors.AccentBlue.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = AppColors.AccentBlue
                )
            ) {
                Text(text, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
        ActionButtonStyle.GRADIENT -> {
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(AppColors.AccentOrange, AppColors.AccentOrange.copy(alpha = 0.8f))
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text, color = AppColors.DarkBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
