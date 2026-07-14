package com.example.myapplication.di

import com.example.myapplication.data.model.SkillStatus
import com.example.myapplication.data.model.SolutionItem
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExpertViewModelTest {

    @RelaxedMockK
    private lateinit var firebaseDb: FirebaseDatabase

    private lateinit var viewModel: ExpertViewModel

    private val deviceId = "testExpertDevice"
    private val questionId = "globalQuestion123"
    private val questionText = "Need help with something"

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = ExpertViewModel(firebaseDb)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty solution history`() {
        assertTrue(viewModel.uiState.value.solutionHistory.isEmpty())
        assertEquals("", viewModel.uiState.value.activeChatRoomId)
    }

    @Test
    fun `listenToSolutions updates history`() = runTest(testDispatcher) {
        val listenerSlot = slot<ValueEventListener>()
        val ref = mockk<DatabaseReference>(relaxed = true)
        every { firebaseDb.getReference("solutions") } returns ref
        every { ref.child(deviceId) } returns ref
        every { ref.addValueEventListener(capture(listenerSlot)) } returns mockk()

        viewModel.listenToSolutions(deviceId)
        advanceUntilIdle()

        val childSnapshot = mockk<DataSnapshot>(relaxed = true)
        every { childSnapshot.key } returns "skill1"
        every { childSnapshot.child("expertise").getValue(String::class.java) } returns "測試技能"
        every { childSnapshot.child("status").getValue(String::class.java) } returns SkillStatus.ACTIVE.name
        every { childSnapshot.child("tags").children } returns emptyList()
        every { childSnapshot.child("timestamp").getValue(Long::class.java) } returns 1000L
        every { childSnapshot.child("questionId").getValue(String::class.java) } returns ""

        val snapshot = mockk<DataSnapshot>(relaxed = true)
        every { snapshot.children } returns listOf(childSnapshot)

        listenerSlot.captured.onDataChange(snapshot)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.solutionHistory.size)
        assertEquals("測試技能", viewModel.uiState.value.solutionHistory[0].expertise)
    }

    @Test
    fun `startSkillEdit sets edit target`() {
        val solution = SolutionItem(
            id = "skill1",
            expertise = "舊技能描述",
            tags = listOf("tag1"),
            timestamp = 1000L,
            status = SkillStatus.ACTIVE
        )

        viewModel.startSkillEdit(solution)

        assertNotNull(viewModel.uiState.value.skillEditTarget)
        assertEquals("舊技能描述", viewModel.uiState.value.editText)
        assertEquals("舊技能描述", viewModel.uiState.value.skillEditTarget?.expertise)
    }

    @Test
    fun `cancelSkillEdit clears edit state`() {
        val solution = SolutionItem(id = "skill1", expertise = "test")
        viewModel.startSkillEdit(solution)
        viewModel.cancelSkillEdit()

        assertNull(viewModel.uiState.value.skillEditTarget)
        assertEquals("", viewModel.uiState.value.editText)
    }

    @Test
    fun `submitSkillEdit with same text dismisses without error`() {
        val solution = SolutionItem(
            id = "skill1",
            expertise = "相同描述",
            status = SkillStatus.ACTIVE
        )

        viewModel.startSkillEdit(solution)
        viewModel.submitSkillEdit(deviceId)

        assertNull(viewModel.uiState.value.skillEditTarget)
    }

    @Test
    fun `updateSkillEditText updates edit text`() {
        val solution = SolutionItem(id = "skill1", expertise = "舊描述")
        viewModel.startSkillEdit(solution)
        viewModel.updateSkillEditText("新描述")

        assertEquals("新描述", viewModel.uiState.value.editText)
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

        simulateSnapshot(listenerSlot.captured, "pending_acceptance")

        assertEquals(questionId, viewModel.uiState.value.globalAssignedQId)
        assertTrue(viewModel.uiState.value.showGlobalAssignDialog)

        simulateSnapshot(listenerSlot.captured, "expert_accepted")

        assertTrue(viewModel.uiState.value.isExpertWaitingForSeeker)
        assertFalse(viewModel.uiState.value.showGlobalAssignDialog)

        simulateSnapshot(listenerSlot.captured, "taken")

        assertEquals(questionId, viewModel.uiState.value.activeChatRoomId)
        assertEquals("expert", viewModel.uiState.value.myRole)
        assertEquals(questionText, viewModel.uiState.value.activeChatQuestionText)
    }

    @Test
    fun `acceptGlobalAssignment hides dialog`() {
        setupQueryMocks()
        val listenerSlot = slot<ValueEventListener>()
        val query = mockk<Query>(relaxed = true)
        every { query.addValueEventListener(capture(listenerSlot)) } returns mockk()
        every { firebaseDb.getReference("questions").orderByChild("expertId").equalTo(deviceId) } returns query

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
