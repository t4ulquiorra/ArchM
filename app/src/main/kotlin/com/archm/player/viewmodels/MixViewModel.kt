package com.archm.player.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.PlaylistItem
import com.music.innertube.utils.completed
import com.archm.player.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MixViewModel @Inject constructor() : ViewModel() {
    private val _mixPlaylists = MutableStateFlow<List<PlaylistItem>>(emptyList())
    val mixPlaylists = _mixPlaylists.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            YouTube.library("FEmusic_mixed_for_you")
                .completed()
                .onSuccess { page ->
                    _mixPlaylists.value = page.items.filterIsInstance<PlaylistItem>()
                }
                .onFailure {
                    reportException(it)
                }
            _isLoading.value = false
        }
    }
}
