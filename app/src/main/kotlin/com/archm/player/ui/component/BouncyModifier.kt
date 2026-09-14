package com.archm.player.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun rememberBouncyScale(
    interactionSource: InteractionSource,
    targetShrinkScale: Float = 0.94f,
    stiffness: Float = Spring.StiffnessMedium,
    dampingRatio: Float = Spring.DampingRatioMediumBouncy,
): State<Float> {
    val animatable = remember { Animatable(1f) }
    LaunchedEffect(interactionSource, targetShrinkScale, stiffness, dampingRatio) {
        var pressJob: Job? = null
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    pressJob?.cancel()
                    pressJob = launch {
                        animatable.animateTo(
                            targetValue = targetShrinkScale,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = stiffness,
                            ),
                        )
                    }
                }
                is PressInteraction.Release -> {
                    pressJob?.cancel()
                    pressJob = launch {
                        // Guaranteed tap bounce: on light taps, finish the punch-down before spring release
                        if (animatable.value > targetShrinkScale + 0.01f) {
                            animatable.animateTo(
                                targetValue = targetShrinkScale,
                                animationSpec = tween(durationMillis = 60, easing = FastOutLinearInEasing),
                            )
                        }
                        animatable.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = dampingRatio,
                                stiffness = stiffness,
                            ),
                        )
                    }
                }
                is PressInteraction.Cancel -> {
                    pressJob?.cancel()
                    pressJob = launch {
                        animatable.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = stiffness,
                            ),
                        )
                    }
                }
            }
        }
    }
    return animatable.asState()
}

@Composable
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    shrinkScale: Float = 0.94f,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberBouncyScale(
        interactionSource = interactionSource,
        targetShrinkScale = if (enabled) shrinkScale else 1.0f,
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioMediumBouncy,
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick,
        )
}
