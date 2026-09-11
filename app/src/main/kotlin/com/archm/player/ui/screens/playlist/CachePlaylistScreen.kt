package com.archm.player.ui.screens.playlist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachReversed
import androidx.compose.ui.util.fastSumBy
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
import com.archm.player.R
import com.archm.player.constants.HideExplicitKey
import com.archm.player.constants.SongSortDescendingKey
import com.archm.player.constants.SongSortType
import com.archm.player.constants.SongSortTypeKey
import com.archm.player.extensions.toMediaItem
import com.archm.player.playback.ExoDownloadService
import com.archm.player.playback.queues.ListQueue
import com.archm.player.ui.component.CombinedIconButton
import com.archm.player.ui.component.DraggableScrollbar
import com.archm.player.ui.component.EmptyPlaceholder
import com.archm.player.ui.component.ExpandableText
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.SongListItem
import com.archm.player.ui.component.SortHeader
import com.archm.player.ui.menu.CachePlaylistMenu
import com.archm.player.ui.menu.SelectionSongMenu
import com.archm.player.ui.menu.SongMenu
import com.archm.player.ui.utils.backToMain
import com.archm.player.ui.utils.resize
import com.archm.player.utils.listItemShape
import com.archm.player.utils.makeTimeString
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.utils.rememberPreference
import com.archm.player.viewmodels.CachePlaylistViewModel
import java.time.LocalDateTime

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CachePlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: CachePlaylistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val downloadUtil = LocalDownloadUtil.current

    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val cachedSongs by viewModel.cachedSongs.collectAsState()

    val (sortType, onSortTypeChange) = rememberEnumPreference(
        SongSortTypeKey,
        SongSortType.CREATE_DATE
    )
    val (sortDescending, onSortDescendingChange) = rememberPreference(SongSortDescendingKey, true)
    val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)

    val sortedSongs = remember(cachedSongs, sortType, sortDescending) {
        val sorted = when (sortType) {
            SongSortType.CREATE_DATE -> cachedSongs.sortedBy { it.song.dateDownload ?: LocalDateTime.MIN }
            SongSortType.NAME -> cachedSongs.sortedBy { it.song.title }
            SongSortType.ARTIST -> cachedSongs.sortedBy { song ->
                song.artists.joinToString(separator = "") { it.name }
            }
            SongSortType.PLAY_TIME -> cachedSongs.sortedBy { it.song.totalPlayTime }
        }
        if (sortDescending) sorted.reversed() else sorted
    }

    val cacheLength = remember(sortedSongs) {
        sortedSongs.fastSumBy { it.song.duration }
    }

    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    LaunchedEffect(cachedSongs) {
        if (cachedSongs.isEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState =
                if (cachedSongs.all { downloads[it.id]?.state == Download.STATE_COMPLETED }) {
                    Download.STATE_COMPLETED
                } else if (cachedSongs.all {
                        downloads[it.id]?.state == Download.STATE_QUEUED ||
                                downloads[it.id]?.state == Download.STATE_DOWNLOADING ||
                                downloads[it.id]?.state == Download.STATE_COMPLETED
                    }
                ) {
                    Download.STATE_DOWNLOADING
                } else {
                    Download.STATE_STOPPED
                }
        }
    }

    val handleDownloadAction: () -> Unit = {
        when (downloadState) {
            Download.STATE_COMPLETED, Download.STATE_DOWNLOADING -> {
                cachedSongs.forEach { song ->
                    DownloadService.sendRemoveDownload(
                        context,
                        ExoDownloadService::class.java,
                        song.id,
                        false,
                    )
                }
            }
            else -> {
                cachedSongs.forEach { song ->
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
    }

    var inSelectMode by rememberSaveable { mutableStateOf(false) }
    val selection = rememberSaveable(
        saver = listSaver<MutableList<String>, String>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf() }
    val onExitSelectionMode = {
        inSelectMode = false
        selection.clear()
    }

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    BackHandler(enabled = inSelectMode || isSearching) {
        if (inSelectMode) {
            onExitSelectionMode()
        } else if (isSearching) {
            isSearching = false
            query = TextFieldValue()
        }
    }

    val filteredSongs = remember(sortedSongs, query) {
        if (query.text.isEmpty()) sortedSongs
        else sortedSongs.filter { song ->
            song.title.contains(query.text, true) ||
                    song.artists.any { it.name.contains(query.text, true) }
        }
    }

    LaunchedEffect(filteredSongs) {
        selection.fastForEachReversed { songId ->
            if (filteredSongs.find { it.id == songId } == null) {
                selection.remove(songId)
            }
        }
    }

    val firstItemVisible by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex == 0 }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }

    val isPlaylistPlaying = remember(cachedSongs, mediaMetadata?.id) {
        cachedSongs.any { it.id == mediaMetadata?.id }
    }

    val hasExplicitContent = remember(sortedSongs) {
        sortedSongs.any { it.song.explicit }
    }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenWidthDp < configuration.screenHeightDp
    val heroHeight = if (isPortrait) (configuration.screenHeightDp / 2).dp else 280.dp
    val gradientHeight = if (isPortrait) (configuration.screenHeightDp * 0.35f).dp else 180.dp

    val titleText = stringResource(R.string.cached_playlist)
    val backdropThumbnail = sortedSongs.firstOrNull()?.thumbnailUrl

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(
                bottom = maxOf(
                    120.dp,
                    LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding() + 24.dp,
                    WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                )
            ),
        ) {
            if (filteredSongs.isEmpty() && !isSearching) {
                item(key = "empty_placeholder") {
                    EmptyPlaceholder(
                        icon = R.drawable.music_note,
                        text = stringResource(R.string.playlist_is_empty),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp)
                            .animateItem()
                    )
                }
            } else if (filteredSongs.isEmpty() && isSearching) {
                item(key = "no_results") {
                    EmptyPlaceholder(
                        icon = R.drawable.search,
                        text = stringResource(R.string.no_results_found),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp)
                            .animateItem()
                    )
                }
            } else {
                if (!isSearching) {
                    // 1. Full-bleed Hero Section with Gradient Fade and Overlaid Metadata
                    item(key = "hero_header") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heroHeight)
                        ) {
                            // Full-bleed hero image centered and cropped
                            AsyncImage(
                                model = backdropThumbnail?.resize(1080, 1080) ?: backdropThumbnail,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                alignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            )

                            // Continuous gradient fade into background surface
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(gradientHeight)
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

                            // Overlaid Header Metadata at Bottom of Hero
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = titleText,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.playlist),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xC4FFFFFF),
                                        textAlign = TextAlign.Center,
                                    )
                                    if (hasExplicitContent) {
                                        Icon(
                                            painter = painterResource(R.drawable.explicit),
                                            contentDescription = "Explicit",
                                            tint = Color(0xC4FFFFFF),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            // Floating circular Back button at top-left
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(start = 16.dp, top = 4.dp)
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

                            // Floating circular action pill at top-right
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 16.dp, top = 4.dp)
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color.Black.copy(alpha = 0.35f)),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = { isSearching = true },
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.search),
                                        contentDescription = stringResource(R.string.search),
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        menuState.show {
                                            CachePlaylistMenu(
                                                downloadState = downloadState,
                                                onQueue = {
                                                    playerConnection.addToQueue(
                                                        sortedSongs.map { it.toMediaItem() }
                                                    )
                                                },
                                                onDownload = handleDownloadAction,
                                                onDismiss = menuState::dismiss
                                            )
                                        }
                                    },
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

                    // 2. Monochrome 3-Action Controls Row: [Shuffle (48dp)] [Play pill (48dp)] [Download (48dp)]
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
                                        if (sortedSongs.isNotEmpty()) {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = titleText,
                                                    items = sortedSongs.shuffled().map { it.toMediaItem() },
                                                )
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
                            val isThisPlaying = isPlaying && isPlaylistPlaying
                            Box(
                                modifier = Modifier
                                    .height(48.dp)
                                    .widthIn(min = 110.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable {
                                        if (isThisPlaying) {
                                            playerConnection.togglePlayPause()
                                        } else if (sortedSongs.isNotEmpty()) {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = titleText,
                                                    items = sortedSongs.map { it.toMediaItem() },
                                                )
                                            )
                                        }
                                    }
                                    .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(if (isThisPlaying) R.drawable.pause else R.drawable.play),
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(22.dp),
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isThisPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
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
                                    .clickable(onClick = handleDownloadAction),
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

                    // 3. Controls & Anchored Metadata Row
                    item(key = "controls_row") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        ) {
                            val staticDescription = remember(sortedSongs.size, cacheLength) {
                                val trackCountText = context.resources.getQuantityString(R.plurals.n_song, sortedSongs.size, sortedSongs.size)
                                "$titleText is your local collection of cached tracks, featuring $trackCountText.${
                                    if (cacheLength > 0) " Combined duration is ${makeTimeString(cacheLength * 1000L)}." else ""
                                } These songs are stored on your device for quick access."
                            }

                            ExpandableText(
                                text = staticDescription,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                collapsedMaxLines = 3,
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                            ) {
                                SortHeader(
                                    sortType = sortType,
                                    sortDescending = sortDescending,
                                    onSortTypeChange = onSortTypeChange,
                                    onSortDescendingChange = onSortDescendingChange,
                                    sortTypeText = { type ->
                                        when (type) {
                                            SongSortType.CREATE_DATE -> R.string.sort_by_create_date
                                            SongSortType.NAME -> R.string.sort_by_name
                                            SongSortType.ARTIST -> R.string.sort_by_artist
                                            SongSortType.PLAY_TIME -> R.string.sort_by_play_time
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }

                            // Anchored Track Count & Duration Section Header
                            val durationText = remember(sortedSongs.size, cacheLength) {
                                buildString {
                                    append(context.resources.getQuantityString(R.plurals.n_song, sortedSongs.size, sortedSongs.size))
                                    if (cacheLength > 0) {
                                        append(" • ")
                                        append(makeTimeString(cacheLength * 1000L))
                                    }
                                }
                            }
                            Text(
                                text = durationText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }
                } else {
                    item(key = "search_spacer") {
                        Spacer(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .height(64.dp)
                        )
                    }
                }

                itemsIndexed(filteredSongs, key = { _, song -> song.id }) { index, song ->
                    val onCheckedChange: (Boolean) -> Unit = {
                        if (it) {
                            selection.add(song.id)
                        } else {
                            selection.remove(song.id)
                        }
                    }

                    SongListItem(
                        song = song,
                        isActive = song.id == mediaMetadata?.id,
                        isPlaying = isPlaying,
                        showInLibraryIcon = true,
                        shape = listItemShape(index, filteredSongs.size),
                        trailingContent = {
                            if (inSelectMode) {
                                Checkbox(
                                    checked = song.id in selection,
                                    onCheckedChange = onCheckedChange
                                )
                            } else {
                                IconButton(onClick = {
                                    menuState.show {
                                        SongMenu(
                                            originalSong = song,
                                            navController = navController,
                                            onDismiss = menuState::dismiss,
                                            isFromCache = true,
                                        )
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(R.drawable.more_vert),
                                        contentDescription = null
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .combinedClickable(
                                onClick = {
                                    if (inSelectMode) {
                                        onCheckedChange(song.id !in selection)
                                    } else if (song.id == mediaMetadata?.id) {
                                        playerConnection.togglePlayPause()
                                    } else {
                                        playerConnection.playQueue(
                                            ListQueue(
                                                title = titleText,
                                                items = cachedSongs.map { it.toMediaItem() },
                                                startIndex = cachedSongs.indexOfFirst { it.id == song.id }
                                            )
                                        )
                                    }
                                },
                                onLongClick = {
                                    if (!inSelectMode) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        inSelectMode = true
                                        onCheckedChange(true)
                                    }
                                }
                            )
                            .animateItem()
                    )
                }
            }
        }

        DraggableScrollbar(
            modifier = Modifier
                .padding(LocalPlayerAwareWindowInsets.current.union(WindowInsets.ime).asPaddingValues())
                .align(Alignment.CenterEnd),
            scrollState = lazyListState,
            headerItems = if (isSearching) 1 else 3
        )

        // Selection TopAppBar (shown during multi-selection mode)
        AnimatedVisibility(
            visible = inSelectMode,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                navigationIcon = {
                    IconButton(
                        onClick = onExitSelectionMode,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.close),
                            contentDescription = stringResource(R.string.close),
                        )
                    }
                },
                title = {
                    Text(
                        text = pluralStringResource(R.plurals.n_song, selection.size, selection.size),
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                actions = {
                    Checkbox(
                        checked = selection.size == filteredSongs.size && selection.isNotEmpty(),
                        onCheckedChange = {
                            if (selection.size == filteredSongs.size) {
                                selection.clear()
                            } else {
                                selection.clear()
                                selection.addAll(filteredSongs.map { it.id })
                            }
                        }
                    )
                    IconButton(
                        enabled = selection.isNotEmpty(),
                        onClick = {
                            menuState.show {
                                SelectionSongMenu(
                                    songSelection = filteredSongs.filter { it.id in selection },
                                    onDismiss = menuState::dismiss,
                                    clearAction = onExitSelectionMode
                                )
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = null
                        )
                    }
                }
            )
        }

        // Search TopAppBar (shown when search is active)
        AnimatedVisibility(
            visible = isSearching && !inSelectMode,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                navigationIcon = {
                    IconButton(
                        onClick = {
                            isSearching = false
                            query = TextFieldValue()
                            focusManager.clearFocus()
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = "Back",
                        )
                    }
                },
                title = {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    )
                },
                actions = {
                    if (query.text.isNotEmpty()) {
                        IconButton(
                            onClick = { query = TextFieldValue() },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.close),
                                contentDescription = "Clear",
                            )
                        }
                    }
                }
            )
        }

        // Sticky TopAppBar (shown on scroll when not selecting or searching)
        AnimatedVisibility(
            visible = shouldHideTopBar && !isSearching && !inSelectMode,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                navigationIcon = {
                    CombinedIconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = "Back",
                        )
                    }
                },
                title = {
                    Text(
                        text = titleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                },
                actions = {
                    IconButton(
                        onClick = { isSearching = true },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.search),
                            contentDescription = stringResource(R.string.search),
                        )
                    }
                    IconButton(
                        onClick = {
                            menuState.show {
                                CachePlaylistMenu(
                                    downloadState = downloadState,
                                    onQueue = {
                                        playerConnection.addToQueue(
                                            sortedSongs.map { it.toMediaItem() }
                                        )
                                    },
                                    onDownload = handleDownloadAction,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        },
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = stringResource(R.string.more_options),
                        )
                    }
                }
            )
        }
    }
}
