package com.example.myapplication.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.ui.navigation.Routes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseDatabase.getInstance().getReference("users").child(uid).child("fcmToken").setValue(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val data = message.data
        val title = data["title"] ?: message.notification?.title ?: "新訊息"
        val body = data["body"] ?: message.notification?.body ?: ""
        val chatroomId = data["chatroomId"] ?: ""
        val myRole = data["myRole"] ?: ""
        val expertId = data["expertId"] ?: ""
        val expertText = data["expertText"] ?: ""
        val expertDate = data["expertDate"] ?: ""

        showNotification(title, body, chatroomId, myRole, expertId, expertText, expertDate)
    }

    private fun showNotification(
        title: String, body: String,
        chatroomId: String, myRole: String, expertId: String, expertText: String, expertDate: String
    ) {
        val channelId = CHANNEL_ID
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "聊天訊息", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Routes.EXTRA_CHATROOM_ID, chatroomId)
            putExtra(Routes.EXTRA_MY_ROLE, myRole)
            putExtra(Routes.EXTRA_EXPERT_ID, expertId)
            putExtra(Routes.EXTRA_EXPERT_TEXT, expertText)
            putExtra(Routes.EXTRA_EXPERT_DATE, expertDate)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        private const val TAG = "FcmService"
        private const val CHANNEL_ID = "chat_messages"
    }
}
