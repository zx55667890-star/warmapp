package com.example.myapplication.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.data.repository.AuthRepository
import com.example.myapplication.data.repository.DataMigrator
import com.example.myapplication.data.repository.UserRepository
import com.example.myapplication.ui.expert.ExpertViewModel
import com.example.myapplication.ui.seeker.SeekerViewModel
import com.example.myapplication.ui.auth.AuthScreen
import com.example.myapplication.ui.auth.AuthViewModel
import com.example.myapplication.ui.auth.NicknameSettingsDialog
import com.example.myapplication.ui.chat.ChatScreen
import com.example.myapplication.ui.expert.ExpertAssignDialog
import com.example.myapplication.ui.expert.ExpertScreen
import com.example.myapplication.ui.expert.ExpertWaitingDialog
import com.example.myapplication.ui.seeker.AskQuestionScreen
import com.example.myapplication.ui.seeker.RoleSelectScreen
import com.example.myapplication.ui.seeker.components.drawBackgroundGlow
import com.example.myapplication.util.uploadChatAndComplete
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavigation() {
    val authRepository: AuthRepository = koinInject()
    val authViewModel: AuthViewModel = koinViewModel()
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val expertViewModel: ExpertViewModel = koinViewModel()
    val seekerViewModel: SeekerViewModel = koinViewModel()
    val expertUiState by expertViewModel.uiState.collectAsStateWithLifecycle()
    val seekerUiState by seekerViewModel.uiState.collectAsStateWithLifecycle()
    val userId: String = authRepository.currentUserId
    val firebaseDb: FirebaseDatabase = koinInject()
    val dataMigrator: DataMigrator = koinInject()
    val userRepository: UserRepository = koinInject()
    var showNicknameSettings by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* granted or denied */ }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(Unit) {
        val activity = context as? Activity
        val intent = activity?.intent
        val chatroomId = intent?.getStringExtra(Routes.EXTRA_CHATROOM_ID) ?: ""
        if (chatroomId.isNotBlank()) {
            val myRole = intent?.getStringExtra(Routes.EXTRA_MY_ROLE) ?: "user"
            val expertId = intent?.getStringExtra(Routes.EXTRA_EXPERT_ID) ?: ""
            val expertText = intent?.getStringExtra(Routes.EXTRA_EXPERT_TEXT) ?: ""
            val expertDate = intent?.getStringExtra(Routes.EXTRA_EXPERT_DATE) ?: ""
            navController.navigate(
                Routes.chat(chatroomId, myRole, expertId, expertText, expertDate)
            ) { popUpTo(Routes.ROLE_SELECT) { inclusive = false }; launchSingleTop = true }
            intent?.removeExtra(Routes.EXTRA_CHATROOM_ID)
        }
    }

    LaunchedEffect(Unit) { dataMigrator.migrateIfNeeded(userId) }
    LaunchedEffect(Unit) {
        if (userId.isNotBlank()) {
            authRepository.saveFcmToken()
            seekerViewModel.checkReconnection(userId)
            expertViewModel.startGlobalAssignListener(userId)
        }
        expertViewModel.initializeExpertStatus(userId)
    }

    LaunchedEffect(seekerUiState.activeChatRoomId) {
        if (seekerUiState.activeChatRoomId.isNotBlank()) {
            navController.navigate(
                Routes.chat(seekerUiState.activeChatRoomId, "user", seekerUiState.matchedExpertId, seekerUiState.matchedExpertText, seekerUiState.matchedExpertDate)
            ) { popUpTo(Routes.ROLE_SELECT) { inclusive = false }; launchSingleTop = true }
        }
    }

    LaunchedEffect(expertUiState.activeChatRoomId) {
        if (expertUiState.activeChatRoomId.isNotBlank()) {
            navController.navigate(
                Routes.chat(expertUiState.activeChatRoomId, "expert", "", "", "")
            ) { popUpTo(Routes.ROLE_SELECT) { inclusive = false }; launchSingleTop = true }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val containerColor = when (currentRoute) {
        Routes.ROLE_SELECT -> Color(0xFF171717)
        else -> Color.Black
    }
// 將原本的 containerColor = when(...) 判斷式移除，改為透明
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBackgroundGlow()
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (authRepository.isLoggedIn()) Routes.ROLE_SELECT else Routes.AUTH,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Routes.AUTH) {
                    AuthScreen(
                        viewModel = authViewModel,
                        onLoggedIn = {
                            navController.navigate(Routes.ROLE_SELECT) {
                                popUpTo(Routes.AUTH) { inclusive = true }
                            }
                        },
                        onSkip = {
                            navController.navigate(Routes.ROLE_SELECT) {
                                popUpTo(Routes.AUTH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Routes.ROLE_SELECT) {
                    val currentUser = authRepository.currentUser
                    val displayName = currentUser?.displayName ?: ""
                    val photoUrl = currentUser?.photoUrl?.toString()
                    var nickname by remember { mutableStateOf("使用者") }
                    LaunchedEffect(userId) {
                        val n = userRepository.getNickname(userId)
                        nickname = n.ifBlank { displayName.ifBlank { "使用者" } }
                    }
                    RoleSelectScreen(
                        nickname = nickname,
                        avatarUrl = photoUrl,
                        onAskQuestion = { navController.navigate(Routes.ASK) { launchSingleTop = true } },
                        onExpertMode = {
                            expertViewModel.setExpertOnline(true, userId)
                            navController.navigate(Routes.EXPERT) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        },
                        onLogout = {
                            authViewModel.logout()
                            seekerViewModel.resetToLoggedOutState()
                            expertViewModel.resetToLoggedOutState()
                            authRepository.logout()
                            navController.navigate(Routes.AUTH) { popUpTo(0) }
                        }
                    )
                }

                composable(
                    Routes.ASK,
                    enterTransition = { slideInHorizontally(animationSpec = tween(350)) { it } + fadeIn(animationSpec = tween(350)) },
                    exitTransition = { slideOutHorizontally(animationSpec = tween(350)) { it / 3 } + fadeOut(animationSpec = tween(350)) },
                    popEnterTransition = { slideInHorizontally(animationSpec = tween(350)) { -it / 3 } + fadeIn(animationSpec = tween(350)) },
                    popExitTransition = { slideOutHorizontally(animationSpec = tween(350)) { it } + fadeOut(animationSpec = tween(350)) }
                ) {
                    var nickname by remember { mutableStateOf("使用者") }
                    LaunchedEffect(userId) { nickname = userRepository.getNickname(userId).ifBlank { "使用者" } }
                    AskQuestionScreen(
                        viewModel = seekerViewModel, userId = userId, nickname = nickname,
                        onBack = { navController.popBackStack(Routes.ROLE_SELECT, inclusive = false) }
                    )
                }

                composable(
                    Routes.EXPERT,
                    enterTransition = { slideInHorizontally(animationSpec = tween(350)) { it } + fadeIn(animationSpec = tween(350)) },
                    exitTransition = { slideOutHorizontally(animationSpec = tween(350)) { it / 3 } + fadeOut(animationSpec = tween(350)) },
                    popEnterTransition = { slideInHorizontally(animationSpec = tween(350)) { -it / 3 } + fadeIn(animationSpec = tween(350)) },
                    popExitTransition = { slideOutHorizontally(animationSpec = tween(350)) { it } + fadeOut(animationSpec = tween(350)) }
                ) {
                    ExpertScreen(
                        viewModel = expertViewModel,
                        userId = userId,
                        onNavigateToInput = {
                            navController.navigate(Routes.ROLE_SELECT) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true; restoreState = true
                            }
                        }
                    )
                }

                composable(
                    Routes.CHAT,
                    arguments = listOf(
                        navArgument(Routes.CHAT_CHATROOM_ID) { type = NavType.StringType },
                        navArgument(Routes.CHAT_MY_ROLE) { type = NavType.StringType },
                        navArgument(Routes.CHAT_EXPERT_ID) { type = NavType.StringType },
                        navArgument(Routes.CHAT_EXPERT_TEXT) { type = NavType.StringType },
                        navArgument(Routes.CHAT_EXPERT_DATE) { type = NavType.StringType }
                    ),
                    enterTransition = { slideInHorizontally(animationSpec = tween(350)) { it } + fadeIn(animationSpec = tween(350)) },
                    exitTransition = { slideOutHorizontally(animationSpec = tween(350)) { it / 3 } + fadeOut(animationSpec = tween(350)) },
                    popEnterTransition = { slideInHorizontally(animationSpec = tween(350)) { -it / 3 } + fadeIn(animationSpec = tween(350)) },
                    popExitTransition = { slideOutHorizontally(animationSpec = tween(350)) { it } + fadeOut(animationSpec = tween(350)) }
                ) { entry ->
                    ChatRouteContent(entry.arguments, userId, firebaseDb, navController, scope)
                }
            }

        Box {
            if (expertUiState.showGlobalAssignDialog) {
                ExpertAssignDialog(
                    questionText = expertUiState.globalAssignedQText, userId = userId,
                    onAccept = { expertViewModel.acceptGlobalAssignment() },
                    onReject = { expertViewModel.rejectGlobalAssignment(userId) }
                )
            }
            if (expertUiState.isExpertWaitingForSeeker) {
                ExpertWaitingDialog(onCancel = { expertViewModel.cancelWaiting(userId) })
            }
            showNicknameSettings.let {
                if (it) NicknameSettingsDialog(userId = userId, userRepository = userRepository, onDismiss = { showNicknameSettings = false })
            }
        }
    }
    }
}

@Composable
private fun ChatRouteContent(
    arguments: android.os.Bundle?,
    userId: String,
    firebaseDb: FirebaseDatabase,
    navController: NavController,
    scope: kotlinx.coroutines.CoroutineScope
) {
    val chatroomId = arguments?.getString(Routes.CHAT_CHATROOM_ID) ?: ""
    val myRole = arguments?.getString(Routes.CHAT_MY_ROLE) ?: ""
    val expertId = arguments?.getString(Routes.CHAT_EXPERT_ID) ?: ""
    val expertText = arguments?.getString(Routes.CHAT_EXPERT_TEXT) ?: ""
    val expertDate = arguments?.getString(Routes.CHAT_EXPERT_DATE) ?: ""

    ChatScreen(
        chatroomId = chatroomId, userId = userId, myRole = myRole,
        chatQuestionText = expertText, expertId = expertId, expertText = expertText, expertDate = expertDate,
        onBack = {
            scope.launch {
                uploadChatAndComplete(chatroomId, firebaseDb)
                navController.popBackStack(Routes.ROLE_SELECT, inclusive = false)
            }
        }
    )
}

