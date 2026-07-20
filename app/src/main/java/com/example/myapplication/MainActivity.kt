package com.example.myapplication

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.example.myapplication.di.authModule
import com.example.myapplication.di.chatModule
import com.example.myapplication.di.coreModule
import com.example.myapplication.di.expertModule
import com.example.myapplication.di.mediaModule
import com.example.myapplication.di.seekerModule
import com.example.myapplication.ui.navigation.AppNavigation
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.koin.android.ext.koin.androidContext
import org.koin.compose.KoinApplication

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        createNotificationChannel()

        setContent {
            MyApplicationTheme {
                @Suppress("DEPRECATION")
                KoinApplication(
                    application = {
                        androidContext(this@MainActivity)
                        modules(coreModule, authModule, chatModule, expertModule, seekerModule, mediaModule)
                    }
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "chat_messages", "聊天訊息",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
