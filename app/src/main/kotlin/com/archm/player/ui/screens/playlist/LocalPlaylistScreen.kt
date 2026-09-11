package com.archm.player.ui.screens.playlist

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachReversed
import androidx.compose.ui.util.fastSumBy
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.archm.player.LocalDatabase
import com.archm.player.LocalDownloadUtil
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.LocalSyncUtils
import com.archm.player.R
import com.archm.player.constants.DarkModeKey
import com.archm.player.constants.PlaylistEditLockKey
import com.archm.player.constants.PlaylistSongSortDescendingKey
import com.archm.player.constants.PlaylistSongSortType
import com.archm.player.constants.PlaylistSongSortTypeKey
import com.archm.player.constants.SwipeToRemoveSongKey
import com.archm.player.db.entities.PlaylistSong
import com.archm.player.db.entities.PlaylistSongMap
import com.archm.player.extensions.move
import com.archm.player.extensions.toMediaItem
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.ExoDownloadService
import com.archm.player.playback.queues.ListQueue
import com.archm.player.ui.component.ActionPromptDialog
import com.archm.player.ui.component.CombinedIconButton
import com.archm.player.ui.component.DefaultDialog
import com.archm.player.ui.component.DraggableScrollbar
import com.archm.player.ui.component.EmptyPlaceholder
import com.archm.player.ui.component.ExpandableText
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.OverlayEditButton
import com.archm.player.ui.component.SongListItem
import com.archm.player.ui.component.SortHeader
import com.archm.player.ui.component.TextFieldDialog
import com.archm.player.ui.menu.CustomThumbnailMenu
import com.archm.player.ui.menu.LocalPlaylistMenu
import com.archm.player.ui.menu.SelectionSongMenu
import com.archm.player.ui.menu.SongMenu
import com.archm.player.ui.screens.settings.DarkMode
import com.archm.player.ui.utils.backToMain
import com.archm.player.ui.utils.resize
import com.archm.player.utils.listItemShape
import com.archm.player.utils.makeTimeString
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.utils.rememberPreference
import com.archm.player.utils.reportException
import com.archm.player.viewmodels.LocalPlaylistViewModel
import com.music.innertube.YouTube
import com.music.innertube.models.SongItem
import com.music.innertube.utils.completed
import com.yalantis.ucrop.UCrop
import io.ktor.client.plugins.ClientRequestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.LocalDateTime

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LocalPlaylistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: LocalPlaylistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val coroutineScope = rememberCoroutineScope()
    val downloadUtil = LocalDownloadUtil.current

    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val playlist by viewModel.playlist.collectAsState()
    val songs by viewModel.playlistSongs.collectAsState()

    val mutableSongs = remember { mutableStateListOf<PlaylistSong>() }
    val playlistLength = remember(songs) {
        songs.fastSumBy { it.song.song.duration }
    }
    val (sortType, onSortTypeChange) = rememberEnumPreference(
        PlaylistSongSortTypeKey,
        PlaylistSongSortType.CUSTOM
    )
    val (sortDescending, onSortDescendingChange) = rememberPreference(
        PlaylistSongSortDescendingKey,
        true
    )
    var locked by rememberPreference(PlaylistEditLockKey, defaultValue = true)

    val snackbarHostState = remember { SnackbarHostState() }

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    val filteredSongs = remember(songs, query) {
        if (query.text.isEmpty()) {
            songs
        } else {
            songs.filter { song ->
                song.song.song.title.contains(query.text, ignoreCase = true) ||
                        song.song.artists.fastAny { it.name.contains(query.text, ignoreCase = true) }
            }
        }
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(songs) {
        if (songs.isNotEmpty()) {
            viewModel.fetchSuggestions()
        }
    }

    var inSelectMode by rememberSaveable { mutableStateOf(false) }
    val selection = rememberSaveable(
        saver = listSaver<MutableList<Int>, Int>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf() }
    val onExitSelectionMode = {
        inSelectMode = false
        selection.clear()
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.bufferedWriter().use { writer ->
                            writer.write("Title,Artist,Album,Duration,URL\n")
                            songs.forEach { playlistSong ->
                                val song = playlistSong.song
                                val title = song.song.title.replace("\"", "\"\"")
                                val artist = song.artists.joinToString(", ") { it.name }.replace("\"", "\"\"")
                                val album = song.album?.title?.replace("\"", "\"\"") ?: ""
                                val duration = song.song.duration
                                val url = "https://music.youtube.com/watch?v=${song.song.id}"
                                writer.write("\"$title\",\"$artist\",\"$album\",$duration,$url\n")
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        snackbarHostState.showSnackbar(context.getString(R.string.export_successful))
                    }
                } catch (e: Exception) {
                    reportException(e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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

    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    val editable: Boolean = playlist?.playlist?.isEditable == true

    LaunchedEffect(songs) {
        selection.fastForEachReversed { mapId ->
            if (songs.find { it.map.id == mapId } == null) {
                selection.remove(Integer.valueOf(mapId))
            }
        }
    }

    LaunchedEffect(songs) {
        mutableSongs.apply {
            clear()
            addAll(songs)
        }
        if (songs.isEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState =
                if (songs.all { downloads[it.song.id]?.state == Download.STATE_COMPLETED }) {
                    Download.STATE_COMPLETED
                } else if (songs.all {
                        downloads[it.song.id]?.state == Download.STATE_QUEUED ||
                                downloads[it.song.id]?.state == Download.STATE_DOWNLOADING ||
                                downloads[it.song.id]?.state == Download.STATE_COMPLETED
                    }
                ) {
                    Download.STATE_DOWNLOADING
                } else {
                    Download.STATE_STOPPED
                }
        }
    }

    // Thumbnail customization logic
    val overrideThumbnail = remember { mutableStateOf<String?>(null) }
    var isCustomThumbnail: Boolean by remember(playlist) {
        mutableStateOf(
            playlist?.thumbnails?.firstOrNull()?.let {
                it.contains("studio_square_thumbnail") || it.contains("content://com.echomusic.music")
            } ?: false
        )
    }

    val result = remember { mutableStateOf<Uri?>(null) }
    var pendingCropDestUri by remember { mutableStateOf<Uri?>(null) }
    var showEditNoteDialog by remember { mutableStateOf(false) }

    val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        if (res.resultCode == android.app.Activity.RESULT_OK) {
            val output = res.data?.let { UCrop.getOutput(it) } ?: pendingCropDestUri
            if (output != null) result.value = output
        }
    }

    val (darkMode, _) = rememberEnumPreference(
        DarkModeKey,
        defaultValue = DarkMode.AUTO
    )
    val cropColor = MaterialTheme.colorScheme
    val darkTheme = darkMode == DarkMode.ON || (darkMode == DarkMode.AUTO && isSystemInDarkTheme())

    val pickLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { sourceUri ->
            val destFile = java.io.File(context.cacheDir, "playlist_cover_crop_${System.currentTimeMillis()}.jpg")
            val destUri = FileProvider.getUriForFile(context, "${context.packageName}.FileProvider", destFile)
            pendingCropDestUri = destUri

            val options = UCrop.Options().apply {
                setCompressionFormat(Bitmap.CompressFormat.JPEG)
                setCompressionQuality(90)
                setHideBottomControls(true)
                setToolbarTitle(context.getString(R.string.edit_playlist_cover))
                setStatusBarLight(!darkTheme)
                setToolbarColor(cropColor.surface.toArgb())
                setToolbarWidgetColor(cropColor.inverseSurface.toArgb())
                setRootViewBackgroundColor(cropColor.surface.toArgb())
                setLogoColor(cropColor.surface.toArgb())
            }

            val intent = UCrop.of(sourceUri, destUri)
                .withAspectRatio(1f, 1f)
                .withOptions(options)
                .getIntent(context)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            cropLauncher.launch(intent)
        }
    }

    LaunchedEffect(result.value) {
        val uri = result.value ?: return@LaunchedEffect
        val currentPlaylist = playlist ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            when {
                currentPlaylist.playlist.browseId == null -> {
                    overrideThumbnail.value = uri.toString()
                    isCustomThumbnail = true
                    database.query {
                        update(currentPlaylist.playlist.copy(thumbnailUrl = uri.toString()))
                    }
                }
                else -> {
                    val bytes = uriToByteArray(context, uri)
                    if (bytes != null) {
                        YouTube.uploadCustomThumbnailLink(
                            currentPlaylist.playlist.browseId!!,
                            bytes
                        ).onSuccess { newThumbnailUrl ->
                            overrideThumbnail.value = newThumbnailUrl
                            isCustomThumbnail = true
                            database.query {
                                update(currentPlaylist.playlist.copy(thumbnailUrl = newThumbnailUrl))
                            }
                        }.onFailure {
                            if (it is ClientRequestException) {
                                snackbarHostState.showSnackbar("Applied locally (${it.response.status.value} ${it.response.status.description})")
                            }
                            reportException(it)
                            overrideThumbnail.value = uri.toString()
                            isCustomThumbnail = true
                            database.query {
                                update(currentPlaylist.playlist.copy(thumbnailUrl = uri.toString()))
                            }
                        }
                    }
                }
            }
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }
    if (showEditDialog) {
        playlist?.playlist?.let { playlistEntity ->
            TextFieldDialog(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.edit),
                        contentDescription = null
                    )
                },
                title = { Text(text = stringResource(R.string.edit_playlist)) },
                onDismiss = { showEditDialog = false },
                initialTextFieldValue = TextFieldValue(
                    playlistEntity.name,
                    TextRange(playlistEntity.name.length)
                ),
                onDone = { name ->
                    database.query {
                        update(
                            playlistEntity.copy(
                                name = name,
                                lastUpdateTime = LocalDateTime.now()
                            )
                        )
                    }
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        playlistEntity.browseId?.let { YouTube.renamePlaylist(it, name) }
                    }
                },
            )
        }
    }

    var showRemoveDownloadDialog by remember { mutableStateOf(false) }
    if (showRemoveDownloadDialog) {
        DefaultDialog(
            onDismiss = { showRemoveDownloadDialog = false },
            content = {
                Text(
                    text = stringResource(
                        R.string.remove_download_playlist_confirm,
                        playlist?.playlist?.name.orEmpty()
                    ),
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
                        if (!editable) {
                            database.transaction {
                                playlist?.id?.let { clearPlaylist(it) }
                            }
                        }
                        songs.forEach { song ->
                            DownloadService.sendRemoveDownload(
                                context,
                                ExoDownloadService::class.java,
                                song.song.id,
                                false
                            )
                        }
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
        )
    }

    var showDeletePlaylistDialog by remember { mutableStateOf(false) }
    if (showDeletePlaylistDialog) {
        DefaultDialog(
            onDismiss = { showDeletePlaylistDialog = false },
            content = {
                Text(
                    text = stringResource(
                        R.string.delete_playlist_confirm,
                        playlist?.playlist?.name.orEmpty()
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            },
            buttons = {
                TextButton(
                    onClick = { showDeletePlaylistDialog = false }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
                TextButton(
                    onClick = {
                        showDeletePlaylistDialog = false
                        database.query {
                            playlist?.let { delete(it.playlist) }
                        }
                        viewModel.viewModelScope.launch(Dispatchers.IO) {
                            playlist?.playlist?.browseId?.let { YouTube.deletePlaylist(it) }
                        }
                        navController.popBackStack()
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        )
    }

    var showAiModifyDialog by rememberSaveable { mutableStateOf(false) }
    if (showAiModifyDialog && playlist != null) {
        AiModifyPlaylistDialog(
            playlistId = playlist!!.id,
            currentSongs = songs,
            onDismiss = { showAiModifyDialog = false }
        )
    }

    if (showEditNoteDialog) {
        ActionPromptDialog(
            title = stringResource(R.string.edit_playlist_cover),
            onDismiss = { showEditNoteDialog = false },
            onConfirm = {
                showEditNoteDialog = false
                pickLauncher.launch(
                    PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onCancel = { showEditNoteDialog = false }
        ) {
            if (playlist?.playlist?.browseId != null) {
                Text(
                    text = stringResource(R.string.edit_playlist_cover_note),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
            }
            Text(
                text = stringResource(R.string.edit_playlist_cover_note_wait),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }

    val headerItems = 3
    val lazyListState = rememberLazyListState()
    var dragInfo by remember {
        mutableStateOf<Pair<Int, Int>?>(null)
    }
    val reorderableState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        scrollThresholdPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) { from, to ->
        if (to.index >= headerItems && from.index >= headerItems) {
            val currentDragInfo = dragInfo
            dragInfo = if (currentDragInfo == null) {
                (from.index - headerItems) to (to.index - headerItems)
            } else {
                currentDragInfo.first to (to.index - headerItems)
            }

            mutableSongs.move(from.index - headerItems, to.index - headerItems)
        }
    }

    LaunchedEffect(reorderableState.isAnyItemDragging) {
        if (!reorderableState.isAnyItemDragging) {
            dragInfo?.let { (from, to) ->
                database.transaction {
                    move(viewModel.playlistId, from, to)
                }

                if (viewModel.playlist.value?.playlist?.browseId != null) {
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        val playlistSongMap = database.playlistSongMaps(viewModel.playlistId, 0)
                        val successorIndex = if (from > to) to else to + 1
                        val successorSetVideoId = playlistSongMap.getOrNull(successorIndex)?.setVideoId

                        playlistSongMap.getOrNull(from)?.setVideoId?.let { setVideoId ->
                            YouTube.moveSongPlaylist(
                                viewModel.playlist.value?.playlist?.browseId!!,
                                setVideoId,
                                successorSetVideoId
                            )
                        }
                    }
                }

                dragInfo = null
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

    val isBookmarked = playlist?.playlist?.bookmarkedAt != null
    val toggleBookmark: () -> Unit = {
        coroutineScope.launch(Dispatchers.IO) {
            playlist?.playlist?.let { pl ->
                database.query {
                    update(pl.toggleLike())
                }
            }
        }
    }

    val isPlaylistPlaying = remember(songs, mediaMetadata?.id) {
        songs.isNotEmpty() && songs.any { it.song.id == mediaMetadata?.id }
    }

    val hasExplicitContent = remember(songs) {
        songs.any { it.song.song.explicit }
    }

    val songCount = if (playlist?.songCount == 0 && (playlist?.playlist?.remoteSongCount ?: 0) != 0) {
        playlist?.playlist?.remoteSongCount ?: 0
    } else {
        playlist?.songCount ?: songs.size
    }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenWidthDp < configuration.screenHeightDp
    val heroHeight = if (isPortrait) (configuration.screenHeightDp / 2).dp else 280.dp
    val gradientHeight = if (isPortrait) (configuration.screenHeightDp * 0.35f).dp else 180.dp

    val backdropThumbnail = overrideThumbnail.value
        ?: playlist?.thumbnails?.firstOrNull()
        ?: songs.firstOrNull()?.song?.thumbnailUrl
        ?: playlist?.playlist?.thumbnailUrl

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
            val pl = playlist
            if (pl == null || (pl.songCount == 0 && (pl.playlist.remoteSongCount ?: 0) == 0 && songs.isEmpty())) {
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
                                    text = pl.playlist.name,
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
                                            LocalPlaylistMenu(
                                                playlist = pl,
                                                songs = songs,
                                                context = context,
                                                downloadState = downloadState,
                                                onEdit = { showEditDialog = true },
                                                onSync = {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        val browseId = pl.playlist.browseId ?: return@launch
                                                        val playlistPage = YouTube.playlist(browseId)
                                                            .completed()
                                                            .getOrNull() ?: return@launch
                                                        database.transaction {
                                                            clearPlaylist(pl.id)
                                                            playlistPage.songs
                                                                .map(SongItem::toMediaMetadata)
                                                                .onEach(::insert)
                                                                .mapIndexed { position, song ->
                                                                    PlaylistSongMap(
                                                                        songId = song.id,
                                                                        playlistId = pl.id,
                                                                        position = position,
                                                                        setVideoId = song.setVideoId
                                                                    )
                                                                }
                                                                .forEach(::insert)
                                                        }
                                                    }
                                                    coroutineScope.launch(Dispatchers.Main) {
                                                        snackbarHostState.showSnackbar(context.getString(R.string.playlist_synced))
                                                    }
                                                },
                                                onDelete = { showDeletePlaylistDialog = true },
                                                onModifyWithAi = { showAiModifyDialog = true },
                                                onDownload = {
                                                    when (downloadState) {
                                                        Download.STATE_COMPLETED -> showRemoveDownloadDialog = true
                                                        Download.STATE_DOWNLOADING -> {
                                                            songs.forEach { song ->
                                                                DownloadService.sendRemoveDownload(
                                                                    context,
                                                                    ExoDownloadService::class.java,
                                                                    song.song.id,
                                                                    false
                                                                )
                                                            }
                                                        }
                                                        else -> {
                                                            songs.forEach { song ->
                                                                val downloadRequest = DownloadRequest
                                                                    .Builder(song.song.id, song.song.id.toUri())
                                                                    .setCustomCacheKey(song.song.id)
                                                                    .setData(song.song.song.title.toByteArray())
                                                                    .build()
                                                                DownloadService.sendAddDownload(
                                                                    context,
                                                                    ExoDownloadService::class.java,
                                                                    downloadRequest,
                                                                    false
                                                                )
                                                            }
                                                        }
                                                    }
                                                },
                                                onExport = {
                                                    exportCsvLauncher.launch("${pl.playlist.name}_export.csv")
                                                },
                                                onQueue = {
                                                    playerConnection.addToQueue(
                                                        items = songs.map { it.song.toMediaItem() }
                                                    )
                                                },
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

                            // Custom thumbnail overlay button if editable
                            if (editable) {
                                OverlayEditButton(
                                    visible = true,
                                    alignment = Alignment.BottomEnd,
                                    onClick = {
                                        if (isCustomThumbnail) {
                                            menuState.show {
                                                CustomThumbnailMenu(
                                                    onEdit = {
                                                        pickLauncher.launch(
                                                            PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                        )
                                                    },
                                                    onRemove = {
                                                        when {
                                                            pl.playlist.browseId == null -> {
                                                                overrideThumbnail.value = null
                                                                database.query {
                                                                    update(pl.playlist.copy(thumbnailUrl = null))
                                                                }
                                                            }
                                                            else -> {
                                                                coroutineScope.launch(Dispatchers.IO) {
                                                                    YouTube.removeThumbnailPlaylist(pl.playlist.browseId!!).onSuccess { newThumbnailUrl ->
                                                                        overrideThumbnail.value = newThumbnailUrl
                                                                        database.query {
                                                                            update(pl.playlist.copy(thumbnailUrl = newThumbnailUrl))
                                                                        }
                                                                    }.onFailure {
                                                                        overrideThumbnail.value = null
                                                                        database.query {
                                                                            update(pl.playlist.copy(thumbnailUrl = null))
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        isCustomThumbnail = false
                                                    },
                                                    onDismiss = menuState::dismiss
                                                )
                                            }
                                        } else {
                                            showEditNoteDialog = true
                                        }
                                    }
                                )
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
                                        if (songs.isNotEmpty()) {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = pl.playlist.name,
                                                    items = songs.shuffled().map { it.song.toMediaItem() },
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
                                        } else if (songs.isNotEmpty()) {
                                            playerConnection.playQueue(
                                                ListQueue(
                                                    title = pl.playlist.name,
                                                    items = songs.map { it.song.toMediaItem() },
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
                                    .clickable {
                                        when (downloadState) {
                                            Download.STATE_COMPLETED -> showRemoveDownloadDialog = true
                                            Download.STATE_DOWNLOADING -> {
                                                songs.forEach { song ->
                                                    DownloadService.sendRemoveDownload(
                                                        context,
                                                        ExoDownloadService::class.java,
                                                        song.song.id,
                                                        false,
                                                    )
                                                }
                                            }
                                            else -> {
                                                songs.forEach { song ->
                                                    val downloadRequest = DownloadRequest
                                                        .Builder(song.song.id, song.song.id.toUri())
                                                        .setCustomCacheKey(song.song.id)
                                                        .setData(song.song.song.title.toByteArray())
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
                            val staticDescription = remember(songCount, playlistLength) {
                                val name = pl.playlist.name
                                val trackCountText = context.resources.getQuantityString(R.plurals.n_song, songCount, songCount)
                                "$name is a custom playlist featuring $trackCountText.${
                                    if (playlistLength > 0) " Combined duration is ${makeTimeString(playlistLength * 1000L)}." else ""
                                }"
                            }

                            ExpandableText(
                                text = staticDescription,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
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
                                            PlaylistSongSortType.CUSTOM -> R.string.sort_by_custom
                                            PlaylistSongSortType.CREATE_DATE -> R.string.sort_by_create_date
                                            PlaylistSongSortType.NAME -> R.string.sort_by_name
                                            PlaylistSongSortType.ARTIST -> R.string.sort_by_artist
                                            PlaylistSongSortType.PLAY_TIME -> R.string.sort_by_play_time
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                                if (editable) {
                                    val description = if (locked) "Unlock playlist" else "Lock playlist"
                                    TooltipBox(
                                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                                        tooltip = { PlainTooltip { Text(description) } },
                                        state = rememberTooltipState(),
                                    ) {
                                        FilledIconToggleButton(
                                            checked = locked,
                                            onCheckedChange = { locked = it },
                                            modifier = Modifier.padding(horizontal = 6.dp),
                                        ) {
                                            if (locked) {
                                                Icon(
                                                    painter = painterResource(R.drawable.lock),
                                                    contentDescription = description,
                                                )
                                            } else {
                                                Icon(
                                                    painter = painterResource(R.drawable.lock_open),
                                                    contentDescription = description,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Anchored Track Count & Duration Section Header
                            val durationText = remember(songCount, playlistLength) {
                                buildString {
                                    append(context.resources.getQuantityString(R.plurals.n_song, songCount, songCount))
                                    if (playlistLength > 0) {
                                        append(" • ")
                                        append(makeTimeString(playlistLength * 1000L))
                                    }
                                }
                            }
                            Text(
                                text = durationText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
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
                    items = if (isSearching) filteredSongs else mutableSongs,
                    key = { _, song -> song.map.id },
                ) { index, song ->
                    ReorderableItem(
                        state = reorderableState,
                        key = song.map.id,
                    ) {
                        val currentItem by rememberUpdatedState(song)

                        fun deleteFromPlaylist() {
                            database.transaction {
                                coroutineScope.launch {
                                    pl.playlist.browseId?.let { browseId ->
                                        val setVideoId = getSetVideoId(currentItem.map.songId)
                                        setVideoId?.setVideoId?.let { setVideoIdValue ->
                                            YouTube.removeFromPlaylist(
                                                browseId,
                                                currentItem.map.songId,
                                                setVideoIdValue
                                            )
                                        }
                                    }
                                }
                                move(
                                    currentItem.map.playlistId,
                                    currentItem.map.position,
                                    Int.MAX_VALUE
                                )
                                delete(currentItem.map.copy(position = Int.MAX_VALUE))
                            }
                        }

                        val swipeRemoveEnabled by rememberPreference(SwipeToRemoveSongKey, defaultValue = false)
                        val dismissBoxState = rememberSwipeToDismissBoxState(
                            positionalThreshold = { totalDistance -> totalDistance }
                        )
                        var processedDismiss by remember { mutableStateOf(false) }
                        LaunchedEffect(dismissBoxState.currentValue) {
                            val dv = dismissBoxState.currentValue
                            if (swipeRemoveEnabled && !processedDismiss && (
                                    dv == SwipeToDismissBoxValue.StartToEnd ||
                                    dv == SwipeToDismissBoxValue.EndToStart
                                )
                            ) {
                                processedDismiss = true
                                deleteFromPlaylist()
                            }
                            if (dv == SwipeToDismissBoxValue.Settled) {
                                processedDismiss = false
                            }
                        }

                        val onCheckedChange: (Boolean) -> Unit = {
                            if (it) {
                                selection.add(song.map.id)
                            } else {
                                selection.remove(Integer.valueOf(song.map.id))
                            }
                        }

                        val content: @Composable () -> Unit = {
                            SongListItem(
                                song = song.song,
                                isActive = song.song.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                showInLibraryIcon = true,
                                shape = listItemShape(
                                    index = index,
                                    count = if (isSearching) filteredSongs.size else mutableSongs.size
                                ),
                                trailingContent = {
                                    if (inSelectMode) {
                                        Checkbox(
                                            checked = selection.contains(song.map.id),
                                            onCheckedChange = onCheckedChange
                                        )
                                    } else {
                                        IconButton(
                                            onClick = {
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song.song,
                                                        playlistSong = song,
                                                        playlistBrowseId = pl.playlist.browseId,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.more_vert),
                                                contentDescription = null,
                                            )
                                        }

                                        if (sortType == PlaylistSongSortType.CUSTOM && !locked && !inSelectMode && !isSearching && editable) {
                                            IconButton(
                                                onClick = { },
                                                modifier = Modifier.draggableHandle(),
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.drag_handle),
                                                    contentDescription = null,
                                                )
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (inSelectMode) {
                                                onCheckedChange(!selection.contains(song.map.id))
                                            } else if (song.song.id == mediaMetadata?.id) {
                                                playerConnection.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = pl.playlist.name,
                                                        items = songs.map { it.song.toMediaItem() },
                                                        startIndex = songs.indexOfFirst { it.map.id == song.map.id },
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
                                    ),
                            )
                        }

                        if (locked || inSelectMode || !swipeRemoveEnabled) {
                            Box(modifier = Modifier.animateItem()) {
                                content()
                            }
                        } else {
                            SwipeToDismissBox(
                                state = dismissBoxState,
                                backgroundContent = {},
                                modifier = Modifier.animateItem()
                            ) {
                                content()
                            }
                        }
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
                        checked = selection.size == songs.size && selection.isNotEmpty(),
                        onCheckedChange = {
                            if (selection.size == songs.size) {
                                selection.clear()
                            } else {
                                selection.clear()
                                selection.addAll(songs.map { it.map.id })
                            }
                        }
                    )
                    IconButton(
                        enabled = selection.isNotEmpty(),
                        onClick = {
                            menuState.show {
                                SelectionSongMenu(
                                    songSelection = selection.mapNotNull { mapId ->
                                        songs.find { it.map.id == mapId }?.song
                                    },
                                    songPosition = selection.mapNotNull { mapId ->
                                        songs.find { it.map.id == mapId }?.map
                                    },
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
                            text = playlist?.playlist?.name.orEmpty(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                },
                actions = {
                    val pl = playlist
                    if (pl != null) {
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
                                menuState.show {
                                    LocalPlaylistMenu(
                                        playlist = pl,
                                        songs = songs,
                                        context = context,
                                        downloadState = downloadState,
                                        onEdit = { showEditDialog = true },
                                        onSync = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                val browseId = pl.playlist.browseId ?: return@launch
                                                val playlistPage = YouTube.playlist(browseId)
                                                    .completed()
                                                    .getOrNull() ?: return@launch
                                                database.transaction {
                                                    clearPlaylist(pl.id)
                                                    playlistPage.songs
                                                        .map(SongItem::toMediaMetadata)
                                                        .onEach(::insert)
                                                        .mapIndexed { position, song ->
                                                            PlaylistSongMap(
                                                                songId = song.id,
                                                                playlistId = pl.id,
                                                                position = position,
                                                                setVideoId = song.setVideoId
                                                            )
                                                        }
                                                        .forEach(::insert)
                                                }
                                            }
                                            coroutineScope.launch(Dispatchers.Main) {
                                                snackbarHostState.showSnackbar(context.getString(R.string.playlist_synced))
                                            }
                                        },
                                        onDelete = { showDeletePlaylistDialog = true },
                                        onModifyWithAi = { showAiModifyDialog = true },
                                        onDownload = {
                                            when (downloadState) {
                                                Download.STATE_COMPLETED -> showRemoveDownloadDialog = true
                                                Download.STATE_DOWNLOADING -> {
                                                    songs.forEach { song ->
                                                        DownloadService.sendRemoveDownload(
                                                            context,
                                                            ExoDownloadService::class.java,
                                                            song.song.id,
                                                            false
                                                        )
                                                    }
                                                }
                                                else -> {
                                                    songs.forEach { song ->
                                                        val downloadRequest = DownloadRequest
                                                            .Builder(song.song.id, song.song.id.toUri())
                                                            .setCustomCacheKey(song.song.id)
                                                            .setData(song.song.song.title.toByteArray())
                                                            .build()
                                                        DownloadService.sendAddDownload(
                                                            context,
                                                            ExoDownloadService::class.java,
                                                            downloadRequest,
                                                            false
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onExport = {
                                            exportCsvLauncher.launch("${pl.playlist.name}_export.csv")
                                        },
                                        onQueue = {
                                            playerConnection.addToQueue(
                                                items = songs.map { it.song.toMediaItem() }
                                            )
                                        },
                                        onDismiss = menuState::dismiss,
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

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.union(WindowInsets.ime))
                .align(Alignment.BottomCenter),
        )
    }
}

fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
    } catch (_: SecurityException) {
        null
    }
}
