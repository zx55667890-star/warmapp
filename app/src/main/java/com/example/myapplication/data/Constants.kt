package com.example.myapplication.data

object FirebasePaths {
    const val SOLUTIONS = "solutions"
    const val PENDING_SKILLS = "pending_skills"
    const val ACTIVE_EXPERIENCES = "active_experiences"
    const val USERS = "users"
    const val QUESTIONS = "questions"
    const val TAGS_BLACKLIST = "tags_blacklist"
    const val TAGS_WHITELIST = "tags_whitelist"
    const val CONFIG = "config"
    const val MODEL_STATUS = "config/model_status"
    const val PENDING_QUESTIONS = "pending_questions"
}

object StatusValues {
    const val MATCHING = "matching"
    const val PENDING_ACCEPTANCE = "pending_acceptance"
    const val EXPERT_ACCEPTED = "expert_accepted"
    const val TAKEN = "taken"
    const val CANCELLED = "cancelled"
    const val ACTIVE = "active"
    const val OFFLINE = "offline"
}

object FirebaseFields {
    const val ID = "id"
    const val EXPERTISE = "expertise"
    const val TAGS = "tags"
    const val TIMESTAMP = "timestamp"
    const val STATUS = "status"
    const val USER_ID = "userId"
    const val TEXT = "text"
    const val QUESTION_ID = "questionId"
    const val EXPERT_ID = "expertId"
    const val RATING = "rating"
    const val HELP_COUNT = "helpCount"
    const val IS_ONLINE = "isOnline"
    const val REJECTED_EXPERTS = "rejectedExperts"
    const val PENDING_QUESTION = "pending_question"
    const val MATCHED_EXP_TEXT = "matchedExpText"
    const val MATCHED_EXP_TIMESTAMP = "matchedExpTimestamp"
    const val AUTHOR_ID = "authorId"
}
