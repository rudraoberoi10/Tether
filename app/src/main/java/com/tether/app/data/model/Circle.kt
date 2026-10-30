package com.tether.app.data.model

import com.google.firebase.Timestamp

data class Circle(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val memberIds: List<String> = emptyList(),
    val inviteCode: String = "",
    val fenceLat: Double = 0.0,
    val fenceLng: Double = 0.0,
    val fenceRadiusM: Double = 300.0,
    val fenceLabel: String = "Home",
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromMap(id: String, data: Map<String, Any?>): Circle {
            val timestamp = data["createdAt"]
            val timeMillis = when (timestamp) {
                is Timestamp -> timestamp.toDate().time
                is Long -> timestamp
                else -> System.currentTimeMillis()
            }

            @Suppress("UNCHECKED_CAST")
            val members = (data["memberIds"] as? List<String>) ?: emptyList()

            return Circle(
                id = id,
                name = data["name"] as? String ?: "Circle",
                ownerId = data["ownerId"] as? String ?: "",
                memberIds = members,
                inviteCode = data["inviteCode"] as? String ?: "",
                fenceLat = (data["fenceLat"] as? Number)?.toDouble() ?: 0.0,
                fenceLng = (data["fenceLng"] as? Number)?.toDouble() ?: 0.0,
                fenceRadiusM = (data["fenceRadiusM"] as? Number)?.toDouble() ?: 300.0,
                fenceLabel = data["fenceLabel"] as? String ?: "Home",
                createdAt = timeMillis
            )
        }
    }
}