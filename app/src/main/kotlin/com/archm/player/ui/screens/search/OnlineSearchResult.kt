/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.ui.screens.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.archm.player.LocalDatabase
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.constants.DarkModeKey
import com.archm.player.constants.PureBlackKey
import com.archm.player.db.entities.ArtistEntity
import com.archm.player.db.entities.PlaylistEntity
import com.archm.player.extensions.togglePlayPause
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.ui.component.EmptyPlaceholder
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.YouTubeListItem
import com.archm.player.ui.component.shimmer.ListItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.ui.screens.settings.DarkMode
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.utils.rememberPreference
import com.archm.player.utils.reportException
import com.archm.player.viewmodels.OnlineSearchSort
import com.archm.player.viewmodels.OnlineSearchViewModel
import com.music.innertube.YouTube
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnlineSearchResult(
    navController: NavController,
    searchSort: OnlineSearchSort = OnlineSearchSort.DEFAULT,
    pureBlack: Boolean = false,
    viewModel: OnlineSearchViewModel = hiltViewModel(),
) {
    val pureBlackPref by rememberPreference(PureBlackKey, defaultValue = false)
    val darkTheme by rememberEnumPreference<DarkMode>(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }
    val effectivePureBlack = pureBlack || (pureBlackPref && useDarkTheme)
    val backgroundColor = if (effectivePureBlack) Color.Black else MaterialTheme.colorScheme.background

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

    // High-confidence Hero Artist Match candidate
    val firstSummaryItem = searchSummary?.summaries?.firstOrNull()?.items?.firstOrNull()
    val topArtistCandidate = (firstSummaryItem as? ArtistItem)
        ?: (viewModel.viewStateMap[FILTER_ARTIST.value]?.items?.firstOrNull() as? ArtistItem)
    val heroArtist = remember(topArtistCandidate, viewModel.query, firstSummaryItem) {
        if (topArtistCandidate != null) {
            val q = viewModel.query.trim().lowercase()
            val artistTitle = topArtistCandidate.title.trim().lowercase()
            if (firstSummaryItem is ArtistItem || (q.isNotBlank() && (artistTitle.contains(q) || q.contains(artistTitle)))) {
                topArtistCandidate
            } else null
        } else null
    }

    // Top Songs strictly capped to 4 items on "All" tab
    val songsFromSummary = searchSummary?.summaries?.find { it.title.equals("Songs", ignoreCase = true) }?.items?.filterIsInstance<SongItem>()
    val songsFromFilter = viewModel.viewStateMap[FILTER_SONG.value]?.items?.filterIsInstance<SongItem>()
    val allTabSongs = remember(songsFromSummary, songsFromFilter, searchSort, firstSummaryItem) {
        val items = (songsFromFilter ?: songsFromSummary ?: emptyList())
        val withTopSong = if (firstSummaryItem is SongItem && items.none { it.id == firstSummaryItem.id }) {
            listOf(firstSummaryItem) + items
        } else items
        viewModel.sortedItems(withTopSong.distinctBy { it.id }, searchSort).filterIsInstance<SongItem>().take(4)
    }

    // Albums for horizontal carousel on "All" tab
    val albumsFromSummary = searchSummary?.summaries?.find { it.title.contains("Album", ignoreCase = true) }?.items?.filterIsInstance<AlbumItem>()
    val albumsFromFilter = viewModel.viewStateMap[FILTER_ALBUM.value]?.items?.filterIsInstance<AlbumItem>()
    val allTabAlbums = remember(albumsFromSummary, albumsFromFilter, firstSummaryItem) {
        val items = (albumsFromFilter ?: albumsFromSummary ?: emptyList())
        val withTopAlbum = if (firstSummaryItem is AlbumItem && items.none { it.id == firstSummaryItem.id }) {
            listOf(firstSummaryItem) + items
        } else items
        withTopAlbum.distinctBy { it.id }
    }

    // Videos for horizontal carousel on "All" tab
    val videosFromSummary = searchSummary?.summaries?.find { it.title.contains("Video", ignoreCase = true) }?.items?.filterIsInstance<SongItem>()
    val videosFromFilter = viewModel.viewStateMap[FILTER_VIDEO.value]?.items?.filterIsInstance<SongItem>()
    val allTabVideos = remember(videosFromSummary, videosFromFilter) {
        (videosFromFilter ?: videosFromSummary ?: emptyList()).distinctBy { it.id }
    }

    // Playlists for horizontal carousel on "All" tab
    val playlistsFromSummary = searchSummary?.summaries?.find { it.title.contains("Playlist", ignoreCase = true) }?.items?.filterIsInstance<PlaylistItem>()
    val playlistsFromFeatured = viewModel.viewStateMap[FILTER_FEATURED_PLAYLIST.value]?.items?.filterIsInstance<PlaylistItem>()
    val playlistsFromCommunity = viewModel.viewStateMap[FILTER_COMMUNITY_PLAYLIST.value]?.items?.filterIsInstance<PlaylistItem>()
    val allTabPlaylists = remember(playlistsFromSummary, playlistsFromFeatured, playlistsFromCommunity, firstSummaryItem) {
        val items = (playlistsFromFeatured.orEmpty() + playlistsFromCommunity.orEmpty()).ifEmpty {
            playlistsFromSummary.orEmpty()
        }
        val withTopPlaylist = if (firstSummaryItem is PlaylistItem && items.none { it.id == firstSummaryItem.id }) {
            listOf(firstSummaryItem) + items
        } else items
        withTopPlaylist.distinctBy { it.id }
    }

    val hasAnyAllContent = heroArtist != null || allTabSongs.isNotEmpty() || allTabAlbums.isNotEmpty() || allTabVideos.isNotEmpty() || allTabPlaylists.isNotEmpty()

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

    LaunchedEffect(viewModel.query) {
        lazyListState.scrollToItem(0)
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" }
        }.collect { shouldLoadMore ->
            if (!shouldLoadMore) return@collect
            viewModel.loadMore()
        }
    }

    val ytItemContent: @Composable LazyItemScope.(YTItem, Boolean, Boolean) -> Unit = { item: YTItem, _, isVideo: Boolean ->
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
                    .padding(vertical = 4.dp)
                    .animateItem(),
        ) {
            YouTubeListItem(
                item = item,
                isVideo = isVideo,
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
                    when (item) {
                        is SongItem -> {
                            SongTrailingActions(
                                song = item,
                                onMenuClick = longClick,
                            )
                        }

                        is AlbumItem -> {
                            AlbumTrailingAction(album = item)
                        }

                        is PlaylistItem -> {
                            PlaylistTrailingAction(playlist = item)
                        }

                        is ArtistItem -> {
                            ArtistTrailingAction(artist = item)
                        }
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
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor),
    ) {
        val tabs = listOf(
            null to stringResource(R.string.filter_all),
            FILTER_SONG to stringResource(R.string.filter_songs),
            FILTER_VIDEO to stringResource(R.string.filter_videos),
            FILTER_ARTIST to stringResource(R.string.filter_artists),
            FILTER_ALBUM to stringResource(R.string.filter_albums),
            FILTER_FEATURED_PLAYLIST to stringResource(R.string.filter_featured_playlists),
            FILTER_COMMUNITY_PLAYLIST to stringResource(R.string.filter_community_playlists),
        )
        val selectedTabIndex = tabs.indexOfFirst {
            it.first == searchFilter
        }.coerceAtLeast(0)

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = backgroundColor,
            contentColor = Color.White,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .wrapContentWidth()
                            .padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        height = 2.5.dp,
                    )
                }
            },
            divider = {},
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars.add(WindowInsets(top = 58.dp)))
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
                            color = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
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
                    .add(WindowInsets(top = 12.dp))
                    .asPaddingValues(),
            modifier = Modifier.weight(1f),
        ) {
            if (searchFilter == null) {
                // 1. Hero Artist Row/Card
                if (heroArtist != null) {
                    item(key = "hero_artist_${heroArtist.id}", contentType = "hero_artist") {
                        HeroArtistCard(
                            artist = heroArtist,
                            onClick = { navController.navigate("artist/${heroArtist.id}") },
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }

                // 2. Songs Section (Capped at 4 items)
                if (allTabSongs.isNotEmpty()) {
                    item(key = "all_songs_header", contentType = "section_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.filter_songs),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            TextButton(
                                onClick = {
                                    viewModel.filter.value = FILTER_SONG
                                    coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                                },
                            ) {
                                Text(
                                    text = stringResource(R.string.see_all),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    itemsIndexed(
                        items = allTabSongs,
                        key = { index, item -> "all_song_${item.id}_$index" },
                        contentType = { _, _ -> "search_result" },
                    ) { _, item ->
                        ytItemContent(item, false, false)
                    }
                }

                // 3. Albums Shelf (Horizontal Carousel)
                if (allTabAlbums.isNotEmpty()) {
                    item(key = "all_albums_header", contentType = "section_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.filter_albums),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            TextButton(
                                onClick = {
                                    viewModel.filter.value = FILTER_ALBUM
                                    coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                                },
                            ) {
                                Text(
                                    text = stringResource(R.string.see_all),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    item(key = "all_albums_shelf", contentType = "horizontal_shelf") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(allTabAlbums, key = { "album_shelf_${it.id}" }) { album ->
                                AlbumShelfCard(
                                    album = album,
                                    onClick = { navController.navigate("album/${album.id}") },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubeAlbumMenu(
                                                albumItem = album,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                // 4. Videos Shelf (Horizontal Carousel with 16:9 Widescreen Cards)
                if (allTabVideos.isNotEmpty()) {
                    item(key = "all_videos_header", contentType = "section_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.filter_videos),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            TextButton(
                                onClick = {
                                    viewModel.filter.value = FILTER_VIDEO
                                    coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                                },
                            ) {
                                Text(
                                    text = stringResource(R.string.see_all),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    item(key = "all_videos_shelf", contentType = "horizontal_shelf") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(allTabVideos, key = { "video_shelf_${it.id}" }) { video ->
                                VideoShelfCard(
                                    video = video,
                                    onClick = {
                                        if (video.id == mediaMetadata?.id) {
                                            playerConnection.player.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                YouTubeQueue(
                                                    WatchEndpoint(videoId = video.id),
                                                    video.toMediaMetadata(),
                                                ),
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubeSongMenu(
                                                song = video,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                // 5. Playlists Shelf (Horizontal Carousel)
                if (allTabPlaylists.isNotEmpty()) {
                    item(key = "all_playlists_header", contentType = "section_header") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(R.string.filter_featured_playlists),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            TextButton(
                                onClick = {
                                    viewModel.filter.value = FILTER_FEATURED_PLAYLIST
                                    coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                                },
                            ) {
                                Text(
                                    text = stringResource(R.string.see_all),
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    item(key = "all_playlists_shelf", contentType = "horizontal_shelf") {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            items(allTabPlaylists, key = { "playlist_shelf_${it.id}" }) { playlist ->
                                PlaylistShelfCard(
                                    playlist = playlist,
                                    onClick = { navController.navigate("online_playlist/${playlist.id}") },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubePlaylistMenu(
                                                playlist = playlist,
                                                coroutineScope = coroutineScope,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }

                item(key = "all_bottom_spacer", contentType = "bottom_spacer") {
                    Spacer(Modifier.height(16.dp))
                }

                if (!hasAnyAllContent && isAllModeLoaded) {
                    item(key = "empty_all", contentType = "empty") {
                        EmptyPlaceholder(
                            icon = R.drawable.search,
                            text = stringResource(R.string.no_results_found),
                        )
                    }
                }
            } else {
                // Dedicated Tabs (Songs, Albums, Artists, Videos, Playlists)
                val sortedFilteredItems = viewModel.sortedItems(itemsPage?.items.orEmpty().distinctBy { it.id }, searchSort)
                val isFilterVideo = searchFilter == FILTER_VIDEO
                itemsIndexed(
                    items = sortedFilteredItems,
                    key = { index, item -> "filtered_${item.id}_$index" },
                    contentType = { _, _ -> "search_result" },
                ) { _, item ->
                    ytItemContent(item, false, isFilterVideo)
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

            if (searchFilter == null && !hasAnyAllContent && !isAllModeLoaded || searchFilter != null && itemsPage == null) {
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

@Composable
private fun HeroArtistCard(
    artist: ArtistItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        // Circular avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(40, 40, 40)),
            contentAlignment = Alignment.Center,
        ) {
            if (!artist.thumbnail.isNullOrBlank()) {
                AsyncImage(
                    model = artist.thumbnail,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            } else {
                Icon(
                    painter = painterResource(R.drawable.person),
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        Spacer(Modifier.width(14.dp))

        // Name + "Artist" subtitle
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = artist.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (artist.isVerified) {
                    Icon(
                        painter = painterResource(R.drawable.check),
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
            Text(
                text = stringResource(R.string.artist_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.width(8.dp))

        ArtistTrailingAction(artist = artist)
    }
}

@Composable
internal fun SongTrailingActions(
    song: SongItem,
    onMenuClick: () -> Unit,
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val dbSong by database.song(song.id).collectAsState(initial = null)
    val isInLibrary = dbSong?.song?.inLibrary != null

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = {
                coroutineScope.launch(Dispatchers.IO) {
                    val existing = database.song(song.id).firstOrNull()
                    if (existing != null) {
                        database.update(existing.song.toggleLibrary())
                    } else {
                        database.transaction {
                            insert(song.toMediaMetadata()) { it.toggleLibrary() }
                        }
                    }
                }
            },
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                painter = painterResource(if (isInLibrary) R.drawable.check_circle else R.drawable.add_circle),
                contentDescription = stringResource(if (isInLibrary) R.string.remove_from_library else R.string.add_to_library),
                tint = if (isInLibrary) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(22.dp),
            )
        }
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.more_vert),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
internal fun AlbumTrailingAction(
    album: AlbumItem,
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val dbAlbum by database.album(album.id).collectAsState(initial = null)
    val isSaved = dbAlbum?.album?.bookmarkedAt != null

    IconButton(
        onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                val existing = database.album(album.id).firstOrNull()
                if (existing != null) {
                    database.update(existing.album.toggleLike())
                } else {
                    YouTube.album(album.id)
                        .onSuccess { albumPage ->
                            database.transaction { insert(albumPage) }
                        }.onFailure { reportException(it) }
                    // After the network call: read+toggle in the coroutine body (suspend ok here)
                    val inserted = database.album(album.id).firstOrNull()
                    inserted?.album?.toggleLike()?.let { database.update(it) }
                }
            }
        },
        modifier = Modifier.size(36.dp),
    ) {
        Icon(
            painter = painterResource(if (isSaved) R.drawable.check_circle else R.drawable.add_circle),
            contentDescription = stringResource(if (isSaved) R.string.remove_from_library else R.string.add_to_library),
            tint = if (isSaved) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
internal fun PlaylistTrailingAction(
    playlist: PlaylistItem,
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val dbPlaylist by database.playlist(playlist.id).collectAsState(initial = null)
    val isSaved = dbPlaylist?.playlist?.bookmarkedAt != null

    IconButton(
        onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                val existing = database.playlist(playlist.id).firstOrNull()
                if (existing != null) {
                    database.update(existing.playlist.toggleLike())
                } else {
                    YouTube.playlist(playlist.id).onSuccess { _ ->
                        database.transaction {
                            insert(
                                PlaylistEntity(
                                    name = playlist.title,
                                    browseId = playlist.id,
                                    thumbnailUrl = playlist.thumbnail,
                                    isEditable = playlist.isEditable,
                                    remoteSongCount = playlist.songCountText?.let {
                                        Regex("""\d+""").find(it)?.value?.toIntOrNull()
                                    },
                                    playEndpointParams = playlist.playEndpoint?.params,
                                    shuffleEndpointParams = playlist.shuffleEndpoint?.params,
                                    radioEndpointParams = playlist.radioEndpoint?.params,
                                ).toggleLike()
                            )
                        }
                    }.onFailure {
                        reportException(it)
                    }
                }
            }
        },
        modifier = Modifier.size(36.dp),
    ) {
        Icon(
            painter = painterResource(if (isSaved) R.drawable.check_circle else R.drawable.add_circle),
            contentDescription = stringResource(if (isSaved) R.string.remove_from_library else R.string.add_to_library),
            tint = if (isSaved) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
internal fun ArtistTrailingAction(
    artist: ArtistItem,
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val dbArtist by database.artist(artist.id).collectAsState(initial = null)
    val isFollowed = dbArtist?.artist?.bookmarkedAt != null

    Surface(
        onClick = {
            coroutineScope.launch(Dispatchers.IO) {
                val existing = database.artist(artist.id).firstOrNull()
                database.query {
                    if (existing != null) {
                        update(existing.artist.toggleLike())
                    } else {
                        insert(
                            ArtistEntity(
                                id = artist.id,
                                name = artist.title,
                                channelId = artist.channelId,
                                thumbnailUrl = artist.thumbnail,
                            ).toggleLike()
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(50),
        color = if (isFollowed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
        border = if (isFollowed) BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null,
        modifier = Modifier.padding(end = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
        ) {
            if (isFollowed) {
                Icon(
                    painter = painterResource(R.drawable.check),
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = Color.White.copy(alpha = 0.9f),
                )
            }
            Text(
                text = if (isFollowed) stringResource(R.string.following) else stringResource(R.string.follow),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isFollowed) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onPrimary,
            )
        }
    }
}

@Composable
private fun AlbumShelfCard(
    album: AlbumItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(135.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(30, 30, 30)),
        ) {
            AsyncImage(
                model = album.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape),
            ) {
                AlbumTrailingAction(album = album)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val releaseLabel = album.explicitType?.takeIf { it.isNotBlank() } ?: "Album"
        val subtitle = listOfNotNull(releaseLabel, album.year?.toString()).joinToString(" • ")
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlaylistShelfCard(
    playlist: PlaylistItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(135.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(30, 30, 30)),
        ) {
            AsyncImage(
                model = playlist.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape),
            ) {
                PlaylistTrailingAction(playlist = playlist)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = playlist.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        val subtitle = playlist.author?.name ?: playlist.songCountText
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun VideoShelfCard(
    video: SongItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(200.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(112.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(30, 30, 30)),
        ) {
            AsyncImage(
                model = video.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            val durationText = video.durationText
            if (!durationText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp),
                ) {
                    Text(
                        text = durationText,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = video.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val subtitle = listOfNotNull(video.artists.joinToString { it.name }.takeIf { it.isNotBlank() }, video.viewCountText).joinToString(" • ")
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
