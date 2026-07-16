package com.example.myapplication.domain.expert

import com.example.myapplication.data.model.SolutionItem
import com.example.myapplication.data.repository.ExpertRepository
import kotlinx.coroutines.flow.Flow

class ObserveSolutionsUseCase(
    private val expertRepository: ExpertRepository
) {
    operator fun invoke(userId: String): Flow<List<SolutionItem>> {
        return expertRepository.listenToSolutionHistory(userId)
    }
}
