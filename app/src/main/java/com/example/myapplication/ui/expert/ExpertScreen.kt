package com.example.myapplication.ui.expert

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.model.SkillStatus
import com.example.myapplication.data.model.SolutionItem
import com.example.myapplication.di.ExpertUiEvent
import com.example.myapplication.di.ExpertUiState
import com.example.myapplication.di.ExpertViewModel
import com.example.myapplication.domain.expert.ExpertInputValidator
import com.example.myapplication.ui.theme.AppColors
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
                    is ExpertUiEvent.ShowToast -> {
                        snackbarHostState.showSnackbar(context.getString(event.resId))
                    }
                    is ExpertUiEvent.ShowToastRaw -> {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    ExpertScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onPublishSkill = { viewModel.publishSkill(userId, it) },
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
    onStartSkillEdit: (SolutionItem) -> Unit,
    onEditSkillConfirm: () -> Unit,
    onEditSkillTextChange: (String) -> Unit,
    onEditSkillDismiss: () -> Unit,
    onNavigateToInput: () -> Unit
) {
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
                        Text(stringResource(R.string.expert_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${stringResource(R.string.expert_help_count)}: ${uiState.helpCount}  |  ${stringResource(R.string.expert_rating)}: ${"%.1f".format(uiState.rating)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                QuickLogCard(
                    onPublish = onPublishSkill
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    stringResource(R.string.expert_knowledge_title),
                    modifier = Modifier.padding(vertical = 16.dp),
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextWhite
                )

                val visibleHistory = uiState.solutionHistory.filter { it.status != SkillStatus.PENDING }
                if (visibleHistory.isEmpty()) {
                    Text(stringResource(R.string.expert_no_records), color = AppColors.TextGray, fontSize = 14.sp)
                }
            }

            items(visibleHistory) { solution ->
                KnowledgeItemCard(
                    solution = solution,
                    onEditClick = { onStartSkillEdit(solution) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onNavigateToInput,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, AppColors.AccentBlue),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.AccentBlue)
                ) {
                    Text(stringResource(R.string.expert_back_button), fontWeight = FontWeight.Bold)
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
        colors = CardDefaults.cardColors(
            containerColor = when (solution.status) {
                SkillStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
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
                    color = if (solution.status == SkillStatus.REJECTED) AppColors.TextGray else AppColors.TextWhite,
                    fontWeight = FontWeight.Medium
                )

                when (solution.status) {
                    SkillStatus.PENDING -> { }
                    SkillStatus.REJECTED -> {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.expert_content_unrecognized),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    SkillStatus.ACTIVE -> {
                        if (solution.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                solution.tags.take(5).forEach { tag ->
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(tag, fontSize = 11.sp) },
                                        border = BorderStroke(1.dp, AppColors.AccentGreen.copy(alpha = 0.5f))
                                    )
                                }
                                if (solution.tags.size > 5) {
                                    Text("+${solution.tags.size - 5}", fontSize = 11.sp, color = AppColors.TextGray)
                                }
                            }
                        }
                    }
                }
            }

            if (solution.status != SkillStatus.PENDING) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.expert_edit_content_desc),
                        tint = AppColors.TextGray
                    )
                }
            }
        }
    }
}

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
        title = { Text(stringResource(R.string.expert_edit_dialog_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = currentText,
                    onValueChange = onTextChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSubmitting,
                    label = { Text(stringResource(R.string.expert_edit_label)) },
                    singleLine = false,
                    minLines = 2,
                    isError = errorMessage != null,
                    supportingText = if (errorMessage != null) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = currentText.isNotBlank() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.expert_edit_dialog_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) { Text(stringResource(R.string.expert_edit_dialog_cancel)) }
        }
    )
}

@Composable
fun QuickLogCard(
    onPublish: (text: String) -> Unit
) {
    var expertise by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val maxCharLimit = ExpertInputValidator.MAX_CHAR_LIMIT

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(stringResource(R.string.expert_quick_log_title), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(stringResource(R.string.expert_quick_log_hint), fontSize = 12.sp, color = AppColors.TextGray, modifier = Modifier.padding(top = 4.dp))

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
                placeholder = { Text(stringResource(R.string.expert_quick_log_placeholder)) },
                singleLine = false,
                minLines = 2,
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
                    val trimmed = expertise.trim()
                    errorMessage = ""
                    onPublish(trimmed)
                    expertise = ""
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = expertise.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.AccentGreen)
            ) {
                Text(stringResource(R.string.expert_publish_button), color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
