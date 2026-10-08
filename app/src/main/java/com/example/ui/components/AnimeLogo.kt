package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AnimeRed
import com.example.ui.theme.AnimeRedBright

@Composable
fun AnimeLogo(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("anime_ball_logo")
            .size(size)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(size / 4),
                ambientColor = AnimeRed,
                spotColor = AnimeRedBright
            )
            .clip(RoundedCornerShape(size / 4))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        AnimeRedBright,
                        AnimeRed,
                        Color(0xFF8B0000)
                    )
                )
            )
            .border(
                width = (size.value * 0.03f).coerceAtLeast(1f).dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.8f),
                        Color.White.copy(alpha = 0.2f)
                    )
                ),
                shape = RoundedCornerShape(size / 4)
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_anime_ball_logo),
            contentDescription = "ANIME BALL Logo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(size / 4))
        )
    }
}
