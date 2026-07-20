package com.example.myapplication.ui.auth

import android.app.Activity
import android.util.Log
import android.view.WindowManager.LayoutParams
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.common.LoadingOverlay
import com.example.myapplication.ui.common.ToastOverlay
import com.example.myapplication.ui.common.UiText
import com.example.myapplication.ui.theme.AppColors
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

private const val TRANSITION_DURATION = 350

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
) {
    val termsUrl = stringResource(R.string.terms_url)
    val privacyUrl = stringResource(R.string.privacy_url)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLoginForm by remember { mutableStateOf(false) }
    var showResetPassword by remember { mutableStateOf(false) }
    var agreed by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<UiText?>(null) }
    var resetEmail by rememberSaveable { mutableStateOf("") }
    var resetVerificationCode by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            delay(2500)
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { msg ->
            toastMessage = msg
            delay(3000)
            toastMessage = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateEvent.collect { event ->
            when (event) {
                AuthViewModel.NavigationEvent.ShowLoginForm -> {
                    showLoginForm = true
                    showResetPassword = false
                    viewModel.toggleMode(false)
                }
            }
        }
    }

    // ── Google Sign-In ──
    val context = LocalContext.current
    val webClientId = stringResource(R.string.default_web_client_id)
    @Suppress("DEPRECATION")
    val googleSignInOptions = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    @Suppress("DEPRECATION")
    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions) }

    @Suppress("DEPRECATION")
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { viewModel.signInWithGoogle(it) }
                    ?: viewModel.setError("無法取得 Google 憑證，請確認 default_web_client_id 是否正確")
            } catch (e: ApiException) {
                viewModel.setError("Google 登入失敗: ${e.localizedMessage}")
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.refreshLoggedInState() }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val previousMode = activity?.window?.attributes?.softInputMode
        activity?.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        onDispose { previousMode?.let { activity.window.setSoftInputMode(it) } }
    }

    LaunchedEffect(uiState.isLoggedIn) {
        if (viewModel.uiState.value.isLoggedIn) onLoggedIn()
    }

    // ── 重設密碼確認彈窗 ──
    if (uiState.resetSent) {
        AlertDialog(
            onDismissRequest = {
                viewModel.dismissResetSent()
                showResetPassword = false
                showLoginForm = true
                viewModel.toggleMode(false)
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = AppColors.SurfaceDark,
            titleContentColor = AppColors.TextWhite,
            textContentColor = AppColors.TextGray,
            title = {
                Text("重設密碼", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("重設密碼信件已發送至 $resetEmail，請檢查信箱。")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissResetSent()
                    showResetPassword = false
                    showLoginForm = true
                    viewModel.toggleMode(false)
                }) {
                    Text("確定", color = AppColors.AccentGreen, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // ── 主畫面 ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        AnimatedContent(
            targetState = when {
                uiState.showNewPasswordForm -> AuthPage.NEW_PASSWORD
                showResetPassword -> AuthPage.RESET_PASSWORD
                showLoginForm -> AuthPage.LOGIN
                else -> AuthPage.WELCOME
            },
            transitionSpec = {
                fadeIn(tween(TRANSITION_DURATION)) + slideInVertically(
                    tween(TRANSITION_DURATION),
                    initialOffsetY = { it / 8 }
                ) togetherWith fadeOut(tween(TRANSITION_DURATION / 2))
            },
            label = "authPageTransition"
        ) { page ->
            when (page) {
                AuthPage.NEW_PASSWORD -> NewPasswordForm(
                    uiState = uiState,
                    onBack = { viewModel.dismissNewPasswordForm() },
                    onConfirm = { newPassword, confirmNewPassword ->
                        viewModel.confirmResetPassword(
                            resetEmail, resetVerificationCode, newPassword, confirmNewPassword
                        )
                    }
                )

                AuthPage.RESET_PASSWORD -> ForgotPasswordPanel(
                    uiState = uiState,
                    onBack = { showResetPassword = false },
                    onSendCode = { email -> viewModel.sendResetVerificationCode(email) },
                    onNext = { email, code ->
                        resetEmail = email
                        resetVerificationCode = code
                        viewModel.sendPasswordReset(email, code)
                    }
                )

                AuthPage.LOGIN -> LoginForm(
                    uiState = uiState,
                    onBack = { showLoginForm = false },
                    onSendCode = { email -> viewModel.sendVerificationCode(email) },
                    onSubmit = { email, password, confirmPassword, nickname, verificationCode ->
                        viewModel.submit(email, password, confirmPassword, nickname, verificationCode)
                    },
                    onForgotPassword = { showResetPassword = true }
                )

                AuthPage.WELCOME -> WelcomePanel(
                    isLoading = uiState.isLoading,
                    agreed = agreed,
                    onAgreedChange = { agreed = it },
                    onTermsClick = {
                        context.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(termsUrl)
                            )
                        )
                    },
                    onPrivacyClick = {
                        context.startActivity(
                            android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse(privacyUrl)
                            )
                        )
                    },
                    onGoogleSignIn = {
                        val avail = GoogleApiAvailability.getInstance()
                            .isGooglePlayServicesAvailable(context)
                        if (avail != ConnectionResult.SUCCESS) {
                            viewModel.setError("Google 服務不可用，請確認已安裝 Google Play 服務")
                        } else {
                            try {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            } catch (e: Exception) {
                                if (e is kotlinx.coroutines.CancellationException) throw e
                                viewModel.setError("Google 登入失敗：${e.localizedMessage ?: "請確認 Google 服務是否正常"}")
                            }
                        }
                    },
                    onLoginClick = {
                        viewModel.toggleMode(false)
                        showLoginForm = true
                    },
                    onRegisterClick = {
                        viewModel.toggleMode(true)
                        showLoginForm = true
                    },
                )
            }
        }

        BackHandler(enabled = showLoginForm) { showLoginForm = false }
        BackHandler(enabled = showResetPassword) { showResetPassword = false }

        // ── 錯誤浮層 ──
        AnimatedVisibility(
            visible = uiState.error != null,
            enter = fadeIn(tween(200)) + slideInVertically(tween(300)) { -it / 2 },
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp)
        ) {
            uiState.error?.let {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AppColors.StatusErrorBg,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, AppColors.StatusError.copy(alpha = 0.2f)
                    ),
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = it.asString(),
                        color = AppColors.StatusError,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }
        }

        ToastOverlay(toastMessage?.asString())
        LoadingOverlay(uiState.isLoading)
    }
}

/** Auth 頁面狀態枚舉，用於 AnimatedContent 切換 */
private enum class AuthPage {
    WELCOME, LOGIN, RESET_PASSWORD, NEW_PASSWORD
}
