package com.example.myapplication.ui.expert.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.AppColors

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
