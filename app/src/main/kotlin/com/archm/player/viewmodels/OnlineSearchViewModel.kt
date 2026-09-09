

package com.archm.player.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.music.innertube.models.SongItem
import com.music.innertube.models.YTItem
import com.music.innertube.models.filterExplicit
import com.music.innertube.models.filterVideoSongs
import com.music.innertube.models.filterYoutubeShorts
import com.music.innertube.pages.SearchSummaryPage
import com.archm.player.constants.HideExplicitKey
import com.archm.player.constants.HideVideoSongsKey
import com.archm.player.constants.HideYoutubeShortsKey
import com.archm.player.models.ItemsPage
import com.archm.player.utils.dataStore
import com.archm.player.utils.get
import com.archm.player.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.archm.player.ui.screens.search.OnlineSearchResultArgument
import com.archm.player.ui.screens.search.decodeOnlineSearchQuery
import javax.inject.Inject

enum class OnlineSearchSort {
    DEFAULT,
    VIEWS,
}

@HiltViewModel
class OnlineSearchViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val query =
        decodeOnlineSearchQuery(
            savedStateHandle.get<String>(OnlineSearchResultArgument).orEmpty(),
        )
    val filter = MutableStateFlow<YouTube.SearchFilter?>(null)
    var summaryPage by mutableStateOf<SearchSummaryPage?>(null)
    val viewStateMap = mutableStateMapOf<String, ItemsPage?>()

    private val allModeFilters =
        listOf(
            FILTER_SONG,
            FILTER_VIDEO,
            FILTER_ALBUM,
            FILTER_ARTIST,
            FILTER_COMMUNITY_PLAYLIST,
            FILTER_FEATURED_PLAYLIST,
        )
    private var isSummaryLoading = false
    private val loadingFilters = mutableSetOf<String>()

    init {
        viewModelScope.launch {
            filter.collect { selectedFilter ->
                if (selectedFilter == null) {
                    viewModelScope.launch {
                        loadSummaryIfNeeded()
                    }
                    allModeFilters.forEach { allModeFilter ->
                        viewModelScope.launch {
                            loadFilterIfNeeded(allModeFilter)
                        }
                    }
                } else {
                    loadFilterIfNeeded(selectedFilter)
                }
            }
        }
    }

    private suspend fun loadSummaryIfNeeded() {
        if (summaryPage != null || isSummaryLoading) return

        isSummaryLoading = true
        try {
            YouTube
                .searchSummary(query)
                .onSuccess {
                    val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
                    val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
                    summaryPage =
                        it.filterExplicit(hideExplicit)
                            .filterVideoSongs(hideVideoSongs)
                            .filterYoutubeShorts(hideYoutubeShorts)
                }.onFailure {
                    reportException(it)
                }
        } finally {
            isSummaryLoading = false
        }
    }

    private suspend fun loadFilterIfNeeded(filter: YouTube.SearchFilter) {
        val filterKey = filter.value
        if (viewStateMap.containsKey(filterKey) || !loadingFilters.add(filterKey)) return

        try {
            YouTube
                .search(query, filter)
                .onSuccess { result ->
                    val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                    val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
                    val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
                    viewStateMap[filterKey] =
                        ItemsPage(
                            result.items
                                .distinctBy { it.id }
                                .filterExplicit(hideExplicit)
                                .let { items ->
                                    if (filter.value == FILTER_VIDEO.value) items
                                    else items.filterVideoSongs(hideVideoSongs)
                                }
                                .filterYoutubeShorts(hideYoutubeShorts),
                            result.continuation,
                        )
                }.onFailure {
                    reportException(it)
                }
        } finally {
            loadingFilters.remove(filterKey)
        }
    }

    fun loadMore() {
        val filter = filter.value?.value
        viewModelScope.launch {
            if (filter == null) return@launch
            val viewState = viewStateMap[filter] ?: return@launch
            val continuation = viewState.continuation
            if (continuation != null) {
                val searchResult =
                    YouTube.searchContinuation(continuation).getOrNull() ?: return@launch
                val hideExplicit = context.dataStore.get(HideExplicitKey, false)
                val hideVideoSongs = context.dataStore.get(HideVideoSongsKey, false)
                val hideYoutubeShorts = context.dataStore.get(HideYoutubeShortsKey, false)
                val newItems = searchResult.items
                    .filterExplicit(hideExplicit)
                    .let { items ->
                        if (filter == FILTER_VIDEO.value) items
                        else items.filterVideoSongs(hideVideoSongs)
                    }
                    .filterYoutubeShorts(hideYoutubeShorts)
                viewStateMap[filter] = ItemsPage(
                    (viewState.items + newItems).distinctBy { it.id },
                    searchResult.continuation
                )
            }
        }
    }

    fun sortedItems(
        items: List<YTItem>,
        sort: OnlineSearchSort = OnlineSearchSort.DEFAULT,
    ): List<YTItem> =
        when (sort) {
            OnlineSearchSort.DEFAULT -> items
            OnlineSearchSort.VIEWS -> {
                items
                    .withIndex()
                    .sortedWith(
                        compareByDescending<IndexedValue<YTItem>> {
                            (it.value as? SongItem)?.viewCount ?: Long.MIN_VALUE
                        }.thenBy { it.index },
                    ).map { it.value }
            }
        }
}
