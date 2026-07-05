package com.example.myapplication.data.repository

interface MatchingRepositoryInterface {
    fun matchAndAssignExpert(questionId: String, text: String, userId: String)
}