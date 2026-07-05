package com.example.myapplication.data.repository
import android.util.Log
import com.example.myapplication.data.model.Experience

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * 負責 bigram 配對邏輯與專家指派，從 QuestionViewModel 抽出以降低複雜度。
 */
class MatchingRepository(
    private val firebaseDb: FirebaseDatabase
) : MatchingRepositoryInterface {

    override fun matchAndAssignExpert(questionId: String, text: String, userId: String) {
        val qRef = firebaseDb.getReference("questions").child(questionId)
        qRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(qSnapshot: DataSnapshot) {
                if (!qSnapshot.exists()) return
                val status = qSnapshot.child("status").value?.toString()
                if (status == "cancelled" || status == "taken") return

                val rejectedExperts = mutableSetOf<String>()
                qSnapshot.child("rejectedExperts").children.forEach {
                    it.key?.let { key -> rejectedExperts.add(key) }
                }

                firebaseDb.getReference("experiences")
                    .orderByChild("status").equalTo("active")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(expSnapshot: DataSnapshot) {
                            val questionBigrams = getBigrams(text)
                            val matches = mutableListOf<Pair<Experience, Int>>()

                            for (child in expSnapshot.children) {
                                val exp = child.getValue(Experience::class.java) ?: continue
                                if (rejectedExperts.contains(exp.authorId)) continue

                                // ✅ Bug 4：過濾掉已離線的專家（切換到提問 Tab 或關閉 App）
                                if (!exp.isOnline) continue

                                val expBigrams = getBigrams(exp.text)
                                val overlap = expBigrams.intersect(questionBigrams).size
                                if (overlap > 0 || (questionBigrams.isNotEmpty() && expBigrams == questionBigrams)) {
                                    matches.add(exp to overlap)
                                }
                            }

                            val sorted = matches.sortedWith(
                                compareByDescending<Pair<Experience, Int>> { it.second }
                                    .thenByDescending { it.first.timestamp }
                            )

                            if (sorted.isNotEmpty()) {
                                val best = sorted.first().first
                                qRef.updateChildren(mapOf(
                                    "expertId" to best.authorId,
                                    "status" to "pending_acceptance",
                                    "matchedExpTimestamp" to best.timestamp,
                                    "matchedExpText" to best.text
                                ))
                            } else {
                                // Keep "matching" status so question stays open for future experts
                                qRef.child("expertId").setValue("")
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.w("MatchingRepository", "Expert query cancelled", error.toException())
                        }
                    })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("MatchingRepository", "Match cancelled", error.toException())
            }
        })
    }

    // ✅ Bug 3：getBigrams 統一定義在此，QuestionViewModel 裡的重複版本已刪除
    private fun getBigrams(str: String): Set<String> {
        val clean = str.replace("\\s+".toRegex(), "")
        if (clean.length < 2) return setOf(clean)
        return clean.windowed(2).toSet()
    }
}