package com.tether.app.ui.detail

import com.tether.app.data.model.Circle
import com.tether.app.data.model.Member
import com.tether.app.data.model.MemberStatus

data class CircleDetailUiState(
    val circle: Circle? = null,
    val members: List<Member> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val isLeaving: Boolean = false,
    val errorMessage: String? = null
) {
    val overallStatus: MemberStatus
        get() {
            if (members.isEmpty()) return MemberStatus.UNKNOWN
            val outsideCount = members.count { it.status == MemberStatus.OUTSIDE }
            return when {
                outsideCount > 0 -> MemberStatus.OUTSIDE
                members.any { it.status == MemberStatus.INSIDE } -> MemberStatus.INSIDE
                else -> MemberStatus.UNKNOWN
            }
        }

    val outsideCount: Int
        get() = members.count { it.status == MemberStatus.OUTSIDE }
}