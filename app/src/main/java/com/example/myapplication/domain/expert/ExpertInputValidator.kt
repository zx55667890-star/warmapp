package com.example.myapplication.domain.expert

object ExpertInputValidator {
    enum class ValidationError {
        BLANK, TOO_SHORT, NO_MEANINGFUL_CHAR, HIGH_REPETITION, GIBBERISH, INVALID_ENGLISH
    }

    const val MAX_CHAR_LIMIT = 20
    const val MIN_SKILL_LENGTH = 10
    private const val UNIQUE_CHAR_RATIO_THRESHOLD = 0.4
    private const val RATIO_CHECK_MIN_LENGTH = 6
    private const val MAX_CONSECUTIVE_DUPLICATES = 3
    private const val MAX_ADJACENT_PAIRS = 3
    private const val SINGLETONS_THRESHOLD = 3
    private const val ADJACENT_CHECK_MIN_LENGTH = 5
    private const val PURE_ENGLISH_MIN_LENGTH = 6
    private const val BIGRAM_REPEAT_THRESHOLD = 2
    private const val MAX_CHAR_FREQUENCY = 3
    private val VOWELS = setOf('a', 'e', 'i', 'o', 'u')
    private val SKILL_UNLIKELY_CHARS = setOf('哦', '呢', '嗎', '吧', '額', '喔', '誒', '欸', '啦', '嘛', '呀', '喲', '嘅', '誰', '該')

    fun validate(text: String): ValidationError? {
        val trimmed = text.trim()

        if (trimmed.isBlank()) return ValidationError.BLANK
        if (trimmed.length < MIN_SKILL_LENGTH) return ValidationError.TOO_SHORT

        val hasMeaningfulChar = trimmed.any {
            it.isLetter() || (it.code in 0x4E00..0x9FFF)
        }
        if (!hasMeaningfulChar) return ValidationError.NO_MEANINGFUL_CHAR

        val uniqueCharsCount = trimmed.toSet().size
        if (trimmed.length >= RATIO_CHECK_MIN_LENGTH && (uniqueCharsCount.toDouble() / trimmed.length) < UNIQUE_CHAR_RATIO_THRESHOLD) {
            return ValidationError.HIGH_REPETITION
        }

        var maxConsecutive = 1
        var currentRun = 1
        var adjacentPairCount = 0
        trimmed.zipWithNext().forEach { (prev, curr) ->
            if (curr == prev) {
                currentRun++
                maxConsecutive = maxOf(maxConsecutive, currentRun)
                if (currentRun == 2) adjacentPairCount++
            } else {
                currentRun = 1
            }
        }
        if (maxConsecutive >= MAX_CONSECUTIVE_DUPLICATES) {
            return ValidationError.GIBBERISH
        }
        if (adjacentPairCount >= MAX_ADJACENT_PAIRS) {
            return ValidationError.GIBBERISH
        }
        if (adjacentPairCount >= 1 && trimmed.length >= ADJACENT_CHECK_MIN_LENGTH) {
            val charCounts = trimmed.groupingBy { it }.eachCount()
            val singletons = charCounts.count { it.value == 1 }
            if (singletons >= SINGLETONS_THRESHOLD) {
                return ValidationError.GIBBERISH
            }
        }

        val bigrams = trimmed.windowed(2).groupingBy { it }.eachCount()
        if (bigrams.any { it.value >= BIGRAM_REPEAT_THRESHOLD && trimmed.length >= 8 }) {
            return ValidationError.GIBBERISH
        }

        if (trimmed.any { it in SKILL_UNLIKELY_CHARS }) {
            return ValidationError.GIBBERISH
        }

        if (trimmed.groupingBy { it }.eachCount().any { it.value > MAX_CHAR_FREQUENCY }) {
            return ValidationError.GIBBERISH
        }

        val isPureEnglish = trimmed.all { it.isLetter() && it.code < 128 }
        if (isPureEnglish && trimmed.length >= PURE_ENGLISH_MIN_LENGTH) {
            val hasVowels = trimmed.any { it.lowercaseChar() in VOWELS }
            if (!hasVowels) return ValidationError.INVALID_ENGLISH
        }

        return null
    }
}
