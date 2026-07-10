package com.example.myapplication.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test

class WelcomePanelTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displayAllMainElements() {
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = false,
                    agreed = false,
                    onAgreedChange = {},
                    onGoogleSignIn = {},
                    onLoginClick = {},
                    onRegisterClick = {},
                    onSkip = {},
                    onTermsClick = {},
                    onPrivacyClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("AppName").assertIsDisplayed()
        composeTestRule.onNodeWithText("Google 登入").assertIsDisplayed()
        composeTestRule.onNodeWithText("密碼登入").assertIsDisplayed()
        composeTestRule.onNodeWithText("註冊").assertIsDisplayed()
        composeTestRule.onNodeWithText("略過直接開始").assertIsDisplayed()
        composeTestRule.onNodeWithText("我已閱讀並同意 ").assertIsDisplayed()
        composeTestRule.onNodeWithText("使用者協議").assertIsDisplayed()
        composeTestRule.onNodeWithText("隱私政策").assertIsDisplayed()
    }

    @Test
    fun loadingStateShowsProgressIndicator() {
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = true,
                    agreed = false,
                    onAgreedChange = {},
                    onGoogleSignIn = {},
                    onLoginClick = {},
                    onRegisterClick = {},
                    onSkip = {},
                    onTermsClick = {},
                    onPrivacyClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("googleSignInLoading").assertIsDisplayed()
        composeTestRule.onNodeWithText("Google 登入").assertDoesNotExist()
    }

    @Test
    fun loginButtonClickTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = false,
                    agreed = false,
                    onAgreedChange = {},
                    onGoogleSignIn = {},
                    onLoginClick = { clicked = true },
                    onRegisterClick = {},
                    onSkip = {},
                    onTermsClick = {},
                    onPrivacyClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("密碼登入").performClick()
        assert(clicked)
    }

    @Test
    fun registerButtonClickTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = false,
                    agreed = false,
                    onAgreedChange = {},
                    onGoogleSignIn = {},
                    onLoginClick = {},
                    onRegisterClick = { clicked = true },
                    onSkip = {},
                    onTermsClick = {},
                    onPrivacyClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("註冊").performClick()
        assert(clicked)
    }

    @Test
    fun googleSignInButtonClickTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = false,
                    agreed = false,
                    onAgreedChange = {},
                    onGoogleSignIn = { clicked = true },
                    onLoginClick = {},
                    onRegisterClick = {},
                    onSkip = {},
                    onTermsClick = {},
                    onPrivacyClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Google 登入").performClick()
        assert(clicked)
    }

    @Test
    fun skipClickTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = false,
                    agreed = false,
                    onAgreedChange = {},
                    onGoogleSignIn = {},
                    onLoginClick = {},
                    onRegisterClick = {},
                    onSkip = { clicked = true },
                    onTermsClick = {},
                    onPrivacyClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("略過直接開始").performClick()
        assert(clicked)
    }

    @Test
    fun termsClickTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = false,
                    agreed = false,
                    onAgreedChange = {},
                    onGoogleSignIn = {},
                    onLoginClick = {},
                    onRegisterClick = {},
                    onSkip = {},
                    onTermsClick = { clicked = true },
                    onPrivacyClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("使用者協議").performClick()
        assert(clicked)
    }

    @Test
    fun privacyClickTriggersCallback() {
        var clicked = false
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = false,
                    agreed = false,
                    onAgreedChange = {},
                    onGoogleSignIn = {},
                    onLoginClick = {},
                    onRegisterClick = {},
                    onSkip = {},
                    onTermsClick = {},
                    onPrivacyClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("隱私政策").performClick()
        assert(clicked)
    }

    @Test
    fun checkboxToggleTriggersCallback() {
        var agreedValue = false
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = false,
                    agreed = agreedValue,
                    onAgreedChange = { agreedValue = it },
                    onGoogleSignIn = {},
                    onLoginClick = {},
                    onRegisterClick = {},
                    onSkip = {},
                    onTermsClick = {},
                    onPrivacyClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("我已閱讀並同意 ").performClick()
        assert(agreedValue)
    }

    @Test
    fun loadingStateDisablesButtons() {
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = true,
                    agreed = false,
                    onAgreedChange = {},
                    onGoogleSignIn = {},
                    onLoginClick = {},
                    onRegisterClick = {},
                    onSkip = {},
                    onTermsClick = {},
                    onPrivacyClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("密碼登入").assertIsNotEnabled()
        composeTestRule.onNodeWithText("註冊").assertIsNotEnabled()
    }

    @Test
    fun normalStateEnablesButtons() {
        composeTestRule.setContent {
            MyApplicationTheme {
                WelcomePanel(
                    isLoading = false,
                    agreed = false,
                    onAgreedChange = {},
                    onGoogleSignIn = {},
                    onLoginClick = {},
                    onRegisterClick = {},
                    onSkip = {},
                    onTermsClick = {},
                    onPrivacyClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Google 登入").assertIsEnabled()
        composeTestRule.onNodeWithText("密碼登入").assertIsEnabled()
        composeTestRule.onNodeWithText("註冊").assertIsEnabled()
    }
}
