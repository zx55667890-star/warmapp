package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.myapplication.di.appModule
import com.example.myapplication.ui.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ✨ 加上這一行，讓 Compose 完全控制鍵盤與系統列的動畫
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        @Suppress("DEPRECATION")
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        @Suppress("DEPRECATION")
        val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "DEVICE_UNKNOWN"
        getSharedPreferences("app_prefs", Context.MODE_PRIVATE).edit().putString("saved_device_id", deviceId).apply()

        setContent {
            MyApplicationTheme {
                KoinApplication(application = {
                    androidContext(this@MainActivity)
                    modules(appModule)
                }) {
                    val isDarkTheme = isSystemInDarkTheme()
                    val view = LocalView.current
                    SideEffect {
                        val window = (view.context as android.app.Activity).window
                        val controller = WindowCompat.getInsetsController(window, view)

                        // 確保狀態列與導覽列的圖標顏色能根據深淺色主題自動切換 (白字/黑字)
                        controller.isAppearanceLightStatusBars = !isDarkTheme
                        controller.isAppearanceLightNavigationBars = !isDarkTheme

                        // 🌟 關鍵 1：明確將狀態列與導覽列設為 `#133281` 藍色
                        window.statusBarColor = android.graphics.Color.parseColor("#133281")
                        window.navigationBarColor = android.graphics.Color.parseColor("#133281")

                    }
                    AppNavigation()
                }
            }
        }
    }
}
