package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // ── 背景層級（保持不動，這部分很好）──
    val DarkBackground   = Color(0xFF0B0E14)
    val SurfaceDark      = Color(0xFF12161F)
    val SurfaceMedium    = Color(0xFF1A1F2E)
    val SurfaceLight     = Color(0xFF232940)

    // ── 強調色（微調）──
    val AccentGreen      = Color(0xFF34D399)      // 柔和一點的綠，護眼
    val AccentBlue       = Color(0xFF60A5FA)       // 稍暖的藍，更實用
    val AccentGradientStart = AccentGreen
    val AccentGradientEnd   = Color(0xFF2DD4BF)    // 綠→青漸層
    val AccentOrange     = Color(0xFFF97316)       // 稍偏暖橙，更有活力

    // ── 文字（不動）──
    val TextWhite        = Color(0xFFF0F2F5)
    val TextGray         = Color(0xFF6B7280)
    val TextMuted        = Color(0xFF3D4455)

    // ── 邊框 & 分隔 ──
    val BorderGray       = Color(0xFF2A3042)
    val BorderGlow       = AccentGreen.copy(alpha = 0.25f)

    // ── 玻璃效果（稍微加強）──
    val GlassStroke      = Color.White.copy(alpha = 0.10f)  // 6% → 10%
    val GlassFill        = Color.White.copy(alpha = 0.05f)  // 3% → 5%

    // ── 狀態（保持不動）──
    val StatusError      = Color(0xFFEF4444)
    val StatusErrorBg    = StatusError.copy(alpha = 0.08f)
    val StatusSuccess    = AccentGreen
    val StatusSuccessBg  = AccentGreen.copy(alpha = 0.08f)
    val StatusPending    = Color(0xFFFBBF24)
    val StatusPendingBg  = StatusPending.copy(alpha = 0.08f)
}
