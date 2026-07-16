package com.example.myapplication.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.R

private val NunitoSans = FontFamily(
    Font(R.font.nunito_sans_regular, FontWeight.Normal),
    Font(R.font.nunito_sans_bold, FontWeight.Bold)
)

val Typography = Typography(
    // 頁面大標題（登入歡迎語）
    displayLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    // 區塊標題
    displayMedium = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp
    ),
    // 統計數字、大數據展示
    headlineLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // 區塊副標題
    headlineMedium = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // 卡片標題
    titleLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    // 次級卡片標題
    titleMedium = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // 標籤、小卡片標題
    titleSmall = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // 正文、聊天訊息
    bodyLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // 副文、描述文字
    bodyMedium = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // 提示文字、時間戳
    bodySmall = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // 按鈕文字
    labelLarge = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    // 標籤、Chip 文字
    labelMedium = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    // 最小輔助文字
    labelSmall = TextStyle(
        fontFamily = NunitoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
)
