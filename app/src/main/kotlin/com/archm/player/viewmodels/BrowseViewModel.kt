

package com.archm.player.viewmodels
 
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.YTItem
import com.music.innertube.utils.completed
import com.archm.player.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BrowseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val browseId: String? = savedStateHandle.get<String>("browseId")
    private val params: String? = savedStateHandle.get<String>("params")
    private val initialTitle: String? = savedStateHandle.get<String>("title")
 
    val items = MutableStateFlow<List<YTItem>?>(null)
    val title = MutableStateFlow<String?>(initialTitle)
 
    init {
        browseId?.let { load(it, params, initialTitle) }
    }

    fun load(targetBrowseId: String, targetParams: String? = null, fallbackTitle: String? = null) {
        viewModelScope.launch {
            if (targetBrowseId.startsWith("FEmusic_")) {
                YouTube.library(targetBrowseId)
                    .completed()
                    .onSuccess { page ->
                        title.value = title.value ?: fallbackTitle
                        items.value = page.items
                    }
                    .onFailure {
                        YouTube.browse(targetBrowseId, targetParams).onSuccess { result ->
                            title.value = result.title?.takeIf { it.isNotBlank() } ?: fallbackTitle
                            val allItems = result.items.flatMap { it.items }
                            items.value = allItems
                        }.onFailure { err ->
                            reportException(err)
                            items.value = emptyList()
                        }
                    }
            } else {
                YouTube.browse(targetBrowseId, targetParams).onSuccess { result ->
                    title.value = result.title?.takeIf { it.isNotBlank() } ?: fallbackTitle
                    val allItems = result.items.flatMap { it.items }
                    items.value = allItems
                }.onFailure {
                    YouTube.library(targetBrowseId)
                        .completed()
                        .onSuccess { page ->
                            title.value = title.value ?: fallbackTitle
                            items.value = page.items
                        }
                        .onFailure { err ->
                            reportException(err)
                            items.value = emptyList()
                        }
                }
            }
        }
    }
}
