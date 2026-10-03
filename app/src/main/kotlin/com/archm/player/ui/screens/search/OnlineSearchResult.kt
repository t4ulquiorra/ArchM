/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.ui.screens.search

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.archm.player.LocalSyncUtils
import com.archm.player.R
import com.archm.player.constants.DarkModeKey
import com.archm.player.constants.ListThumbnailSize
import com.archm.player.constants.PureBlackKey
import com.archm.player.constants.ThumbnailCornerRadius
import com.archm.player.db.entities.ArtistEntity
import com.archm.player.db.entities.PlaylistEntity
import com.archm.player.db.entities.SongEntity
import com.archm.player.extensions.togglePlayPause
import com.archm.player.models.toMediaMetadata
import com.archm.player.models.toSongEntity
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.ui.component.EmptyPlaceholder
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.PlayingIndicator
import com.archm.player.ui.component.RowMoreMenuButton
import com.archm.player.ui.component.RowStarButton
import com.archm.player.ui.component.YouTubeListItem
import com.archm.player.ui.component.durationText
import com.archm.player.ui.component.formatReleaseSubtitle
import com.archm.player.ui.component.formattedDuration
import com.archm.player.ui.component.shimmer.ListItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.menu.LocalSavedInSheetState
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.ui.screens.artist.OutlinedFollowPillButton
import com.archm.player.ui.screens.settings.DarkMode
import com.archm.player.ui.theme.LocalAccentColor
import com.archm.player.ui.theme.Marble
import com.archm.player.utils.joinByBullet
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
import kotlinx.coroutines.delay
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
    val currentMediaId = mediaMetadata?.id ?: playerConnection.player.currentMediaItem?.mediaId

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = remember(viewModel.query) { LazyListState(0, 0) }

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

    // Top Results (Songs & Videos) strictly capped to 4 items on "All" tab
    val songsFromSummary = searchSummary?.summaries?.find { it.title.equals("Songs", ignoreCase = true) }?.items?.filterIsInstance<SongItem>()
    val videosFromSummary = searchSummary?.summaries?.find { it.title.contains("Video", ignoreCase = true) }?.items?.filterIsInstance<SongItem>()
    val songsFromFilter = viewModel.viewStateMap[FILTER_SONG.value]?.items?.filterIsInstance<SongItem>()
    val allTabResults = remember(songsFromSummary, videosFromSummary, songsFromFilter, searchSort, firstSummaryItem) {
        val baseItems = songsFromFilter ?: run {
            val s = songsFromSummary.orEmpty()
            if (s.isEmpty()) videosFromSummary.orEmpty() else s
        }
        val withTopItem = if (firstSummaryItem is SongItem && baseItems.none { it.id == firstSummaryItem.id }) {
            listOf(firstSummaryItem) + baseItems
        } else baseItems
        viewModel.sortedItems(withTopItem.distinctBy { it.id }, searchSort).filterIsInstance<SongItem>().take(4)
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

    val hasAnyAllContent = heroArtist != null || allTabResults.isNotEmpty() || allTabAlbums.isNotEmpty() || allTabVideos.isNotEmpty() || allTabPlaylists.isNotEmpty()

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

    val isPrimaryResolved =
        searchSummary != null ||
            (viewModel.viewStateMap.containsKey(FILTER_SONG.value) && viewModel.viewStateMap.containsKey(FILTER_ARTIST.value)) ||
            isAllModeLoaded

    // Scroll to top the moment primary results first resolve
    LaunchedEffect(isPrimaryResolved) {
        if (isPrimaryResolved) {
            lazyListState.scrollToItem(0)
        }
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
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .animateItem(),
        ) {
            when (item) {
                is SongItem -> {
                    val isItemVideo = isVideo || item.isVideoSong
                    SearchSongListItem(
                        song = item,
                        isVideo = isItemVideo,
                        isActive = item.id == currentMediaId,
                        isPlaying = isPlaying,
                        onClick = {
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
                        },
                        onLongClick = longClick,
                    )
                }

                is ArtistItem -> {
                    HeroArtistCard(
                        artist = item,
                        onClick = { navController.navigate("artist/${item.id}") },
                    )
                }

                is AlbumItem -> {
                    SearchCollectionListItem(
                        title = item.title,
                        subtitle = joinByBullet(item.artists?.joinToString { it.name }, formatReleaseSubtitle(item)),
                        thumbnailUrl = item.thumbnail,
                        isActive = item.id == mediaMetadata?.album?.id,
                        isPlaying = isPlaying,
                        onClick = { navController.navigate("album/${item.id}") },
                        onLongClick = longClick,
                        trailingContent = {
                            AlbumTrailingAction(
                                album = item,
                                onMenuClick = longClick,
                            )
                        },
                    )
                }

                is PlaylistItem -> {
                    SearchCollectionListItem(
                        title = item.title,
                        subtitle = joinByBullet(item.author?.name, item.songCountText),
                        thumbnailUrl = item.thumbnail,
                        isActive = false,
                        isPlaying = false,
                        onClick = { navController.navigate("online_playlist/${item.id}") },
                        onLongClick = longClick,
                        trailingContent = {
                            PlaylistTrailingAction(
                                playlist = item,
                                onMenuClick = longClick,
                            )
                        },
                    )
                }

                else -> {}
            }
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
        val tabListState = rememberLazyListState()
        LaunchedEffect(selectedTabIndex) {
            tabListState.animateScrollToItem(selectedTabIndex)
        }

        LazyRow(
            state = tabListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars.add(WindowInsets(top = 58.dp)))
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
        ) {
            itemsIndexed(tabs) { index, (filter, label) ->
                val selected = selectedTabIndex == index
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (selected) Marble else Color(0xFF262626),
                    contentColor = if (selected) Color.Black else Color.White,
                    modifier = Modifier.clickable {
                        if (viewModel.filter.value != filter) {
                            viewModel.filter.value = filter
                        }
                        coroutineScope.launch {
                            lazyListState.animateScrollToItem(0)
                        }
                    },
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
        }

        LazyColumn(
            state = lazyListState,
            contentPadding =
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                    .add(WindowInsets(top = 13.2.dp))
                    .asPaddingValues(),
            modifier = Modifier.weight(1f),
        ) {
            if (searchFilter == null) {
                if (!isPrimaryResolved) {
                    item(key = "all_primary_placeholder", contentType = "loading") {
                        ShimmerHost {
                            repeat(8) {
                                ListItemPlaceHolder()
                            }
                        }
                    }
                } else {
                    // 1. Hero Artist Row/Card
                    if (heroArtist != null) {
                        item(key = "hero_artist_${heroArtist.id}", contentType = "hero_artist") {
                            HeroArtistCard(
                                artist = heroArtist,
                                onClick = { navController.navigate("artist/${heroArtist.id}") },
                                modifier = Modifier.padding(bottom = 13.2.dp),
                            )
                        }
                    }

                    // 2. Results Section (Capped at 4 items)
                    if (allTabResults.isNotEmpty()) {
                        item(key = "all_results_header", contentType = "section_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        viewModel.filter.value = FILTER_SONG
                                        coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                                    }
                                    .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.results),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(R.drawable.navigate_next),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        itemsIndexed(
                            items = allTabResults,
                            key = { index, item -> "all_result_${item.id}_$index" },
                            contentType = { _, _ -> "search_result" },
                        ) { _, item ->
                            val isItemVideo = item.isVideoSong
                            ytItemContent(item, false, isItemVideo)
                        }
                    }

                    // 3. Albums Shelf (Horizontal Carousel)
                    if (allTabAlbums.isNotEmpty()) {
                        item(key = "all_albums_header", contentType = "section_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        viewModel.filter.value = FILTER_ALBUM
                                        coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                                    }
                                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.filter_albums),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(R.drawable.navigate_next),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
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
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
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
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        viewModel.filter.value = FILTER_VIDEO
                                        coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                                    }
                                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.filter_videos),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(R.drawable.navigate_next),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
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
                                        isActive = video.id == currentMediaId,
                                        isPlaying = isPlaying,
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
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                    ) {
                                        viewModel.filter.value = FILTER_FEATURED_PLAYLIST
                                        coroutineScope.launch { lazyListState.animateScrollToItem(0) }
                                    }
                                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.filter_featured_playlists),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    painter = painterResource(R.drawable.navigate_next),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
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

            if (searchFilter != null && itemsPage == null) {
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
private fun SearchSongListItem(
    song: SongItem,
    isVideo: Boolean,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isHeld by remember { mutableStateOf(false) }
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(400)
            isHeld = true
        } else {
            isHeld = false
        }
    }
    val isActivelyPlaying = isActive && isPlaying

    val thumbAlpha by animateFloatAsState(
        targetValue = when {
            isHeld -> 0.5f
            isPressed -> 0.7f
            else -> 1.0f
        },
        animationSpec = tween(150),
        label = "song_thumb_alpha",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(start = 16.dp, end = 0.dp, top = 8.dp, bottom = 8.dp),
    ) {
        val thumbModifier = if (isVideo) {
            Modifier.height(ListThumbnailSize).aspectRatio(16f / 9f)
        } else {
            Modifier.size(ListThumbnailSize)
        }

        Box(
            modifier = thumbModifier
                .clip(RoundedCornerShape(ThumbnailCornerRadius))
                .background(Color(30, 30, 30)),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = song.thumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = thumbAlpha },
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isActive) {
                    PlayingIndicator(
                        color = LocalAccentColor.current,
                        modifier = Modifier.height(15.dp),
                        isPlaying = isPlaying,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            val subtitle = listOfNotNull(
                song.artists.joinToString { it.name }.takeIf { it.isNotBlank() },
                song.durationText ?: song.formattedDuration()
            ).joinToString(" • ")
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        SongTrailingActions(
            song = song,
            onMenuClick = onLongClick,
        )
    }
}

@Composable
private fun HeroArtistCard(
    artist: ArtistItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val avatarAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1.0f,
        animationSpec = tween(150),
        label = "hero_avatar_alpha",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(start = 16.dp, end = 0.dp, top = 8.dp, bottom = 8.dp),
    ) {
        // Circular avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer { alpha = avatarAlpha }
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
                        painter = painterResource(R.drawable.ic_verified_badge),
                        contentDescription = "Verified",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp),
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
private fun SearchCollectionListItem(
    title: String,
    subtitle: String?,
    thumbnailUrl: String?,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    trailingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isHeld by remember { mutableStateOf(false) }
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(400)
            isHeld = true
        } else {
            isHeld = false
        }
    }
    val isActivelyPlaying = isActive && isPlaying

    val thumbAlpha by animateFloatAsState(
        targetValue = when {
            isHeld -> 0.5f
            isPressed -> 0.7f
            else -> 1.0f
        },
        animationSpec = tween(150),
        label = "coll_thumb_alpha",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(start = 16.dp, end = 0.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(ListThumbnailSize)
                .clip(RoundedCornerShape(ThumbnailCornerRadius))
                .background(Color(30, 30, 30)),
            contentAlignment = Alignment.Center,
        ) {
            if (!thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = thumbAlpha },
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isActive) {
                    PlayingIndicator(
                        color = LocalAccentColor.current,
                        modifier = Modifier.height(15.dp),
                        isPlaying = isPlaying,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        trailingContent()
    }
}

@Composable
internal fun SongTrailingActions(
    song: SongItem,
    onMenuClick: () -> Unit,
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val syncUtils = LocalSyncUtils.current
    val savedInSheetState = LocalSavedInSheetState.current
    val dbSong by database.song(song.id).collectAsState(initial = null)
    val isLiked = dbSong?.song?.liked == true

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowStarButton(
            isLiked = isLiked,
            contentDescription = if (isLiked) stringResource(R.string.liked) else stringResource(R.string.like),
            likedTint = MaterialTheme.colorScheme.primary,
            onClick = {
                if (isLiked) {
                    savedInSheetState.show(song.toMediaMetadata())
                } else {
                    coroutineScope.launch(Dispatchers.IO) {
                        val existing = database.song(song.id).firstOrNull()
                        val s: SongEntity
                        if (existing != null) {
                            s = existing.song.toggleLike()
                            database.update(s)
                        } else {
                            database.transaction {
                                insert(song.toMediaMetadata(), SongEntity::toggleLike)
                            }
                            s = song.toMediaMetadata().toSongEntity().let(SongEntity::toggleLike)
                        }
                        syncUtils.likeSong(s)
                    }
                }
            },
        )
        RowMoreMenuButton(
            onClick = onMenuClick,
        )
    }
}

@Composable
internal fun AlbumTrailingAction(
    album: AlbumItem,
    onMenuClick: (() -> Unit)? = null,
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val dbAlbum by database.album(album.id).collectAsState(initial = null)
    val isSaved = dbAlbum?.album?.bookmarkedAt != null

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowStarButton(
            isLiked = isSaved,
            contentDescription = stringResource(if (isSaved) R.string.remove_from_library else R.string.add_to_library),
            likedTint = MaterialTheme.colorScheme.primary,
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
        )
        if (onMenuClick != null) {
            RowMoreMenuButton(
                onClick = onMenuClick,
            )
        } else {
            Spacer(Modifier.width(16.dp))
        }
    }
}

@Composable
internal fun PlaylistTrailingAction(
    playlist: PlaylistItem,
    onMenuClick: (() -> Unit)? = null,
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val dbPlaylist by database.playlist(playlist.id).collectAsState(initial = null)
    val isSaved = dbPlaylist?.playlist?.bookmarkedAt != null

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RowStarButton(
            isLiked = isSaved,
            contentDescription = stringResource(if (isSaved) R.string.remove_from_library else R.string.add_to_library),
            likedTint = MaterialTheme.colorScheme.primary,
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
        )
        if (onMenuClick != null) {
            RowMoreMenuButton(
                onClick = onMenuClick,
            )
        } else {
            Spacer(Modifier.width(16.dp))
        }
    }
}

@Composable
private fun AlbumArtworkBookmarkButton(
    album: AlbumItem,
    modifier: Modifier = Modifier,
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
                    val inserted = database.album(album.id).firstOrNull()
                    inserted?.album?.toggleLike()?.let { database.update(it) }
                }
            }
        },
        modifier = modifier.size(36.dp),
    ) {
        Icon(
            painter = painterResource(if (isSaved) R.drawable.star else R.drawable.star_border),
            contentDescription = stringResource(if (isSaved) R.string.remove_from_library else R.string.add_to_library),
            tint = if (isSaved) MaterialTheme.colorScheme.primary else Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun PlaylistArtworkBookmarkButton(
    playlist: PlaylistItem,
    modifier: Modifier = Modifier,
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
        modifier = modifier.size(36.dp),
    ) {
        Icon(
            painter = painterResource(if (isSaved) R.drawable.star else R.drawable.star_border),
            contentDescription = stringResource(if (isSaved) R.string.remove_from_library else R.string.add_to_library),
            tint = if (isSaved) MaterialTheme.colorScheme.primary else Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
internal fun ArtistTrailingAction(
    artist: ArtistItem,
    modifier: Modifier = Modifier,
) {
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    val dbArtist by database.artist(artist.id).collectAsState(initial = null)
    val isFollowed = dbArtist?.artist?.bookmarkedAt != null

    OutlinedFollowPillButton(
        isFollowed = isFollowed,
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
        modifier = modifier.padding(end = 16.dp),
    )
}

@Composable
private fun AlbumShelfCard(
    album: AlbumItem,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isActivelyPlaying = isActive && isPlaying

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "album_card_scale",
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1.0f,
        animationSpec = tween(150),
        label = "album_card_alpha",
    )

    val thumbAlpha by animateFloatAsState(
        targetValue = when {
            isActivelyPlaying -> 0.5f
            isPressed -> 0.7f
            else -> 1.0f
        },
        animationSpec = tween(150),
        label = "album_thumb_alpha",
    )

    Column(
        modifier = modifier
            .width(135.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
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
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = thumbAlpha },
            )

            if (isActivelyPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    PlayingIndicator(
                        color = LocalAccentColor.current,
                        modifier = Modifier.height(24.dp),
                        isPlaying = true,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape),
            ) {
                AlbumArtworkBookmarkButton(album = album)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = album.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
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
                modifier = Modifier.graphicsLayer { alpha = contentAlpha },
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "playlist_card_scale",
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1.0f,
        animationSpec = tween(150),
        label = "playlist_card_alpha",
    )

    Column(
        modifier = modifier
            .width(135.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
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
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = contentAlpha },
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape),
            ) {
                PlaylistArtworkBookmarkButton(playlist = playlist)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = playlist.title,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
        )
        val subtitle = playlist.author?.name ?: playlist.songCountText
        if (!subtitle.isNullOrBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer { alpha = contentAlpha },
            )
        }
    }
}

@Composable
private fun VideoShelfCard(
    video: SongItem,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isActivelyPlaying = isActive && isPlaying

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "video_card_scale",
    )

    val contentAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1.0f,
        animationSpec = tween(150),
        label = "video_card_alpha",
    )

    val thumbAlpha by animateFloatAsState(
        targetValue = when {
            isActivelyPlaying -> 0.5f
            isPressed -> 0.7f
            else -> 1.0f
        },
        animationSpec = tween(150),
        label = "video_thumb_alpha",
    )

    Column(
        modifier = modifier
            .width(200.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
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
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = thumbAlpha },
            )

            if (isActivelyPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) {
                    PlayingIndicator(
                        color = LocalAccentColor.current,
                        modifier = Modifier.height(24.dp),
                        isPlaying = true,
                    )
                }
            }

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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha },
        )
        val subtitle = listOfNotNull(video.artists.joinToString { it.name }.takeIf { it.isNotBlank() }, video.viewCountText).joinToString(" • ")
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.graphicsLayer { alpha = contentAlpha },
            )
        }
    }
}
