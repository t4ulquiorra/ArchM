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
import android.net.Uri
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.archm.player.LocalDatabase
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.artistvideo.ArtistVideo
import com.archm.player.canvas.AppleMusicArtistBackgroundProvider
import com.archm.player.constants.DataSaverEnabledKey
import com.archm.player.constants.ShowArtistBackgroundVideoKey
import com.archm.player.constants.ShowArtistDescriptionKey
import com.archm.player.constants.ShowArtistSubscriberCountKey
import com.archm.player.constants.ShowMonthlyListenersKey
import com.archm.player.db.entities.ArtistEntity
import com.archm.player.extensions.toMediaItem
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.queues.ListQueue
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.ui.component.ExpandableText
import com.archm.player.ui.component.IconButton
import com.archm.player.ui.component.LinkSegment
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.shimmer.ButtonPlaceholder
import com.archm.player.ui.component.shimmer.ListItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.component.shimmer.TextPlaceholder
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.menu.YouTubeSelectionSongMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.ui.utils.backToMain
import com.archm.player.ui.utils.resize
import com.archm.player.utils.rememberPreference
import com.archm.player.viewmodels.ArtistViewModel
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.BrowseEndpoint
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

private const val MAX_SONG_SELECTION = 25

@Stable
private class SongSelectionState(
    private val limitMessage: String,
    private val showToast: (String) -> Unit,
) {
    var isActive by mutableStateOf(false)
        private set

    private val _selected = mutableStateListOf<String>()
    val selected: List<String> get() = _selected
    val count: Int get() = _selected.size
    val isFull: Boolean get() = _selected.size >= MAX_SONG_SELECTION

    fun isSelected(videoId: String): Boolean = _selected.contains(videoId)

    fun start(videoId: String) {
        if (isActive) return
        isActive = true
        add(videoId)
    }

    fun toggle(videoId: String) {
        if (!isActive) return
        if (_selected.remove(videoId)) {
            if (_selected.isEmpty()) exit()
            return
        }
        add(videoId)
    }

    fun toggleSelectAll(videoIds: List<String>) {
        if (!isActive) return
        val candidates = videoIds.filter { it.isNotBlank() }.distinct()
        val everythingReachableIsPicked =
            candidates.isNotEmpty() &&
                candidates.take(MAX_SONG_SELECTION).all { _selected.contains(it) }
        if (everythingReachableIsPicked) {
            _selected.clear()
            return
        }
        for (videoId in candidates) {
            if (!add(videoId)) return
        }
    }

    fun exit() {
        isActive = false
        _selected.clear()
    }

    private fun add(videoId: String): Boolean {
        if (videoId.isBlank() || _selected.contains(videoId)) return true
        if (isFull) {
            showToast(limitMessage)
            return false
        }
        _selected.add(videoId)
        return true
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ArtistScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: ArtistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return

    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val artistPage = viewModel.artistPage
    val libraryArtist by viewModel.libraryArtist.collectAsState()

    val showArtistDescription by rememberPreference(key = ShowArtistDescriptionKey, defaultValue = true)
    val showArtistSubscriberCount by rememberPreference(key = ShowArtistSubscriberCountKey, defaultValue = true)
    val showMonthlyListeners by rememberPreference(key = ShowMonthlyListenersKey, defaultValue = true)
    val dataSaverEnabled by rememberPreference(key = DataSaverEnabledKey, defaultValue = false)
    val showArtistBackgroundVideoPref by rememberPreference(key = ShowArtistBackgroundVideoKey, defaultValue = true)
    val showArtistBackgroundVideo = if (dataSaverEnabled) false else showArtistBackgroundVideoPref

    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val unknownArtist = stringResource(R.string.unknown_artist)
    val artistName = artistPage?.artist?.title ?: libraryArtist?.artist?.name
    val thumbnail = artistPage?.artist?.thumbnail ?: libraryArtist?.artist?.thumbnailUrl

    val firstItemVisible by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex == 0 }
    }
    var shouldHideTopBar by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(firstItemVisible) {
        shouldHideTopBar = !firstItemVisible
    }

    val maxSelectionReachedString = stringResource(R.string.max_selection_reached, MAX_SONG_SELECTION)
    val selectionState = remember {
        SongSelectionState(
            limitMessage = maxSelectionReachedString,
            showToast = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() },
        )
    }

    BackHandler(enabled = selectionState.isActive) {
        selectionState.exit()
    }

    var backgroundVideoUrl by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(artistName, showArtistBackgroundVideo) {
        if (artistName != null && showArtistBackgroundVideo) {
            withContext(Dispatchers.IO) {
                backgroundVideoUrl = AppleMusicArtistBackgroundProvider.getByArtistName(artistName)
            }
        } else {
            backgroundVideoUrl = null
        }
    }

    // SimpMusic Carousel Ordering: Popular Songs -> Singles -> Albums -> Videos -> Featured On -> Related Artists
    val sections = artistPage?.sections.orEmpty()
    val popularSongsSection = remember(sections) {
        sections.firstOrNull { section ->
            section.items.all { it is SongItem } ||
                (section.items.firstOrNull() as? SongItem)?.album != null ||
                section.title.contains("popular", ignoreCase = true) ||
                section.title.contains("song", ignoreCase = true)
        }
    }
    val singlesSection = remember(sections, popularSongsSection) {
        sections.firstOrNull { section ->
            section !== popularSongsSection && (
                section.title.contains("single", ignoreCase = true) ||
                section.title.contains("ep", ignoreCase = true)
            )
        }
    }
    val albumsSection = remember(sections, popularSongsSection, singlesSection) {
        sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection &&
                section.title.contains("album", ignoreCase = true)
        }
    }
    val videosSection = remember(sections, popularSongsSection, singlesSection, albumsSection) {
        sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection &&
                (section.title.contains("video", ignoreCase = true) || section.items.any { (it as? SongItem)?.musicVideoType != null })
        }
    }
    val featuredSection = remember(sections, popularSongsSection, singlesSection, albumsSection, videosSection) {
        sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection && section !== videosSection &&
                (section.title.contains("feature", ignoreCase = true) || section.title.contains("appear", ignoreCase = true))
        }
    }
    val relatedSection = remember(sections, popularSongsSection, singlesSection, albumsSection, videosSection, featuredSection) {
        sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection && section !== videosSection && section !== featuredSection &&
                (section.title.contains("relat", ignoreCase = true) || section.title.contains("similar", ignoreCase = true) || section.items.all { it is ArtistItem })
        }
    }

    val isFollowed = libraryArtist?.artist?.bookmarkedAt != null

    val onRadio: (() -> Unit)? = artistPage?.artist?.radioEndpoint?.let { endpoint ->
        {
            playerConnection.playQueue(YouTubeQueue(endpoint))
        }
    }

    val onShuffle: () -> Unit = {
        val shuffleEndpoint = artistPage?.artist?.shuffleEndpoint
        if (shuffleEndpoint != null) {
            val watchEndpoint = if (shuffleEndpoint.playlistId != null) {
                WatchEndpoint(
                    playlistId = shuffleEndpoint.playlistId,
                    params = null,
                    videoId = null,
                )
            } else {
                shuffleEndpoint
            }
            playerConnection.playQueue(YouTubeQueue(watchEndpoint))
        } else {
            val songs = popularSongsSection?.items
                ?.filterIsInstance<SongItem>()
                ?.shuffled()
                ?.map { it.toMediaItem() }
                .orEmpty()
            if (songs.isNotEmpty()) {
                playerConnection.playQueue(
                    ListQueue(
                        title = artistName ?: unknownArtist,
                        items = songs,
                    ),
                )
            }
        }
    }

    val onToggleFollow: () -> Unit = {
        coroutineScope.launch(Dispatchers.IO) {
            database.transaction {
                val artist = libraryArtist?.artist
                if (artist != null) {
                    update(artist.toggleLike())
                } else {
                    val remoteArtist = artistPage?.artist
                    if (remoteArtist != null) {
                        insert(
                            ArtistEntity(
                                id = remoteArtist.id,
                                name = remoteArtist.title,
                                channelId = remoteArtist.channelId,
                                thumbnailUrl = remoteArtist.thumbnail,
                            ).toggleLike(),
                        )
                    }
                }
            }
        }
    }

    val showArtistOverflowMenu: () -> Unit = {
        menuState.show {
            ArtistOverflowMenu(
                onShare = {
                    val shareLink = artistPage?.artist?.shareLink ?: "https://music.youtube.com/channel/${viewModel.artistId}"
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareLink)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, null))
                    menuState.dismiss()
                },
                onCopyLink = {
                    val shareLink = artistPage?.artist?.shareLink ?: "https://music.youtube.com/channel/${viewModel.artistId}"
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.copy_link), shareLink))
                    Toast.makeText(context, R.string.link_copied, Toast.LENGTH_SHORT).show()
                    menuState.dismiss()
                },
                onRadio = onRadio?.let { action ->
                    {
                        action()
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
            if (artistPage == null) {
                item(key = "shimmer") {
                    ShimmerHost {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 400.dp)
                                .shimmer()
                                .background(MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                TextPlaceholder(height = 36.dp, modifier = Modifier.fillMaxWidth(0.55f))
                                Spacer(modifier = Modifier.height(12.dp))
                                TextPlaceholder(height = 16.dp, modifier = Modifier.fillMaxWidth(0.40f))
                                Spacer(modifier = Modifier.height(20.dp))
                                ButtonPlaceholder(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(52.dp),
                                )
                            }
                        }
                        repeat(6) {
                            ListItemPlaceHolder()
                        }
                    }
                }
            } else {
                // Header Item (SimpMusic 1:1 layout)
                item(key = "header") {
                    Column(
                        verticalArrangement = Arrangement.spacedBy((-36).dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isPortrait) {
                                        Modifier.aspectRatio(1f)
                                    } else {
                                        Modifier.height((configuration.screenHeightDp / 2).dp)
                                    }
                                ),
                        ) {
                            // Artwork image (Crop, alignment = Center so artist is framed dead-center like in SimpMusic)
                            if (thumbnail != null) {
                                AsyncImage(
                                    model = thumbnail.resize(
                                        width = 1200,
                                        height = 1200,
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.artist_screen),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(96.dp),
                                    )
                                }
                            }

                            // Canvas Video
                            if (backgroundVideoUrl != null && showArtistBackgroundVideo) {
                                ArtistVideo(
                                    videoUrl = backgroundVideoUrl!!,
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = {},
                                )
                            }

                            // Single continuous vertical gradient fade directly over the image
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                                MaterialTheme.colorScheme.background,
                                            ),
                                        ),
                                    ),
                            )

                            // Artist title & subtitle (subscribers • monthly listeners)
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = (-36).dp)
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(bottom = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = artistName ?: unknownArtist,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 2,
                                    textAlign = TextAlign.Center,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                val meta = buildList {
                                    if (showArtistSubscriberCount) {
                                        artistPage.subscriberCountText?.takeIf { it.isNotBlank() }?.let {
                                            add("$it ${stringResource(R.string.subscribers)}")
                                        }
                                    }
                                    if (showMonthlyListeners) {
                                        artistPage.monthlyListenerCount?.takeIf { it.isNotBlank() }?.let {
                                            add("$it ${stringResource(R.string.monthly_listeners)}")
                                        }
                                    }
                                }.joinToString(" • ")

                                if (meta.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = meta,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xC4FFFFFF),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }

                            // Floating circular translucent back button at top-left
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.40f)),
                            ) {
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

                            // Floating circular translucent 3-dot overflow button at top-right
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp)
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.40f)),
                            ) {
                                IconButton(
                                    onClick = showArtistOverflowMenu,
                                    onLongClick = {},
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.more_horiz),
                                        contentDescription = stringResource(R.string.more_options),
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }

                        // SimpMusic 3-Button Row (Radio circle, Center Shuffle circle, Subscribe circle)
                        SimpMusicActionRow(
                            onRadio = onRadio,
                            onShuffle = onShuffle,
                            isFollowed = isFollowed,
                            onFollow = onToggleFollow,
                            accentColor = Color.White,
                        )
                    }
                }

                // 1. Popular Songs Section
                popularSongsSection?.let { section ->
                    val distinctSongs = section.items.filterIsInstance<SongItem>().distinctBy { it.id }
                    if (distinctSongs.isNotEmpty()) {
                        item(key = "section_popular_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 16.dp, bottom = 8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.popular),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                )
                                val moreEndpoint = section.moreEndpoint
                                if (moreEndpoint != null) {
                                    TextButton(
                                        onClick = {
                                            navController.navigate(buildArtistItemsRoute(viewModel.artistId, moreEndpoint))
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                                    ) {
                                        Text(stringResource(R.string.more), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        items(
                            items = distinctSongs.take(5),
                            key = { "popular_song_${it.id}" },
                        ) { song ->
                            ArtistSongRow(
                                song = song,
                                isPlaying = song.id == mediaMetadata?.id && isPlaying,
                                selectionMode = selectionState.isActive,
                                isSelected = selectionState.isSelected(song.id),
                                onClick = {
                                    if (song.id == mediaMetadata?.id) {
                                        playerConnection.togglePlayPause()
                                    } else {
                                        playerConnection.playQueue(
                                            YouTubeQueue(
                                                WatchEndpoint(videoId = song.id),
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
                        }
                    }
                }

                // 2. Horizontal "Singles" Carousel (LazyRow)
                singlesSection?.let { section ->
                    val distinctSingles = section.items.filterIsInstance<AlbumItem>().distinctBy { it.id }
                    if (distinctSingles.isNotEmpty()) {
                        item(key = "section_singles_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 16.dp, bottom = 4.dp),
                            ) {
                                Text(
                                    text = "Singles",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                )
                                val moreEndpoint = section.moreEndpoint
                                if (moreEndpoint != null) {
                                    TextButton(
                                        onClick = {
                                            navController.navigate(buildArtistItemsRoute(viewModel.artistId, moreEndpoint))
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                                    ) {
                                        Text(stringResource(R.string.more), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        item(key = "section_singles_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                items(
                                    items = distinctSingles,
                                    key = { "single_${it.id}" },
                                ) { single ->
                                    HomeItemContentPlaylist(
                                        title = single.title,
                                        subtitle = single.year?.toString(),
                                        thumbnailUrl = single.thumbnail,
                                        thumbSize = 180.dp,
                                        onClick = { navController.navigate("album/${single.id}") },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                YouTubeAlbumMenu(
                                                    albumItem = single,
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
                }

                // 3. Horizontal "Albums" Carousel (LazyRow)
                albumsSection?.let { section ->
                    val distinctAlbums = section.items.filterIsInstance<AlbumItem>().distinctBy { it.id }
                    if (distinctAlbums.isNotEmpty()) {
                        item(key = "section_albums_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 16.dp, bottom = 4.dp),
                            ) {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                )
                                val moreEndpoint = section.moreEndpoint
                                if (moreEndpoint != null) {
                                    TextButton(
                                        onClick = {
                                            navController.navigate(buildArtistItemsRoute(viewModel.artistId, moreEndpoint))
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                                    ) {
                                        Text(stringResource(R.string.more), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        item(key = "section_albums_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                items(
                                    items = distinctAlbums,
                                    key = { "album_${it.id}" },
                                ) { album ->
                                    HomeItemContentPlaylist(
                                        title = album.title,
                                        subtitle = album.year?.toString(),
                                        thumbnailUrl = album.thumbnail,
                                        thumbSize = 180.dp,
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
                }

                // 4. Horizontal 16:9 "Videos" Carousel (LazyRow with widescreen thumbnails)
                videosSection?.let { section ->
                    val distinctVideos = section.items.filterIsInstance<SongItem>().distinctBy { it.id }
                    if (distinctVideos.isNotEmpty()) {
                        item(key = "section_videos_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 16.dp, bottom = 4.dp),
                            ) {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                )
                                val moreEndpoint = section.moreEndpoint
                                if (moreEndpoint != null) {
                                    TextButton(
                                        onClick = {
                                            navController.navigate(buildArtistItemsRoute(viewModel.artistId, moreEndpoint))
                                        },
                                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                                    ) {
                                        Text(stringResource(R.string.more), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        item(key = "section_videos_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                items(
                                    items = distinctVideos,
                                    key = { "video_${it.id}" },
                                ) { video ->
                                    HomeItemVideo(
                                        title = video.title,
                                        subtitle = listOfNotNull(
                                            video.artists.joinToString(", ") { it.name }.takeIf { it.isNotBlank() },
                                            formatDuration(video.duration),
                                        ).joinToString(" • "),
                                        thumbnailUrl = video.thumbnail,
                                        onClick = {
                                            playerConnection.playQueue(
                                                YouTubeQueue(
                                                    WatchEndpoint(videoId = video.id),
                                                    video.toMediaMetadata(),
                                                ),
                                            )
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
                }

                // 5. Horizontal "Featured on" Carousel (LazyRow)
                featuredSection?.let { section ->
                    val distinctFeatured = section.items.distinctBy { it.id }
                    if (distinctFeatured.isNotEmpty()) {
                        item(key = "section_featured_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 16.dp, bottom = 4.dp),
                            ) {
                                Text(
                                    text = section.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 10.dp),
                                )
                            }
                        }

                        item(key = "section_featured_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                items(
                                    items = distinctFeatured,
                                    key = { "featured_${it.id}" },
                                ) { feature ->
                                    HomeItemContentPlaylist(
                                        title = when (feature) {
                                            is SongItem -> feature.title
                                            is AlbumItem -> feature.title
                                            is PlaylistItem -> feature.title
                                            is ArtistItem -> feature.title
                                            else -> ""
                                        },
                                        subtitle = when (feature) {
                                            is SongItem -> feature.artists.joinToString(", ") { it.name }
                                            is AlbumItem -> feature.year?.toString()
                                            is PlaylistItem -> feature.author?.name
                                            else -> null
                                        },
                                        thumbnailUrl = when (feature) {
                                            is SongItem -> feature.thumbnail
                                            is AlbumItem -> feature.thumbnail
                                            is PlaylistItem -> feature.thumbnail
                                            is ArtistItem -> feature.thumbnail
                                            else -> null
                                        },
                                        thumbSize = 180.dp,
                                        onClick = {
                                            when (feature) {
                                                is SongItem -> playerConnection.playQueue(
                                                    YouTubeQueue(
                                                        WatchEndpoint(videoId = feature.id),
                                                        feature.toMediaMetadata(),
                                                    ),
                                                )
                                                is AlbumItem -> navController.navigate("album/${feature.id}")
                                                is PlaylistItem -> navController.navigate("online_playlist/${feature.id}")
                                                is ArtistItem -> navController.navigate("artist/${feature.id}")
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                when (feature) {
                                                    is SongItem -> YouTubeSongMenu(
                                                        song = feature,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                    is AlbumItem -> YouTubeAlbumMenu(
                                                        albumItem = feature,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                    is PlaylistItem -> YouTubePlaylistMenu(
                                                        playlist = feature,
                                                        coroutineScope = coroutineScope,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                    is ArtistItem -> Unit
                                                }
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Horizontal "Related Artists" Carousel (LazyRow with circular images)
                relatedSection?.let { section ->
                    val distinctArtists = section.items.filterIsInstance<ArtistItem>().distinctBy { it.id }
                    if (distinctArtists.isNotEmpty()) {
                        item(key = "section_related_header") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .padding(top = 16.dp, bottom = 4.dp),
                            ) {
                                Text(
                                    text = "Related Artists",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 10.dp),
                                )
                            }
                        }

                        item(key = "section_related_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                items(
                                    items = distinctArtists,
                                    key = { "related_${it.id}" },
                                ) { artist ->
                                    HomeItemArtist(
                                        title = artist.title,
                                        subscribers = null,
                                        thumbnailUrl = artist.thumbnail,
                                        onClick = { navController.navigate("artist/${artist.id}") },
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Description Section (ElevatedCard at the very bottom)
                if (showArtistDescription) {
                    val description = artistPage.description
                    val descriptionRuns = artistPage.descriptionRuns
                    if (!description.isNullOrBlank() || !descriptionRuns.isNullOrEmpty()) {
                        item(key = "artist_description_spacer") {
                            Spacer(Modifier.height(10.dp))
                        }
                        item(key = "artist_description_title") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.description),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 12.dp),
                                )
                            }
                        }
                        item(
                            key = "artist_description_card",
                        ) {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                ),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                ) {
                                    ExpandableText(
                                        text = description.orEmpty(),
                                        runs = descriptionRuns?.map {
                                            LinkSegment(
                                                text = it.text,
                                                url = it.navigationEndpoint?.urlEndpoint?.url,
                                            )
                                        },
                                        collapsedMaxLines = 5,
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }

        // Snackbar
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
                            val popularSongs = popularSongsSection?.items?.filterIsInstance<SongItem>().orEmpty()
                            val allIds = popularSongs.take(MAX_SONG_SELECTION).map { it.id }
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
                                val popularSongs = popularSongsSection?.items?.filterIsInstance<SongItem>().orEmpty()
                                val selectedSongs = popularSongs.filter { selectionState.isSelected(it.id) }
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

        // Sticky TopAppBar (shown on scroll when not selecting)
        AnimatedVisibility(
            visible = shouldHideTopBar && !selectionState.isActive,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = {
                    Text(
                        text = artistName ?: unknownArtist,
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
                        onClick = showArtistOverflowMenu,
                        onLongClick = {},
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.more_horiz),
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
}

/**
 * Exact SimpMusic 3-button row:
 * [Radio circle (48dp)] [Center Shuffle circle (64dp)] [Follow circle (48dp)]
 */
@Composable
private fun SimpMusicActionRow(
    onRadio: (() -> Unit)?,
    onShuffle: () -> Unit,
    isFollowed: Boolean,
    onFollow: () -> Unit,
    accentColor: Color = Color.White,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Radio — outlined circle (48dp)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(1.5.dp, accentColor, CircleShape)
                .clickable(enabled = onRadio != null) {
                    onRadio?.invoke()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.radio),
                contentDescription = stringResource(R.string.start_radio),
                tint = accentColor,
                modifier = Modifier.size(22.dp),
            )
        }

        // Shuffle — filled accent circle (64dp)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(accentColor)
                .clickable {
                    onShuffle()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = stringResource(R.string.shuffle),
                tint = Color.Black,
                modifier = Modifier.size(28.dp),
            )
        }

        // Follow — outlined circle when unfollowed, filled when followed (48dp)
        // User silhouette styling matching SimpMusic (person_add / check vectors)
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isFollowed) accentColor else Color.Transparent)
                .border(1.5.dp, accentColor, CircleShape)
                .clickable {
                    onFollow()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(if (isFollowed) R.drawable.check else R.drawable.person_add),
                contentDescription = stringResource(if (isFollowed) R.string.subscribed else R.string.subscribe),
                tint = if (isFollowed) Color.Black else accentColor,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * Port of SimpMusic's SongFullWidthItems:
 * - 20.dp leading alignment
 * - Swipe right gesture to reveal queue icon and add to queue
 * - Multi-selection support (animated checkbox + primary background tint)
 */
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

                // Artwork (48dp) starting directly at 20.dp when selectionMode is false
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

/**
 * Port of SimpMusic's HomeItemContentPlaylist for 1:1 Singles, Albums, and Featured carousels
 * Starts at card's left edge so first card aligns with 20.dp guideline
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeItemContentPlaylist(
    title: String,
    subtitle: String?,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbSize: Dp = 180.dp,
) {
    Box(
        modifier = modifier
            .wrapContentSize()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .heightIn(min = thumbSize + 76.dp),
        ) {
            AsyncImage(
                model = thumbnailUrl?.resize(540, 540),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(thumbSize)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp)),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .width(thumbSize)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .padding(top = 8.dp),
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xC4FFFFFF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .width(thumbSize)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                )
            }
        }
    }
}

/**
 * Port of SimpMusic's HomeItemVideo for 1:1 16:9 widescreen Video carousel
 * Balanced 10.dp padding on all 4 sides frames the artwork evenly during press/ripple
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeItemVideo(
    title: String,
    subtitle: String?,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .heightIn(min = 236.dp),
        ) {
            AsyncImage(
                model = thumbnailUrl?.resize(854, 480),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(160.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(10.dp)),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .width(284.5.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .padding(top = 8.dp),
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xC4FFFFFF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .width(284.5.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .basicMarquee(),
                )
            }
        }
    }
}

/**
 * Port of SimpMusic's HomeItemArtist for 1:1 circular avatar Related Artists carousel
 * Balanced 10.dp padding on all 4 sides frames the avatar evenly during press/ripple
 */
@Composable
private fun HomeItemArtist(
    title: String,
    subscribers: String? = null,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .heightIn(min = 236.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AsyncImage(
                model = thumbnailUrl?.resize(480, 480),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(160.dp)
                    .clip(CircleShape),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .width(160.dp)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .padding(top = 8.dp),
            )
            if (!subscribers.isNullOrBlank()) {
                Text(
                    text = subscribers,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xC4FFFFFF),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .width(160.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                )
            }
        }
    }
}

@Composable
private fun ArtistOverflowMenu(
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
            ArtistOverflowMenuItem(
                text = text,
                iconRes = iconRes,
                index = index,
                count = menuItems.size,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun ArtistOverflowMenuItem(
    text: String,
    iconRes: Int,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SegmentedListItem(
        onClick = onClick,
        enabled = enabled,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        modifier = modifier
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

private fun formatDuration(seconds: Int?): String? {
    if (seconds == null || seconds <= 0) return null
    val m = seconds / 60
    val s = seconds % 60
    return "$m:${if (s < 10) "0$s" else "$s"}"
}

private fun buildArtistItemsRoute(
    artistId: String,
    endpoint: BrowseEndpoint,
): String {
    val encodedArtistId = Uri.encode(artistId)
    val encodedBrowseId = Uri.encode(endpoint.browseId)
    val encodedParams = endpoint.params
        ?.takeIf { it.isNotBlank() }
        ?.let { Uri.encode(it) }

    return buildString {
        append("artist/")
        append(encodedArtistId)
        append("/items?browseId=")
        append(encodedBrowseId)
        if (encodedParams != null) {
            append("?params=")
            append(encodedParams)
        }
    }
}
