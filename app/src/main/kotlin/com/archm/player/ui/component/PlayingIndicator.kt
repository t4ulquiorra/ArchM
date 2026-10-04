

package com.archm.player.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.archm.player.R
import com.archm.player.constants.ThumbnailCornerRadius
import com.archm.player.ui.theme.LocalAccentColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun PlayingIndicator(
    color: Color,
    modifier: Modifier = Modifier,
    bars: Int = 3,
    barWidth: Dp = 2.5.dp,
    barSpacing: Dp = 2.dp,
    cornerRadius: Dp = 1.dp,
    isPlaying: Boolean = true,
) {
    val animatables =
        remember(bars) {
            List(bars) {
                Animatable(0.2f)
            }
        }

    val staticHeights = remember(bars) {
        listOf(0.35f, 0.75f, 0.5f, 0.7f, 0.4f)
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            animatables.forEach { animatable ->
                launch {
                    while (true) {
                        animatable.animateTo(
                            targetValue = Random.nextFloat() * 0.85f + 0.15f,
                            animationSpec = tween(durationMillis = 200),
                        )
                        delay(50)
                    }
                }
            }
        } else {
            animatables.forEachIndexed { index, animatable ->
                launch {
                    animatable.animateTo(
                        targetValue = staticHeights.getOrElse(index) { 0.5f },
                        animationSpec = tween(durationMillis = 200),
                    )
                }
            }
        }
    }

    val totalWidth = barWidth * bars + barSpacing * (bars - 1)
    Canvas(
        modifier = modifier
            .width(totalWidth)
            .androidx.compose.ui.layout.layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                layout(
                    width = placeable.width,
                    height = placeable.height,
                    alignmentLines = mapOf(
                        androidx.compose.ui.layout.FirstBaseline to placeable.height,
                        androidx.compose.ui.layout.LastBaseline to placeable.height,
                    ),
                ) {
                    placeable.placeRelative(0, 0)
                }
            },
    ) {
        val barWidthPx = barWidth.toPx()
        val spacingPx = barSpacing.toPx()
        val cornerRadiusPx = cornerRadius.toPx()

        for (i in 0 until bars) {
            val fraction = animatables[i].value
            val barHeight = size.height * fraction
            val left = i * (barWidthPx + spacingPx)
            val top = size.height - barHeight

            drawRoundRect(
                color = color,
                topLeft = Offset(x = left, y = top),
                size = Size(width = barWidthPx, height = barHeight),
                cornerRadius = CornerRadius(cornerRadiusPx),
            )
        }
    }
}

@Composable
fun PlayingIndicatorBox(
    modifier: Modifier = Modifier,
    isActive: Boolean,
    playWhenReady: Boolean,
    color: Color = LocalAccentColor.current,
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(tween(500)),
        exit = fadeOut(tween(500)),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier,
        ) {
            if (playWhenReady) {
                PlayingIndicator(
                    color = color,
                    modifier = Modifier.height(24.dp),
                    barWidth = 4.dp,
                    barSpacing = 4.dp,
                    cornerRadius = ThumbnailCornerRadius,
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = null,
                    tint = color,
                )
            }
        }
    }
}
