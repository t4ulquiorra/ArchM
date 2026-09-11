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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachReversed
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.archm.player.LocalDatabase
import com.archm.player.LocalDownloadUtil
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.constants.HideExplicitKey
import com.archm.player.db.entities.PlaylistEntity
import com.archm.player.db.entities.PlaylistSongMap
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.ExoDownloadService
import com.archm.player.playback.queues.YouTubePlaylistQueue
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.ui.component.CombinedIconButton
import com.archm.player.ui.component.ExpandableText
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.NavigationTitle
import com.archm.player.ui.component.YouTubeGridItem
import com.archm.player.ui.component.YouTubeListItem
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.menu.YouTubeSelectionSongMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.ui.utils.backToMain
import com.archm.player.ui.utils.resize
import com.archm.player.utils.listItemShape
import com.archm.player.utils.rememberPreference
import com.archm.player.viewmodels.OnlinePlaylistViewModel
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnlinePlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: OnlinePlaylistViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val downloadUtil = LocalDownloadUtil.current

    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.playlistSongs.collectAsState()
    val dbPlaylist by viewModel.dbPlaylist.collectAsState()
    val relatedItems by viewModel.relatedItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val error by viewModel.error.collectAsState()

    val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)

    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }

    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    LaunchedEffect(songs) {
        if (songs.isEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState =
                if (songs.all { downloads[it.id]?.state == Download.STATE_COMPLETED }) {
                    Download.STATE_COMPLETED
                } else if (songs.all {
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

    val filteredSongs = remember(songs, query) {
        if (query.text.isEmpty()) songs.mapIndexed { i, s -> i to s }
        else songs.mapIndexed { i, s -> i to s }.filter {
            it.second.title.contains(query.text, true) ||
                    it.second.artists.fastAny { a -> a.name.contains(query.text, true) }
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

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isSearching) { if (isSearching) focusRequester.requestFocus() }

    LaunchedEffect(filteredSongs) {
        selection.fastForEachReversed { songId ->
            if (filteredSongs.find { it.second.id == songId } == null) {
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

    BackHandler(enabled = inSelectMode || isSearching) {
        if (inSelectMode) {
            onExitSelectionMode()
        } else if (isSearching) {
            isSearching = false
            query = TextFieldValue()
        }
    }

    val isBookmarked = dbPlaylist?.playlist?.bookmarkedAt != null
    val toggleBookmark: () -> Unit = {
        coroutineScope.launch(Dispatchers.IO) {
            playlist?.let { pl ->
                val currentDbPlaylist = dbPlaylist
                if (currentDbPlaylist != null) {
                    database.withTransaction {
                        val currentPlaylist = currentDbPlaylist.playlist
                        update(currentPlaylist, pl)
                        update(currentPlaylist.toggleLike())
                    }
                } else {
                    database.withTransaction {
                        val playlistEntity = PlaylistEntity(
                            name = pl.title,
                            browseId = pl.id,
                            thumbnailUrl = pl.thumbnail,
                            isEditable = pl.isEditable,
                            remoteSongCount = pl.songCountText?.let {
                                Regex("""\d+""").find(it)?.value?.toIntOrNull()
                            },
                            playEndpointParams = pl.playEndpoint?.params,
                            shuffleEndpointParams = pl.shuffleEndpoint?.params,
                            radioEndpointParams = pl.radioEndpoint?.params
                        ).toggleLike()
                        insert(playlistEntity)
                        songs.map { it.toMediaMetadata() }
                            .onEach { insert(it) }
                            .mapIndexed { index, song ->
                                PlaylistSongMap(
                                    songId = song.id,
                                    playlistId = playlistEntity.id,
                                    position = index,
                                    setVideoId = song.setVideoId
                                )
                            }
                            .forEach { insert(it) }
                    }
                }
            }
        }
    }

    val isPlaylistPlaying = remember(songs, mediaMetadata?.id) {
        songs.isNotEmpty() && songs.any { it.id == mediaMetadata?.id }
    }

    val hasExplicitContent = remember(songs) {
        songs.any { it.explicit }
    }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenWidthDp < configuration.screenHeightDp
    val heroHeight = if (isPortrait) (configuration.screenHeightDp / 2).dp else 280.dp
    val gradientHeight = if (isPortrait) (configuration.screenHeightDp * 0.35f).dp else 180.dp

    val backdropThumbnail = playlist?.thumbnail
        ?: songs.firstOrNull()?.thumbnail
        ?: dbPlaylist?.playlist?.thumbnailUrl

    Box(Modifier.fillMaxSize()) {
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
            if (playlist == null || songs.isEmpty()) {
                if (isLoading) {
                    item(key = "loading_placeholder") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ContainedLoadingIndicator()
                        }
                    }
                }
            } else {
                playlist?.let { pl ->
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
                                        text = pl.title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 2,
                                        textAlign = TextAlign.Center,
                                    )
                                    pl.author?.let { author ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = author.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.clickable(enabled = !author.id.isNullOrEmpty()) {
                                                author.id?.let { navController.navigate("artist/$it") }
                                            }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        val subText = buildString {
                                            append(stringResource(R.string.playlist))
                                            if (!pl.year.isNullOrBlank()) {
                                                append(" • ${pl.year}")
                                            }
                                        }
                                        Text(
                                            text = subText,
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

                                // Floating circular action pill at top-right
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
                                        onClick = toggleBookmark,
                                    ) {
                                        Icon(
                                            painter = painterResource(if (isBookmarked) R.drawable.favorite else R.drawable.favorite_border),
                                            contentDescription = stringResource(if (isBookmarked) R.string.saved else R.string.save),
                                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else Color.White,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
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
                                                YouTubePlaylistMenu(
                                                    playlist = pl,
                                                    songs = songs,
                                                    coroutineScope = coroutineScope,
                                                    onDismiss = menuState::dismiss,
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

                        // 2. Single 3-Action Controls Row: [Shuffle (48dp)] [Play pill (48dp)] [Download (48dp)]
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
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable {
                                            if (songs.isNotEmpty()) {
                                                playerConnection.playQueue(
                                                    YouTubePlaylistQueue(
                                                        playlistId = pl.id,
                                                        playlistTitle = pl.title,
                                                        initialSongs = songs.shuffled(),
                                                        initialContinuation = viewModel.continuation,
                                                    )
                                                )
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.shuffle),
                                        contentDescription = stringResource(R.string.shuffle),
                                        tint = MaterialTheme.colorScheme.onSurface,
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
                                        .background(MaterialTheme.colorScheme.primary)
                                        .clickable {
                                            if (isThisPlaying) {
                                                playerConnection.togglePlayPause()
                                            } else if (songs.isNotEmpty()) {
                                                playerConnection.playQueue(
                                                    YouTubePlaylistQueue(
                                                        playlistId = pl.id,
                                                        playlistTitle = pl.title,
                                                        initialSongs = songs,
                                                        initialContinuation = viewModel.continuation,
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
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(22.dp),
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isThisPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                                            color = MaterialTheme.colorScheme.onPrimary,
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
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .clickable {
                                            when (downloadState) {
                                                Download.STATE_COMPLETED, Download.STATE_DOWNLOADING -> {
                                                    songs.forEach { song ->
                                                        DownloadService.sendRemoveDownload(
                                                            context,
                                                            ExoDownloadService::class.java,
                                                            song.id,
                                                            false,
                                                        )
                                                    }
                                                }
                                                else -> {
                                                    songs.forEach { song ->
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
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }
                                        Download.STATE_DOWNLOADING -> {
                                            CircularProgressIndicator(
                                                strokeWidth = 2.dp,
                                                modifier = Modifier.size(20.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                painter = painterResource(R.drawable.download),
                                                contentDescription = stringResource(R.string.action_download),
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(22.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Description Block (collapsible via ExpandableText)
                        if (!pl.description.isNullOrBlank()) {
                            item(key = "description") {
                                ExpandableText(
                                    text = pl.description ?: "",
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                    collapsedMaxLines = 3,
                                )
                            }
                        }

                        // 4. Anchored Track Count & Duration Section Header
                        item(key = "track_count_header") {
                            val totalDuration = remember(songs) { songs.sumOf { it.duration ?: 0 } }
                            val durationText = remember(songs.size, totalDuration) {
                                buildString {
                                    append(context.resources.getQuantityString(R.plurals.n_song, songs.size, songs.size))
                                    if (totalDuration > 0) {
                                        append(" • ")
                                        val hours = totalDuration / 3600
                                        val minutes = (totalDuration % 3600) / 60
                                        if (hours > 0) {
                                            append("${hours}h ${minutes}m")
                                        } else {
                                            append("${minutes}m")
                                        }
                                    }
                                }
                            }
                            Text(
                                text = durationText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            )
                        }
                    } else {
                        // Spacer when search bar is visible
                        item(key = "search_spacer") {
                            Spacer(
                                modifier = Modifier
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .height(64.dp)
                            )
                        }
                    }

                    // 5. Track Items with Multi-Selection & Native 3-Dot Menus
                    itemsIndexed(
                        items = filteredSongs,
                        key = { _, (_, songItem) -> songItem.id },
                    ) { index, (_, songItem) ->
                        val onCheckedChange: (Boolean) -> Unit = {
                            if (it) {
                                selection.add(songItem.id)
                            } else {
                                selection.remove(songItem.id)
                            }
                        }

                        YouTubeListItem(
                            item = songItem,
                            isActive = mediaMetadata?.id == songItem.id,
                            isPlaying = isPlaying,
                            isSelected = inSelectMode && songItem.id in selection,
                            shape = listItemShape(index, filteredSongs.size),
                            modifier = Modifier
                                .combinedClickable(
                                    enabled = !hideExplicit || !songItem.explicit,
                                    onClick = {
                                        if (inSelectMode) {
                                            onCheckedChange(songItem.id !in selection)
                                        } else if (songItem.id == mediaMetadata?.id) {
                                            playerConnection.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                YouTubePlaylistQueue(
                                                    playlistId = pl.id,
                                                    playlistTitle = pl.title,
                                                    initialSongs = filteredSongs.map { it.second },
                                                    initialContinuation = viewModel.continuation,
                                                    startIndex = index,
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
                                .animateItem(),
                            trailingContent = {
                                if (inSelectMode) {
                                    Checkbox(
                                        checked = songItem.id in selection,
                                        onCheckedChange = onCheckedChange,
                                    )
                                } else {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                YouTubeSongMenu(songItem, navController, menuState::dismiss)
                                            }
                                        }
                                    ) {
                                        Icon(painterResource(R.drawable.more_vert), null)
                                    }
                                }
                            }
                        )
                    }

                    if (isLoadingMore) {
                        item(key = "loading_more") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ContainedLoadingIndicator()
                            }
                        }
                    }

                    if (relatedItems.isNotEmpty() && !isSearching) {
                        item(key = "related_title") {
                            NavigationTitle(
                                title = stringResource(R.string.you_might_also_like),
                                modifier = Modifier.animateItem()
                            )
                        }

                        item(key = "related_items") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem()
                            ) {
                                items(relatedItems) { item ->
                                    YouTubeGridItem(
                                        item = item,
                                        modifier = Modifier
                                            .width(160.dp)
                                            .combinedClickable(
                                                onClick = {
                                                    when (item) {
                                                        is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                        is AlbumItem -> navController.navigate("album/${item.browseId}")
                                                        is ArtistItem -> navController.navigate("artist/${item.id}")
                                                        is SongItem -> playerConnection.playQueue(
                                                            YouTubeQueue(WatchEndpoint(videoId = item.id))
                                                        )
                                                    }
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        when (item) {
                                                            is PlaylistItem -> YouTubePlaylistMenu(
                                                                playlist = item,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                            is SongItem -> YouTubeSongMenu(
                                                                song = item,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                            is AlbumItem -> YouTubeAlbumMenu(
                                                                albumItem = item,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                            is ArtistItem -> YouTubeArtistMenu(
                                                                artist = item,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                        }
                                                    }
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }

                    item(key = "bottom_spacer") {
                        Spacer(Modifier.height(120.dp))
                    }
                }
            }
        }

        // Selection TopAppBar (shown when multi-selection mode is active)
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
                                selection.addAll(filteredSongs.map { it.second.id })
                            }
                        }
                    )
                    IconButton(
                        enabled = selection.isNotEmpty(),
                        onClick = {
                            menuState.show {
                                YouTubeSelectionSongMenu(
                                    songSelection = filteredSongs.filter { it.second.id in selection }
                                        .map { it.second },
                                    onDismiss = menuState::dismiss,
                                    clearAction = onExitSelectionMode,
                                )
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_vert),
                            contentDescription = null,
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

        // Sticky TopAppBar (shown on scroll when not selecting or searching, or fallback while loading)
        AnimatedVisibility(
            visible = (playlist == null) || (shouldHideTopBar && !isSearching && !inSelectMode),
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
                    if (playlist != null) {
                        Text(
                            text = playlist?.title.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                },
                actions = {
                    if (playlist != null) {
                        IconButton(
                            onClick = toggleBookmark,
                        ) {
                            Icon(
                                painter = painterResource(if (isBookmarked) R.drawable.favorite else R.drawable.favorite_border),
                                contentDescription = stringResource(if (isBookmarked) R.string.saved else R.string.save),
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
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
                                playlist?.let { pl ->
                                    menuState.show {
                                        YouTubePlaylistMenu(
                                            playlist = pl,
                                            songs = songs,
                                            coroutineScope = coroutineScope,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}
