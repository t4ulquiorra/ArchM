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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.archm.player.constants.HideExplicitKey
import com.archm.player.constants.ShowArtistBackgroundVideoKey
import com.archm.player.constants.ShowArtistDescriptionKey
import com.archm.player.constants.ShowArtistSubscriberCountKey
import com.archm.player.constants.ShowMonthlyListenersKey
import com.archm.player.db.entities.ArtistEntity
import com.archm.player.extensions.toMediaItem
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.queues.ListQueue
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.ui.component.AlbumGridItem
import com.archm.player.ui.component.ExpandableText
import com.archm.player.ui.component.HideOnScrollFAB
import com.archm.player.ui.component.IconButton
import com.archm.player.ui.component.LinkSegment
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.SongListItem
import com.archm.player.ui.component.YouTubeGridItem
import com.archm.player.ui.component.YouTubeListItem
import com.archm.player.ui.component.shimmer.ButtonPlaceholder
import com.archm.player.ui.component.shimmer.ListItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.component.shimmer.TextPlaceholder
import com.archm.player.ui.menu.AlbumMenu
import com.archm.player.ui.menu.SongMenu
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.ui.utils.backToMain
import com.archm.player.ui.utils.resize
import com.archm.player.utils.listItemShape
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
    val librarySongs by viewModel.librarySongs.collectAsState()
    val libraryAlbums by viewModel.libraryAlbums.collectAsState()

    val hideExplicit by rememberPreference(key = HideExplicitKey, defaultValue = false)
    val showArtistDescription by rememberPreference(key = ShowArtistDescriptionKey, defaultValue = true)
    val showArtistSubscriberCount by rememberPreference(key = ShowArtistSubscriberCountKey, defaultValue = true)
    val showMonthlyListeners by rememberPreference(key = ShowMonthlyListenersKey, defaultValue = true)
    val dataSaverEnabled by rememberPreference(key = DataSaverEnabledKey, defaultValue = false)
    val showArtistBackgroundVideoPref by rememberPreference(key = ShowArtistBackgroundVideoKey, defaultValue = true)
    val showArtistBackgroundVideo = if (dataSaverEnabled) false else showArtistBackgroundVideoPref

    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLocal by rememberSaveable { mutableStateOf(false) }

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

    LaunchedEffect(libraryArtist) {
        showLocal = libraryArtist?.artist?.isLocal == true
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

    val latestRelease = remember(showLocal, artistPage, libraryAlbums) {
        if (showLocal) {
            libraryAlbums.maxByOrNull { it.album.year ?: Int.MIN_VALUE }?.let { album ->
                ArtistReleaseUiModel(
                    id = album.id,
                    title = album.album.title,
                    thumbnailUrl = album.album.thumbnailUrl,
                    year = album.album.year,
                    releaseType = AlbumReleaseType.ALBUM,
                )
            }
        } else {
            artistPage?.sections.orEmpty()
                .asSequence()
                .flatMap { it.items.asSequence() }
                .filterIsInstance<AlbumItem>()
                .maxByOrNull { it.year ?: Int.MIN_VALUE }
                ?.toArtistReleaseUiModel()
        }
    }

    // SimpMusic Carousel Ordering: Popular Songs -> Singles -> Albums -> Videos -> Featured On -> Related Artists -> Remaining
    val orderedRemoteSections = remember(artistPage?.sections) {
        val sections = artistPage?.sections.orEmpty()
        val popularSongsSection = sections.firstOrNull { section ->
            section.items.all { it is SongItem } ||
                (section.items.firstOrNull() as? SongItem)?.album != null ||
                section.title.contains("popular", ignoreCase = true) ||
                section.title.contains("song", ignoreCase = true)
        }
        val singlesSection = sections.firstOrNull { section ->
            section !== popularSongsSection && (
                section.title.contains("single", ignoreCase = true) ||
                section.title.contains("ep", ignoreCase = true)
            )
        }
        val albumsSection = sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection &&
                section.title.contains("album", ignoreCase = true)
        }
        val videosSection = sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection &&
                (section.title.contains("video", ignoreCase = true) || section.items.any { (it as? SongItem)?.musicVideoType != null })
        }
        val featuredSection = sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection && section !== videosSection &&
                (section.title.contains("feature", ignoreCase = true) || section.title.contains("appear", ignoreCase = true))
        }
        val relatedSection = sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection && section !== videosSection && section !== featuredSection &&
                (section.title.contains("relat", ignoreCase = true) || section.title.contains("similar", ignoreCase = true) || section.items.all { it is ArtistItem })
        }
        val remainingSections = sections.filter { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection &&
            section !== videosSection && section !== featuredSection && section !== relatedSection
        }

        buildList {
            popularSongsSection?.let { add(it) }
            singlesSection?.let { add(it) }
            albumsSection?.let { add(it) }
            videosSection?.let { add(it) }
            featuredSection?.let { add(it) }
            relatedSection?.let { add(it) }
            addAll(remainingSections)
        }
    }

    val isSubscribed = libraryArtist?.artist?.bookmarkedAt != null

    val canShuffle = if (showLocal) {
        librarySongs.isNotEmpty()
    } else {
        artistPage?.artist?.shuffleEndpoint != null ||
            artistPage?.sections?.any { it.items.any { item -> item is SongItem } } == true
    }

    val canPlay = if (showLocal) {
        librarySongs.isNotEmpty()
    } else {
        artistPage?.artist?.playEndpoint != null ||
            artistPage?.sections?.any { it.items.any { item -> item is SongItem } } == true
    }

    val onShuffle: () -> Unit = {
        if (showLocal) {
            if (librarySongs.isNotEmpty()) {
                playerConnection.playQueue(
                    ListQueue(
                        title = artistName ?: unknownArtist,
                        items = librarySongs.shuffled().map { it.toMediaItem() },
                    ),
                )
            }
        } else {
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
                val songSection = artistPage?.sections?.firstOrNull { section ->
                    section.items.any { it is SongItem }
                }
                val songs = songSection?.items
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
    }

    val onPlay: () -> Unit = {
        if (showLocal) {
            if (librarySongs.isNotEmpty()) {
                playerConnection.playQueue(
                    ListQueue(
                        title = artistName ?: unknownArtist,
                        items = librarySongs.map { it.toMediaItem() },
                        startIndex = 0,
                    ),
                )
            }
        } else {
            val playEndpoint = artistPage?.artist?.playEndpoint
            if (playEndpoint != null) {
                playerConnection.playQueue(YouTubeQueue(playEndpoint))
            } else {
                val songSection = artistPage?.sections?.firstOrNull { section ->
                    section.items.any { it is SongItem }
                }
                val firstSong = songSection?.items?.filterIsInstance<SongItem>()?.firstOrNull()
                if (firstSong != null) {
                    playerConnection.playQueue(
                        YouTubeQueue(
                            WatchEndpoint(videoId = firstSong.id),
                            firstSong.toMediaMetadata(),
                        ),
                    )
                }
            }
        }
    }

    val onToggleSubscription: () -> Unit = {
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
                onRadio = if (!showLocal) {
                    artistPage?.artist?.radioEndpoint?.let { endpoint ->
                        {
                            playerConnection.playQueue(YouTubeQueue(endpoint))
                            menuState.dismiss()
                        }
                    }
                } else {
                    null
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
            if (artistPage == null && !showLocal) {
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
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
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
                // Header Item (SimpMusic style with negative spacing pulling the action row up)
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
                            // Artwork image (FillWidth in portrait, Crop in landscape matching SimpMusic)
                            if (thumbnail != null) {
                                AsyncImage(
                                    model = thumbnail.resize(
                                        width = ArtistHeroArtworkSizePx,
                                        height = ArtistHeroArtworkSizePx,
                                    ),
                                    contentDescription = null,
                                    contentScale = if (isPortrait) ContentScale.FillWidth else ContentScale.Crop,
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

                            // Canvas Video (if enabled and present)
                            if (backgroundVideoUrl != null && showArtistBackgroundVideo) {
                                ArtistVideo(
                                    videoUrl = backgroundVideoUrl!!,
                                    modifier = Modifier.fillMaxSize(),
                                    onClick = {},
                                )
                            }

                            // Bottom gradient scrim (70% width in portrait, 35% height in landscape matching SimpMusic)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(
                                        if (isPortrait) {
                                            (configuration.screenWidthDp * 0.7f).dp
                                        } else {
                                            (configuration.screenHeightDp * 0.35f).dp
                                        }
                                    )
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        Brush.verticalGradient(
                                            0f to Color.Transparent,
                                            0.35f to Color.Black.copy(alpha = 0.35f),
                                            0.70f to surfaceColor.copy(alpha = 0.85f),
                                            1f to surfaceColor,
                                        )
                                    ),
                            )

                            // Artist title & subtitle (subscribers · listeners)
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
                                        artistPage?.subscriberCountText?.takeIf { it.isNotBlank() }?.let {
                                            add("$it ${stringResource(R.string.subscribers)}")
                                        }
                                    }
                                    if (showMonthlyListeners) {
                                        artistPage?.monthlyListenerCount?.takeIf { it.isNotBlank() }?.let {
                                            add("$it ${stringResource(R.string.monthly_listeners)}")
                                        }
                                    }
                                    if (showLocal) {
                                        add("${librarySongs.size} ${stringResource(R.string.songs)} • ${libraryAlbums.size} ${stringResource(R.string.albums)}")
                                    }
                                }.joinToString(" · ")

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

                            // Glass / Translucent Back Button at top start (SimpMusic header back button)
                            IconButton(
                                onClick = navController::navigateUp,
                                onLongClick = navController::backToMain,
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.40f)),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_back),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        // Graft Component 2: ArchM 3 Hero Action Buttons (replacing SimpMusic's 3 action buttons)
                        ArtistHeroActionRow(
                            canShuffle = canShuffle,
                            canPlay = canPlay,
                            isSubscribed = isSubscribed,
                            onShuffle = onShuffle,
                            onPlay = onPlay,
                            onToggleSubscription = onToggleSubscription,
                        )
                    }
                }

                // Graft Component 3: "Latest Release" Card (inserted directly above "Popular" songs header)
                latestRelease?.let { release ->
                    item(
                        key = "latest_release_${release.id}",
                        contentType = CONTENT_TYPE_ALBUM,
                    ) {
                        ArtistNewReleaseSection(
                            release = release,
                            onClick = { navController.navigate("album/${release.id}") },
                        )
                    }
                }

                // Sections:
                // Local View
                if (showLocal) {
                    if (librarySongs.isNotEmpty()) {
                        item(key = "local_songs_title") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.songs),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(
                                    onClick = {
                                        navController.navigate("artist/${viewModel.artistId}/songs")
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color.White,
                                    ),
                                ) {
                                    Text(stringResource(R.string.more), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        val filteredLibrarySongs = if (hideExplicit) {
                            librarySongs.filter { !it.song.explicit }
                        } else {
                            librarySongs
                        }
                        val displayedSongs = filteredLibrarySongs.take(5)

                        itemsIndexed(
                            items = displayedSongs,
                            key = { index, item -> "local_song_${item.id}_$index" },
                            contentType = { _, _ -> CONTENT_TYPE_SONG },
                        ) { index, song ->
                            SongListItem(
                                song = song,
                                showInLibraryIcon = true,
                                isActive = song.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                shape = listItemShape(index, displayedSongs.size),
                                trailingContent = {
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
                                        onLongClick = {},
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = null,
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .combinedClickable(
                                        onClick = {
                                            if (song.id == mediaMetadata?.id) {
                                                playerConnection.togglePlayPause()
                                            } else {
                                                playerConnection.playQueue(
                                                    ListQueue(
                                                        title = libraryArtist?.artist?.name ?: unknownArtist,
                                                        items = librarySongs.map { it.toMediaItem() },
                                                        startIndex = index,
                                                    ),
                                                )
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                SongMenu(
                                                    originalSong = song,
                                                    navController = navController,
                                                    onDismiss = menuState::dismiss,
                                                )
                                            }
                                        },
                                    )
                                    .animateItem(),
                            )
                        }

                        if (filteredLibrarySongs.size > 5) {
                            item(key = "local_songs_more") {
                                Surface(
                                    onClick = {
                                        navController.navigate("artist/${viewModel.artistId}/songs")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                ) {
                                    Text(
                                        text = stringResource(R.string.view_all),
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }

                    if (libraryAlbums.isNotEmpty()) {
                        item(key = "local_albums_title") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.albums),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f),
                                )
                                TextButton(
                                    onClick = {
                                        navController.navigate("artist/${viewModel.artistId}/albums")
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color.White,
                                    ),
                                ) {
                                    Text(stringResource(R.string.more), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        item(key = "local_albums_list") {
                            val filteredLibraryAlbums = if (hideExplicit) {
                                libraryAlbums.filter { !it.album.explicit }
                            } else {
                                libraryAlbums
                            }
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                item { Spacer(Modifier.size(10.dp)) }
                                itemsIndexed(
                                    items = filteredLibraryAlbums,
                                    key = { index, it -> "local_album_${it.id}_$index" },
                                ) { _, album ->
                                    AlbumGridItem(
                                        album = album,
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier = Modifier
                                            .combinedClickable(
                                                onClick = {
                                                    navController.navigate("album/${album.id}")
                                                },
                                                onLongClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    menuState.show {
                                                        AlbumMenu(
                                                            originalAlbum = album,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                    )
                                }
                                item { Spacer(Modifier.size(10.dp)) }
                            }
                        }
                    }
                } else {
                    // Remote Sections in SimpMusic Carousel Ordering
                    orderedRemoteSections.forEach { section ->
                        if (section.items.isNotEmpty()) {
                            val isSongSection = section.items.all { it is SongItem } ||
                                (section.items.firstOrNull() as? SongItem)?.album != null

                            if (isSongSection) {
                                item(key = "section_${section.title}_header") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 8.dp),
                                    ) {
                                        Text(
                                            text = section.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f),
                                        )
                                        section.moreEndpoint?.let { moreEndpoint ->
                                            TextButton(
                                                onClick = {
                                                    navController.navigate(buildArtistItemsRoute(viewModel.artistId, moreEndpoint))
                                                },
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = Color.White,
                                                ),
                                            ) {
                                                Text(stringResource(R.string.more), style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }

                                val distinctSongs = section.items.filterIsInstance<SongItem>().distinctBy { it.id }
                                val displayedSongs = distinctSongs.take(5)

                                itemsIndexed(
                                    items = displayedSongs,
                                    key = { _, song -> "youtube_song_${song.id}" },
                                    contentType = { _, _ -> CONTENT_TYPE_SONG },
                                ) { index, song ->
                                    YouTubeListItem(
                                        item = song,
                                        isActive = mediaMetadata?.id == song.id,
                                        isPlaying = isPlaying,
                                        shape = listItemShape(index, displayedSongs.size),
                                        trailingContent = {
                                            IconButton(
                                                onClick = {
                                                    menuState.show {
                                                        YouTubeSongMenu(
                                                            song = song,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                                onLongClick = {},
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.more_vert),
                                                    contentDescription = null,
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .combinedClickable(
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
                                                    menuState.show {
                                                        YouTubeSongMenu(
                                                            song = song,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                    }
                                                },
                                            )
                                            .animateItem(),
                                    )
                                }

                                val moreEndpoint = section.moreEndpoint
                                if (distinctSongs.size > 5 && moreEndpoint != null) {
                                    item(key = "section_${section.title}_more") {
                                        Surface(
                                            onClick = {
                                                navController.navigate(buildArtistItemsRoute(viewModel.artistId, moreEndpoint))
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 20.dp, vertical = 8.dp),
                                        ) {
                                            Text(
                                                text = stringResource(R.string.view_all),
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                textAlign = TextAlign.Center,
                                            )
                                        }
                                    }
                                }
                            } else {
                                item(key = "section_${section.title}_header") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 8.dp),
                                    ) {
                                        Text(
                                            text = section.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f),
                                        )
                                        section.moreEndpoint?.let { moreEndpoint ->
                                            TextButton(
                                                onClick = {
                                                    navController.navigate(buildArtistItemsRoute(viewModel.artistId, moreEndpoint))
                                                },
                                                colors = ButtonDefaults.textButtonColors(
                                                    contentColor = Color.White,
                                                ),
                                            ) {
                                                Text(stringResource(R.string.more), style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }

                                item(key = "section_carousel_${section.title}") {
                                    val distinctItems = section.items.distinctBy { it.id }
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        item { Spacer(Modifier.size(10.dp)) }
                                        items(
                                            items = distinctItems,
                                            key = { "carousel_item_${it.id}" },
                                        ) { item ->
                                            YouTubeGridItem(
                                                item = item,
                                                isActive = when (item) {
                                                    is SongItem -> mediaMetadata?.id == item.id
                                                    is AlbumItem -> mediaMetadata?.album?.id == item.id
                                                    else -> false
                                                },
                                                isPlaying = isPlaying,
                                                coroutineScope = coroutineScope,
                                                thumbnailRatio = 1f,
                                                modifier = Modifier
                                                    .combinedClickable(
                                                        onClick = {
                                                            when (item) {
                                                                is SongItem -> playerConnection.playQueue(
                                                                    YouTubeQueue(
                                                                        WatchEndpoint(videoId = item.id),
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
                                        item { Spacer(Modifier.size(10.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }

                // Description Section (SimpMusic card at bottom with limitLine 5 and RoundedCornerShape 8.dp)
                if (!showLocal && showArtistDescription && artistPage != null) {
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
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 12.dp),
                                )
                            }
                        }
                        item(
                            key = "artist_description_card",
                            contentType = CONTENT_TYPE_DESCRIPTION,
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
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // FAB for switching between local/remote view
        HideOnScrollFAB(
            visible = librarySongs.isNotEmpty() && libraryArtist?.artist?.isLocal != true,
            lazyListState = lazyListState,
            icon = if (showLocal) R.drawable.language else R.drawable.library_music,
            onClick = {
                showLocal = showLocal.not()
                if (!showLocal && artistPage == null) viewModel.fetchArtistsFromYTM()
            },
        )

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .align(Alignment.BottomCenter),
        )

        // Graft Component 1: Top Bar 3-Dots
        // Floating TopAppBar appears when the user scrolls away from the header (SimpMusic style)
        AnimatedVisibility(
            visible = shouldHideTopBar,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
        ) {
            TopAppBar(
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
            )
        }
    }
}

@Composable
private fun ArtistHeroActionRow(
    canShuffle: Boolean,
    canPlay: Boolean,
    isSubscribed: Boolean,
    onShuffle: () -> Unit,
    onPlay: () -> Unit,
    onToggleSubscription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: Circular Shuffle button
        FilledTonalIconButton(
            onClick = onShuffle,
            enabled = canShuffle,
            shape = CircleShape,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.05f),
                disabledContentColor = Color.White.copy(alpha = 0.38f),
            ),
            modifier = Modifier.size(52.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.shuffle),
                contentDescription = stringResource(R.string.shuffle),
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }

        // Center: Prominent Play pill button
        Button(
            onClick = onPlay,
            enabled = canPlay,
            shape = RoundedCornerShape(percent = 50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black,
                disabledContainerColor = Color.White.copy(alpha = 0.38f),
                disabledContentColor = Color.Black.copy(alpha = 0.38f),
            ),
            contentPadding = PaddingValues(horizontal = 28.dp, vertical = 14.dp),
            modifier = Modifier.heightIn(min = 52.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.play),
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.play),
                color = Color.Black,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        // Right: Circular Follow / Library (+) button
        FilledTonalIconButton(
            onClick = onToggleSubscription,
            shape = CircleShape,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = Color.White.copy(alpha = 0.12f),
                contentColor = Color.White,
            ),
            modifier = Modifier.size(52.dp),
        ) {
            Icon(
                painter = painterResource(if (isSubscribed) R.drawable.done else R.drawable.add),
                contentDescription = stringResource(if (isSubscribed) R.string.subscribed else R.string.subscribe),
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
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

@Composable
private fun ArtistNewReleaseSection(
    release: ArtistReleaseUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val releaseTypeLabel = stringResource(
        when (release.releaseType) {
            AlbumReleaseType.ALBUM -> R.string.release_type_album
            AlbumReleaseType.SINGLE -> R.string.release_type_single
            AlbumReleaseType.EP -> R.string.ep
        },
    )
    val metadata = release.year?.let { year ->
        stringResource(R.string.release_metadata, releaseTypeLabel, year)
    } ?: releaseTypeLabel

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            onClick = onClick,
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = ArtistContentMaxWidth),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (release.thumbnailUrl != null) {
                    AsyncImage(
                        model = release.thumbnailUrl.resize(
                            width = ArtistReleaseArtworkSizePx,
                            height = ArtistReleaseArtworkSizePx,
                        ),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(ArtistReleaseArtworkSize)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(ArtistReleaseArtworkSize)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.album),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.latest_release).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Text(
                        text = release.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = metadata,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private fun AlbumItem.toArtistReleaseUiModel() =
    ArtistReleaseUiModel(
        id = id,
        title = title,
        thumbnailUrl = thumbnail,
        year = year,
        releaseType = when {
            title.contains("EP", ignoreCase = true) -> AlbumReleaseType.EP
            title.contains("Single", ignoreCase = true) -> AlbumReleaseType.SINGLE
            else -> AlbumReleaseType.ALBUM
        },
    )

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

private const val ArtistHeroArtworkSizePx = 1200
private const val ArtistReleaseArtworkSizePx = 320
private val ArtistContentMaxWidth = 720.dp
private val ArtistReleaseArtworkSize = 112.dp

private const val CONTENT_TYPE_SONG = "song"
private const val CONTENT_TYPE_ALBUM = "album"
private const val CONTENT_TYPE_DESCRIPTION = "description"

private enum class AlbumReleaseType {
    ALBUM,
    SINGLE,
    EP,
}

@Immutable
private data class ArtistReleaseUiModel(
    val id: String,
    val title: String,
    val thumbnailUrl: String?,
    val year: Int?,
    val releaseType: AlbumReleaseType,
)
