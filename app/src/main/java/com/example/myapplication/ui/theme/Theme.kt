package com.example.myapplication.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

// 把 AppColors 映射到 Material3 的色彩角色
private val AppDarkColorScheme = darkColorScheme(
    primary            = AppColors.AccentGreen,
    onPrimary          = AppColors.DarkBackground,
    primaryContainer   = AppColors.AccentGreen.copy(alpha = 0.12f),
    onPrimaryContainer = AppColors.AccentGreen,

    secondary          = AppColors.AccentBlue,
    onSecondary        = AppColors.DarkBackground,
    secondaryContainer = AppColors.AccentBlue.copy(alpha = 0.12f),
    onSecondaryContainer = AppColors.AccentBlue,

    tertiary           = AppColors.AccentOrange,
    onTertiary         = AppColors.DarkBackground,

    background         = AppColors.DarkBackground,
    onBackground       = AppColors.TextWhite,

    surface            = AppColors.SurfaceDark,
    onSurface          = AppColors.TextWhite,
    surfaceVariant     = AppColors.SurfaceMedium,
    onSurfaceVariant   = AppColors.TextGray,

    error              = AppColors.StatusError,
    onError            = AppColors.DarkBackground,
    errorContainer     = AppColors.StatusErrorBg,
    onErrorContainer   = AppColors.StatusError,

    outline            = AppColors.BorderGray,
    outlineVariant     = AppColors.BorderGray.copy(alpha = 0.5f),

    inverseSurface     = AppColors.TextWhite,
    inverseOnSurface   = AppColors.DarkBackground,
    inversePrimary     = AppColors.AccentGreen.copy(alpha = 0.7f)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        typography = Typography,
        content = content
    )
}
