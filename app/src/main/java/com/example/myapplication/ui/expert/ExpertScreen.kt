package com.example.myapplication.ui.expert

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
import com.example.myapplication.data.model.SkillStatus
import com.example.myapplication.data.model.SolutionItem
import com.example.myapplication.domain.expert.ExpertInputValidator
import com.example.myapplication.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

// ─── 進入動畫修飾符 ───────────────────────────────────
@Composable
fun Modifier.fadeSlideIn(index: Int, baseDelay: Long = 80L): Modifier {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * baseDelay); visible = true }
    return this.then(
        Modifier.animateContentSize()
            .let {
                if (visible) it else it
            }
    )
}

// ─── 主畫面 ────────────────────────────────────────────
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
    var buttonCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var outerBoxLayout by remember { mutableStateOf<LayoutCoordinates?>(null) }
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

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = AppColors.DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .onGloballyPositioned { outerBoxLayout = it }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── 頂部問候 ──
                item {
                    HeaderSection()
                }

                // ── 統計卡片 ──
                item {
                    StatsCard(
                        helpCount = uiState.helpCount,
                        rating = uiState.rating
                    )
                }

                // ── 技能輸入卡片 ──
                item {
                    QuickLogCard(
                        onPublish = onPublishSkill,
                        onClearFeedback = onClearPublishFeedback,
                        onButtonLayoutChanged = { coords -> buttonCoords = coords },
                        clearInputSignal = successVersion
                    )
                }

                // ── 知識庫區塊 ──
                item {
                    SectionHeader(
                        title = stringResource(R.string.expert_knowledge_title),
                        icon = Icons.Outlined.Lightbulb
                    )
                }

                val visibleHistory = uiState.solutionHistory.filter { it.status != SkillStatus.PENDING }

                if (visibleHistory.isEmpty()) {
                    item {
                        EmptyKnowledgeCard()
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

                // ── 操作按鈕 ──
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

            // ── 浮動回饋提示 ──
            AnimatedVisibility(
                visible = feedbackMsg != null && buttonCoords != null && outerBoxLayout != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                if (feedbackMsg != null && buttonCoords != null && outerBoxLayout != null) {
                    val density = LocalDensity.current
                    val buttonRoot = buttonCoords!!.localToRoot(Offset.Zero).y + buttonCoords!!.size.height
                    val outerRoot = outerBoxLayout!!.localToRoot(Offset.Zero).y
                    val anchorY = with(density) { (buttonRoot - outerRoot + 12.dp.toPx()).roundToInt() }

                    FeedbackBanner(
                        message = feedbackMsg,
                        isError = uiState.publishFeedbackIsError,
                        offsetY = anchorY
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════
// 子元件
// ═══════════════════════════════════════════════════════

// ─── 頂部問候區 ───────────────────────────────────────
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

// ─── 統計卡片 ─────────────────────────────────────────
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
                    icon = Icons.Outlined.TrendingUp,
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

// ─── 區段標題 ─────────────────────────────────────────
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

// ─── 技能輸入卡片 ─────────────────────────────────────
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
                        color = if (expertise.isNotBlank()) Color(0xFF0B0E14) else AppColors.TextGray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

// ─── 空知識庫卡片 ─────────────────────────────────────
@Composable
private fun EmptyKnowledgeCard() {
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

// ─── 知識庫項目卡片 ───────────────────────────────────
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

// ─── 按鈕樣式 ─────────────────────────────────────────
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
                    Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ─── 浮動回饋 ─────────────────────────────────────────
@Composable
private fun FeedbackBanner(message: String, isError: Boolean, offsetY: Int) {
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

// ─── 編輯對話框 ───────────────────────────────────────
@Composable
fun SkillEditDialog(
    currentText: String,
    errorMessage: String?,
    isSubmitting: Boolean,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        shape = RoundedCornerShape(20.dp),
        containerColor = AppColors.SurfaceDark,
        titleContentColor = AppColors.TextWhite,
        title = {
            Text(
                stringResource(R.string.expert_edit_dialog_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = currentText,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubmitting,
                label = { Text(stringResource(R.string.expert_edit_label)) },
                singleLine = false,
                minLines = 2,
                shape = RoundedCornerShape(14.dp),
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppColors.TextWhite,
                    unfocusedTextColor = AppColors.TextWhite,
                    focusedBorderColor = AppColors.AccentGreen,
                    unfocusedBorderColor = AppColors.BorderGray,
                    cursorColor = AppColors.AccentGreen,
                    focusedContainerColor = AppColors.SurfaceMedium,
                    unfocusedContainerColor = AppColors.SurfaceMedium
                ),
                supportingText = if (errorMessage != null) {
                    { Text(errorMessage, color = AppColors.StatusError) }
                } else null
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = currentText.isNotBlank() && !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentGreen)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = AppColors.DarkBackground
                    )
                } else {
                    Text(stringResource(R.string.expert_edit_dialog_confirm), color = AppColors.DarkBackground)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss, enabled = !isSubmitting
            ) {
                Text(stringResource(R.string.expert_edit_dialog_cancel), color = AppColors.TextGray)
            }
        }
    )
}
