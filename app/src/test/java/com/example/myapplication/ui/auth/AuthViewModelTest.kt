package com.example.myapplication.ui.auth

import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.ui.common.UiText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.util.Log
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: AuthViewModel
    private val testDispatcher = StandardTestDispatcher()

    @RelaxedMockK
    private lateinit var firebaseDb: FirebaseDatabase

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        MockKAnnotations.init(this)
        authRepository = mockk(relaxed = true)
        every { authRepository.isLoggedIn() } returns false
        every { authRepository.currentUserId } returns ""
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        userRepository = UserRepository(firebaseDb)
        viewModel = AuthViewModel(authRepository, userRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `init checks login state`() = runTest {
        every { authRepository.isLoggedIn() } returns true
        val vm = AuthViewModel(authRepository, userRepository)
        assertTrue(vm.uiState.value.isLoggedIn)

        every { authRepository.isLoggedIn() } returns false
        val vm2 = AuthViewModel(authRepository, userRepository)
        assertFalse(vm2.uiState.value.isLoggedIn)
    }

    @Test
    fun `login sets isLoggedIn on success`() = runTest {
        coEvery { authRepository.login(any(), any()) } just runs

        viewModel.submit("test@gmail.com", "Test1234", "", "", "")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `login sets error on failure`() = runTest {
        coEvery { authRepository.login(any(), any()) } throws Exception("登入失敗")

        viewModel.submit("test@gmail.com", "Test1234", "", "", "")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("登入失敗", (viewModel.uiState.value.error as? UiText.Dynamic)?.value)
    }

    @Test
    fun `register saves nickname and sets isLoggedIn on success`() = runTest {
        every { authRepository.isLoggedIn() } returns false
        every { authRepository.currentUserId } returns "newUid"
        coEvery { authRepository.verifyVerificationCode(any(), any()) } returns true
        coEvery { authRepository.register(any(), any()) } just runs

        val usersRef = mockk<DatabaseReference>(relaxed = true)
        every { firebaseDb.getReference("users") } returns usersRef

        viewModel.toggleMode()
        viewModel.submit("new@gmail.com", "Test1234", "Test1234", "TestNick", "123456")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        verify { usersRef.child("newUid").child("nickname").setValue("TestNick") }
    }

    @Test
    fun `register fails when passwords do not match`() = runTest {
        viewModel.toggleMode()
        viewModel.submit("test@gmail.com", "Test1234", "Test5678", "Nick", "123456")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `register fails when nickname is blank`() = runTest {
        viewModel.toggleMode()
        viewModel.submit("test@gmail.com", "Test1234", "Test1234", "", "123456")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `sendPasswordReset sets showNewPasswordForm on verify success`() = runTest {
        coEvery { authRepository.verifyVerificationCode(any(), any(), any()) } returns true

        viewModel.sendPasswordReset("reset@gmail.com", "123456")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.showNewPasswordForm)
    }

    @Test
    fun `sendPasswordReset fails when email is blank`() = runTest {
        viewModel.sendPasswordReset("", "123456")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showNewPasswordForm)
    }

    @Test
    fun `logout resets state to defaults`() = runTest {
        viewModel.logout()

        val state = viewModel.uiState.value
        assertFalse(state.isLoggedIn)
        assertNull(state.error)
    }

    @Test
    fun `toggleMode switches between login and register`() = runTest {
        assertFalse(viewModel.uiState.value.isRegisterMode)

        viewModel.toggleMode()
        assertTrue(viewModel.uiState.value.isRegisterMode)

        viewModel.toggleMode()
        assertFalse(viewModel.uiState.value.isRegisterMode)
    }
}
