package com.example.myapplication.domain.chat

import com.example.myapplication.data.repository.MessageRepositoryFactory
import com.example.myapplication.data.repository.UserRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FetchOpponentUseCase(
    private val repoFactory: MessageRepositoryFactory,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(opponentId: String): OpponentProfile {
        val repo = repoFactory.create("") // chatroomId unused in fetchOpponentProfile
        val (rating, helpCount) = suspendCancellableCoroutine { cont ->
            repo.fetchOpponentProfile(
                userId = opponentId,
                onSuccess = { r, h -> cont.resume(Pair(r, h)) },
                onFailure = { cont.resume(Pair(5.0, 0L)) }
            )
        }
        val nickname = suspendCancellableCoroutine { cont ->
            userRepository.getNickname(opponentId) { cont.resume(it) }
        }
        return OpponentProfile(nickname, rating, helpCount)
    }
}
