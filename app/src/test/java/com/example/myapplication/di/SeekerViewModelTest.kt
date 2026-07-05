package com.example.myapplication.di

import android.content.SharedPreferences
import com.example.myapplication.data.repository.AiRepository
import com.example.myapplication.data.repository.MatchingRepositoryInterface
import com.example.myapplication.data.repository.MediaUploader
import com.example.myapplication.data.repository.QuestionRepository
import com.example.myapplication.di.SeekerViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SeekerViewModelTest {

    private val mainDispatcher = newSingleThreadContext("test-main")

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

    private lateinit var viewModel: SeekerViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        MockKAnnotations.init(this)
        every { prefs.edit() } returns editor
        every { editor.putString(any(), any()) } returns editor
        every { editor.remove(any()) } returns editor
        every { editor.apply() } returns Unit
        viewModel = SeekerViewModel(firebaseDb, prefs, matchingRepository, aiRepository, mediaUploader, questionRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainDispatcher.close()
        unmockkAll()
    }

    @Test
    fun `listenToMyQuestionStatus with taken status sets navigation state`() {
        val questionId = "testQuestion123"
        val questionText = "Hello, this is a test question"

        val questionsRef = mockk<DatabaseReference>(relaxed = true)
        val questionRef = mockk<DatabaseReference>(relaxed = true)
        every { firebaseDb.getReference("questions") } returns questionsRef
        every { questionsRef.child(questionId) } returns questionRef

        val listenerSlot = slot<ValueEventListener>()
        every { questionRef.addValueEventListener(capture(listenerSlot)) } returns mockk()

        viewModel.listenToMyQuestionStatus(questionId)

        val snapshot = mockk<DataSnapshot>(relaxed = true)
        every { snapshot.exists() } returns true
        every { snapshot.child("status").value } returns "taken"
        every { snapshot.child("text").value } returns questionText

        listenerSlot.captured.onDataChange(snapshot)

        assertEquals(questionId, viewModel.activeChatRoomId)
        assertEquals("user", viewModel.uiState.value.myRole)
        assertEquals(questionText, viewModel.uiState.value.activeChatQuestionText)
        assertFalse(viewModel.uiState.value.isUserMatching)
        assertFalse(viewModel.uiState.value.showSeekerConfirmDialog)
    }

    @Test
    fun `listenToMyQuestionStatus with expert_accepted status shows confirm dialog`() {
        val questionId = "testQuestion456"
        val expertId = "expert789"
        val expertText = "I can help with this"
        val timestamp = System.currentTimeMillis()

        val questionsRef = mockk<DatabaseReference>(relaxed = true)
        val questionRef = mockk<DatabaseReference>(relaxed = true)
        every { firebaseDb.getReference("questions") } returns questionsRef
        every { questionsRef.child(questionId) } returns questionRef

        val listenerSlot = slot<ValueEventListener>()
        every { questionRef.addValueEventListener(capture(listenerSlot)) } returns mockk()

        viewModel.listenToMyQuestionStatus(questionId)

        val snapshot = mockk<DataSnapshot>(relaxed = true)
        every { snapshot.exists() } returns true
        every { snapshot.child("status").value } returns "expert_accepted"
        every { snapshot.child("matchedExpTimestamp").value } returns timestamp
        every { snapshot.child("matchedExpText").value } returns expertText
        every { snapshot.child("expertId").value } returns expertId

        listenerSlot.captured.onDataChange(snapshot)

        assertTrue(viewModel.uiState.value.showSeekerConfirmDialog)
        assertFalse(viewModel.uiState.value.isUserMatching)
        assertEquals(expertId, viewModel.matchedExpertId)
        assertEquals(expertText, viewModel.matchedExpertText)
    }

    @Test
    fun `listenToMyQuestionStatus with no_experts legacy status resets UI state`() {
        val questionId = "testNoExpert"
        val questionText = "How do I fix this bug?"

        val questionsRef = mockk<DatabaseReference>(relaxed = true)
        val questionRef = mockk<DatabaseReference>(relaxed = true)
        every { firebaseDb.getReference("questions") } returns questionsRef
        every { questionsRef.child(questionId) } returns questionRef

        val listenerSlot = slot<ValueEventListener>()
        every { questionRef.addValueEventListener(capture(listenerSlot)) } returns mockk()

        viewModel.listenToMyQuestionStatus(questionId)

        val snapshot = mockk<DataSnapshot>(relaxed = true)
        every { snapshot.exists() } returns true
        every { snapshot.child("status").value } returns "no_experts"
        every { snapshot.child("text").value } returns questionText

        listenerSlot.captured.onDataChange(snapshot)

        assertFalse(viewModel.uiState.value.isUserMatching)
    }

    @Test
    fun `listenToMyQuestionStatus with cancelled status clears state`() {
        val questionId = "testCancelled"

        val questionsRef = mockk<DatabaseReference>(relaxed = true)
        val questionRef = mockk<DatabaseReference>(relaxed = true)
        every { firebaseDb.getReference("questions") } returns questionsRef
        every { questionsRef.child(questionId) } returns questionRef

        val listenerSlot = slot<ValueEventListener>()
        every { questionRef.addValueEventListener(capture(listenerSlot)) } returns mockk()

        viewModel.listenToMyQuestionStatus(questionId)

        val snapshot = mockk<DataSnapshot>(relaxed = true)
        every { snapshot.exists() } returns true
        every { snapshot.child("status").value } returns "cancelled"

        listenerSlot.captured.onDataChange(snapshot)

        assertFalse(viewModel.uiState.value.isUserMatching)
        assertFalse(viewModel.uiState.value.showSeekerConfirmDialog)
    }
}