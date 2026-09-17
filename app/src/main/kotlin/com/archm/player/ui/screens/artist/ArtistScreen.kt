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
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import com.archm.player.ui.screens.library.rememberArtworkCardColor
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.archm.player.LocalDatabase
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.constants.ShowArtistDescriptionKey
import com.archm.player.constants.ShowArtistSubscriberCountKey
import com.archm.player.constants.ShowMonthlyListenersKey
import com.archm.player.db.entities.ArtistEntity
import com.archm.player.extensions.toMediaItem
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.queues.ListQueue
import com.archm.player.playback.queues.LocalAlbumRadio
import com.archm.player.playback.queues.YouTubeQueue
import androidx.compose.material3.IconButton
import com.archm.player.ui.component.LongClickIconButton
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.YouTubeListItem
import com.archm.player.ui.component.bouncyClickable
import com.archm.player.ui.component.rememberBouncyScale
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
import com.archm.player.utils.reportException
import com.archm.player.viewmodels.ArtistViewModel
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.Artist
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.BrowseEndpoint
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.pages.ArtistPage
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

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
    val libraryAlbums by viewModel.libraryAlbums.collectAsState()

    val latestRelease = remember(artistPage, libraryAlbums) {
        artistPage?.lastRelease ?: libraryAlbums.maxByOrNull { it.album.year ?: Int.MIN_VALUE }?.let { album ->
            AlbumItem(
                browseId = album.id,
                playlistId = album.id,
                title = album.album.title,
                artists = null,
                thumbnail = album.album.thumbnailUrl.orEmpty(),
                year = album.album.year,
            )
        }
    }

    val showArtistDescription by rememberPreference(key = ShowArtistDescriptionKey, defaultValue = true)
    val showArtistSubscriberCount by rememberPreference(key = ShowArtistSubscriberCountKey, defaultValue = true)
    val showMonthlyListeners by rememberPreference(key = ShowMonthlyListenersKey, defaultValue = true)

    val lazyListState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val unknownArtist = stringResource(R.string.unknown_artist)
    val artistName = artistPage?.artist?.title ?: libraryArtist?.artist?.name
    val thumbnail = artistPage?.artist?.thumbnail ?: libraryArtist?.artist?.thumbnailUrl
    val isArtistVerified = remember(artistPage) {
        if (artistPage?.isVerified == true || artistPage?.artist?.isVerified == true) {
            true
        } else {
            // Grab all text sources from artistPage
            val statsText = listOfNotNull(
                artistPage?.subscriberCountText,
                artistPage?.monthlyListenerCount,
                artistPage?.description,
                artistPage?.descriptionRuns?.joinToString(" ") { it.text },
            ).joinToString(" ")

            // Check for Millions, Billions, or >= 70K
            val hasMillionOrBillion = Regex("""\d+(?:\.\d+)?\s*[MmBb]""").containsMatchIn(statsText)
            val has70kOrMore = Regex("""(\d+(?:\.\d+)?)\s*[Kk]""").findAll(statsText).any { match ->
                (match.groupValues.getOrNull(1)?.toDoubleOrNull() ?: 0.0) >= 70.0
            }
            val hasNumbersAbove70k = Regex("""\b(\d{1,3}(?:,\d{3})+|\d{5,})\b""").findAll(statsText).any { match ->
                (match.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0) >= 70000.0
            }

            hasMillionOrBillion || has70kOrMore || hasNumbersAbove70k
        }
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE || configuration.screenWidthDp >= 600

    val topBarTitleVisible by remember {
        derivedStateOf {
            if (isLandscape) {
                lazyListState.firstVisibleItemIndex > 0
            } else {
                lazyListState.firstVisibleItemIndex > 1 || (lazyListState.firstVisibleItemIndex == 1 && lazyListState.firstVisibleItemScrollOffset > 80)
            }
        }
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

    // SimpMusic Carousel Ordering: Popular Songs -> Singles -> Albums -> Videos -> Featured On -> Playlists by [Artist] -> Live performances -> Related Artists
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
                !section.title.contains("live", ignoreCase = true) && !section.title.contains("performance", ignoreCase = true) &&
                (section.title.contains("video", ignoreCase = true) || section.items.any { (it as? SongItem)?.musicVideoType != null })
        }
    }
    val featuredSection = remember(sections, popularSongsSection, singlesSection, albumsSection, videosSection) {
        sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection && section !== videosSection &&
                (section.title.contains("feature", ignoreCase = true) || section.title.contains("appear", ignoreCase = true))
        }
    }
    val playlistsSection = remember(sections, popularSongsSection, singlesSection, albumsSection, videosSection, featuredSection) {
        artistPage?.playlists ?: sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection && section !== videosSection && section !== featuredSection &&
                (section.title.contains("playlist", ignoreCase = true) || section.items.all { it is PlaylistItem })
        }
    }
    val livePerformancesSection = remember(sections, popularSongsSection, singlesSection, albumsSection, videosSection, featuredSection, playlistsSection) {
        artistPage?.livePerformances ?: sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection && section !== videosSection && section !== featuredSection && section !== playlistsSection &&
                (section.title.contains("live", ignoreCase = true) || section.title.contains("performance", ignoreCase = true))
        }
    }
    val relatedSection = remember(sections, popularSongsSection, singlesSection, albumsSection, videosSection, featuredSection, playlistsSection, livePerformancesSection) {
        sections.firstOrNull { section ->
            section !== popularSongsSection && section !== singlesSection && section !== albumsSection && section !== videosSection && section !== featuredSection && section !== playlistsSection && section !== livePerformancesSection &&
                (section.title.contains("relat", ignoreCase = true) || section.title.contains("similar", ignoreCase = true) || section.items.all { it is ArtistItem })
        }
    }

    val distinctArtists = remember(relatedSection) {
        relatedSection?.items?.filterIsInstance<ArtistItem>()?.distinctBy { it.id }.orEmpty()
    }
    val topRowArtists = remember(distinctArtists) {
        distinctArtists.filterIndexed { index, _ -> index % 2 == 0 }
    }
    val bottomRowArtists = remember(distinctArtists) {
        distinctArtists.filterIndexed { index, _ -> index % 2 != 0 }
    }

    val description = artistPage?.description
    val descriptionRuns = artistPage?.descriptionRuns
    val hasDescription = !description.isNullOrBlank() || !descriptionRuns.isNullOrEmpty()
    val hasAudienceStat = !artistPage?.monthlyListenerCount.isNullOrBlank() || !artistPage?.subscriberCountText.isNullOrBlank()
    val hasAbout = showArtistDescription && artistPage != null && (hasDescription || hasAudienceStat)

    val portraitUrl = thumbnail
        ?: artistPage?.artist?.thumbnail
        ?: libraryArtist?.artist?.thumbnailUrl
    val monthlyListeners = artistPage?.monthlyListenerCount
    val subscribers = artistPage?.subscriberCountText
    val audienceStat = when {
        !monthlyListeners.isNullOrBlank() -> {
            if (monthlyListeners.contains("listener", ignoreCase = true)) {
                monthlyListeners
            } else {
                "$monthlyListeners monthly listeners"
            }
        }
        !subscribers.isNullOrBlank() -> {
            if (subscribers.contains("subscriber", ignoreCase = true)) {
                subscribers
            } else {
                "$subscribers subscribers"
            }
        }
        else -> null
    }
    val bioText = description ?: descriptionRuns?.joinToString(separator = "") { it.text }
    val audienceStats = buildList {
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
    }.joinToString(" • ")

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

    val isCurrentArtistLoaded = mediaMetadata?.artists?.any {
        it.id == viewModel.artistId || (!artistName.isNullOrBlank() && it.name.equals(artistName, ignoreCase = true))
    } == true
    val isCurrentArtistPlaying = isPlaying && isCurrentArtistLoaded

    val onPlay: () -> Unit = {
        if (isCurrentArtistPlaying) {
            playerConnection.player.pause()
        } else if (isCurrentArtistLoaded) {
            playerConnection.player.play()
        } else {
            val topSong = popularSongsSection?.items?.filterIsInstance<SongItem>()?.firstOrNull()
            if (topSong != null) {
                playerConnection.playQueue(
                    YouTubeQueue(
                        WatchEndpoint(videoId = topSong.id),
                        topSong.toMediaMetadata(),
                    ),
                )
            } else {
                onShuffle()
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

    val onNavigateToAbout: () -> Unit = {
        val encodedArtistName = if (!artistName.isNullOrBlank()) Uri.encode(artistName) else null
        val route = if (encodedArtistName != null) {
            "artist/${viewModel.artistId}/about?artistName=$encodedArtistName"
        } else {
            "artist/${viewModel.artistId}/about"
        }
        navController.navigate(route)
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        val screenHeight = maxHeight
        val backdropHeight = screenHeight * 0.45f // Exactly 45% of screen height
        val transparentSpacerHeight = screenHeight * 0.30f // Top 30% window
        val gradientZoneHeight = screenHeight * 0.20f // 30% -> 50% transition (20% height)

        val headerHeight = (configuration.screenHeightDp * 0.45f).dp
        val avatarSize = if (isLandscape) {
            if (headerHeight >= 220.dp) 125.dp else 110.dp
        } else {
            when {
                headerHeight >= 320.dp -> 130.dp
                headerHeight >= 260.dp -> 120.dp
                else -> 100.dp
            }
        }
        val topContentPadding = if (headerHeight < 240.dp) 36.dp else 56.dp
        val titleStyle = if (headerHeight < 240.dp) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineLarge

        // Layer 1: Pinned Background Photo (zIndex 0f)
        if (!isLandscape) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(backdropHeight)
                    .align(Alignment.TopCenter)
                    .zIndex(0f),
            ) {
                if (portraitUrl != null) {
                    AsyncImage(
                        model = portraitUrl.resize(1080, 1080) ?: portraitUrl,
                        contentDescription = artistName,
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
                            modifier = Modifier.size(64.dp),
                        )
                    }
                }
            }
        }

        val density = LocalDensity.current
        if (!isLandscape) {
            val contentSheetOffsetY by remember {
                derivedStateOf {
                    val firstIndex = lazyListState.firstVisibleItemIndex
                    val firstOffset = lazyListState.firstVisibleItemScrollOffset
                    if (firstIndex == 0) {
                        val spacerPx = with(density) { transparentSpacerHeight.toPx() }
                        val gradientPx = with(density) { gradientZoneHeight.toPx() }
                        (spacerPx + gradientPx - firstOffset).coerceAtLeast(0f)
                    } else if (firstIndex == 1) {
                        val gradientPx = with(density) { gradientZoneHeight.toPx() }
                        (gradientPx - firstOffset).coerceAtLeast(0f)
                    } else {
                        0f
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = contentSheetOffsetY
                    }
                    .background(Color.Black)
                    .zIndex(0.5f),
            )
        }

        // Layer 2: Sliding Content Sheet (zIndex 1f)
        LazyColumn(
            state = lazyListState,
            contentPadding = PaddingValues(
                bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding(),
            ),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
        ) {
            if (artistPage == null) {
                item(key = "shimmer") {
                    ShimmerHost {
                        Box(
                            modifier = if (isLandscape) {
                                Modifier
                                    .fillMaxWidth()
                                    .height(headerHeight)
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            },
                        ) {
                            if (isLandscape) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .windowInsetsPadding(WindowInsets.statusBars)
                                        .padding(top = topContentPadding),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 36.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.Bottom,
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(160.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                                        )
                                        Spacer(modifier = Modifier.width(32.dp))
                                        Column(
                                            modifier = Modifier.wrapContentHeight(),
                                        ) {
                                            TextPlaceholder(height = 12.dp, modifier = Modifier.fillMaxWidth(0.35f))
                                            Spacer(modifier = Modifier.height(4.dp))
                                            TextPlaceholder(height = 28.dp, modifier = Modifier.fillMaxWidth(0.75f))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            TextPlaceholder(height = 14.dp, modifier = Modifier.fillMaxWidth(0.5f))
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                ButtonPlaceholder(modifier = Modifier.width(88.dp).height(42.dp))
                                                ButtonPlaceholder(modifier = Modifier.width(96.dp).height(42.dp))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(modifier = Modifier.height(transparentSpacerHeight))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = gradientZoneHeight)
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black),
                                                ),
                                            )
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.Bottom,
                                    ) {
                                        TextPlaceholder(height = 32.dp, modifier = Modifier.fillMaxWidth(0.6f))
                                        Spacer(modifier = Modifier.height(6.dp))
                                        TextPlaceholder(height = 14.dp, modifier = Modifier.fillMaxWidth(0.45f))
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                                ButtonPlaceholder(modifier = Modifier.width(96.dp).height(36.dp))
                                                ButtonPlaceholder(modifier = Modifier.size(36.dp))
                                            }
                                            ButtonPlaceholder(modifier = Modifier.size(56.dp))
                                        }
                                    }
                                }
                            }
                        }
                        repeat(6) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black),
                            ) {
                                ListItemPlaceHolder()
                            }
                        }
                    }
                }
            } else {
                if (isLandscape) {
                    item(key = "header_landscape") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(headerHeight),
                        ) {
                        // Ambient blurred backdrop with vertical gradient fade to AMOLED black
                        Box(
                            modifier = Modifier.matchParentSize(),
                        ) {
                            if (thumbnail != null) {
                                AsyncImage(
                                    model = thumbnail.resize(
                                        width = 800,
                                        height = 800,
                                    ),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    alignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .blur(radius = 42.dp)
                                        .alpha(0.55f),
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.40f),
                                                Color.Black.copy(alpha = 0.20f),
                                                Color.Black.copy(alpha = 0.65f),
                                                Color(0xFF000000),
                                            ),
                                        ),
                                    ),
                            )
                        }
                        // Landscape: Circular Avatar Flush Layout (Horizontal Row)
                        Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .windowInsetsPadding(WindowInsets.statusBars)
                                    .padding(top = topContentPadding),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 36.dp, vertical = 16.dp),
                                    verticalAlignment = Alignment.Bottom,
                                ) {
                                    // Circular avatar prominent size
                                    Box(
                                        modifier = Modifier
                                            .size(160.dp)
                                            .clip(CircleShape)
                                            .border(
                                                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.15f)),
                                                shape = CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (thumbnail != null) {
                                            AsyncImage(
                                                model = thumbnail.resize(
                                                    width = 500,
                                                    height = 500,
                                                ),
                                                contentDescription = artistName,
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
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
                                                    modifier = Modifier.size(56.dp),
                                                )
                                            }
                                        }
                                    }

                                    // Increased breathing room shifting text elements right
                                    Spacer(modifier = Modifier.width(32.dp))

                                    // Right info column
                                    Column(
                                        modifier = Modifier.wrapContentHeight(),
                                        horizontalAlignment = Alignment.Start,
                                    ) {
                                        // Artist Name: Bold display typography with Inline Verified Badge
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Start,
                                        ) {
                                            Text(
                                                text = artistName ?: unknownArtist,
                                                style = titleStyle,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false),
                                            )
                                            if (isArtistVerified) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    painter = painterResource(R.drawable.ic_verified_badge),
                                                    contentDescription = "Verified",
                                                    tint = Color.Unspecified, // Keeps the built-in blue and white colors
                                                    modifier = Modifier.size(20.dp),
                                                )
                                            }
                                        }

                                        // 3. Stats pills row (Subscribers / Monthly)
                                        val subscriberText = if (showArtistSubscriberCount) {
                                            artistPage?.subscriberCountText?.takeIf { it.isNotBlank() }
                                        } else null
                                        val monthlyListenerText = if (showMonthlyListeners) {
                                            artistPage?.monthlyListenerCount?.takeIf { it.isNotBlank() }
                                        } else null

                                        if (subscriberText != null || monthlyListenerText != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            FlowRow(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                            ) {
                                                if (subscriberText != null) {
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = Color.White.copy(alpha = 0.08f),
                                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        ) {
                                                            Icon(
                                                                painter = painterResource(R.drawable.person),
                                                                contentDescription = null,
                                                                modifier = Modifier.size(14.dp),
                                                                tint = Color.White.copy(alpha = 0.7f),
                                                            )
                                                            Text(
                                                                text = "$subscriberText ${stringResource(R.string.subscribers)}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color.White,
                                                            )
                                                        }
                                                    }
                                                }

                                                if (monthlyListenerText != null) {
                                                    Surface(
                                                        shape = RoundedCornerShape(50),
                                                        color = Color.White.copy(alpha = 0.08f),
                                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        ) {
                                                            Icon(
                                                                painter = painterResource(R.drawable.listening),
                                                                contentDescription = null,
                                                                modifier = Modifier.size(14.dp),
                                                                tint = Color.White.copy(alpha = 0.7f),
                                                            )
                                                            Text(
                                                                text = "$monthlyListenerText ${stringResource(R.string.monthly_listeners)}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color.White,
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // 4. Action buttons row: [ Play/Pause ] [ Follow/Following ] [ ((•)) ]
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                        ) {
                                            // Primary Play / Pause capsule button
                                            Box(
                                                modifier = Modifier
                                                    .bouncyClickable(onClick = onPlay)
                                                    .height(42.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .background(Color.White)
                                                    .padding(start = 14.dp, end = 18.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Icon(
                                                        painter = painterResource(if (isCurrentArtistPlaying) R.drawable.pause else R.drawable.play),
                                                        contentDescription = stringResource(if (isCurrentArtistPlaying) R.string.pause else R.string.play),
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(18.dp),
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = stringResource(if (isCurrentArtistPlaying) R.string.pause else R.string.play),
                                                        style = MaterialTheme.typography.labelLarge.copy(
                                                            fontWeight = FontWeight.SemiBold,
                                                        ),
                                                        color = Color.Black,
                                                    )
                                                }
                                            }

                                            // Secondary Outlined Follow capsule button
                                            OutlinedFollowPillButton(
                                                isFollowed = isFollowed,
                                                onClick = onToggleFollow,
                                                height = 42.dp,
                                                horizontalPadding = 20.dp,
                                                borderAlpha = 0.25f,
                                            )

                                            // Radio icon button (if onRadio != null)
                                            if (onRadio != null) {
                                                Box(
                                                    modifier = Modifier
                                                        .bouncyClickable(onClick = onRadio)
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .border(
                                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                                            shape = CircleShape,
                                                        ),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.radio),
                                                        contentDescription = stringResource(R.string.start_radio),
                                                        tint = Color.White,
                                                        modifier = Modifier.size(20.dp),
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Item 1 (Transparent Spacer)
                    item(key = "transparent_spacer") {
                        Spacer(modifier = Modifier.height(transparentSpacerHeight))
                    }

                    // Item 2 (The Gradient & Identity Zone)
                    item(key = "identity_zone") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = gradientZoneHeight)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black),
                                    ),
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.Bottom,
                        ) {
                            // Artist Name + Scalloped Rosette Badge (ic_verified_badge)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Start,
                            ) {
                                Text(
                                    text = artistName ?: unknownArtist,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 32.sp,
                                    ),
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                if (isArtistVerified) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        painter = painterResource(R.drawable.ic_verified_badge),
                                        contentDescription = "Verified",
                                        tint = Color.Unspecified, // Keeps the built-in blue and white colors
                                        modifier = Modifier.size(24.dp),
                                    )
                                }
                            }

                            // Subtitle: "$subscribers Subscribers • $monthlyListeners Monthly"
                            if (audienceStats.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = audienceStats,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Row:
                            // Left: "Following" pill button + "Radio" pill button
                            // Right: Large circular Play button (FAB style)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    OutlinedFollowPillButton(
                                        isFollowed = isFollowed,
                                        onClick = onToggleFollow,
                                        height = 36.dp,
                                        horizontalPadding = 18.dp,
                                        borderAlpha = 0.35f,
                                    )

                                    if (onRadio != null) {
                                        Box(
                                            modifier = Modifier
                                                .bouncyClickable(onClick = onRadio)
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .border(
                                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                                    shape = CircleShape,
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.radio),
                                                contentDescription = stringResource(R.string.start_radio),
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp),
                                            )
                                        }
                                    }
                                }

                                // Right: Large circular Play button (FAB style)
                                Box(
                                    modifier = Modifier
                                        .bouncyClickable(onClick = onPlay)
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(if (isCurrentArtistPlaying) R.drawable.pause else R.drawable.play),
                                        contentDescription = stringResource(if (isCurrentArtistPlaying) R.string.pause else R.string.play),
                                        tint = Color.Black,
                                        modifier = Modifier.size(26.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                // Latest Release Section (placed directly above Popular)
                latestRelease?.let { release ->
                    item(key = "section_latest_release_card") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black),
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(20.dp))
                                ArtistLatestReleaseCard(
                                    release = release,
                                    onClick = { navController.navigate("album/${release.id}") },
                                    isLandscape = isLandscape,
                                )
                            }
                        }
                    }
                }

                // 1. Popular Songs Section
                popularSongsSection?.let { section ->
                    val distinctSongs = section.items.filterIsInstance<SongItem>().distinctBy { it.id }
                    if (distinctSongs.isNotEmpty()) {
                        item(key = "section_popular_header") {
                            ArtistSectionHeader(
                                title = stringResource(R.string.popular),
                                topSpacing = if (latestRelease != null) 24.dp else 0.dp,
                                bottomSpacing = 7.dp,
                                onMoreClick = section.moreEndpoint?.let { moreEndpoint ->
                                    {
                                        navController.navigate(
                                            buildArtistItemsRoute(
                                                viewModel.artistId,
                                                moreEndpoint,
                                                title = context.getString(R.string.popular),
                                                artistName = artistName,
                                            ),
                                        )
                                    }
                                },
                            )
                        }

                        items(
                            items = distinctSongs.take(5),
                            key = { "popular_song_${it.id}" },
                        ) { song ->
                            YouTubeListItem(
                                item = song,
                                isActive = song.id == mediaMetadata?.id,
                                isPlaying = isPlaying,
                                inSelectionMode = selectionState.isActive,
                                isSelected = selectionState.isSelected(song.id),
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
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = null,
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .combinedClickable(
                                        onClick = {
                                            if (selectionState.isActive) {
                                                selectionState.toggle(song.id)
                                            } else if (song.id == mediaMetadata?.id) {
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
                                            if (!selectionState.isActive) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                selectionState.start(song.id)
                                            }
                                        },
                                    ),
                            )
                        }
                    }
                }

                // 2. Horizontal "Singles" Carousel (LazyRow)
                singlesSection?.let { section ->
                    val distinctSingles = section.items.filterIsInstance<AlbumItem>().distinctBy { it.id }
                    if (distinctSingles.isNotEmpty()) {
                        item(key = "section_singles_header") {
                            ArtistSectionHeader(
                                title = "Singles",
                                onMoreClick = section.moreEndpoint?.let { moreEndpoint ->
                                    {
                                        navController.navigate(
                                            buildArtistItemsRoute(
                                                viewModel.artistId,
                                                moreEndpoint,
                                                title = "Singles",
                                                artistName = artistName,
                                            ),
                                        )
                                    }
                                },
                            )
                        }

                        item(key = "section_singles_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                                        thumbSize = 150.dp,
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
                                        onPlayClick = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                var albumWithSongs = database.albumWithSongs(single.id).first()
                                                if (albumWithSongs?.songs.isNullOrEmpty()) {
                                                    YouTube.album(single.id).onSuccess { albumPage ->
                                                        database.transaction { insert(albumPage) }
                                                        albumWithSongs = database.albumWithSongs(single.id).first()
                                                    }.onFailure { reportException(it) }
                                                }
                                                albumWithSongs?.let {
                                                    withContext(Dispatchers.Main) {
                                                        playerConnection.playQueue(LocalAlbumRadio(it))
                                                    }
                                                }
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
                            ArtistSectionHeader(
                                title = section.title,
                                onMoreClick = section.moreEndpoint?.let { moreEndpoint ->
                                    {
                                        navController.navigate(
                                            buildArtistItemsRoute(
                                                viewModel.artistId,
                                                moreEndpoint,
                                                title = section.title,
                                                artistName = artistName,
                                            ),
                                        )
                                    }
                                },
                            )
                        }

                        item(key = "section_albums_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                                        thumbSize = 150.dp,
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
                                        onPlayClick = {
                                            coroutineScope.launch(Dispatchers.IO) {
                                                var albumWithSongs = database.albumWithSongs(album.id).first()
                                                if (albumWithSongs?.songs.isNullOrEmpty()) {
                                                    YouTube.album(album.id).onSuccess { albumPage ->
                                                        database.transaction { insert(albumPage) }
                                                        albumWithSongs = database.albumWithSongs(album.id).first()
                                                    }.onFailure { reportException(it) }
                                                }
                                                albumWithSongs?.let {
                                                    withContext(Dispatchers.Main) {
                                                        playerConnection.playQueue(LocalAlbumRadio(it))
                                                    }
                                                }
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
                            ArtistSectionHeader(
                                title = section.title,
                                onMoreClick = section.moreEndpoint?.let { moreEndpoint ->
                                    {
                                        navController.navigate(
                                            buildArtistItemsRoute(
                                                viewModel.artistId,
                                                moreEndpoint,
                                                title = section.title,
                                                artistName = artistName,
                                            ),
                                        )
                                    }
                                },
                            )
                        }

                        item(key = "section_videos_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                                        onPlayClick = {
                                            playerConnection.playQueue(
                                                YouTubeQueue(
                                                    WatchEndpoint(videoId = video.id),
                                                    video.toMediaMetadata(),
                                                ),
                                            )
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
                            ArtistSectionHeader(
                                title = section.title,
                                onMoreClick = section.moreEndpoint?.let { moreEndpoint ->
                                    {
                                        navController.navigate(
                                            buildArtistItemsRoute(
                                                viewModel.artistId,
                                                moreEndpoint,
                                                title = section.title,
                                                artistName = artistName,
                                            ),
                                        )
                                    }
                                },
                            )
                        }

                        item(key = "section_featured_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                                        thumbSize = 150.dp,
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
                                        onPlayClick = {
                                            when (feature) {
                                                is SongItem -> playerConnection.playQueue(
                                                    YouTubeQueue(
                                                        WatchEndpoint(videoId = feature.id),
                                                        feature.toMediaMetadata(),
                                                    ),
                                                )
                                                is AlbumItem -> {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        var albumWithSongs = database.albumWithSongs(feature.id).first()
                                                        if (albumWithSongs?.songs.isNullOrEmpty()) {
                                                            YouTube.album(feature.id).onSuccess { albumPage ->
                                                                database.transaction { insert(albumPage) }
                                                                albumWithSongs = database.albumWithSongs(feature.id).first()
                                                            }.onFailure { reportException(it) }
                                                        }
                                                        albumWithSongs?.let {
                                                            withContext(Dispatchers.Main) {
                                                                playerConnection.playQueue(LocalAlbumRadio(it))
                                                            }
                                                        }
                                                    }
                                                }
                                                else -> {}
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Horizontal "Playlists by [Artist]" Carousel (LazyRow)
                playlistsSection?.let { section ->
                    val distinctPlaylists = section.items.distinctBy { it.id }
                    if (distinctPlaylists.isNotEmpty()) {
                        val playlistArtistName = artistName ?: artistPage?.artist?.title
                        val playlistTitle = if (!playlistArtistName.isNullOrBlank()) {
                            "Playlists by $playlistArtistName"
                        } else {
                            section.title.ifBlank { "Playlists" }
                        }
                        item(key = "section_playlists_header") {
                            ArtistSectionHeader(
                                title = playlistTitle,
                                onMoreClick = section.moreEndpoint?.let { moreEndpoint ->
                                    {
                                        navController.navigate(
                                            buildArtistItemsRoute(
                                                viewModel.artistId,
                                                moreEndpoint,
                                                title = playlistTitle,
                                                artistName = artistName,
                                            ),
                                        )
                                    }
                                },
                            )
                        }

                        item(key = "section_playlists_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                items(
                                    items = distinctPlaylists,
                                    key = { "playlist_${it.id}" },
                                ) { playlistItem ->
                                    HomeItemContentPlaylist(
                                        title = when (playlistItem) {
                                            is PlaylistItem -> playlistItem.title
                                            is AlbumItem -> playlistItem.title
                                            is SongItem -> playlistItem.title
                                            is ArtistItem -> playlistItem.title
                                            else -> ""
                                        },
                                        subtitle = when (playlistItem) {
                                            is PlaylistItem -> playlistItem.author?.name
                                            is AlbumItem -> playlistItem.year?.toString()
                                            is SongItem -> playlistItem.artists.joinToString(", ") { it.name }
                                            else -> null
                                        },
                                        thumbnailUrl = when (playlistItem) {
                                            is PlaylistItem -> playlistItem.thumbnail
                                            is AlbumItem -> playlistItem.thumbnail
                                            is SongItem -> playlistItem.thumbnail
                                            is ArtistItem -> playlistItem.thumbnail
                                            else -> null
                                        },
                                        thumbSize = 150.dp,
                                        onClick = {
                                            when (playlistItem) {
                                                is PlaylistItem -> navController.navigate("online_playlist/${playlistItem.id}")
                                                is AlbumItem -> navController.navigate("album/${playlistItem.id}")
                                                is SongItem -> playerConnection.playQueue(
                                                    YouTubeQueue(
                                                        WatchEndpoint(videoId = playlistItem.id),
                                                        playlistItem.toMediaMetadata(),
                                                    ),
                                                )
                                                is ArtistItem -> navController.navigate("artist/${playlistItem.id}")
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                when (playlistItem) {
                                                    is PlaylistItem -> YouTubePlaylistMenu(
                                                        playlist = playlistItem,
                                                        coroutineScope = coroutineScope,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                    is AlbumItem -> YouTubeAlbumMenu(
                                                        albumItem = playlistItem,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                    is SongItem -> YouTubeSongMenu(
                                                        song = playlistItem,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                    is ArtistItem -> Unit
                                                }
                                            }
                                        },
                                        onPlayClick = {
                                            when (playlistItem) {
                                                is PlaylistItem -> {
                                                    val watchEndpoint = playlistItem.playEndpoint
                                                        ?: WatchEndpoint(playlistId = playlistItem.id)
                                                    playerConnection.playQueue(YouTubeQueue(watchEndpoint))
                                                }
                                                is AlbumItem -> {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        var albumWithSongs = database.albumWithSongs(playlistItem.id).first()
                                                        if (albumWithSongs?.songs.isNullOrEmpty()) {
                                                            YouTube.album(playlistItem.id).onSuccess { albumPage ->
                                                                database.transaction { insert(albumPage) }
                                                                albumWithSongs = database.albumWithSongs(playlistItem.id).first()
                                                            }.onFailure { reportException(it) }
                                                        }
                                                        albumWithSongs?.let {
                                                            withContext(Dispatchers.Main) {
                                                                playerConnection.playQueue(LocalAlbumRadio(it))
                                                            }
                                                        }
                                                    }
                                                }
                                                is SongItem -> playerConnection.playQueue(
                                                    YouTubeQueue(
                                                        WatchEndpoint(videoId = playlistItem.id),
                                                        playlistItem.toMediaMetadata(),
                                                    ),
                                                )
                                                else -> {}
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. Horizontal "Live performances" Carousel (LazyRow with widescreen thumbnails)
                livePerformancesSection?.let { section ->
                    val distinctLive = section.items.distinctBy { it.id }
                    if (distinctLive.isNotEmpty()) {
                        val liveTitle = section.title.ifBlank { "Live performances" }
                        item(key = "section_live_header") {
                            ArtistSectionHeader(
                                title = liveTitle,
                                onMoreClick = section.moreEndpoint?.let { moreEndpoint ->
                                    {
                                        navController.navigate(
                                            buildArtistItemsRoute(
                                                viewModel.artistId,
                                                moreEndpoint,
                                                title = liveTitle,
                                                artistName = artistName,
                                            ),
                                        )
                                    }
                                },
                            )
                        }

                        item(key = "section_live_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                items(
                                    items = distinctLive,
                                    key = { "live_${it.id}" },
                                ) { item ->
                                    val songItem = item as? SongItem
                                    HomeItemVideo(
                                        title = item.title,
                                        subtitle = if (songItem != null) {
                                            listOfNotNull(
                                                songItem.artists.joinToString(", ") { it.name }.takeIf { it.isNotBlank() },
                                                formatDuration(songItem.duration),
                                            ).joinToString(" • ")
                                        } else {
                                            null
                                        },
                                        thumbnailUrl = item.thumbnail,
                                        onClick = {
                                            if (songItem != null) {
                                                playerConnection.playQueue(
                                                    YouTubeQueue(
                                                        WatchEndpoint(videoId = songItem.id),
                                                        songItem.toMediaMetadata(),
                                                    ),
                                                )
                                            } else {
                                                when (item) {
                                                    is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                                    is AlbumItem -> navController.navigate("album/${item.id}")
                                                    is ArtistItem -> navController.navigate("artist/${item.id}")
                                                    else -> {}
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            menuState.show {
                                                if (songItem != null) {
                                                    YouTubeSongMenu(
                                                        song = songItem,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                } else {
                                                    when (item) {
                                                        is PlaylistItem -> YouTubePlaylistMenu(
                                                            playlist = item,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                        is AlbumItem -> YouTubeAlbumMenu(
                                                            albumItem = item,
                                                            navController = navController,
                                                            onDismiss = menuState::dismiss,
                                                        )
                                                        else -> Unit
                                                    }
                                                }
                                            }
                                        },
                                        onPlayClick = {
                                            if (songItem != null) {
                                                playerConnection.playQueue(
                                                    YouTubeQueue(
                                                        WatchEndpoint(videoId = songItem.id),
                                                        songItem.toMediaMetadata(),
                                                    ),
                                                )
                                            }
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                // 8 & 9. Adaptive "About" & "Related Artists" Sections
                if (isLandscape && hasAbout && distinctArtists.isNotEmpty()) {
                    // Landscape / Tablet Side-by-Side Row
                    item(key = "section_about_and_related_row") {
                        val sectionHeight = 316.dp

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            // Left Pane: About Pod (~48% width)
                            Column(
                                modifier = Modifier.weight(1f),
                            ) {
                                ArtistSectionHeader(
                                    title = "About",
                                    horizontalPadding = 0.dp,
                                )
                                ArtistAboutCard(
                                    artistName = artistName,
                                    portraitUrl = portraitUrl,
                                    audienceStat = audienceStat,
                                    bioText = bioText,
                                    isFollowed = isFollowed,
                                    isLandscape = true,
                                    isVerified = isArtistVerified,
                                    onToggleFollow = onToggleFollow,
                                    onNavigateToAbout = onNavigateToAbout,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(sectionHeight),
                                )
                            }

                            // Right Pane: Related Artists with 2 Independent Scrollable Rows (~52% width)
                            Column(
                                modifier = Modifier.weight(1.1f),
                            ) {
                                ArtistSectionHeader(
                                    title = "Related Artists",
                                    horizontalPadding = 0.dp,
                                    onMoreClick = relatedSection?.moreEndpoint?.let { moreEndpoint ->
                                        {
                                            navController.navigate(
                                                buildArtistItemsRoute(
                                                    viewModel.artistId,
                                                    moreEndpoint,
                                                    title = "Related Artists",
                                                    artistName = artistName,
                                                ),
                                            )
                                        }
                                    },
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(sectionHeight)
                                        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                                        .drawWithContent {
                                            drawContent()
                                            drawRect(
                                                brush = Brush.horizontalGradient(
                                                    0f to Color.Transparent,
                                                    0.06f to Color.Black,
                                                    0.95f to Color.Black,
                                                    1f to Color.Transparent,
                                                ),
                                                blendMode = BlendMode.DstIn,
                                            )
                                        },
                                    verticalArrangement = Arrangement.spacedBy(24.dp),
                                ) {
                                    LazyRow(
                                        contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        items(
                                            items = topRowArtists,
                                            key = { "related_top_${it.id}" },
                                        ) { artist ->
                                            HomeItemArtist(
                                                title = artist.title,
                                                subscribers = null,
                                                thumbnailUrl = artist.thumbnail,
                                                onClick = { navController.navigate("artist/${artist.id}") },
                                                avatarSize = 122.dp,
                                                isSingleLine = true,
                                                labelSpacing = 6.dp,
                                            )
                                        }
                                    }

                                    if (bottomRowArtists.isNotEmpty()) {
                                        LazyRow(
                                            contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            items(
                                                items = bottomRowArtists,
                                                key = { "related_bottom_${it.id}" },
                                            ) { artist ->
                                                HomeItemArtist(
                                                    title = artist.title,
                                                    subscribers = null,
                                                    thumbnailUrl = artist.thumbnail,
                                                    onClick = { navController.navigate("artist/${artist.id}") },
                                                    avatarSize = 122.dp,
                                                    isSingleLine = true,
                                                    labelSpacing = 6.dp,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Portrait Mode (or single section in Landscape)
                    if (hasAbout) {
                        item(key = "section_about_header") {
                            ArtistSectionHeader(
                                title = "About",
                            )
                        }

                        item(key = "section_about_card") {
                            ArtistAboutCard(
                                artistName = artistName,
                                portraitUrl = portraitUrl,
                                audienceStat = audienceStat,
                                bioText = bioText,
                                isFollowed = isFollowed,
                                isLandscape = isLandscape,
                                isVerified = isArtistVerified,
                                onToggleFollow = onToggleFollow,
                                onNavigateToAbout = onNavigateToAbout,
                                modifier = if (isLandscape) {
                                    Modifier
                                        .fillMaxWidth(0.5f)
                                        .padding(horizontal = 16.dp)
                                        .aspectRatio(1.1f)
                                        .heightIn(max = 320.dp)
                                } else {
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .aspectRatio(1.1f)
                                        .heightIn(max = 320.dp)
                                },
                            )
                        }
                    }

                    if (distinctArtists.isNotEmpty()) {
                        item(key = "section_related_header") {
                            ArtistSectionHeader(
                                title = "Related Artists",
                                onMoreClick = relatedSection?.moreEndpoint?.let { moreEndpoint ->
                                    {
                                        navController.navigate(
                                            buildArtistItemsRoute(
                                                viewModel.artistId,
                                                moreEndpoint,
                                                title = "Related Artists",
                                                artistName = artistName,
                                            ),
                                        )
                                    }
                                },
                            )
                        }

                        item(key = "section_related_carousel") {
                            LazyRow(
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
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

                item {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .background(Color.Black),
                    )
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .align(Alignment.BottomCenter)
                .zIndex(4f),
        )

        // Selection TopAppBar (shown when multi-selection mode is active)
        AnimatedVisibility(
            visible = selectionState.isActive,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(3f),
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

        // Layer 3: Top Navigation Overlay (zIndex 2f)
        if (!selectionState.isActive) {
            val topBarBackgroundColor by animateColorAsState(
                targetValue = if (topBarTitleVisible) Color.Black.copy(alpha = 0.85f) else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(2f)
                    .background(topBarBackgroundColor)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Pinned floating Back arrow (top-left) in circular semi-translucent dark pill background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    LongClickIconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                AnimatedVisibility(
                    visible = topBarTitleVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                ) {
                    Text(
                        text = artistName ?: unknownArtist,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                animationMode = MarqueeAnimationMode.Immediately,
                            )
                            .focusable(),
                    )
                }

                // Pinned floating 3-dot menu (top-right) in circular semi-translucent dark pill background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    IconButton(
                        onClick = showArtistOverflowMenu,
                        modifier = Modifier.size(40.dp),
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
        }
    }
}

/**
 * Exact container pod implementation matching LibraryScreen's horizontal 'Your Playlists' LazyRow.
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
    thumbSize: Dp = 150.dp,
    onPlayClick: (() -> Unit)? = null,
) {
    val cardBgColor =
        rememberArtworkCardColor(
            thumbnailUrl = thumbnailUrl,
            fallbackColor = MaterialTheme.colorScheme.surfaceContainerLow,
        )

    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberBouncyScale(
        interactionSource = interactionSource,
        targetShrinkScale = 0.97f,
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioMediumBouncy,
    )

    val innerPadding = 5.dp
    val artworkSize = thumbSize - (innerPadding * 2)

    Column(
        modifier =
            modifier
                .width(thumbSize)
                .heightIn(min = (thumbSize * 168f / 130f))
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clip(RoundedCornerShape(18.dp))
                .background(cardBgColor)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ).padding(start = innerPadding, top = innerPadding, end = innerPadding, bottom = 8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(artworkSize)
                    .clip(RoundedCornerShape(14.dp)),
        ) {
            AsyncImage(
                model = thumbnailUrl?.resize(540, 540),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (onPlayClick != null) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .bouncyClickable(onClick = onPlayClick)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Elevated container pod matching Singles/Albums cards for 16:9 widescreen Video carousel items.
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
    cardWidth: Dp = (150f * 16f / 9f).dp,
    onPlayClick: (() -> Unit)? = null,
) {
    val cardBgColor =
        rememberArtworkCardColor(
            thumbnailUrl = thumbnailUrl,
            fallbackColor = MaterialTheme.colorScheme.surfaceContainer,
        )

    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberBouncyScale(
        interactionSource = interactionSource,
        targetShrinkScale = 0.97f,
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioMediumBouncy,
    )

    val innerPadding = 5.dp

    Column(
        modifier =
            modifier
                .width(cardWidth)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(18.dp))
                .background(cardBgColor)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(start = innerPadding, top = innerPadding, end = innerPadding, bottom = 8.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = thumbnailUrl?.resize(854, 480),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (onPlayClick != null) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .bouncyClickable(onClick = onPlayClick)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp)
                .heightIn(min = 48.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.basicMarquee(),
                )
            }
        }
    }
}

/**
 * Circular avatar Related Artists carousel item matching pod touch interaction with CircleShape bounds.
 */
@Composable
private fun HomeItemArtist(
    title: String,
    subscribers: String? = null,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    avatarSize: Dp = 150.dp,
    isSingleLine: Boolean = false,
    labelSpacing: Dp = 8.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberBouncyScale(
        interactionSource = interactionSource,
        targetShrinkScale = 0.95f,
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioMediumBouncy,
    )

    Column(
        modifier = modifier.width(avatarSize),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(avatarSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = thumbnailUrl?.resize(480, 480),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(modifier = Modifier.height(labelSpacing))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = if (isSingleLine) MaterialTheme.typography.bodySmall else MaterialTheme.typography.titleSmall,
                color = Color.White,
                maxLines = if (isSingleLine) 1 else 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(align = Alignment.CenterVertically),
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
                        .fillMaxWidth()
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


@Composable
fun OutlinedFollowPillButton(
    isFollowed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 32.dp,
    borderAlpha: Float = 0.5f,
    horizontalPadding: Dp = 20.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by rememberBouncyScale(
        interactionSource = interactionSource,
        targetShrinkScale = 0.94f,
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioMediumBouncy,
    )
    val backgroundColor = if (isPressed) Color.White.copy(alpha = 0.08f) else Color.Transparent

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .height(height)
            .clip(RoundedCornerShape(50))
            .border(
                border = BorderStroke(1.dp, Color.White.copy(alpha = borderAlpha)),
                shape = RoundedCornerShape(50),
            )
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = horizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (isFollowed) "Following" else "Follow",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            color = Color.White,
        )
    }
}

@Composable
private fun ArtistSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    topSpacing: Dp = 28.dp,
    bottomSpacing: Dp = 9.dp,
    horizontalPadding: Dp = 16.dp,
    onMoreClick: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topSpacing),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = horizontalPadding),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f),
            )
            if (onMoreClick != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .bouncyClickable(
                            shrinkScale = 0.94f,
                            onClick = onMoreClick,
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.more),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(bottomSpacing))
    }
}

@Composable
private fun ArtistAboutCard(
    artistName: String?,
    portraitUrl: String?,
    audienceStat: String?,
    bioText: String?,
    isFollowed: Boolean,
    isLandscape: Boolean,
    isVerified: Boolean = false,
    onToggleFollow: () -> Unit,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val followButtonHeight = 32.dp

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Top Image Pane ("Oil"): Occupies weight(1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                val nonNullPortrait = portraitUrl
                if (!nonNullPortrait.isNullOrBlank()) {
                    AsyncImage(
                        model = nonNullPortrait.resize(1280, 720),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                // Soft bottom gradient seam (height ~28.dp) dissolving into surfaceContainerHigh
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    containerColor,
                                ),
                            ),
                        ),
                )
            }

            // Bottom Text Pane ("Water")
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(containerColor)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                // 1. Header Row: Left Column (Artist Name + Monthly Listeners) | Right (Follow Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Name on line 1, Monthly listeners on line 2
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Artist Name + Verified Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = artistName.orEmpty(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (isVerified) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_verified_badge),
                                    contentDescription = "Verified",
                                    tint = Color.Unspecified, // Keeps the built-in blue and white colors
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }

                        // Monthly Listeners
                        if (!audienceStat.isNullOrBlank()) {
                            Text(
                                text = audienceStat,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Follow Button vertically centered with the (Name + Listeners) block
                    OutlinedFollowPillButton(
                        isFollowed = isFollowed,
                        onClick = onToggleFollow,
                        height = followButtonHeight
                    )
                }

                // 2. Description / Bio Section
                if (!bioText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    // Render 3 lines with inline clickable "...see more"
                    val displayBio = remember(bioText) {
                        val cleaned = bioText.trim()
                        if (cleaned.length > 180) {
                            cleaned.take(180).trimEnd() + "..."
                        } else {
                            cleaned
                        }
                    }

                    Text(
                        text = buildAnnotatedString {
                            append(displayBio)
                            if (bioText.length > 180) {
                                withStyle(
                                    SpanStyle(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                ) {
                                    append("see more")
                                }
                            }
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            lineHeight = 18.sp
                        ),
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = if (isLandscape) 2 else 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.bouncyClickable(onClick = onNavigateToAbout)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistLatestReleaseCard(
    release: AlbumItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
) {
    val releaseType = when {
        release.title.contains("EP", ignoreCase = true) -> stringResource(R.string.ep)
        release.title.contains("Single", ignoreCase = true) -> stringResource(R.string.release_type_single)
        else -> stringResource(R.string.release_type_album)
    }
    val subtitle = listOfNotNull(
        releaseType,
        release.year?.toString(),
    ).joinToString(" • ")

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (isLandscape) 36.dp else 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .bouncyClickable(
                shrinkScale = 0.97f,
                onClick = onClick,
            ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Album Artwork
            if (release.thumbnail.isNotBlank()) {
                AsyncImage(
                    model = release.thumbnail.resize(width = 300, height = 300),
                    contentDescription = release.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.album),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                // Eyebrow label inside the card
                Text(
                    text = "LATEST RELEASE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Album Title
                Text(
                    text = release.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Subtitle (e.g. "Album • 2026")
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            // No chevron (>) or trailing icon
        }
    }
}

val ArtistPage.lastRelease: AlbumItem?
    get() {
        val explicitSection = sections.firstOrNull { section ->
            section.title.contains("latest", ignoreCase = true) ||
                section.title.contains("new release", ignoreCase = true) ||
                section.title.contains("last release", ignoreCase = true)
        }
        val explicitAlbum = explicitSection?.items?.filterIsInstance<AlbumItem>()?.firstOrNull()
        if (explicitAlbum != null) return explicitAlbum
        val explicitSong = explicitSection?.items?.filterIsInstance<SongItem>()?.firstOrNull()
        val currentAlbum = explicitSong?.album
        if (explicitSong != null && currentAlbum != null) {
            return AlbumItem(
                browseId = currentAlbum.id,
                playlistId = explicitSong.id,
                title = explicitSong.title,
                artists = explicitSong.artists,
                thumbnail = explicitSong.thumbnail,
                year = null,
                explicit = explicitSong.explicit,
            )
        }
        val releaseItems = sections
            .filter { section ->
                !section.title.contains("popular", ignoreCase = true) &&
                    !section.title.contains("song", ignoreCase = true) &&
                    (
                        section.title.contains("single", ignoreCase = true) ||
                        section.title.contains("album", ignoreCase = true) ||
                        section.title.contains("ep", ignoreCase = true) ||
                        section.title.contains("release", ignoreCase = true)
                    )
            }
            .flatMap { it.items }
            .filterIsInstance<AlbumItem>()
            .distinctBy { it.id }

        if (releaseItems.isNotEmpty()) {
            return releaseItems.maxByOrNull { it.year ?: Int.MIN_VALUE }
        }

        return sections
            .filter { !it.title.contains("popular", ignoreCase = true) && !it.title.contains("song", ignoreCase = true) }
            .flatMap { it.items }
            .filterIsInstance<AlbumItem>()
            .distinctBy { it.id }
            .maxByOrNull { it.year ?: Int.MIN_VALUE }
    }



