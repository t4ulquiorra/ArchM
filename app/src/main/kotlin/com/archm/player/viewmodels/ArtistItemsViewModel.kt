

package com.archm.player.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.BrowseEndpoint
import com.music.innertube.models.filterExplicit
import com.music.innertube.models.filterVideoSongs
import com.music.innertube.pages.ArtistPage
import com.archm.player.constants.HideExplicitKey
import com.archm.player.constants.HideVideoSongsKey
import com.archm.player.db.MusicDatabase
import com.archm.player.db.entities.Artist
import com.archm.player.db.entities.ArtistEntity
import com.archm.player.models.ItemsPage
import com.archm.player.utils.dataStore
import com.archm.player.utils.get
import com.archm.player.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArtistItemsViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    val database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val artistId = savedStateHandle.get<String>("artistId")
    private val browseId = savedStateHandle.get<String>("browseId")
    private val params = savedStateHandle.get<String>("params")

    val title = MutableStateFlow("")
    val itemsPage = MutableStateFlow<ItemsPage?>(null)
    val artistPage = MutableStateFlow<ArtistPage?>(null)

    val libraryArtist: StateFlow<Artist?> = if (artistId != null) {
        database.artist(artistId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    } else {
        MutableStateFlow(null)
    }

    init {
        if (browseId != null) {
            viewModelScope.launch {
                YouTube
                    .artistItems(
                        BrowseEndpoint(
                            browseId = browseId,
                            params = params,
                        ),
                    ).onSuccess { artistItemsPage ->
                        val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                        val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
                        title.value = artistItemsPage.title
                        itemsPage.value =
                            ItemsPage(
                                items = artistItemsPage.items
                                    .distinctBy { it.id }
                                    .filterExplicit(hideExplicit)
                                    .filterVideoSongs(hideVideoSongs),
                                continuation = artistItemsPage.continuation,
                            )
                    }.onFailure {
                        reportException(it)
                    }
            }
        }

        if (artistId != null) {
            viewModelScope.launch {
                YouTube.artist(artistId)
                    .onSuccess { page ->
                        artistPage.value = page
                    }
                    .onFailure {
                        reportException(it)
                    }
            }
        }
    }

    fun toggleFollow() {
        val id = artistId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            database.transaction {
                val current = libraryArtist.value?.artist
                if (current != null) {
                    update(current.toggleLike())
                } else {
                    val remote = artistPage.value?.artist
                    if (remote != null) {
                        insert(
                            ArtistEntity(
                                id = remote.id,
                                name = remote.title,
                                channelId = remote.channelId,
                                thumbnailUrl = remote.thumbnail,
                            ).toggleLike(),
                        )
                    }
                }
            }
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            val oldItemsPage = itemsPage.value ?: return@launch
            val continuation = oldItemsPage.continuation ?: return@launch
            YouTube
                .artistItemsContinuation(continuation)
                .onSuccess { artistItemsContinuationPage ->
                    val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
                    itemsPage.update {
                        ItemsPage(
                            items =
                            (oldItemsPage.items + artistItemsContinuationPage.items)
                                .distinctBy { it.id }
                                .filterExplicit(hideExplicit)
                                .filterVideoSongs(hideVideoSongs),
                            continuation = artistItemsContinuationPage.continuation,
                        )
                    }
                }.onFailure {
                    reportException(it)
                }
        }
    }
}
