package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.myapplication.di.appModule
import com.example.myapplication.ui.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🌊 1. 讓 Compose 可以畫到整個螢幕（關鍵）
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 🌌 2. Edge-to-edge
        enableEdgeToEdge()

        // ⚡ 3. 移除 system bar 背景（避免黑條來源）
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = false

        // ⚡ 4. 控制 icon 顏色（避免白黑錯亂）
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(android.view.WindowInsets.Type.statusBars())
        controller.hide(android.view.WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {

            MyApplicationTheme {

                KoinApplication(
                    application = {
                        androidContext(this@MainActivity)
                        modules(appModule)
                    }
                ) {

                    // 🚀 這裡才是整個 UI Root
                    AppNavigation()
                }
            }
        }
    }
}