/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.constants.AppBarHeight
import com.archm.player.extensions.togglePlayPause
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ALBUM
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_ARTIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_COMMUNITY_PLAYLIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_FEATURED_PLAYLIST
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_SONG
import com.music.innertube.YouTube.SearchFilter.Companion.FILTER_VIDEO
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.pages.SearchSummary
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.queues.YouTubeQueue
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.unit.sp
import com.archm.player.ui.component.EmptyPlaceholder
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.YouTubeListItem
import com.archm.player.ui.component.shimmer.ListItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.viewmodels.OnlineSearchSort
import com.archm.player.viewmodels.OnlineSearchViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnlineSearchResult(
    navController: NavController,
    searchSort: OnlineSearchSort = OnlineSearchSort.DEFAULT,
    viewModel: OnlineSearchViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    val searchFilter by viewModel.filter.collectAsStateWithLifecycle()
    val searchSummary = viewModel.summaryPage
    val itemsPage by remember(searchFilter) {
        derivedStateOf {
            searchFilter?.value?.let {
                viewModel.viewStateMap[it]
            }
        }
    }
    val allModeSections =
        buildList<SearchResultSection> {
            searchSummary
                ?.summaries
                ?.firstOrNull()
                ?.takeIf { it.items.isNotEmpty() }
                ?.let { summary ->
                    val title = if (summary.title.equals("Top result", ignoreCase = true)) {
                        stringResource(R.string.top_result)
                    } else {
                        summary.title
                    }
                    add(
                        SearchResultSection(
                            isTopResult = true,
                            title = title,
                            items = summary.items.distinctBy { it.id },
                        ),
                    )
                }

            listOf(
                FILTER_SONG to stringResource(R.string.filter_songs),
                FILTER_VIDEO to stringResource(R.string.filter_videos),
                FILTER_ALBUM to stringResource(R.string.filter_albums),
                FILTER_ARTIST to stringResource(R.string.filter_artists),
                FILTER_COMMUNITY_PLAYLIST to stringResource(R.string.filter_community_playlists),
                FILTER_FEATURED_PLAYLIST to stringResource(R.string.filter_featured_playlists),
            ).forEach { (sectionFilter, sectionTitle) ->
                viewModel.viewStateMap[sectionFilter.value]
                    ?.items
                    ?.takeIf { it.isNotEmpty() }
                    ?.let { items ->
                        add(
                            SearchResultSection(
                                isTopResult = false,
                                title = sectionTitle,
                                items = viewModel.sortedItems(items.distinctBy { it.id }, searchSort),
                            ),
                        )
                    }
            }
        }
    val isAllModeLoaded =
        searchSummary != null ||
            listOf(
                FILTER_SONG,
                FILTER_VIDEO,
                FILTER_ALBUM,
                FILTER_ARTIST,
                FILTER_COMMUNITY_PLAYLIST,
                FILTER_FEATURED_PLAYLIST,
            ).all { viewModel.viewStateMap.containsKey(it.value) }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" }
        }.collect { shouldLoadMore ->
            if (!shouldLoadMore) return@collect
            viewModel.loadMore()
        }
    }

    val ytItemContent: @Composable LazyItemScope.(YTItem, Boolean) -> Unit = { item: YTItem, showDivider: Boolean ->
        val longClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
                when (item) {
                    is SongItem -> {
                        YouTubeSongMenu(
                            song = item,
                            navController = navController,
                            onDismiss = menuState::dismiss,
                        )
                    }

                    is AlbumItem -> {
                        YouTubeAlbumMenu(
                            albumItem = item,
                            navController = navController,
                            onDismiss = menuState::dismiss,
                        )
                    }

                    is ArtistItem -> {
                        YouTubeArtistMenu(
                            artist = item,
                            onDismiss = menuState::dismiss,
                        )
                    }

                    is PlaylistItem -> {
                        YouTubePlaylistMenu(
                            playlist = item,
                            coroutineScope = coroutineScope,
                            onDismiss = menuState::dismiss,
                        )
                    }
                }
            }
        }
        val currentMediaId = mediaMetadata?.id ?: playerConnection.player.currentMediaItem?.mediaId
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateItem(),
        ) {
            YouTubeListItem(
                item = item,
                viewCountText = (item as? SongItem)?.viewCountText,
                containerColor = Color.Transparent,
                color = Color.Transparent,
                showActiveContainer = true,
                isActive =
                    when (item) {
                        is SongItem -> item.id == currentMediaId
                        is AlbumItem -> mediaMetadata?.album?.id == item.id
                        else -> false
                    },
                isPlaying = isPlaying,
                trailingContent = {
                    IconButton(
                        onClick = longClick,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = null,
                        )
                    }
                },
                modifier =
                    Modifier
                        .combinedClickable(
                            onClick = {
                                when (item) {
                                    is SongItem -> {
                                        if (item.id == mediaMetadata?.id) {
                                            playerConnection.player.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                YouTubeQueue(
                                                    WatchEndpoint(videoId = item.id),
                                                    item.toMediaMetadata(),
                                                ),
                                            )
                                        }
                                    }

                                    is AlbumItem -> {
                                        navController.navigate("album/${item.id}")
                                    }

                                    is ArtistItem -> {
                                        navController.navigate("artist/${item.id}")
                                    }

                                    is PlaylistItem -> {
                                        navController.navigate("online_playlist/${item.id}")
                                    }
                                }
                            },
                            onLongClick = longClick,
                        ),
            )
            if (showDivider) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp)
                            .height(0.5.dp)
                            .background(Color.White.copy(alpha = 0.28f)),
                )
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        val tabs = remember {
            listOf(
                null to "ALL",
                FILTER_SONG to "SONGS",
                FILTER_ALBUM to "ALBUMS",
                FILTER_VIDEO to "VIDEOS",
                FILTER_ARTIST to "ARTISTS",
                FILTER_FEATURED_PLAYLIST to "PLAYLISTS",
            )
        }
        val selectedTabIndex = tabs.indexOfFirst {
            it.first == searchFilter || (it.first == FILTER_FEATURED_PLAYLIST && searchFilter == FILTER_COMMUNITY_PLAYLIST)
        }.coerceAtLeast(0)

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = Color.White,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFFFA2D48),
                        height = 2.5.dp,
                    )
                }
            },
            divider = {
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = Color.White.copy(alpha = 0.12f),
                )
            },
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top).add(WindowInsets(top = AppBarHeight)))
                    .fillMaxWidth(),
        ) {
            tabs.forEachIndexed { index, (filter, label) ->
                val selected = selectedTabIndex == index
                Tab(
                    selected = selected,
                    onClick = {
                        if (viewModel.filter.value != filter) {
                            viewModel.filter.value = filter
                        }
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    },
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
                            ),
                            color = if (selected) Color(0xFFFA2D48) else Color.White.copy(alpha = 0.6f),
                        )
                    },
                )
            }
        }

        LazyColumn(
            state = lazyListState,
            contentPadding =
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                    .add(WindowInsets(top = 8.dp))
                    .asPaddingValues(),
            modifier = Modifier.weight(1f),
        ) {
            if (searchFilter == null) {
                allModeSections.forEachIndexed { shelfIndex, section ->
                    if (shelfIndex > 0) {
                        item(key = "divider_$shelfIndex", contentType = "divider") {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                                thickness = 0.5.dp,
                                color = Color.White.copy(alpha = 0.28f),
                            )
                        }
                    }

                    item(
                        key = "section_header_${shelfIndex}_${section.title}",
                        contentType = "section_header",
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .width(3.dp)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = if (section.title.equals("Top result", ignoreCase = true)) {
                                    stringResource(R.string.top_result)
                                } else {
                                    section.title
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    val sortedItems = viewModel.sortedItems(section.items, searchSort)
                    itemsIndexed(
                        items = sortedItems,
                        key = { itemIndex, item ->
                            if (section.isTopResult) {
                                "top_result_${item.id}_$itemIndex"
                            } else {
                                "search_shelf_${shelfIndex}_${section.title}_${item.id}_$itemIndex"
                            }
                        },
                        contentType = { _, _ -> "search_result" },
                    ) { itemIndex, item ->
                        ytItemContent(item, itemIndex < sortedItems.lastIndex)
                    }

                    item(
                        key = "section_spacer_${shelfIndex}_${section.title}",
                        contentType = "section_spacer",
                    ) {
                        Spacer(Modifier.height(4.dp))
                    }
                }

                if (allModeSections.isEmpty() && isAllModeLoaded) {
                    item(key = "empty_all", contentType = "empty") {
                        EmptyPlaceholder(
                            icon = R.drawable.search,
                            text = stringResource(R.string.no_results_found),
                        )
                    }
                }
            } else {
                val sortedFilteredItems = viewModel.sortedItems(itemsPage?.items.orEmpty().distinctBy { it.id }, searchSort)
                itemsIndexed(
                    items = sortedFilteredItems,
                    key = { index, item -> "filtered_${item.id}_$index" },
                    contentType = { _, _ -> "search_result" },
                ) { index, item ->
                    ytItemContent(item, index < sortedFilteredItems.lastIndex)
                }

                if (itemsPage?.continuation != null) {
                    item(key = "loading", contentType = "loading") {
                        ShimmerHost {
                            repeat(3) {
                                ListItemPlaceHolder()
                            }
                        }
                    }
                }

                if (itemsPage?.items?.isEmpty() == true) {
                    item(key = "empty_filtered", contentType = "empty") {
                        EmptyPlaceholder(
                            icon = R.drawable.search,
                            text = stringResource(R.string.no_results_found),
                        )
                    }
                }
            }

            if (searchFilter == null && allModeSections.isEmpty() && !isAllModeLoaded || searchFilter != null && itemsPage == null) {
                item(key = "initial_loading", contentType = "loading") {
                    ShimmerHost {
                        repeat(8) {
                            ListItemPlaceHolder()
                        }
                    }
                }
            }
        }
    }
}

private data class SearchResultSection(
    val isTopResult: Boolean,
    val title: String,
    val items: List<YTItem>,
)
