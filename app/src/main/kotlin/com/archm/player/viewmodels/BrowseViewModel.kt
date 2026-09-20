

package com.archm.player.viewmodels
 
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.YTItem
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
 
    val items = MutableStateFlow<List<YTItem>?>(null)
    val title = MutableStateFlow<String?>("")
 
    init {
        viewModelScope.launch {
            browseId?.let {
                YouTube.browse(browseId, params).onSuccess { result ->
                    title.value = result.title
                    val allItems = result.items.flatMap { it.items }
                    items.value = allItems
                }.onFailure {
                    reportException(it)
                    items.value = emptyList()
                }
            }
        }
    }
}
