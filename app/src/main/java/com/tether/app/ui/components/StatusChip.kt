package com.tether.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tether.app.data.model.MemberStatus
import com.tether.app.ui.theme.TetherCoral
import com.tether.app.ui.theme.TetherSurfaceElevated
import com.tether.app.ui.theme.TetherTeal
import com.tether.app.ui.theme.TextSecondary

@Composable
fun StatusChip(
    status: MemberStatus,
    outsideCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val (dotColor, text) = when (status) {
        MemberStatus.INSIDE -> TetherTeal to "Everyone inside"
        MemberStatus.OUTSIDE -> TetherCoral to if (outsideCount > 1) "$outsideCount outside" else "1 outside"
        MemberStatus.UNKNOWN -> TextSecondary to "Waiting for updates"
    }

    Row(
        modifier = modifier
            .background(TetherSurfaceElevated, RoundedCornerShape(12.dp))
            .border(1.dp, dotColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}