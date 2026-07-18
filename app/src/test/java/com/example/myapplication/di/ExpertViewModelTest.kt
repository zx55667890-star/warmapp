package com.example.myapplication.di

import com.example.myapplication.data.FirebasePaths
import com.example.myapplication.data.StatusValues
import com.example.myapplication.data.model.SkillStatus
import com.example.myapplication.data.model.SolutionItem
import com.example.myapplication.data.repository.ExpertRepository
import com.example.myapplication.ui.expert.ExpertUiState
import com.example.myapplication.ui.expert.ExpertViewModel
import com.example.myapplication.domain.expert.ObserveSolutionsUseCase
import com.example.myapplication.domain.expert.PublishSkillUseCase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ExpertViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val deviceId = "testDeviceId123"
    private val questionId = "testQ456"
    private val questionText = "Can someone help me with this?"

    @RelaxedMockK
    private lateinit var firebaseDb: FirebaseDatabase

    @RelaxedMockK
    private lateinit var expertRepository: ExpertRepository

    @RelaxedMockK
    private lateinit var publishSkillUseCase: PublishSkillUseCase

    @RelaxedMockK
    private lateinit var observeSolutionsUseCase: ObserveSolutionsUseCase

    private lateinit var viewModel: ExpertViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        viewModel = ExpertViewModel(firebaseDb, expertRepository, publishSkillUseCase, observeSolutionsUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun setupQueryMocks(): Query {
        val query = mockk<Query>(relaxed = true)
        every { firebaseDb.getReference("questions") } returns mockk(relaxed = true)
        every { firebaseDb.getReference("questions").orderByChild("expertId").equalTo(deviceId) } returns query
        return query
    }

    private fun simulateSnapshot(listener: ValueEventListener, status: String) {
        val childSnapshot = mockk<DataSnapshot>(relaxed = true)
        every { childSnapshot.key } returns questionId
        every { childSnapshot.child("status").value } returns status
        every { childSnapshot.child("text").value } returns questionText
        every { childSnapshot.child("expertId").value } returns deviceId

        val snapshot = mockk<DataSnapshot>(relaxed = true)
        every { snapshot.children } returns listOf(childSnapshot)
        every { snapshot.child(any()).value } returns null

        listener.onDataChange(snapshot)
    }

    @Test
    fun `startGlobalAssignListener with taken status sets dialog state`() {
        setupQueryMocks()

        val listenerSlot = slot<ValueEventListener>()
        val query = mockk<Query>(relaxed = true)
        every { firebaseDb.getReference("questions").orderByChild("expertId").equalTo(deviceId) } returns query
        every { query.addValueEventListener(capture(listenerSlot)) } returns mockk()

        viewModel.startGlobalAssignListener(deviceId)

        val childSnapshot = mockk<DataSnapshot>(relaxed = true)
        every { childSnapshot.key } returns questionId
        every { childSnapshot.child("status").value } returns "taken"
        every { childSnapshot.child("text").value } returns questionText

        val snapshot = mockk<DataSnapshot>(relaxed = true)
        every { snapshot.children } returns listOf(childSnapshot)
        every { snapshot.child(any()).value } returns null

        listenerSlot.captured.onDataChange(snapshot)

        assertEquals(questionId, viewModel.uiState.value.globalAssignedQId)
        assertEquals(questionText, viewModel.uiState.value.globalAssignedQText)
        assertTrue(viewModel.uiState.value.showGlobalAssignDialog)
    }

    @Test
    fun `startGlobalAssignListener transitions taken to chat`() {
        val query = setupQueryMocks()

        val listenerSlot = slot<ValueEventListener>()
        every { query.addValueEventListener(capture(listenerSlot)) } returns mockk()

        viewModel.startGlobalAssignListener(deviceId)

        // First taken → dialog
        simulateSnapshot(listenerSlot.captured, "taken")

        assertEquals(questionId, viewModel.uiState.value.globalAssignedQId)
        assertTrue(viewModel.uiState.value.showGlobalAssignDialog)

        // Second taken with same qId → navigate to chat
        simulateSnapshot(listenerSlot.captured, "taken")

        assertEquals("ai_$questionId", viewModel.uiState.value.activeChatRoomId)
        assertEquals("expert", viewModel.uiState.value.myRole)
        assertEquals(questionText, viewModel.uiState.value.activeChatQuestionText)
    }

    @Test
    fun `acceptGlobalAssignment hides dialog and navigates to chat`() {
        setupQueryMocks()
        val listenerSlot = slot<ValueEventListener>()
        val query = mockk<Query>(relaxed = true)
        every { query.addValueEventListener(capture(listenerSlot)) } returns mockk()
        every { firebaseDb.getReference("questions").orderByChild("expertId").equalTo(deviceId) } returns query

        viewModel.startGlobalAssignListener(deviceId)

        // First, simulate taken to populate globalAssignedQId
        val childSnapshot = mockk<DataSnapshot>(relaxed = true)
        every { childSnapshot.key } returns questionId
        every { childSnapshot.child("status").value } returns "taken"
        every { childSnapshot.child("text").value } returns questionText

        val snapshot = mockk<DataSnapshot>(relaxed = true)
        every { snapshot.children } returns listOf(childSnapshot)
        every { snapshot.child(any()).value } returns null

        listenerSlot.captured.onDataChange(snapshot)

        // Now accept
        viewModel.acceptGlobalAssignment()

        assertFalse(viewModel.uiState.value.showGlobalAssignDialog)
        assertEquals("ai_$questionId", viewModel.uiState.value.activeChatRoomId)
        assertEquals("expert", viewModel.uiState.value.myRole)
    }
}
