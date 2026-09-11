

/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.archm.player.ui.screens.artist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.archm.player.LocalDownloadUtil
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.playback.ExoDownloadService
import com.archm.player.R
import com.archm.player.constants.GridItemSize
import com.archm.player.constants.GridItemsSizeKey
import com.archm.player.constants.GridThumbnailHeight
import com.archm.player.extensions.toMediaItem
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.queues.ListQueue
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.ui.component.IconButton
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.YouTubeGridItem
import com.archm.player.ui.component.shimmer.GridItemPlaceHolder
import com.archm.player.ui.component.shimmer.ListItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.menu.YouTubeSelectionSongMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.db.entities.Artist
import com.archm.player.db.entities.ArtistEntity
import com.archm.player.ui.utils.backToMain
import com.archm.player.ui.utils.resize
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.viewmodels.ArtistItemsViewModel
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ArtistSongRow(
    song: SongItem,
    isPlaying: Boolean,
    selectionMode: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectToggle: () -> Unit,
    onAddToQueue: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val maxOffset = 360f
    val offsetX = remember { Animatable(initialValue = 0f) }
    var heightDp by remember { mutableStateOf(0.dp) }

    Box(modifier = modifier.fillMaxWidth()) {
        // Behind row: revealed Add to Queue icon
        Crossfade(
            targetState = offsetX.value >= maxOffset / 2,
            label = "addToQueueCrossfade",
        ) { shouldShow ->
            if (shouldShow) {
                Box(
                    modifier = Modifier
                        .height(heightDp)
                        .aspectRatio(1f)
                        .padding(start = 20.dp)
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.queue_music),
                        contentDescription = stringResource(R.string.add_to_queue),
                        tint = Color.White,
                    )
                }
            }
        }

        // Foreground row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else Color.Transparent,
                )
                .combinedClickable(
                    onClick = {
                        if (selectionMode) {
                            onSelectToggle()
                        } else {
                            onClick()
                        }
                    },
                    onLongClick = {
                        if (!selectionMode) {
                            onLongClick()
                        }
                    },
                )
                .pointerInput(selectionMode) {
                    if (!selectionMode) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { change, dragAmount ->
                                if (offsetX.value + dragAmount > 0) {
                                    change.consume()
                                    coroutineScope.launch {
                                        offsetX.snapTo((offsetX.value + dragAmount).coerceAtMost(maxOffset))
                                    }
                                }
                            },
                            onDragEnd = {
                                if (offsetX.value >= maxOffset * 0.75f) {
                                    onAddToQueue()
                                }
                                coroutineScope.launch {
                                    offsetX.animateTo(0f)
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    offsetX.animateTo(0f)
                                }
                            },
                        )
                    }
                }
                .onGloballyPositioned { coordinates ->
                    with(density) {
                        heightDp = coordinates.size.height.toDp()
                    }
                },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Checkbox when selectionMode is true
                AnimatedVisibility(
                    visible = selectionMode,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally(),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.6f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(R.drawable.done),
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                }

                // Artwork (48dp)
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AsyncImage(
                        model = song.thumbnail.resize(192, 192),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp)),
                    )
                }

                // Title & artist info
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 8.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(),
                    )
                    Text(
                        text = song.artists.joinToString(", ") { it.name },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        color = Color(0xC4FFFFFF),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically),
                    )
                }

                // More button
                IconButton(
                    onClick = onMoreClick,
                    onLongClick = {},
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistItemsOverflowMenu(
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onRadio: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val menuItems = buildList {
        if (onRadio != null) {
            add(Triple(stringResource(R.string.start_radio), R.drawable.radio, onRadio))
        }
        add(Triple(stringResource(R.string.share), R.drawable.share, onShare))
        add(Triple(stringResource(R.string.copy_link), R.drawable.copy, onCopyLink))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        menuItems.forEachIndexed { index, (text, iconRes, onClick) ->
            SegmentedListItem(
                onClick = onClick,
                shapes = ListItemDefaults.segmentedShapes(index = index, count = menuItems.size),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp),
                colors = ListItemDefaults.segmentedColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                leadingContent = {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                    )
                },
            ) {
                Text(text = text)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArtistItemsScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: ArtistItemsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

    val title by viewModel.title.collectAsState()
    val itemsPage by viewModel.itemsPage.collectAsState()
    val artistPage by viewModel.artistPage.collectAsState()
    val libraryArtist by viewModel.libraryArtist.collectAsState()
    val dbPlaylist by viewModel.dbPlaylist.collectAsState()
    val isBookmarked = dbPlaylist?.playlist?.bookmarkedAt != null

    val downloadUtil = LocalDownloadUtil.current
    var downloadState by remember { mutableIntStateOf(Download.STATE_STOPPED) }

    var showSearchBar by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val maxSelectionReachedString = stringResource(R.string.max_selection_reached, MAX_SONG_SELECTION)
    val selectionState = remember {
        SongSelectionState(
            limitMessage = maxSelectionReachedString,
            showToast = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
        )
    }

    BackHandler(enabled = selectionState.isActive || showSearchBar) {
        if (selectionState.isActive) {
            selectionState.exit()
        } else if (showSearchBar) {
            showSearchBar = false
            searchQuery = ""
        }
    }

    val firstItemVisible by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex == 0 }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.layoutInfo.visibleItemsInfo.any { it.key == "loading" }
        }.collect { shouldLoadMore ->
            if (!shouldLoadMore) return@collect
            viewModel.loadMore()
        }
    }

    LaunchedEffect(lazyGridState) {
        snapshotFlow {
            lazyGridState.layoutInfo.visibleItemsInfo.any { it.key == "loading" }
        }.collect { shouldLoadMore ->
            if (!shouldLoadMore) return@collect
            viewModel.loadMore()
        }
    }

    val isSongList = itemsPage?.items?.firstOrNull() is SongItem

    if (isSongList) {
        val distinctSongs = remember(itemsPage?.items) {
            itemsPage?.items.orEmpty().filterIsInstance<SongItem>().distinctBy { it.id }
        }
        val filteredSongs = remember(distinctSongs, searchQuery, showSearchBar) {
            if (searchQuery.isBlank() || !showSearchBar) {
                distinctSongs
            } else {
                distinctSongs.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artists.any { artist -> artist.name.contains(searchQuery, ignoreCase = true) }
                }
            }
        }

        LaunchedEffect(distinctSongs) {
            if (distinctSongs.isEmpty()) return@LaunchedEffect
            downloadUtil.downloads.collect { downloads ->
                downloadState =
                    if (distinctSongs.all { downloads[it.id]?.state == Download.STATE_COMPLETED }) {
                        Download.STATE_COMPLETED
                    } else if (distinctSongs.all {
                            downloads[it.id]?.state == Download.STATE_QUEUED ||
                                downloads[it.id]?.state == Download.STATE_DOWNLOADING ||
                                downloads[it.id]?.state == Download.STATE_COMPLETED
                        }) {
                        Download.STATE_DOWNLOADING
                    } else {
                        Download.STATE_STOPPED
                    }
            }
        }

        val unknownArtist = stringResource(R.string.unknown_artist)
        val fallbackArtistName = distinctSongs.firstOrNull()?.artists?.firstOrNull()?.name
        val artistName = artistPage?.artist?.title ?: libraryArtist?.artist?.name ?: fallbackArtistName
        val displayTitle = title.ifBlank { stringResource(R.string.top_songs) }
        val backdropThumbnail = dbPlaylist?.playlist?.thumbnailUrl
            ?: distinctSongs.firstOrNull()?.thumbnail
            ?: itemsPage?.items?.firstOrNull()?.thumbnail

        val isPlaylistPlaying = distinctSongs.any { it.id == mediaMetadata?.id }

        val showOverflowMenu: () -> Unit = {
            menuState.show {
                ArtistItemsOverflowMenu(
                    onShare = {
                        val artistId = viewModel.artistId ?: ""
                        val shareLink = artistPage?.artist?.shareLink ?: "https://music.youtube.com/channel/$artistId"
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareLink)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, null))
                        menuState.dismiss()
                    },
                    onCopyLink = {
                        val artistId = viewModel.artistId ?: ""
                        val shareLink = artistPage?.artist?.shareLink ?: "https://music.youtube.com/channel/$artistId"
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.copy_link), shareLink))
                        Toast.makeText(context, R.string.link_copied, Toast.LENGTH_SHORT).show()
                        menuState.dismiss()
                    },
                    onRadio = artistPage?.artist?.radioEndpoint?.let { endpoint ->
                        {
                            playerConnection.playQueue(YouTubeQueue(endpoint))
                            menuState.dismiss()
                        }
                    },
                )
            }
        }

        Box(Modifier.fillMaxSize()) {
            val configuration = LocalConfiguration.current
            val isPortrait = configuration.screenWidthDp < configuration.screenHeightDp

            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding(),
                ),
            ) {
                if (itemsPage == null) {
                    item(key = "shimmer") {
                        ShimmerHost(
                            modifier = Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current),
                        ) {
                            repeat(8) {
                                ListItemPlaceHolder()
                            }
                        }
                    }
                } else {
                    // Header Hero Section (hidden when searching)
                    if (!showSearchBar) {
                        item(key = "hero_header") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isPortrait) (configuration.screenHeightDp / 2).dp else 280.dp),
                            ) {
                                // Full-bleed hero image
                                AsyncImage(
                                    model = backdropThumbnail?.resize(1080, 1080),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize(),
                                )

                                // Continuous gradient fade into background surface
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isPortrait) (configuration.screenHeightDp * 0.35f).dp else 180.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                0.00f to Color.Transparent,
                                                0.30f to Color.Transparent,
                                                0.60f to MaterialTheme.colorScheme.background.copy(alpha = 0.35f),
                                                0.82f to MaterialTheme.colorScheme.background.copy(alpha = 0.75f),
                                                1.00f to MaterialTheme.colorScheme.background,
                                            ),
                                        ),
                                )

                                // Overlaid Header Text at Bottom of Hero
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp)
                                        .padding(bottom = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = displayTitle,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = artistName ?: unknownArtist,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (distinctSongs.isNotEmpty()) {
                                            "${stringResource(R.string.playlist)} • ${pluralStringResource(R.plurals.n_song, distinctSongs.size, distinctSongs.size)}"
                                        } else {
                                            stringResource(R.string.playlist)
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xC4FFFFFF),
                                        textAlign = TextAlign.Center,
                                    )
                                }

                                // Floating circular Back button at top-left
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 12.dp, top = 4.dp)
                                        .windowInsetsPadding(WindowInsets.statusBars)
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.35f))
                                        .combinedClickable(
                                            onClick = navController::navigateUp,
                                            onLongClick = navController::backToMain,
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_back),
                                        contentDescription = "Back",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }

                                // Floating circular action buttons at top-right
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 12.dp, top = 4.dp)
                                        .windowInsetsPadding(WindowInsets.statusBars)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.Black.copy(alpha = 0.35f)),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconButton(
                                        onClick = { viewModel.togglePlaylistBookmark() },
                                        onLongClick = {},
                                    ) {
                                        Icon(
                                            painter = painterResource(if (isBookmarked) R.drawable.favorite else R.drawable.favorite_border),
                                            contentDescription = stringResource(if (isBookmarked) R.string.saved else R.string.save),
                                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color.White,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            showSearchBar = !showSearchBar
                                            if (!showSearchBar) searchQuery = ""
                                        },
                                        onLongClick = {},
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.search),
                                            contentDescription = stringResource(R.string.search),
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                    IconButton(
                                        onClick = showOverflowMenu,
                                        onLongClick = {},
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = stringResource(R.string.more_options),
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // 3-Action Controls Row: [Shuffle (48dp)] [Play pill (48dp)] [Download / Action (48dp)]
                        item(key = "action_row") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Shuffle circle button (48dp)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .clickable {
                                            val songsToPlay = distinctSongs.shuffled().map { it.toMediaItem() }
                                            if (songsToPlay.isNotEmpty()) {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = displayTitle,
                                                        items = songsToPlay,
                                                    ),
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.shuffle),
                                        contentDescription = stringResource(R.string.shuffle),
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }

                                // Play pill button (48dp height, minWidth 110dp)
                                Box(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .widthIn(min = 110.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable {
                                            if (isPlaylistPlaying) {
                                                playerConnection.togglePlayPause()
                                            } else {
                                                val songsToPlay = distinctSongs.map { it.toMediaItem() }
                                                if (songsToPlay.isNotEmpty()) {
                                                    playerConnection.playQueue(
                                                        ListQueue(
                                                            title = displayTitle,
                                                            items = songsToPlay,
                                                        ),
                                                    )
                                                }
                                            }
                                        }
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(if (isPlaylistPlaying && isPlaying) R.drawable.pause else R.drawable.play),
                                            contentDescription = null,
                                            tint = Color.Black,
                                            modifier = Modifier.size(22.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isPlaylistPlaying && isPlaying) "Pause" else "Play",
                                            color = Color.Black,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }

                                // Download circle button (48dp)
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.12f))
                                        .clickable {
                                            when (downloadState) {
                                                Download.STATE_COMPLETED, Download.STATE_DOWNLOADING -> {
                                                    distinctSongs.forEach { song ->
                                                        DownloadService.sendRemoveDownload(
                                                            context,
                                                            ExoDownloadService::class.java,
                                                            song.id,
                                                            false,
                                                        )
                                                    }
                                                }
                                                else -> {
                                                    distinctSongs.forEach { song ->
                                                        val downloadRequest = DownloadRequest
                                                            .Builder(song.id, song.id.toUri())
                                                            .setCustomCacheKey(song.id)
                                                            .setData(song.title.toByteArray())
                                                            .build()
                                                        DownloadService.sendAddDownload(
                                                            context,
                                                            ExoDownloadService::class.java,
                                                            downloadRequest,
                                                            false,
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    when (downloadState) {
                                        Download.STATE_COMPLETED -> {
                                            Icon(
                                                painter = painterResource(R.drawable.offline),
                                                contentDescription = stringResource(R.string.saved),
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }
                                        Download.STATE_DOWNLOADING -> {
                                            CircularProgressIndicator(
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                painter = painterResource(R.drawable.download),
                                                contentDescription = stringResource(R.string.action_download),
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Track Counter
                        item(key = "track_count") {
                            Text(
                                text = "${filteredSongs.size} tracks",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                        }
                    } else {
                        // Spacer when search bar is visible
                        item(key = "search_spacer") {
                            Spacer(
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .height(64.dp),
                            )
                        }
                    }

                    // Song List Items with Multi-Select, Swipe-to-Queue, and 3-Dot Menus
                    itemsIndexed(
                        items = filteredSongs,
                        key = { _, song -> song.id },
                    ) { index, song ->
                        ArtistSongRow(
                            song = song,
                            isPlaying = isPlaying && mediaMetadata?.id == song.id,
                            selectionMode = selectionState.isActive,
                            isSelected = selectionState.isSelected(song.id),
                            onClick = {
                                if (song.id == mediaMetadata?.id) {
                                    playerConnection.togglePlayPause()
                                } else {
                                    playerConnection.playQueue(
                                        YouTubeQueue(
                                            song.endpoint ?: WatchEndpoint(videoId = song.id),
                                            song.toMediaMetadata(),
                                        ),
                                    )
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectionState.start(song.id)
                            },
                            onSelectToggle = {
                                selectionState.toggle(song.id)
                            },
                            onAddToQueue = {
                                playerConnection.addToQueue(listOf(song.toMediaItem()))
                                Toast.makeText(context, context.getString(R.string.add_to_queue), Toast.LENGTH_SHORT).show()
                            },
                            onMoreClick = {
                                menuState.show {
                                    YouTubeSongMenu(
                                        song = song,
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                        )

                        if (index < filteredSongs.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 72.dp, end = 16.dp),
                                thickness = 0.5.dp,
                                color = Color.White.copy(alpha = 0.12f),
                            )
                        }
                    }

                    if (itemsPage?.continuation != null) {
                        item(key = "loading") {
                            ShimmerHost {
                                repeat(3) {
                                    ListItemPlaceHolder()
                                }
                            }
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(modifier = Modifier.height(100.dp))
                    }
                }
            }

            // Snackbar Host
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                    .align(Alignment.BottomCenter),
            )

            // Selection TopAppBar (shown when multi-selection mode is active)
            AnimatedVisibility(
                visible = selectionState.isActive,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ) {
                TopAppBar(
                    windowInsets = WindowInsets.statusBars,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        navigationIconContentColor = Color.White,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                    navigationIcon = {
                        Box(Modifier.padding(horizontal = 5.dp)) {
                            IconButton(
                                onClick = { selectionState.exit() },
                                onLongClick = {},
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = stringResource(R.string.close),
                                    tint = Color.White,
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = "${selectionState.count} selected",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val allIds = distinctSongs.take(MAX_SONG_SELECTION).map { it.id }
                                selectionState.toggleSelectAll(allIds)
                            },
                            onLongClick = {},
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.select_all),
                                contentDescription = stringResource(R.string.select_all),
                                tint = Color.White,
                            )
                        }
                        if (selectionState.count > 0) {
                            IconButton(
                                onClick = {
                                    val selectedSongs = distinctSongs.filter { selectionState.isSelected(it.id) }
                                    menuState.show {
                                        YouTubeSelectionSongMenu(
                                            songSelection = selectedSongs,
                                            onDismiss = menuState::dismiss,
                                            clearAction = { selectionState.exit() },
                                        )
                                    }
                                },
                                onLongClick = {},
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.more_vert),
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                            }
                        }
                    },
                )
            }

            // Search TopAppBar (shown when search is active)
            AnimatedVisibility(
                visible = showSearchBar && !selectionState.isActive,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ) {
                TopAppBar(
                    windowInsets = WindowInsets.statusBars,
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.search),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.6f),
                                )
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = MaterialTheme.colorScheme.primary,
                            ),
                        )
                    },
                    navigationIcon = {
                        Box(Modifier.padding(horizontal = 5.dp)) {
                            IconButton(
                                onClick = {
                                    showSearchBar = false
                                    searchQuery = ""
                                },
                                onLongClick = {},
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back),
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    },
                    actions = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                onLongClick = {},
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = "Clear",
                                    tint = Color.White,
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                        navigationIconContentColor = Color.White,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                )
            }

            // Sticky TopAppBar (shown on scroll when not selecting or searching)
            AnimatedVisibility(
                visible = shouldHideTopBar && !showSearchBar && !selectionState.isActive,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ) {
                TopAppBar(
                    windowInsets = WindowInsets.statusBars,
                    title = {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight(align = Alignment.CenterVertically)
                                .basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    animationMode = MarqueeAnimationMode.Immediately,
                                )
                                .focusable(),
                        )
                    },
                    navigationIcon = {
                        Box(Modifier.padding(horizontal = 5.dp)) {
                            IconButton(
                                onClick = navController::navigateUp,
                                onLongClick = navController::backToMain,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back),
                                    contentDescription = "Back",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showSearchBar = !showSearchBar },
                            onLongClick = {},
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.search),
                                contentDescription = stringResource(R.string.search),
                                tint = Color.White,
                            )
                        }
                        IconButton(
                            onClick = showOverflowMenu,
                            onLongClick = {},
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = stringResource(R.string.more_options),
                                tint = Color.White,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        navigationIconContentColor = Color.White,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                )
            }
        }
    } else {
        // Non-Song items: Preserve Grid View (Albums, Singles, Artists, Playlists)
        Box(Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                state = lazyGridState,
                columns = GridCells.Adaptive(minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp),
                contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
            ) {
                items(
                    items = itemsPage?.items.orEmpty().distinctBy { it.id },
                    key = { it.id },
                ) { item ->
                    YouTubeGridItem(
                        item = item,
                        isActive = when (item) {
                            is SongItem -> mediaMetadata?.id == item.id
                            is AlbumItem -> mediaMetadata?.album?.id == item.id
                            else -> false
                        },
                        isPlaying = isPlaying,
                        fillMaxWidth = true,
                        coroutineScope = coroutineScope,
                        modifier = Modifier
                            .combinedClickable(
                                onClick = {
                                    when (item) {
                                        is SongItem -> playerConnection.playQueue(
                                            YouTubeQueue(
                                                item.endpoint ?: WatchEndpoint(videoId = item.id),
                                                item.toMediaMetadata(),
                                            ),
                                        )

                                        is AlbumItem -> navController.navigate("album/${item.id}")
                                        is ArtistItem -> navController.navigate("artist/${item.id}")
                                        is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        when (item) {
                                            is SongItem -> YouTubeSongMenu(
                                                song = item,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )

                                            is AlbumItem -> YouTubeAlbumMenu(
                                                albumItem = item,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )

                                            is ArtistItem -> YouTubeArtistMenu(
                                                artist = item,
                                                onDismiss = menuState::dismiss,
                                            )

                                            is PlaylistItem -> YouTubePlaylistMenu(
                                                playlist = item,
                                                coroutineScope = coroutineScope,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    }
                                },
                            )
                            .animateItem(),
                    )
                }

                if (itemsPage?.continuation != null) {
                    item(key = "loading") {
                        ShimmerHost(Modifier.animateItem()) {
                            GridItemPlaceHolder(fillMaxWidth = true)
                        }
                    }
                }
            }

            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain,
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            contentDescription = null,
                        )
                    }
                },
            )
        }
    }
}

