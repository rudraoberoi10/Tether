package com.tether.app.data.model

import com.google.firebase.Timestamp

data class Member(
    val uid: String = "",
    val displayName: String = "",
    val avatarColor: Int = 0,
    val status: MemberStatus = MemberStatus.UNKNOWN,
    val statusUpdatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromMap(uid: String, data: Map<String, Any?>): Member {
            val statusStr = data["status"] as? String ?: "UNKNOWN"
            val statusEnum = try {
                MemberStatus.valueOf(statusStr)
            } catch (e: Exception) {
                MemberStatus.UNKNOWN
            }

            val timestamp = data["statusUpdatedAt"]
            val timeMillis = when (timestamp) {
                is Timestamp -> timestamp.toDate().time
                is Long -> timestamp
                else -> System.currentTimeMillis()
            }

            return Member(
                uid = uid,
                displayName = data["displayName"] as? String ?: "Member",
                avatarColor = (data["avatarColor"] as? Long)?.toInt() ?: 0,
                status = statusEnum,
                statusUpdatedAt = timeMillis
            )
        }
    }
}