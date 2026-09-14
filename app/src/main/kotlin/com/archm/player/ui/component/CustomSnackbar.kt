package com.archm.player.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

/**
 * Global manager for triggering the custom floating snackbar from anywhere in the app.
 */
object CustomSnackbarManager {
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var currentJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun show(text: String, durationMillis: Long = 2000L) {
        currentJob?.cancel()
        currentJob = scope.launch {
            _message.value = text
            delay(durationMillis)
            _message.value = null
        }
    }
}

/**
 * Visual implementation of the custom floating snackbar according to design specs:
 * - Shape: RoundedCornerShape(8.dp)
 * - Background: Pure white (Color.White) with subtle elevation/drop shadow (shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp)))
 * - Text Color: Accent color (theme primary)
 * - Text Style: MaterialTheme.typography.bodyMedium with FontWeight.SemiBold
 */
@Composable
fun CustomFloatingSnackbar(
    message: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        tonalElevation = 0.dp,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Host component that presents the custom floating snackbar with smooth fade-in/fade-out
 * and slide animations.
 */
@Composable
fun CustomSnackbarHost(
    modifier: Modifier = Modifier,
    hostState: SnackbarHostState? = null,
) {
    val managerMessage by CustomSnackbarManager.message.collectAsState()

    // Host for standard SnackbarHostState calls if provided
    if (hostState != null) {
        SnackbarHost(
            hostState = hostState,
            modifier = modifier,
        ) { data ->
            CustomFloatingSnackbar(message = data.visuals.message)
        }
    }

    AnimatedVisibility(
        visible = managerMessage != null,
        enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 2 },
        exit = fadeOut(tween(250)) + slideOutVertically(tween(250)) { it / 2 },
        modifier = modifier,
    ) {
        managerMessage?.let { text ->
            CustomFloatingSnackbar(message = text)
        }
    }
}
