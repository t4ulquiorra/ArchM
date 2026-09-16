package com.archm.player.ui.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Global manager for triggering the custom top banner from anywhere in the app.
 */
object CustomSnackbarManager {
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    val messages: StateFlow<String?> = message

    private var currentJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun show(text: String, durationMillis: Long = 3500L) {
        currentJob?.cancel()
        currentJob = scope.launch {
            _message.value = text
            delay(durationMillis)
            _message.value = null
        }
    }

    fun dismiss() {
        currentJob?.cancel()
        _message.value = null
    }
}

/**
 * Visual implementation of the swipeable top banner:
 * - Portrait mode: fillMaxWidth() edge-to-edge (0.dp horizontal padding, subtle bottom corner rounding RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
 * - Landscape mode: Centered with a constrained maximum width (widthIn(max = 520.dp)), leaving side breathing room
 * - Swipe-to-Dismiss Gesture: Swipe upward to dismiss instantly
 * - Enter: slideInVertically(initialOffsetY = { -it }) + fadeIn()
 * - Exit: slideOutVertically(targetOffsetY = { -it }) + fadeOut()
 */
@Composable
fun CustomFloatingSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val shape = if (isLandscape) {
        RoundedCornerShape(16.dp)
    } else {
        RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    var offsetY by remember(message) { mutableFloatStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        label = "bannerOffsetY"
    )

    val responsiveModifier = if (isLandscape) {
        Modifier
            .widthIn(max = 520.dp)
            .padding(horizontal = 24.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    Surface(
        modifier = modifier
            .then(responsiveModifier)
            .offset { IntOffset(0, animatedOffsetY.roundToInt().coerceAtMost(0)) }
            .pointerInput(message) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY < -40f) {
                            onDismiss()
                        } else {
                            offsetY = 0f
                        }
                    },
                    onDragCancel = {
                        offsetY = 0f
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = offsetY + dragAmount
                        if (newOffset <= 0f) {
                            offsetY = newOffset
                        }
                        if (offsetY < -100f) {
                            onDismiss()
                        }
                    }
                )
            }
            .shadow(elevation = 6.dp, shape = shape),
        shape = shape,
        color = Color.White,
        contentColor = Color(0xFF121212),
        shadowElevation = 6.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message,
                color = Color(0xFF121212),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Host component that presents the top banner with smooth fade-in/fade-out
 * and slide animations from the top.
 */
@Composable
fun CustomSnackbarHost(
    modifier: Modifier = Modifier,
    hostState: SnackbarHostState? = null,
) {
    val managerMessage by CustomSnackbarManager.message.collectAsState()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        // Host for standard SnackbarHostState calls if provided
        if (hostState != null) {
            SnackbarHost(
                hostState = hostState,
            ) { data ->
                CustomFloatingSnackbar(
                    message = data.visuals.message,
                    onDismiss = { data.dismiss() },
                )
            }
        }

        AnimatedVisibility(
            visible = managerMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(250)) + fadeOut(animationSpec = tween(250)),
        ) {
            managerMessage?.let { text ->
                CustomFloatingSnackbar(
                    message = text,
                    onDismiss = { CustomSnackbarManager.dismiss() },
                )
            }
        }
    }
}
