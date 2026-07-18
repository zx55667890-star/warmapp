package com.example.myapplication.data.repository
import android.util.Log
import com.example.myapplication.data.FirebasePaths
import com.example.myapplication.data.model.Experience

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * 負責 bigram 配對邏輯與專家指派，升級優化版（加入 Jaccard 相似度與信心閥值防禦）
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

                firebaseDb.getReference(FirebasePaths.ACTIVE_EXPERIENCES)
                    .orderByChild("status").equalTo("active")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(expSnapshot: DataSnapshot) {
                            val questionBigrams = getBigrams(text)
                            
                            val matches = mutableListOf<Pair<Experience, Double>>()

                            for (child in expSnapshot.children) {
                                val exp = child.getValue(Experience::class.java) ?: continue
                                if (rejectedExperts.contains(exp.authorId)) continue
                                if (!exp.isOnline) continue

                                val expBigrams = getBigrams(exp.text)
                                
                                val intersectSize = expBigrams.intersect(questionBigrams).size
                                val unionSize = expBigrams.union(questionBigrams).size

                                val jaccardScore = if (unionSize > 0) {
                                    intersectSize.toDouble() / unionSize
                                } else {
                                    0.0
                                }

                                val MIN_MATCH_THRESHOLD = 0.08
                                
                                if (jaccardScore >= MIN_MATCH_THRESHOLD) {
                                    matches.add(exp to jaccardScore)
                                }
                            }

                            val sorted = matches.sortedWith(
                                compareByDescending<Pair<Experience, Double>> { it.second }
                                    .thenByDescending { it.first.timestamp }
                            )

                            if (sorted.isNotEmpty()) {
                                val best = sorted.first().first
                                qRef.updateChildren(mapOf(
                                    "expertId" to best.authorId,
                                    "status" to "taken",
                                    "matchedExpTimestamp" to best.timestamp,
                                    "matchedExpText" to best.text
                                ))
                            } else {
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

    private fun getBigrams(str: String): Set<String> {
        val clean = str.replace("\\s+".toRegex(), "")
        if (clean.length < 2) return setOf(clean)
        return clean.windowed(2).toSet()
    }
}
