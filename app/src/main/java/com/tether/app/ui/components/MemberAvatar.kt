package com.tether.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tether.app.data.model.MemberStatus
import com.tether.app.ui.theme.AvatarColors
import com.tether.app.ui.theme.TetherBg
import com.tether.app.ui.theme.TetherCoral
import com.tether.app.ui.theme.TetherTeal
import com.tether.app.ui.theme.TextSecondary

@Composable
fun MemberAvatar(
    name: String,
    avatarColorIndex: Int,
    modifier: Modifier = Modifier,
    status: MemberStatus? = null,
    size: Dp = 44.dp
) {
    val bgColor = AvatarColors.getOrElse(avatarColorIndex % AvatarColors.size) { AvatarColors[0] }
    val initials = name.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
        .ifEmpty { "T" }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(bgColor, CircleShape)
                .border(1.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = TetherBg,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38f).sp
            )
        }

        if (status != null) {
            val statusColor = when (status) {
                MemberStatus.INSIDE -> TetherTeal
                MemberStatus.OUTSIDE -> TetherCoral
                MemberStatus.UNKNOWN -> TextSecondary
            }
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .background(statusColor, CircleShape)
                    .border(2.dp, TetherBg, CircleShape)
            )
        }
    }
}