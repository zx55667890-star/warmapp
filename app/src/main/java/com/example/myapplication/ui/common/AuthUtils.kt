package com.example.myapplication.ui.common

object AuthUtils {
    private const val MAX_NICKNAME_LENGTH = 20

    val ALLOWED_DOMAINS = listOf(
        "yahoo.com.tw", "yahoo.co.jp", "yahoo.com.hk",
        "yahoo.co.uk", "yahoo.com.au", "yahoo.com.sg", "yahoo.com.my",
        "yahoo.co.in", "yahoo.com.br", "yahoo.com.mx", "yahoo.com.ar",
        "yahoo.es", "yahoo.de", "yahoo.fr", "yahoo.it", "yahoo.ca",
        "yahoo.co.kr", "yahoo.com.cn", "yahoo.com.ph", "yahoo.com.vn",
        "yahoo.co.th", "yahoo.com.id",
        "outlook.com", "hotmail.com", "hotmail.co.uk", "live.com",
        "msn.com", "outlook.com.tw", "outlook.com.br",
    )

    fun validatePassword(password: String): String? {
        val missing = mutableListOf<String>()
        if (password.length < 8) missing.add("至少 8 個字元")
        if (!password.any { it.isUpperCase() }) missing.add("大寫字母")
        if (!password.any { it.isLowerCase() }) missing.add("小寫字母")
        if (!password.any { it.isDigit() }) missing.add("數字")
        return if (missing.isNotEmpty()) "密碼需包含：${missing.joinToString("、")}" else null
    }

    fun validateNickname(nickname: String): String? {
        if (nickname.length > MAX_NICKNAME_LENGTH) return "暱稱最多 $MAX_NICKNAME_LENGTH 個字元"
        if (!nickname.matches(Regex("^[\\p{L}\\p{N} _\\-·.]+$"))) return "暱稱只能包含文字、數字、空格、底線、連字號、間隔號或句點"
        return null
    }

    fun isAllowedEmail(email: String): Boolean {
        val domain = email.substringAfter("@").lowercase()
        return domain == "gmail.com" || ALLOWED_DOMAINS.any { domain.endsWith(it) }
    }
}
