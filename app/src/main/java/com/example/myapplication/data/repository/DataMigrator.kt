package com.example.myapplication.data.repository

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.database.FirebaseDatabase

class DataMigrator(
    private val firebaseDb: FirebaseDatabase,
    private val prefs: SharedPreferences
) {
    companion object {
        private const val KEY_MIGRATION_DONE = "migration_deviceId_to_uid_done"
        private const val KEY_DEVICE_ID = "saved_device_id"
    }

    fun migrateIfNeeded(uid: String) {
        val deviceId = prefs.getString(KEY_DEVICE_ID, "") ?: ""
        if (deviceId.isEmpty() || deviceId == uid) return
        if (prefs.getBoolean(KEY_MIGRATION_DONE, false)) return
        if (uid.isBlank()) return

        migrateExpertData(deviceId, uid)
        migrateQuestions(deviceId, uid)
        prefs.edit { putBoolean(KEY_MIGRATION_DONE, true) }
        Log.d("DataMigrator", "Migration complete: $deviceId → $uid")
    }

    fun saveDeviceId(deviceId: String) {
        prefs.edit { putString(KEY_DEVICE_ID, deviceId) }
    }

    private fun migrateExpertData(deviceId: String, uid: String) {
        val oldRef = firebaseDb.getReference("experts").child(deviceId)
        oldRef.get().addOnSuccessListener { snap ->
            if (snap.exists()) {
                val data = snap.value as? Map<*, *>
                val map = data?.entries?.associate { it.key.toString() to (it.value as Any) } ?: return@addOnSuccessListener
                firebaseDb.getReference("experts").child(uid).updateChildren(map)
                    .addOnSuccessListener { oldRef.removeValue() }
            }
        }
    }

    private fun migrateQuestions(deviceId: String, uid: String) {
        firebaseDb.getReference("questions").get().addOnSuccessListener { snap ->
            for (child in snap.children) {
                val qId = child.key ?: continue
                val updates = mutableMapOf<String, Any>()
                if (child.child("authorId").value?.toString() == deviceId) {
                    updates["authorId"] = uid
                }
                if (child.child("expertId").value?.toString() == deviceId) {
                    updates["expertId"] = uid
                }
                if (updates.isNotEmpty()) {
                    firebaseDb.getReference("questions").child(qId).updateChildren(updates)
                }
                if (child.child("rejectedExperts").hasChild(deviceId)) {
                    val rejectedVal = child.child("rejectedExperts").child(deviceId).value
                    val qRef = firebaseDb.getReference("questions").child(qId)
                    if (rejectedVal != null) {
                        qRef.child("rejectedExperts").child(uid).setValue(rejectedVal)
                    }
                    qRef.child("rejectedExperts").child(deviceId).removeValue()
                }
            }
        }
    }
}
