package com.example.myapplication.di

import com.example.myapplication.di.ExpertViewModel
import com.google.firebase.database.DataSnapshot
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
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExpertViewModelTest {

    @RelaxedMockK
    private lateinit var firebaseDb: FirebaseDatabase

    private lateinit var viewModel: ExpertViewModel

    private val deviceId = "testExpertDevice"
    private val questionId = "globalQuestion123"
    private val questionText = "Need help with something"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        viewModel = ExpertViewModel(firebaseDb)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `startGlobalAssignListener with pending_acceptance sets dialog state`() {
        setupQueryMocks()

        val listenerSlot = slot<ValueEventListener>()
        val query = mockk<Query>(relaxed = true)
        every { firebaseDb.getReference("questions").orderByChild("expertId").equalTo(deviceId) } returns query
        every { query.addValueEventListener(capture(listenerSlot)) } returns mockk()

        viewModel.startGlobalAssignListener(deviceId)

        val childSnapshot = mockk<DataSnapshot>(relaxed = true)
        every { childSnapshot.key } returns questionId
        every { childSnapshot.child("status").value } returns "pending_acceptance"
        every { childSnapshot.child("text").value } returns questionText

        val snapshot = mockk<DataSnapshot>(relaxed = true)
        every { snapshot.children } returns listOf(childSnapshot)
        every { snapshot.child(any()).value } returns null

        listenerSlot.captured.onDataChange(snapshot)

        assertEquals(questionId, viewModel.uiState.value.globalAssignedQId)
        assertEquals(questionText, viewModel.uiState.value.globalAssignedQText)
        assertTrue(viewModel.uiState.value.showGlobalAssignDialog)
        assertFalse(viewModel.uiState.value.isExpertWaitingForSeeker)
    }

    @Test
    fun `startGlobalAssignListener transitions through statuses to taken`() {
        val query = setupQueryMocks()

        val listenerSlot = slot<ValueEventListener>()
        every { query.addValueEventListener(capture(listenerSlot)) } returns mockk()

        viewModel.startGlobalAssignListener(deviceId)

        // Step 1: pending_acceptance
        simulateSnapshot(listenerSlot.captured, "pending_acceptance")

        assertEquals(questionId, viewModel.uiState.value.globalAssignedQId)
        assertTrue(viewModel.uiState.value.showGlobalAssignDialog)

        // Step 2: expert_accepted
        simulateSnapshot(listenerSlot.captured, "expert_accepted")

        assertTrue(viewModel.uiState.value.isExpertWaitingForSeeker)
        assertFalse(viewModel.uiState.value.showGlobalAssignDialog)

        // Step 3: taken (chat ready)
        simulateSnapshot(listenerSlot.captured, "taken")

        assertEquals(questionId, viewModel.uiState.value.activeChatRoomId)
        assertEquals("expert", viewModel.uiState.value.myRole)
        assertEquals(questionText, viewModel.uiState.value.activeChatQuestionText)
    }

    @Test
    fun `acceptGlobalAssignment calls repository method`() {
        setupQueryMocks()
        val listenerSlot = slot<ValueEventListener>()
        every { mockk<Query>(relaxed = true).addValueEventListener(capture(listenerSlot)) } returns mockk()

        viewModel.startGlobalAssignListener(deviceId)

        viewModel.acceptGlobalAssignment()
        assertFalse(viewModel.uiState.value.showGlobalAssignDialog)
    }

    @Test
    fun `initial navigation state is empty`() {
        assertEquals("", viewModel.uiState.value.activeChatRoomId)
        assertEquals("", viewModel.uiState.value.myRole)
        assertEquals("", viewModel.uiState.value.activeChatQuestionText)
    }

    // ---- helpers ----

    private fun setupQueryMocks(): Query {
        val questionsRef = mockk<DatabaseReference>(relaxed = true)
        val query1 = mockk<Query>(relaxed = true)
        val query = mockk<Query>(relaxed = true)

        every { firebaseDb.getReference("questions") } returns questionsRef
        every { questionsRef.orderByChild("expertId") } returns query1
        every { query1.equalTo(deviceId) } returns query

        return query
    }

    private fun simulateSnapshot(listener: ValueEventListener, status: String) {
        val childSnapshot = mockk<DataSnapshot>(relaxed = true)
        every { childSnapshot.key } returns questionId
        every { childSnapshot.child("status").value } returns status
        every { childSnapshot.child("text").value } returns questionText

        val snapshot = mockk<DataSnapshot>(relaxed = true)
        every { snapshot.children } returns listOf(childSnapshot)

        listener.onDataChange(snapshot)
    }
}