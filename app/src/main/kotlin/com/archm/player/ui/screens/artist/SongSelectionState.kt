package com.archm.player.ui.screens.artist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

const val MAX_SONG_SELECTION = 25

@Stable
class SongSelectionState(
    private val limitMessage: String,
    private val showToast: (String) -> Unit,
) {
    var isActive by mutableStateOf(false)
        private set

    private val _selected = mutableStateListOf<String>()
    val selected: List<String> get() = _selected
    val count: Int get() = _selected.size
    val isFull: Boolean get() = _selected.size >= MAX_SONG_SELECTION

    fun isSelected(videoId: String): Boolean = _selected.contains(videoId)

    fun start(videoId: String) {
        if (isActive) return
        isActive = true
        add(videoId)
    }

    fun toggle(videoId: String) {
        if (!isActive) return
        if (_selected.remove(videoId)) {
            if (_selected.isEmpty()) exit()
            return
        }
        add(videoId)
    }

    fun toggleSelectAll(videoIds: List<String>) {
        if (!isActive) return
        val candidates = videoIds.filter { it.isNotBlank() }.distinct()
        val everythingReachableIsPicked =
            candidates.isNotEmpty() &&
                candidates.take(MAX_SONG_SELECTION).all { _selected.contains(it) }
        if (everythingReachableIsPicked) {
            _selected.clear()
            return
        }
        for (videoId in candidates) {
            if (!add(videoId)) return
        }
    }

    fun exit() {
        isActive = false
        _selected.clear()
    }

    private fun add(videoId: String): Boolean {
        if (videoId.isBlank() || _selected.contains(videoId)) return true
        if (isFull) {
            showToast(limitMessage)
            return false
        }
        _selected.add(videoId)
        return true
    }
}

@Composable
fun rememberSongSelectionState(
    limitMessage: String,
    showToast: (String) -> Unit,
): SongSelectionState = remember {
    SongSelectionState(
        limitMessage = limitMessage,
        showToast = showToast,
    )
}
