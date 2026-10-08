package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AnimeGold
import com.example.ui.theme.AnimeGoldLight

@Composable
fun ProphetBlessingPill(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("prophet_blessing_pill")
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF1B281B).copy(alpha = 0.9f),
                        Color(0xFF0F1A12).copy(alpha = 0.95f),
                        Color(0xFF1B281B).copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        AnimeGold.copy(alpha = 0.6f),
                        Color(0xFF4CAF50).copy(alpha = 0.7f),
                        AnimeGold.copy(alpha = 0.6f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AnimeGoldLight,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "صلِّ على رسول الله ﷺ",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.3.sp
                ),
                color = Color(0xFFF1F8E9)
            )
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = AnimeGoldLight,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
