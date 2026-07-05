package com.example.myapplication.data.repository

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.random.Random

class AuthRepository(
    private val firebaseAuth: FirebaseAuth,
    private val appContext: Context,
    private val firebaseDb: FirebaseDatabase,
) {

    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser
    val currentUserId: String get() = firebaseAuth.currentUser?.uid ?: ""

    fun isLoggedIn(): Boolean = firebaseAuth.currentUser != null

    private fun translateError(e: Exception): String {
        val msg = e.message ?: return "登入失敗"
        return when {
            msg.contains("INVALID_PASSWORD") || msg.contains("WRONG_PASSWORD") -> "密碼錯誤"
            msg.contains("USER_NOT_FOUND") || msg.contains("user record") -> "帳號不存在"
            msg.contains("USER_DISABLED") -> "帳號已被停用"
            msg.contains("TOO_MANY_REQUESTS") -> "請求次數過多，請稍後再試"
            msg.contains("EMAIL_ALREADY_IN_USE") || msg.contains("already in use") -> "此 Email 已被註冊"
            msg.contains("INVALID_EMAIL") || msg.contains("badly formatted") -> "Email 格式錯誤"
            msg.contains("WEAK_PASSWORD") -> "密碼強度不足"
            msg.contains("NETWORK_REQUEST_FAILED") || msg.contains("network error") -> "網路連線異常，請檢查網路"
            msg.contains("INVALID_LOGIN_CREDENTIALS") || msg.contains("invalid credential") -> "Email 或密碼錯誤"
            else -> "登入失敗"
        }
    }

    suspend fun login(email: String, password: String) {
        suspendCancellableCoroutine { cont ->
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(Exception(translateError(it))) }
        }
    }

    suspend fun register(email: String, password: String) {
        suspendCancellableCoroutine { cont ->
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(Exception(translateError(it))) }
        }
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        suspendCancellableCoroutine { cont ->
            firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(Exception(translateError(it))) }
        }
    }

    suspend fun sendPasswordReset(email: String) {
        val emailKey = "reset_" + email.replace(".", ",").replace("@", "~")
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val countRef = firebaseDb.getReference("email_verification").child(emailKey).child("dailyCount").child(today)

        suspendCancellableCoroutine { cont ->
            countRef.get().addOnSuccessListener { snap ->
                val count = snap.value?.toString()?.toIntOrNull() ?: 0
                if (count >= 3) {
                    cont.resumeWithException(Exception("請求次數過多，請明天再試"))
                    return@addOnSuccessListener
                }
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        countRef.setValue(count + 1)
                        Log.d("AuthRepository", "Password reset email sent to $email (daily: ${count + 1})")
                        cont.resume(Unit)
                    }
                    .addOnFailureListener {
                        Log.e("AuthRepository", "Password reset failed for $email: ${it.message}")
                        cont.resumeWithException(Exception(translateError(it)))
                    }
            }.addOnFailureListener {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Log.d("AuthRepository", "Password reset email sent to $email")
                        cont.resume(Unit)
                    }
                    .addOnFailureListener { e ->
                        Log.e("AuthRepository", "Password reset failed for $email: ${e.message}")
                        cont.resumeWithException(Exception(translateError(e)))
                    }
            }
        }
    }

    fun markResetVerified(email: String) {
        val emailKey = "reset_" + email.replace(".", ",").replace("@", "~")
        firebaseDb.getReference("email_verification").child(emailKey).child("verified").setValue(true)
    }

    suspend fun resetPasswordCloudFunction(email: String, newPassword: String, code: String) {
        suspendCancellableCoroutine { cont ->
            FirebaseFunctions.getInstance().getHttpsCallable("resetPassword")
                .call(mapOf("email" to email, "newPassword" to newPassword, "code" to code))
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { cont.resumeWithException(Exception(it.message ?: "密碼重設失敗")) }
        }
    }

    fun saveFcmToken() {
        val uid = currentUserId
        if (uid.isBlank()) return
        com.google.firebase.messaging.FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                firebaseDb.getReference("users").child(uid).child("fcmToken").setValue(token)
            }
    }

    fun logout() {
        firebaseAuth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(appContext.getString(com.example.myapplication.R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(appContext, gso).signOut()
    }

    suspend fun generateVerificationCode(email: String, prefix: String = "") {
        val emailKey = prefix + email.replace(".", ",").replace("@", "~")
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val countRef = firebaseDb.getReference("email_verification").child(emailKey).child("dailyCount").child(today)

        suspendCancellableCoroutine { cont ->
            countRef.get().addOnSuccessListener { snap ->
                val count = snap.value?.toString()?.toIntOrNull() ?: 0
                if (count >= 3) {
                    cont.resumeWithException(Exception("請求次數過多，請明天再試"))
                    return@addOnSuccessListener
                }
                val code = String.format("%06d", Random.nextInt(0, 999999))
                countRef.setValue(count + 1)
                firebaseDb.getReference("email_verification").child(emailKey).child("code").setValue(code)
                firebaseDb.getReference("email_verification").child(emailKey).child("createdAt")
                    .setValue(System.currentTimeMillis())
                val tag = if (prefix == "reset_") "ResetCode" else "RegCode"
                Log.d("AuthRepository", "$tag for $email: $code (daily: ${count + 1})")
                val type = if (prefix == "reset_") "reset" else "register"
                FirebaseFunctions.getInstance().getHttpsCallable("sendVerificationEmail")
                    .call(mapOf("email" to email, "code" to code, "type" to type))
                    .addOnFailureListener { e -> Log.e("AuthRepository", "sendEmail failed: ${e.message}") }
                cont.resume(Unit)
            }.addOnFailureListener {
                val code = String.format("%06d", Random.nextInt(0, 999999))
                firebaseDb.getReference("email_verification").child(emailKey).child("code").setValue(code)
                firebaseDb.getReference("email_verification").child(emailKey).child("createdAt")
                    .setValue(System.currentTimeMillis())
                val tag = if (prefix == "reset_") "ResetCode" else "RegCode"
                Log.d("AuthRepository", "$tag for $email: $code (count check failed, allowed)")
                val type = if (prefix == "reset_") "reset" else "register"
                FirebaseFunctions.getInstance().getHttpsCallable("sendVerificationEmail")
                    .call(mapOf("email" to email, "code" to code, "type" to type))
                    .addOnFailureListener { e -> Log.e("AuthRepository", "sendEmail failed: ${e.message}") }
                cont.resume(Unit)
            }
        }
    }

    suspend fun verifyVerificationCode(email: String, code: String, prefix: String = ""): Boolean {
        val emailKey = prefix + email.replace(".", ",").replace("@", "~")
        Log.d("AuthRepository", "verifyVerificationCode: email=$email prefix=$prefix emailKey=$emailKey enteredCode=$code")
        return suspendCancellableCoroutine { cont ->
            firebaseDb.getReference("email_verification").child(emailKey).child("code").get()
                .addOnSuccessListener { snap ->
                    val storedCode = snap.value?.toString()
                    Log.d("AuthRepository", "verifyVerificationCode: storedCode=$storedCode match=${storedCode == code}")
                    cont.resume(storedCode == code)
                }
                .addOnFailureListener { e ->
                    Log.w("AuthRepository", "verifyVerificationCode: failed to read: ${e.message}")
                    cont.resume(false)
                }
        }
    }

    fun addAuthStateListener(listener: (FirebaseUser?) -> Unit): FirebaseAuth.AuthStateListener {
        val authListener = FirebaseAuth.AuthStateListener { listener(it.currentUser) }
        firebaseAuth.addAuthStateListener(authListener)
        return authListener
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        firebaseAuth.removeAuthStateListener(listener)
    }
}
