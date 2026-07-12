package com.example.myapplication.ui.auth

import android.app.Activity
import android.util.Log
import android.view.WindowManager.LayoutParams
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.R
import com.example.myapplication.ui.common.LoadingOverlay
import com.example.myapplication.ui.common.ToastOverlay
import com.example.myapplication.ui.common.UiText
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onLoggedIn: () -> Unit,
    onSkip: () -> Unit = {}
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
            delay(2000)
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

    val context = LocalContext.current
    val webClientId = stringResource(R.string.default_web_client_id)
    val googleSignInOptions = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("AuthScreen", "Google account: ${account.email}, idToken=${account.idToken?.take(20)}")
                account.idToken?.let { viewModel.signInWithGoogle(it) }
                    ?: run {
                        viewModel.setError("ç„¡æ³•å–å¾— Google æ†‘è­‰ï$resetEmail，è«‹ç¢ºèª default_web_client_id æ˜¯å¦æ­£ç¢º")
                    }
            } catch (e: ApiException) {
                Log.w("AuthScreen", "Google Sign-In ApiException: ${e.statusCode}", e)
                viewModel.setError("Google ç™»å…¥å¤±æ•—: ${e.localizedMessage}")
            }
        } else {
            Log.d("AuthScreen", "Google Sign-In cancelled: resultCode=$result.resultCode")
        }
    }

    LaunchedEffect(Unit) { viewModel.refreshLoggedInState() }

    DisposableEffect(Unit) {
        val activity = context as? Activity
        val previousMode = activity?.window?.attributes?.softInputMode
        activity?.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
        onDispose { previousMode?.let { activity?.window?.setSoftInputMode(it) } }
    }

    LaunchedEffect(uiState.isLoggedIn) {
        if (viewModel.uiState.value.isLoggedIn) onLoggedIn()
    }

    if (uiState.resetSent) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissResetSent(); showResetPassword = false; showLoginForm = true; viewModel.toggleMode(false) },
            title = { Text("重設密碼") },
            text = { Text("重設密碼信件已發送至 $resetEmail，請檢查信箱。") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissResetSent(); showResetPassword = false; showLoginForm = true; viewModel.toggleMode(false) }) { Text("確定") }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        when {
            uiState.showNewPasswordForm -> NewPasswordForm(
                uiState = uiState,
                onBack = { viewModel.dismissNewPasswordForm() },
                onConfirm = { newPassword, confirmNewPassword ->
                    viewModel.confirmResetPassword(resetEmail, resetVerificationCode, newPassword, confirmNewPassword)
                }
            )
            showResetPassword -> ForgotPasswordPanel(
                uiState = uiState,
                onBack = { showResetPassword = false },
                onSendCode = { email -> viewModel.sendResetVerificationCode(email) },
                onNext = { email, code ->
                    resetEmail = email
                    resetVerificationCode = code
                    viewModel.sendPasswordReset(email, code)
                }
            )
            showLoginForm -> LoginForm(
                uiState = uiState,
                onBack = { showLoginForm = false },
                onSendCode = { email -> viewModel.sendVerificationCode(email) },
                onSubmit = { email, password, confirmPassword, nickname, verificationCode ->
                    viewModel.submit(email, password, confirmPassword, nickname, verificationCode)
                },
                onForgotPassword = { showResetPassword = true }
            )
            else -> WelcomePanel(
                isLoading = uiState.isLoading,
                agreed = agreed,
                onAgreedChange = { agreed = it },
                onTermsClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(termsUrl))
                    context.startActivity(intent)
                },
                onPrivacyClick = {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(privacyUrl))
                    context.startActivity(intent)
                },
                onGoogleSignIn = {
                    val avail = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
                    if (avail != ConnectionResult.SUCCESS) {
                        viewModel.setError("Google æœå‹™ä¸å¯ç”¨ï$resetEmail，è«‹ç¢ºèªæ¨¡æ“¬å™¨å·²å®‰è£ Google Play æœå‹™")
                    } else {
                        try {
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e;
                            viewModel.setError("Google ç™»å…¥å¤±æ•—ï¼š${e.localizedMessage ?: "è«‹ç¢ºèª Google æœå‹™æ˜¯å¦æ­£å¸¸"}")
                        }
                    }
                },
                onLoginClick = { viewModel.toggleMode(false); showLoginForm = true },
                onRegisterClick = { viewModel.toggleMode(true); showLoginForm = true },
                onSkip = onSkip
            )
        }

        BackHandler(enabled = showLoginForm) { showLoginForm = false }
        BackHandler(enabled = showResetPassword) { showResetPassword = false }

        uiState.error?.let {
            Surface(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 40.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF333333),
                shadowElevation = 8.dp
            ) {
                Text(
                    text = it.asString(),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
                )
            }
        }

        ToastOverlay(toastMessage?.asString())
        LoadingOverlay(uiState.isLoading)
    }
}

