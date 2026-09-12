package com.archm.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ripple
import com.music.innertube.models.BrowseEndpoint
import androidx.compose.ui.util.fastForEachIndexed
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
import com.archm.player.constants.AlbumCanvasEnabledKey
import com.archm.player.constants.DataSaverEnabledKey
import com.archm.player.constants.HideExplicitKey
import com.archm.player.constants.HideVideoSongsKey
import com.archm.player.db.entities.Album
import com.archm.player.playback.ExoDownloadService
import com.archm.player.playback.queues.LocalAlbumRadio
import com.archm.player.ui.component.CombinedIconButton
import com.archm.player.ui.component.ExpandableText
import com.archm.player.ui.component.LinkSegment
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.NavigationTitle
import com.archm.player.ui.component.SongListItem
import com.archm.player.ui.component.YouTubeGridItem
import com.archm.player.ui.menu.AlbumMenu
import com.archm.player.ui.menu.SelectionSongMenu
import com.archm.player.ui.menu.SongMenu
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.player.CanvasArtworkPlayer
import com.archm.player.ui.utils.backToMain
import com.archm.player.ui.utils.resize
import com.archm.player.utils.rememberPreference
import com.archm.player.viewmodels.AlbumViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlbumScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val scope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return

    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val playlistId by viewModel.playlistId.collectAsState()
    val albumWithSongs by viewModel.albumWithSongs.collectAsState()
    val otherVersions by viewModel.otherVersions.collectAsState()
    val releasesForYou by viewModel.releasesForYou.collectAsState()
    val moreByArtist by viewModel.moreByArtist.collectAsState()
    val moreByArtistEndpoint by viewModel.moreByArtistEndpoint.collectAsState()
    val description by viewModel.description.collectAsState()
    val descriptionRuns by viewModel.descriptionRuns.collectAsState()

    val currentAlbumId = viewModel.albumId
    val moreByArtistAlbums = remember(moreByArtist, currentAlbumId) {
        moreByArtist.filter { it.id != currentAlbumId }.distinctBy { it.id }.take(7)
    }
    val distinctRecommendations = remember(releasesForYou) {
        releasesForYou.distinctBy { it.id }
    }
    val primaryArtist = albumWithSongs?.artists?.firstOrNull()
    val artistName = primaryArtist?.name ?: "Artist"
    val onMoreByArtistClick: (() -> Unit)? = primaryArtist?.id?.let { artistId ->
        {
            val endpoint = moreByArtistEndpoint
            if (endpoint != null) {
                navController.navigate(
                    buildArtistItemsRoute(
                        artistId = artistId,
                        endpoint = endpoint,
                        title = "Albums",
                        artistName = artistName,
                    )
                )
            } else {
                navController.navigate("artist/$artistId/albums")
            }
        }
    }
    val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)
    val dataSaverEnabled by rememberPreference(key = DataSaverEnabledKey, defaultValue = false)
    val hideVideoSongsPref by rememberPreference(key = HideVideoSongsKey, defaultValue = false)
    val hideVideoSongs = if (dataSaverEnabled) true else hideVideoSongsPref
    val albumCanvasEnabledPref by rememberPreference(key = AlbumCanvasEnabledKey, defaultValue = false)
    val albumCanvasEnabled = if (dataSaverEnabled) false else albumCanvasEnabledPref

    val canvasArtwork = rememberAlbumCanvas(
        albumTitle = albumWithSongs?.album?.title,
        artistName = albumWithSongs?.artists?.firstOrNull()?.name,
        firstSongTitle = albumWithSongs?.songs?.firstOrNull()?.song?.title
    )

    val filteredSongs = remember(albumWithSongs, hideExplicit, hideVideoSongs) {
        var songs = albumWithSongs?.songs ?: emptyList()
        if (hideExplicit) {
            songs = songs.filter { !it.song.explicit }
        }
        if (hideVideoSongs) {
            songs = songs.filter { !it.song.isVideo }
        }
        songs
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
    if (inSelectMode) {
        BackHandler(onBack = onExitSelectionMode)
    }

    LaunchedEffect(filteredSongs) {
        selection.fastForEachReversed { songId ->
            if (filteredSongs.find { it.id == songId } == null) {
                selection.remove(songId)
            }
        }
    }

    val downloadUtil = LocalDownloadUtil.current
    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    LaunchedEffect(albumWithSongs) {
        val songs = albumWithSongs?.songs?.map { it.id }
        if (songs.isNullOrEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState =
                if (songs.all { downloads[it]?.state == Download.STATE_COMPLETED }) {
                    Download.STATE_COMPLETED
                } else if (songs.all {
                        downloads[it]?.state == Download.STATE_QUEUED ||
                                downloads[it]?.state == Download.STATE_DOWNLOADING ||
                                downloads[it]?.state == Download.STATE_COMPLETED
                    }
                ) {
                    Download.STATE_DOWNLOADING
                } else {
                    Download.STATE_STOPPED
                }
        }
    }

    val hasExplicitContent = remember(albumWithSongs) {
        albumWithSongs?.album?.explicit == true
    }

    val lazyListState = rememberLazyListState()

    val firstItemVisible by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex == 0 }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenWidthDp < configuration.screenHeightDp
    val heroHeight = if (isPortrait) (configuration.screenHeightDp / 2).dp else 280.dp
    val gradientHeight = if (isPortrait) (configuration.screenHeightDp * 0.35f).dp else 180.dp

    val backdropThumbnail = albumWithSongs?.album?.thumbnailUrl
        ?: filteredSongs.firstOrNull()?.song?.thumbnailUrl

    val isBookmarked = albumWithSongs?.album?.bookmarkedAt != null

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
            val currentAlbumWithSongs = albumWithSongs
            if (currentAlbumWithSongs == null || currentAlbumWithSongs.songs.isEmpty()) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ContainedLoadingIndicator()
                    }
                }
            } else {
                // 1. Hero Header
                item(key = "hero_header") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(heroHeight)
                    ) {
                        AsyncImage(
                            model = backdropThumbnail?.resize(1080, 1080) ?: backdropThumbnail,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize(),
                        )

                        if (albumCanvasEnabled && canvasArtwork != null) {
                            CanvasArtworkPlayer(
                                primaryUrl = canvasArtwork.animated,
                                fallbackUrl = canvasArtwork.videoUrl,
                                isPlaying = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

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
                                text = currentAlbumWithSongs.album.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                textAlign = TextAlign.Center,
                            )
                            if (currentAlbumWithSongs.artists.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                if (currentAlbumWithSongs.artists.size == 1) {
                                    val artist = currentAlbumWithSongs.artists.first()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.combinedClickable(
                                            onClick = {
                                                navController.navigate("artist/${artist.id}")
                                            }
                                        )
                                    ) {
                                        artist.thumbnailUrl?.let { thumb ->
                                            AsyncImage(
                                                model = thumb,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = artist.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                        )
                                    }
                                } else {
                                    Text(
                                        text = currentAlbumWithSongs.artists.joinToString(", ") { it.name },
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                val subText = buildString {
                                    append(stringResource(R.string.album_text))
                                    if (currentAlbumWithSongs.album.year != null) {
                                        append(" • ${currentAlbumWithSongs.album.year}")
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
                                onClick = {
                                    database.query {
                                        update(currentAlbumWithSongs.album.toggleLike())
                                    }
                                },
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
                                    menuState.show {
                                        AlbumMenu(
                                            originalAlbum = Album(
                                                currentAlbumWithSongs.album,
                                                currentAlbumWithSongs.artists
                                            ),
                                            navController = navController,
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
                                    playerConnection.service.getAutomix(playlistId)
                                    playerConnection.playQueue(
                                        LocalAlbumRadio(currentAlbumWithSongs.copy(songs = currentAlbumWithSongs.songs.shuffled())),
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.shuffle),
                                contentDescription = stringResource(R.string.shuffle_label),
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        // Play pill button (48dp height, minWidth 110dp)
                        val isAlbumPlaying = isPlaying && mediaMetadata?.album?.id == currentAlbumWithSongs.album.id
                        Box(
                            modifier = Modifier
                                .height(48.dp)
                                .widthIn(min = 110.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable {
                                    if (isPlaying && mediaMetadata?.album?.id == currentAlbumWithSongs.album.id) {
                                        playerConnection.player.pause()
                                    } else if (mediaMetadata?.album?.id == currentAlbumWithSongs.album.id) {
                                        playerConnection.player.play()
                                    } else {
                                        playerConnection.service.getAutomix(playlistId)
                                        playerConnection.playQueue(
                                            LocalAlbumRadio(currentAlbumWithSongs)
                                        )
                                    }
                                }
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(if (isAlbumPlaying) R.drawable.pause else R.drawable.play),
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp),
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAlbumPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
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
                                            currentAlbumWithSongs.songs.forEach { song ->
                                                DownloadService.sendRemoveDownload(
                                                    context,
                                                    ExoDownloadService::class.java,
                                                    song.id,
                                                    false,
                                                )
                                            }
                                        }
                                        else -> {
                                            currentAlbumWithSongs.songs.forEach { song ->
                                                val downloadRequest = DownloadRequest
                                                    .Builder(song.id, song.id.toUri())
                                                    .setCustomCacheKey(song.id)
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

                // 3. Anchored Track Count & Duration Section Header
                item(key = "track_count_header") {
                    val totalDuration = remember(currentAlbumWithSongs.songs) { currentAlbumWithSongs.songs.sumOf { it.song.duration } }
                    val durationText = remember(currentAlbumWithSongs.songs.size, totalDuration) {
                        buildString {
                            append(context.resources.getQuantityString(R.plurals.n_song, currentAlbumWithSongs.songs.size, currentAlbumWithSongs.songs.size))
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
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                // 4. Track Items with Multi-Selection & Native 3-Dot Menus
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
                        isActive = song.id == mediaMetadata?.id,
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
                            .animateItem()
                            .clip(RoundedCornerShape(12.dp))
                            .combinedClickable(
                                onClick = {
                                    if (inSelectMode) {
                                        onCheckedChange(song.id !in selection)
                                    } else if (song.id == mediaMetadata?.id) {
                                        playerConnection.togglePlayPause()
                                    } else {
                                        playerConnection.service.getAutomix(playlistId)
                                        playerConnection.playQueue(
                                            LocalAlbumRadio(currentAlbumWithSongs, startIndex = index),
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

                // 5. Other Versions Carousel
                if (otherVersions.isNotEmpty()) {
                    item(key = "other_versions_title") {
                        NavigationTitle(
                            title = stringResource(R.string.other_versions),
                            modifier = Modifier.animateItem()
                        )
                    }
                    item(key = "other_versions_list") {
                        LazyRow(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        ) {
                            items(
                                items = otherVersions.distinctBy { it.id },
                                key = { it.id },
                            ) { item ->
                                YouTubeGridItem(
                                    item = item,
                                    isActive = mediaMetadata?.album?.id == item.id,
                                    isPlaying = isPlaying,
                                    coroutineScope = scope,
                                    thumbnailSize = 150.dp,
                                    contentPadding = PaddingValues(0.dp),
                                    thumbnailCornerRadius = 12.dp,
                                    onClick = { navController.navigate("album/${item.id}") },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubeAlbumMenu(
                                                albumItem = item,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }

                // 5.5 More By Artist Carousel
                if (moreByArtistAlbums.isNotEmpty()) {
                    item(key = "more_by_artist_header") {
                        AlbumSectionHeader(
                            title = "More by $artistName",
                            bottomSpacing = 9.dp,
                            onMoreClick = onMoreByArtistClick,
                        )
                    }

                    item(key = "more_by_artist_list") {
                        LazyRow(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                        ) {
                            items(
                                items = moreByArtistAlbums,
                                key = { "more_by_artist_${it.id}" },
                            ) { item ->
                                YouTubeGridItem(
                                    item = item,
                                    isActive = mediaMetadata?.album?.id == item.id,
                                    isPlaying = isPlaying,
                                    coroutineScope = scope,
                                    thumbnailSize = 150.dp,
                                    contentPadding = PaddingValues(0.dp),
                                    thumbnailCornerRadius = 12.dp,
                                    onClick = { navController.navigate("album/${item.id}") },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubeAlbumMenu(
                                                albumItem = item,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }

                // 6. You Might Also Like 2-Column Grid
                if (distinctRecommendations.isNotEmpty()) {
                    item(key = "releases_for_you_title") {
                        NavigationTitle(
                            title = stringResource(R.string.you_might_also_like),
                            modifier = Modifier.animateItem()
                        )
                    }
                    item(key = "releases_for_you_grid") {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(bottom = 12.dp)
                                .animateItem(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            distinctRecommendations.forEach { item ->
                                YouTubeGridItem(
                                    item = item,
                                    isActive = mediaMetadata?.album?.id == item.id,
                                    isPlaying = isPlaying,
                                    coroutineScope = scope,
                                    thumbnailSize = 150.dp,
                                    contentPadding = PaddingValues(0.dp),
                                    thumbnailCornerRadius = 12.dp,
                                    onClick = { navController.navigate("album/${item.id}") },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        menuState.show {
                                            YouTubeAlbumMenu(
                                                albumItem = item,
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

                // 7. Relocated About Section at the bottom of LazyColumn
                item(key = "about_album_section") {
                    val staticDescription = remember(currentAlbumWithSongs) {
                        "${currentAlbumWithSongs.album.title} is an album by ${currentAlbumWithSongs.artists.joinToString { it.name }}${
                            if (currentAlbumWithSongs.album.year != null) ", released in ${currentAlbumWithSongs.album.year}" else ""
                        }. This collection features ${currentAlbumWithSongs.songs.size} tracks showcasing their musical artistry."
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.about_album),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExpandableText(
                            text = description ?: staticDescription,
                            runs = descriptionRuns?.map {
                                LinkSegment(
                                    text = it.text,
                                    url = it.navigationEndpoint?.urlEndpoint?.url
                                )
                            },
                            collapsedMaxLines = 3
                        )

                        if (currentAlbumWithSongs.artists.size > 1) {
                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = buildAnnotatedString {
                                    append(stringResource(R.string.by_text))
                                    append(" ")
                                    currentAlbumWithSongs.artists.fastForEachIndexed { index, artist ->
                                        val link = LinkAnnotation.Clickable(artist.id) {
                                            navController.navigate("artist/${artist.id}")
                                        }
                                        withLink(link) {
                                            append(artist.name)
                                        }
                                        if (index != currentAlbumWithSongs.artists.lastIndex) {
                                            append(", ")
                                        }
                                    }
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }

                item(key = "bottom_spacer") {
                    Spacer(Modifier.height(120.dp))
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
                    IconButton(onClick = onExitSelectionMode) {
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
                                    songSelection = selection.mapNotNull { songId ->
                                        filteredSongs.find { it.id == songId }
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

        // Sticky TopAppBar (shown on scroll when not selecting, or fallback while loading)
        AnimatedVisibility(
            visible = (albumWithSongs == null) || (shouldHideTopBar && !inSelectMode),
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
                    albumWithSongs?.let {
                        Text(
                            text = it.album.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                },
                actions = {
                    albumWithSongs?.let { currentAlbumWithSongs ->
                        val currentBookmarked = currentAlbumWithSongs.album.bookmarkedAt != null
                        IconButton(
                            onClick = {
                                database.query {
                                    update(currentAlbumWithSongs.album.toggleLike())
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(if (currentBookmarked) R.drawable.favorite else R.drawable.favorite_border),
                                contentDescription = stringResource(if (currentBookmarked) R.string.saved else R.string.save),
                                tint = if (currentBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        IconButton(
                            onClick = {
                                menuState.show {
                                    AlbumMenu(
                                        originalAlbum = Album(
                                            currentAlbumWithSongs.album,
                                            currentAlbumWithSongs.artists
                                        ),
                                        navController = navController,
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
    }
}

@Composable
private fun AlbumSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    bottomSpacing: Dp = 9.dp,
    onMoreClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 28.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            if (onMoreClick != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = MaterialTheme.colorScheme.onBackground, bounded = true),
                            onClick = onMoreClick,
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.more),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(bottomSpacing))
    }
}

private fun buildArtistItemsRoute(
    artistId: String,
    endpoint: BrowseEndpoint,
    title: String? = null,
    artistName: String? = null,
): String {
    val encodedArtistId = Uri.encode(artistId)
    val encodedBrowseId = Uri.encode(endpoint.browseId)
    val encodedParams = endpoint.params
        ?.takeIf { it.isNotBlank() }
        ?.let { Uri.encode(it) }
    val encodedTitle = title?.takeIf { it.isNotBlank() }?.let { Uri.encode(it) }
    val encodedArtistName = artistName?.takeIf { it.isNotBlank() }?.let { Uri.encode(it) }

    return buildString {
        append("artist/")
        append(encodedArtistId)
        append("/items?browseId=")
        append(encodedBrowseId)
        if (encodedParams != null) {
            append("&params=")
            append(encodedParams)
        }
        if (encodedTitle != null) {
            append("&title=")
            append(encodedTitle)
        }
        if (encodedArtistName != null) {
            append("&artistName=")
            append(encodedArtistName)
        }
    }
}
