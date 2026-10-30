package com.tether.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tether.app.ui.theme.BorderSubtle
import com.tether.app.ui.theme.TetherCoral
import com.tether.app.ui.theme.TetherSurfaceElevated
import com.tether.app.ui.theme.TetherTeal
import com.tether.app.ui.theme.TextPrimary
import com.tether.app.ui.theme.TextSecondary

@Composable
fun CodeBoxes(
    code: String,
    isError: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 6) {
            val char = code.getOrNull(i)?.uppercaseChar()?.toString() ?: ""
            val isCurrent = i == code.length
            val borderColor = when {
                isError -> TetherCoral
                isCurrent -> TetherTeal
                char.isNotEmpty() -> TetherTeal.copy(alpha = 0.6f)
                else -> BorderSubtle
            }

            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(56.dp)
                    .background(TetherSurfaceElevated, RoundedCornerShape(12.dp))
                    .border(1.5.dp, borderColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = char,
                    color = if (isError) TetherCoral else TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}