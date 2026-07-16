package com.example.myapplication.di

import android.content.SharedPreferences
import com.example.myapplication.data.repository.AiRepository
import com.example.myapplication.data.repository.MatchingRepositoryInterface
import com.example.myapplication.data.repository.MediaUploader
import com.example.myapplication.data.repository.QuestionRepository
import com.example.myapplication.ui.seeker.SeekerViewModel
import com.example.myapplication.domain.seeker.ObserveQuestionStatusUseCase
import com.example.myapplication.domain.seeker.QuestionStatus
import com.example.myapplication.domain.seeker.SendQuestionMediaUseCase
import com.example.myapplication.domain.seeker.ValidateQuestionQuotaUseCase
import com.google.firebase.database.FirebaseDatabase
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SeekerViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @RelaxedMockK
    private lateinit var firebaseDb: FirebaseDatabase

    @RelaxedMockK
    private lateinit var prefs: SharedPreferences

    @RelaxedMockK
    private lateinit var matchingRepository: MatchingRepositoryInterface

    @RelaxedMockK
    private lateinit var questionRepository: QuestionRepository

    @RelaxedMockK
    private lateinit var aiRepository: AiRepository

    @RelaxedMockK
    private lateinit var mediaUploader: MediaUploader

    @RelaxedMockK
    private lateinit var editor: SharedPreferences.Editor

    @RelaxedMockK
    private lateinit var validateQuestionQuotaUseCase: ValidateQuestionQuotaUseCase

    @RelaxedMockK
    private lateinit var sendQuestionMediaUseCase: SendQuestionMediaUseCase

    @RelaxedMockK
    private lateinit var observeQuestionStatusUseCase: ObserveQuestionStatusUseCase
    private val statusFlow = MutableSharedFlow<QuestionStatus>(extraBufferCapacity = 64)

    private lateinit var viewModel: SeekerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } returns Unit
        every { observeQuestionStatusUseCase.invoke(any()) } returns statusFlow

        viewModel = SeekerViewModel(
            firebaseDb, prefs, matchingRepository, aiRepository, mediaUploader,
            questionRepository, validateQuestionQuotaUseCase, observeQuestionStatusUseCase, sendQuestionMediaUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `observeStatus with taken status sets navigation state`() {
        val questionId = "testQuestion123"
        val questionText = "Hello, this is a test question"

        viewModel.observeStatus(questionId)
        statusFlow.tryEmit(QuestionStatus.Taken(questionId = questionId, questionText = questionText))

        assertEquals(questionId, viewModel.activeChatRoomId)
        assertEquals("user", viewModel.uiState.value.myRole)
        assertEquals(questionText, viewModel.uiState.value.activeChatQuestionText)
        assertFalse(viewModel.uiState.value.isUserMatching)
        assertFalse(viewModel.uiState.value.showSeekerConfirmDialog)
    }

    @Test
    fun `observeStatus with expert_accepted status shows confirm dialog`() {
        val questionId = "testQuestion456"
        val expertId = "expert789"
        val expertText = "I can help with this"
        val timestamp = System.currentTimeMillis()

        viewModel.observeStatus(questionId)
        statusFlow.tryEmit(QuestionStatus.ExpertAccepted(
            expertId = expertId, expertText = expertText, timestamp = timestamp
        ))

        assertTrue(viewModel.uiState.value.showSeekerConfirmDialog)
        assertFalse(viewModel.uiState.value.isUserMatching)
        assertEquals(expertId, viewModel.matchedExpertId)
        assertEquals(expertText, viewModel.matchedExpertText)
    }

    @Test
    fun `observeStatus with no_experts legacy status resets UI state`() {
        val questionId = "testNoExpert"

        viewModel.observeStatus(questionId)
        statusFlow.tryEmit(QuestionStatus.NoExperts)

        assertFalse(viewModel.uiState.value.isUserMatching)
    }

    @Test
    fun `observeStatus with cancelled status clears state`() {
        val questionId = "testCancelled"

        viewModel.observeStatus(questionId)
        statusFlow.tryEmit(QuestionStatus.Cancelled(authorId = ""))

        assertFalse(viewModel.uiState.value.isUserMatching)
        assertFalse(viewModel.uiState.value.showSeekerConfirmDialog)
    }
}
