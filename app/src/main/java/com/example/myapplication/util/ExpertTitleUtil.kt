package com.example.myapplication.util

import androidx.compose.ui.graphics.Color

/**
 * 根據專家積分計算稱號與對應顏色
 * @param score helpCount * rating
 * @return Pair(稱號文字, 顏色)
 */
fun calculateExpertTitle(score: Double): Pair<String, Color> {
    return when {
        score >= 100 -> "Lv 5 王牌專家" to Color(0xFF9C27B0)
        score >= 50  -> "Lv 4 資深專家" to Color(0xFFFF9800)
        score >= 25  -> "Lv 3 可靠專家" to Color(0xFF2196F3)
        score >= 10  -> "Lv 2 經驗分享者" to Color(0xFF4CAF50)
        else         -> "Lv 1 剛加入" to Color(0xFF757575)
    }
}
