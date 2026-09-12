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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import com.archm.player.constants.YtmSyncKey
import com.archm.player.db.entities.Song
import com.archm.player.extensions.toMediaItem
import com.archm.player.models.MediaMetadata
import com.archm.player.playback.ExoDownloadService
import com.archm.player.playback.PlayerConnection
import com.archm.player.playback.queues.ListQueue
import com.archm.player.ui.component.CombinedIconButton
import com.archm.player.ui.component.DefaultDialog
import com.archm.player.ui.component.DraggableScrollbar
import com.archm.player.ui.component.EmptyPlaceholder
import com.archm.player.ui.component.ExpandableText
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.SongListItem
import com.archm.player.ui.component.SortHeader
import com.archm.player.ui.menu.AutoPlaylistMenu
import com.archm.player.ui.menu.SelectionSongMenu
import com.archm.player.ui.menu.SongMenu
import com.archm.player.ui.utils.backToMain
import com.archm.player.ui.utils.resize
import com.archm.player.utils.makeTimeString
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.utils.rememberPreference
import com.archm.player.viewmodels.AutoPlaylistViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AutoPlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AutoPlaylistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val playerConnection = LocalPlayerConnection.current
    val isPlaying by (playerConnection?.isEffectivelyPlaying ?: flowOf(false)).collectAsState(false)
    val mediaMetadata by (playerConnection?.mediaMetadata ?: flowOf<MediaMetadata?>(null)).collectAsState(null)
    val playlist = when (viewModel.playlist) {
        "liked" -> stringResource(R.string.liked)
        "uploaded" -> stringResource(R.string.uploaded_playlist)
        "exported" -> stringResource(R.string.action_exported)
        else -> stringResource(R.string.offline)
    }

    val songs by viewModel.likedSongs.collectAsState(null)
    val mutableSongs = remember { mutableStateListOf<Song>() }

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)
    val (ytmSync) = rememberPreference(YtmSyncKey, true)

    val likeLength = remember(songs) {
        songs?.fastSumBy { it.song.duration } ?: 0
    }

    val playlistId = viewModel.playlist
    val playlistType = when (playlistId) {
        "liked" -> PlaylistType.LIKE
        "downloaded" -> PlaylistType.DOWNLOAD
        "uploaded" -> PlaylistType.UPLOADED
        "exported" -> PlaylistType.EXPORTED
        else -> PlaylistType.OTHER
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

    BackHandler(enabled = inSelectMode || isSearching) {
        if (inSelectMode) {
            onExitSelectionMode()
        } else if (isSearching) {
            isSearching = false
            query = TextFieldValue()
        }
    }

    val (sortType, onSortTypeChange) = rememberEnumPreference(
        SongSortTypeKey,
        SongSortType.CREATE_DATE
    )
    val (sortDescending, onSortDescendingChange) = rememberPreference(SongSortDescendingKey, true)

    val downloadUtil = LocalDownloadUtil.current
    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    LaunchedEffect(Unit) {
        if (ytmSync) {
            withContext(Dispatchers.IO) {
                if (playlistType == PlaylistType.LIKE) {
                    viewModel.syncLikedSongs()
                }
                if (playlistType == PlaylistType.UPLOADED) {
                    viewModel.syncUploadedSongs()
                }
            }
        }
    }

    LaunchedEffect(songs) {
        mutableSongs.apply {
            clear()
            songs?.let { addAll(it) }
        }
        if (songs?.isEmpty() == true) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState =
                if (songs?.all { downloads[it.song.id]?.state == Download.STATE_COMPLETED } == true) {
                    Download.STATE_COMPLETED
                } else if (songs?.all {
                        downloads[it.song.id]?.state == Download.STATE_QUEUED ||
                                downloads[it.song.id]?.state == Download.STATE_DOWNLOADING ||
                                downloads[it.song.id]?.state == Download.STATE_COMPLETED
                    } == true
                ) {
                    Download.STATE_DOWNLOADING
                } else {
                    Download.STATE_STOPPED
                }
        }
    }

    var showRemoveDownloadDialog by remember {
        mutableStateOf(false)
    }

    if (showRemoveDownloadDialog) {
        DefaultDialog(
            onDismiss = { showRemoveDownloadDialog = false },
            content = {
                Text(
                    text = stringResource(R.string.remove_download_playlist_confirm, playlist),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
            },
            buttons = {
                TextButton(
                    onClick = { showRemoveDownloadDialog = false },
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }

                TextButton(
                    onClick = {
                        showRemoveDownloadDialog = false
                        songs?.forEach { song ->
                            DownloadService.sendRemoveDownload(
                                context,
                                ExoDownloadService::class.java,
                                song.song.id,
                                false,
                            )
                        }
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
        )
    }

    val filteredSongs = remember(songs, query) {
        if (query.text.isEmpty()) songs ?: emptyList()
        else songs?.filter { song ->
            song.song.title.contains(query.text, true) ||
                    song.artists.any { it.name.contains(query.text, true) }
        } ?: emptyList()
    }

    LaunchedEffect(filteredSongs) {
        selection.fastForEachReversed { songId ->
            if (filteredSongs.find { it.id == songId } == null) {
                selection.remove(songId)
            }
        }
    }

    val lazyListState = rememberLazyListState()

    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()
    val canRefresh = playlistType == PlaylistType.LIKE || playlistType == PlaylistType.UPLOADED

    val firstItemVisible by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex == 0 }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }

    val isPlaylistPlaying = remember(songs, mediaMetadata?.id) {
        songs?.any { it.song.id == mediaMetadata?.id } == true
    }

    val hasExplicitContent = remember(songs) {
        songs?.any { it.song.explicit } == true
    }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenWidthDp < configuration.screenHeightDp
    val heroHeight = if (isPortrait) (configuration.screenHeightDp / 2).dp else 280.dp
    val gradientHeight = if (isPortrait) (configuration.screenHeightDp * 0.35f).dp else 180.dp

    val backdropThumbnail = songs?.firstOrNull()?.thumbnailUrl

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (canRefresh) {
                    Modifier.pullToRefresh(
                        state = pullRefreshState,
                        isRefreshing = isRefreshing,
                        onRefresh = viewModel::refresh
                    )
                } else {
                    Modifier
                }
            ),
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
            val songList = songs
            if (songList != null) {
                if (songList.isEmpty()) {
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
                                        text = playlist,
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
                                                AutoPlaylistMenu(
                                                    downloadState = downloadState,
                                                    onQueue = {
                                                        playerConnection?.addToQueue(
                                                            songList.map { it.toMediaItem() }
                                                        )
                                                    },
                                                    onDownload = {
                                                        when (downloadState) {
                                                            Download.STATE_COMPLETED -> showRemoveDownloadDialog = true
                                                            Download.STATE_DOWNLOADING -> {
                                                                songList.forEach { song ->
                                                                    DownloadService.sendRemoveDownload(
                                                                        context,
                                                                        ExoDownloadService::class.java,
                                                                        song.song.id,
                                                                        false,
                                                                    )
                                                                }
                                                            }
                                                            else -> {
                                                                songList.forEach { song ->
                                                                    val downloadRequest = DownloadRequest
                                                                        .Builder(song.song.id, song.song.id.toUri())
                                                                        .setCustomCacheKey(song.song.id)
                                                                        .setData(song.song.title.toByteArray())
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
                                        if (songList.isNotEmpty()) {
                                            playerConnection?.playQueue(
                                                ListQueue(
                                                    title = playlist,
                                                    items = songList.shuffled().map { it.toMediaItem() },
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
                                val isThisPlaying = isPlaying && isPlaylistPlaying
                                Box(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .widthIn(min = 110.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .clickable {
                                            if (isThisPlaying) {
                                                playerConnection?.togglePlayPause()
                                            } else if (songList.isNotEmpty()) {
                                                playerConnection?.playQueue(
                                                    ListQueue(
                                                        title = playlist,
                                                        items = songList.map { it.toMediaItem() },
                                                    ),
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
                                        .clickable {
                                            when (downloadState) {
                                                Download.STATE_COMPLETED -> showRemoveDownloadDialog = true
                                                Download.STATE_DOWNLOADING -> {
                                                    songList.forEach { song ->
                                                        DownloadService.sendRemoveDownload(
                                                            context,
                                                            ExoDownloadService::class.java,
                                                            song.song.id,
                                                            false,
                                                        )
                                                    }
                                                }
                                                else -> {
                                                    songList.forEach { song ->
                                                        val downloadRequest = DownloadRequest
                                                            .Builder(song.song.id, song.song.id.toUri())
                                                            .setCustomCacheKey(song.song.id)
                                                            .setData(song.song.title.toByteArray())
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

                        // 3. Controls & Anchored Metadata Row
                        item(key = "controls_row") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                            ) {
                                val staticDescription = remember(playlist, songList.size, likeLength) {
                                    val trackCountText = context.resources.getQuantityString(R.plurals.n_song, songList.size, songList.size)
                                    "$playlist is a personalized collection featuring $trackCountText.${
                                        if (likeLength > 0) " Total listening time is ${makeTimeString(likeLength * 1000L)}." else ""
                                    } This playlist is automatically curated for your musical enjoyment."
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
                                val durationText = remember(songList.size, likeLength) {
                                    buildString {
                                        append(context.resources.getQuantityString(R.plurals.n_song, songList.size, songList.size))
                                        if (likeLength > 0) {
                                            append(" • ")
                                            append(makeTimeString(likeLength * 1000L))
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

                    itemsIndexed(
                        items = filteredSongs,
                        key = { _, song -> song.id },
                    ) { index, song ->
                        val onCheckedChange: (Boolean) -> Unit = {
                            if (it) {
                                selection.add(song.id)
                            } else {
                                selection.remove(song.id)
                            }
                        }

                        SongListItem(
                            song = song,
                            isActive = song.song.id == mediaMetadata?.id,
                            isPlaying = isPlaying,
                            isSelected = inSelectMode && song.id in selection,
                            showInLibraryIcon = true,
                            shape = RoundedCornerShape(12.dp),
                            trailingContent = {
                                if (inSelectMode) {
                                    Checkbox(
                                        checked = song.id in selection,
                                        onCheckedChange = onCheckedChange
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = null,
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = {
                                        if (inSelectMode) {
                                            onCheckedChange(song.id !in selection)
                                        } else if (song.song.id == mediaMetadata?.id) {
                                            playerConnection?.togglePlayPause()
                                        } else {
                                            playerConnection?.playQueue(
                                                ListQueue(
                                                    title = playlist,
                                                    items = songList.map { it.toMediaItem() },
                                                    startIndex = songList.indexOfFirst { it.id == song.id }
                                                ),
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        if (!inSelectMode) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            inSelectMode = true
                                            onCheckedChange(true)
                                        }
                                    },
                                )
                                .animateItem()
                        )
                    }
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

        if (canRefresh) {
            Indicator(
                isRefreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
            )
        }

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
            visible = (songs == null) || (shouldHideTopBar && !isSearching && !inSelectMode),
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
                        text = playlist,
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
                    val songList = songs
                    if (songList != null) {
                        IconButton(
                            onClick = {
                                menuState.show {
                                    AutoPlaylistMenu(
                                        downloadState = downloadState,
                                        onQueue = {
                                            playerConnection?.addToQueue(
                                                songList.map { it.toMediaItem() }
                                            )
                                        },
                                        onDownload = {
                                            when (downloadState) {
                                                Download.STATE_COMPLETED -> showRemoveDownloadDialog = true
                                                Download.STATE_DOWNLOADING -> {
                                                    songList.forEach { song ->
                                                        DownloadService.sendRemoveDownload(
                                                            context,
                                                            ExoDownloadService::class.java,
                                                            song.song.id,
                                                            false,
                                                        )
                                                    }
                                                }
                                                else -> {
                                                    songList.forEach { song ->
                                                        val downloadRequest = DownloadRequest
                                                            .Builder(song.song.id, song.song.id.toUri())
                                                            .setCustomCacheKey(song.song.id)
                                                            .setData(song.song.title.toByteArray())
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
                }
            )
        }
    }
}

enum class PlaylistType {
    LIKE, DOWNLOAD, UPLOADED, EXPORTED, OTHER
}
