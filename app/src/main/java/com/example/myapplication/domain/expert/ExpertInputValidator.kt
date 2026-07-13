package com.example.myapplication.domain.expert

object ExpertInputValidator {
    fun validate(text: String): String? {
        val trimmed = text.trim()

        if (trimmed.isBlank()) return "輸入內容不能為空"
        if (trimmed.length < 4) return "描述太短囉，請輸入至少 4 個字讓 AI 更好理解"

        val hasMeaningfulChar = trimmed.any {
            it.isLetter() || it.toString().matches(Regex("[\\u4e00-\\u9fa5]"))
        }
        if (!hasMeaningfulChar) return "請輸入有效的中文或英文專業技能描述"

        val uniqueCharsCount = trimmed.toSet().size
        if (trimmed.length >= 6 && (uniqueCharsCount.toDouble() / trimmed.length) < 0.4) {
            return "文字重複率過高，請輸入更具體的技能描述"
        }

        var maxConsecutive = 1
        var currentConsecutive = 1
        var adjacentPairCount = 0
        for (i in 1 until trimmed.length) {
            if (trimmed[i] == trimmed[i - 1]) {
                currentConsecutive++
                if (currentConsecutive > maxConsecutive) {
                    maxConsecutive = currentConsecutive
                }
                if (currentConsecutive == 2) adjacentPairCount++
            } else {
                currentConsecutive = 1
            }
        }
        if (maxConsecutive >= 3) {
            return "請輸入有意義的內容，避免過多重複的字元"
        }
        if (adjacentPairCount >= 3) {
            return "請輸入有意義的內容，避免過多重複的字元"
        }
        if (adjacentPairCount >= 1 && trimmed.length >= 6) {
            val charCounts = trimmed.groupingBy { it }.eachCount()
            val singletons = charCounts.count { it.value == 1 }
            if (singletons >= 4) {
                return "請輸入有意義的內容，避免過多重複的字元"
            }
        }

        val isPureEnglish = trimmed.all { it.isLetter() && it.code < 128 }
        if (isPureEnglish && trimmed.length >= 6) {
            val hasVowels = trimmed.any { it.lowercaseChar() in listOf('a', 'e', 'i', 'o', 'u') }
            if (!hasVowels) return "請輸入正確的英文單字或技能名稱"
        }

        return null
    }
}
