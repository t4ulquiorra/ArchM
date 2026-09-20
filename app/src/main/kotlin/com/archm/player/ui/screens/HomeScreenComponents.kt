/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)

package com.archm.player.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.archm.player.R
import com.archm.player.constants.QuickPicksDisplayMode
import com.archm.player.db.entities.Album
import com.archm.player.db.entities.Artist
import com.archm.player.db.entities.LocalItem
import com.archm.player.db.entities.Playlist
import com.archm.player.db.entities.Song
import com.archm.player.extensions.toMediaItem
import com.archm.player.extensions.togglePlayPause
import com.archm.player.models.MediaMetadata
import com.archm.player.models.SimilarRecommendation
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.PlayerConnection
import com.archm.player.playback.queues.ListQueue
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.playback.queues.LocalAlbumRadio
import com.archm.player.utils.reportException
import com.music.innertube.YouTube
import com.music.innertube.models.isLandscapeThumbnail
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import com.archm.player.ui.component.MenuState
import com.archm.player.ui.component.SpeedDialGridItem
import com.archm.player.ui.component.SpeedDialPodItem
import com.archm.player.ui.menu.AlbumMenu
import com.archm.player.ui.menu.ArtistMenu
import com.archm.player.ui.menu.PlaylistMenu
import com.archm.player.ui.menu.SongMenu
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.ui.utils.SnapLayoutInfoProvider as buildSnapLayoutInfoProvider
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.pages.HomePage
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import com.archm.player.ui.component.bouncyClickable
import com.archm.player.ui.component.rememberBouncyScale
import com.archm.player.ui.screens.library.rememberArtworkCardColor
import com.archm.player.ui.utils.resize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.archm.player.LocalDatabase
import com.archm.player.LocalPlayerConnection
import kotlin.math.roundToInt
import kotlin.random.Random

// ==========================================
// 1. SimpMusic Top App Bar & Chip Components
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val hour =
        remember {
            val date = java.time.LocalTime.now()
            date.hour
        }
    TopAppBar(
        windowInsets =
            TopAppBarDefaults.windowInsets.exclude(
                TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Start),
            ),
        title = {
            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text =
                        when (hour) {
                            in 6..12 -> stringResource(R.string.good_morning)
                            in 13..17 -> stringResource(R.string.good_afternoon)
                            in 18..23 -> stringResource(R.string.good_evening)
                            else -> stringResource(R.string.good_night)
                        },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        actions = {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                IconButton(
                    onClick = { navController.navigate("history") },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.history),
                        contentDescription = stringResource(R.string.history),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = { navController.navigate("news") },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.newspaper),
                        contentDescription = stringResource(R.string.news),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.settings),
                        contentDescription = stringResource(R.string.settings),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
        modifier = modifier,
    )
}

@Composable
fun SimpChip(
    isSelected: Boolean = false,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        ElevatedFilterChip(
            shape = CircleShape,
            elevation = FilterChipDefaults.elevatedFilterChipElevation(elevation = 0.dp),
            colors =
                FilterChipDefaults.elevatedFilterChipColors(
                    containerColor = Color.Transparent,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            onClick = onClick,
            label = {
                Text(text, maxLines = 1, style = MaterialTheme.typography.labelMedium)
            },
            border =
                FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = Color.Transparent,
                    borderColor = MaterialTheme.colorScheme.outline,
                ),
            selected = isSelected,
            modifier = modifier.height(32.dp),
        )
    }
}

@Composable
fun HomeCategoryChips(
    chips: List<HomePage.Chip>,
    selectedChip: HomePage.Chip?,
    onChipSelected: (HomePage.Chip?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filteredChips = remember(chips) {
        chips.filterNot { it.title.equals("all", ignoreCase = true) }
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 15.dp)
                .padding(top = 4.dp, bottom = 6.dp),
    ) {
        SimpChip(
            isSelected = selectedChip == null,
            text = stringResource(R.string.filter_all),
            onClick = {
                if (selectedChip != null) {
                    onChipSelected(null)
                }
            },
        )
        filteredChips.forEach { chip ->
            SimpChip(
                isSelected = chip == selectedChip,
                text = chip.title,
                onClick = { onChipSelected(chip) },
            )
        }
    }
}

// ==========================================
// 2. SimpMusic Account Layout
// ==========================================

@Composable
fun AccountLayout(
    accountName: String,
    url: String?,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (accountName.isBlank() || accountName.equals("Guest", ignoreCase = true)) return

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
                .padding(vertical = 4.dp),
    ) {
        Text(
            text = stringResource(R.string.welcome_back),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 3.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            AsyncImage(
                model =
                    ImageRequest.Builder(LocalContext.current)
                        .data(url)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .diskCacheKey(url)
                        .crossfade(true)
                        .build(),
                placeholder = painterResource(R.drawable.person),
                error = painterResource(R.drawable.person),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape),
            )
            Text(
                text = accountName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 10.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ==========================================
// 3. SimpMusic Quick Picks Components
// ==========================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickPicksItem(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    widthDp: Dp,
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    isExplicit: Boolean = false,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val itemWidth = if (widthDp > 500.dp) 340.dp else (widthDp - 30.dp).coerceAtLeast(260.dp)
    val cardShape = RoundedCornerShape(12.dp)
    Box(
        modifier =
            modifier
                .wrapContentHeight()
                .width(itemWidth)
                .clip(cardShape)
                .background(Color(0xFF141414))
                .border(1.dp, Color.White.copy(alpha = 0.12f), cardShape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp)),
            ) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build(),
                    placeholder = painterResource(R.drawable.music_note),
                    error = painterResource(R.drawable.music_note),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                if (isActive && isPlaying) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.volume_up),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 10.dp, end = 4.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = title,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isActive) MaterialTheme.colorScheme.primary else Color.White,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .basicMarquee(
                                iterations = Int.MAX_VALUE,
                                initialDelayMillis = 2000,
                                repeatDelayMillis = 2000,
                                velocity = 25.dp,
                            ),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isExplicit) {
                        Text(
                            text = "E",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFAAAAAA),
                            modifier =
                                Modifier
                                    .padding(end = 4.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(2.dp),
                                    ).padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
                    Text(
                        text = subtitle,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                            ),
                        color = Color(0xFFAAAAAA),
                        minLines = 1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .basicMarquee(
                                    initialDelayMillis = 2000,
                                    repeatDelayMillis = 2000,
                                    velocity = 25.dp,
                                ),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SimpQuickPicks(
    quickPicks: List<Song>,
    remoteQuickPicks: HomePage.Section?,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyGridState()
    val density = LocalDensity.current
    var widthDp by remember { mutableStateOf(0.dp) }
    val snapLayoutInfoProvider =
        remember(lazyListState) {
            buildSnapLayoutInfoProvider(
                lazyGridState = lazyListState,
                positionInLayout = { _, _ -> 0f },
            )
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .onGloballyPositioned { coordinates ->
                    with(density) {
                        widthDp = coordinates.size.width.toDp()
                    }
                },
    ) {
        Text(
            text = stringResource(R.string.let_s_start_with_a_radio),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.quick_picks),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
        )
        LazyHorizontalGrid(
            rows = GridCells.Fixed(4),
            modifier = Modifier.height(256.dp),
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 2.dp),
        ) {
            if (remoteQuickPicks?.items?.isNotEmpty() == true) {
                items(
                    items = remoteQuickPicks.items,
                    key = { it.id },
                ) { item ->
                    val isActive = item.id == mediaMetadata?.id
                    QuickPicksItem(
                        onClick = {
                            if (item is SongItem) {
                                if (isActive) {
                                    playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.playQueue(
                                        YouTubeQueue(
                                            item.endpoint ?: WatchEndpoint(videoId = item.id),
                                            item.toMediaMetadata(),
                                        ),
                                    )
                                }
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (item is SongItem) {
                                menuState.show {
                                    YouTubeSongMenu(
                                        song = item,
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            }
                        },
                        widthDp = widthDp,
                        title = item.title,
                        subtitle = (item as? SongItem)?.artists?.joinToString { it.name } ?: "",
                        thumbnailUrl = item.thumbnail,
                        isExplicit = item.explicit,
                        isActive = isActive,
                        isPlaying = isPlaying,
                    )
                }
            } else {
                items(
                    items = quickPicks,
                    key = { it.id },
                ) { song ->
                    val isActive = song.id == mediaMetadata?.id
                    QuickPicksItem(
                        onClick = {
                            if (isActive) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    if (song.song.isLocal) {
                                        ListQueue(items = listOf(song.toMediaItem()))
                                    } else {
                                        YouTubeQueue.radio(song.toMediaMetadata())
                                    },
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
                        widthDp = widthDp,
                        title = song.song.title,
                        subtitle = song.artists.joinToString { it.name },
                        thumbnailUrl = song.song.thumbnailUrl,
                        isExplicit = song.song.explicit,
                        isActive = isActive,
                        isPlaying = isPlaying,
                    )
                }
            }
        }
    }
}

@Composable
fun QuickPicksSection(
    quickPicks: List<Song>,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    displayMode: QuickPicksDisplayMode,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier,
) {
    SimpQuickPicks(
        quickPicks = quickPicks,
        remoteQuickPicks = null,
        mediaMetadata = mediaMetadata,
        isPlaying = isPlaying,
        navController = navController,
        playerConnection = playerConnection,
        menuState = menuState,
        haptic = haptic,
        modifier = modifier,
    )
}

// ==========================================
// 4. SimpMusic Shelves & Cards
// ==========================================

@Composable
fun SimpHomeShelf(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    avatarUrl: String? = null,
    onHeaderClick: (() -> Unit)? = null,
    onMoreClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(if (onHeaderClick != null) Modifier.clickable(onClick = onHeaderClick) else Modifier)
                    .padding(vertical = 4.dp),
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(avatarUrl)
                            .crossfade(true)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build(),
                    placeholder = painterResource(R.drawable.person),
                    error = painterResource(R.drawable.person),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (onMoreClick != null) {
                TextButton(
                    onClick = onMoreClick,
                    colors =
                        ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                ) {
                    Text(
                        text = stringResource(R.string.more),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        content()
    }
}

/**
 * Exact container pod implementation matching ArtistScreen's / LibraryScreen's horizontal carousels.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemContentPlaylist(
    title: String,
    subtitle: String?,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
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
    val cardShape = RoundedCornerShape(18.dp)

    Column(
        modifier =
            modifier
                .width(thumbSize)
                .heightIn(min = (thumbSize * 168f / 130f))
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(cardShape)
                .background(cardBgColor)
                .border(1.dp, Color.White.copy(alpha = 0.12f), cardShape)
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
                model = thumbnailUrl?.resize(540, 540) ?: thumbnailUrl,
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
            modifier =
                Modifier
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemSong(
    title: String,
    subtitle: String,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isExplicit: Boolean = false,
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
    val cardShape = RoundedCornerShape(18.dp)

    Column(
        modifier =
            modifier
                .width(thumbSize)
                .heightIn(min = (thumbSize * 168f / 130f))
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(cardShape)
                .background(cardBgColor)
                .border(1.dp, Color.White.copy(alpha = 0.12f), cardShape)
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
                model = thumbnailUrl?.resize(540, 540) ?: thumbnailUrl,
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
            modifier =
                Modifier
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
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(1.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isExplicit) {
                        Text(
                            text = "E",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            modifier =
                                Modifier
                                    .padding(end = 4.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.12f),
                                        RoundedCornerShape(2.dp),
                                    ).padding(horizontal = 4.dp, vertical = 1.dp),
                        )
                    }
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
}

/**
 * Circular avatar Related Artists carousel item matching pod touch interaction with CircleShape bounds.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemArtist(
    title: String,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    subscribers: String? = null,
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
            modifier =
                Modifier
                    .size(avatarSize)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(CircleShape)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                        onLongClick = onLongClick,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = thumbnailUrl?.resize(480, 480) ?: thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(modifier = Modifier.height(labelSpacing))
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick,
                        onLongClick = onLongClick,
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
                modifier =
                    Modifier
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
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(align = Alignment.CenterVertically),
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
fun HomeItemVideo(
    title: String,
    subtitle: String?,
    thumbnailUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
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
    val cardShape = RoundedCornerShape(18.dp)

    Column(
        modifier =
            modifier
                .width(cardWidth)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(cardShape)
                .background(cardBgColor)
                .border(1.dp, Color.White.copy(alpha = 0.12f), cardShape)
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
                model = thumbnailUrl?.resize(854, 480) ?: thumbnailUrl,
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
            modifier =
                Modifier
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
}// ==========================================
// 5. Speed Dial Section (Spotify-style Matrix)
// ==========================================

@Composable
fun SpeedDialSection(
    speedDialItems: List<LocalItem>,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val columns = if (isLandscape) 4 else 2
    val maxRows = 3
    val maxItems = columns * maxRows // 6 items in Portrait, 12 items in Landscape

    val distinctSpeedDial =
        remember(speedDialItems) {
            speedDialItems.distinctBy {
                when (it) {
                    is Song -> "song_${it.id}"
                    is Album -> "album_${it.id}"
                    is Artist -> "artist_${it.id}"
                    is Playlist -> "playlist_${it.id}"
                }
            }
        }

    val displayItems =
        remember(distinctSpeedDial, maxItems) {
            distinctSpeedDial.take(maxItems)
        }

    val speedDialSongs = remember(distinctSpeedDial) { distinctSpeedDial.filterIsInstance<Song>() }
    val speedDialSongIndexById =
        remember(speedDialSongs) {
            speedDialSongs.mapIndexed { index, song -> song.id to index }.toMap()
        }

    fun playSpeedDialQueue(startIndex: Int) {
        if (speedDialSongs.isEmpty()) return
        playerConnection.playQueue(
            ListQueue(
                title = context.getString(R.string.speed_dial),
                items = speedDialSongs.map { it.toMediaItem() },
                startIndex = startIndex,
            ),
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        displayItems.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowItems.forEach { item ->
                    val isActive =
                        when (item) {
                            is Song -> item.id == mediaMetadata?.id
                            is Album -> item.id == mediaMetadata?.album?.id
                            is Artist -> false
                            is Playlist -> false
                        }
                    val isCurrentPlaying = isActive && isPlaying

                    Box(modifier = Modifier.weight(1f)) {
                        SpeedDialPodItem(
                            item = item,
                            isActive = isActive,
                            isPlaying = isCurrentPlaying,
                            onClick = {
                                when (item) {
                                    is Song -> {
                                        if (isActive) {
                                            playerConnection.player.togglePlayPause()
                                        } else {
                                            val songIndex = speedDialSongIndexById[item.id] ?: 0
                                            playSpeedDialQueue(songIndex)
                                        }
                                    }

                                    is Album -> {
                                        navController.navigate("album/${item.id}")
                                    }

                                    is Artist -> {
                                        navController.navigate("artist/${item.id}")
                                    }

                                    is Playlist -> {
                                        navController.navigate("local_playlist/${item.id}")
                                    }
                                }
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    when (item) {
                                        is Song -> {
                                            SongMenu(
                                                originalSong = item,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }

                                        is Album -> {
                                            AlbumMenu(
                                                originalAlbum = item,
                                                navController = navController,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }

                                        is Artist -> {
                                            ArtistMenu(
                                                originalArtist = item,
                                                coroutineScope = scope,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }

                                        is Playlist -> {
                                            PlaylistMenu(
                                                playlist = item,
                                                coroutineScope = scope,
                                                onDismiss = menuState::dismiss,
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
                // Fill empty trailing slots in the last row if uneven
                val emptySlots = columns - rowItems.size
                repeat(emptySlots) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// ==========================================
// 6. Shelves: KeepListening, Playlists, etc.
// ==========================================

@Composable
fun KeepListeningShelf(
    keepListening: List<LocalItem>,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val database = LocalDatabase.current
    val lazyListState = rememberLazyListState()
    val snapLayoutInfoProvider =
        remember(lazyListState) {
            SnapLayoutInfoProvider(lazyListState = lazyListState)
        }

    SimpHomeShelf(
        title = stringResource(R.string.keep_listening),
        modifier = modifier,
    ) {
        LazyRow(
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = keepListening,
                key = { item ->
                    when (item) {
                        is Song -> "song_${item.id}"
                        is Album -> "album_${item.id}"
                        is Artist -> "artist_${item.id}"
                        is Playlist -> "playlist_${item.id}"
                    }
                },
            ) { item ->
                when (item) {
                    is Song -> {
                        val isActive = item.id == mediaMetadata?.id
                        val onPlay: () -> Unit = {
                            if (isActive) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(YouTubeQueue.radio(item.toMediaMetadata()))
                            }
                        }
                        val onLongClickAction: () -> Unit = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                SongMenu(
                                    originalSong = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        }
                        if (item.song.isVideo) {
                            HomeItemVideo(
                                title = item.title,
                                subtitle = item.artists.joinToString { it.name },
                                thumbnailUrl = item.song.thumbnailUrl,
                                onClick = onPlay,
                                onLongClick = onLongClickAction,
                            )
                        } else {
                            HomeItemSong(
                                title = item.title,
                                subtitle = item.artists.joinToString { it.name },
                                thumbnailUrl = item.song.thumbnailUrl,
                                isExplicit = item.song.explicit,
                                onClick = onPlay,
                                onLongClick = onLongClickAction,
                            )
                        }
                    }

                    is Album -> {
                        val onPlay: () -> Unit = {
                            scope.launch(Dispatchers.IO) {
                                val albumWithSongs = database.albumWithSongs(item.id).first()
                                albumWithSongs?.songs?.map { it.toMediaItem() }?.let { mediaItems ->
                                    if (mediaItems.isNotEmpty()) {
                                        withContext(Dispatchers.Main) {
                                            playerConnection.playQueue(ListQueue(items = mediaItems))
                                        }
                                    }
                                }
                            }
                        }
                        HomeItemContentPlaylist(
                            title = item.title,
                            subtitle = item.album.year?.toString() ?: item.artists.joinToString { it.name },
                            thumbnailUrl = item.album.thumbnailUrl,
                            onClick = { navController.navigate("album/${item.id}") },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    AlbumMenu(
                                        originalAlbum = item,
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                            onPlayClick = onPlay,
                        )
                    }

                    is Artist -> {
                        HomeItemArtist(
                            title = item.title,
                            thumbnailUrl = item.artist.thumbnailUrl,
                            onClick = { navController.navigate("artist/${item.id}") },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    ArtistMenu(
                                        originalArtist = item,
                                        coroutineScope = scope,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                        )
                    }

                    is Playlist -> {
                        HomeItemContentPlaylist(
                            title = item.title,
                            subtitle = stringResource(R.string.playlist),
                            thumbnailUrl = item.thumbnails.firstOrNull(),
                            onClick = { navController.navigate("local_playlist/${item.id}") },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    PlaylistMenu(
                                        playlist = item,
                                        coroutineScope = scope,
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
}

@Composable
fun AccountPlaylistsShelf(
    accountPlaylists: List<PlaylistItem>,
    accountName: String,
    accountImageUrl: String?,
    navController: NavController,
    modifier: Modifier = Modifier,
    playerConnection: PlayerConnection? = LocalPlayerConnection.current,
) {
    val lazyListState = rememberLazyListState()
    val snapLayoutInfoProvider =
        remember(lazyListState) {
            SnapLayoutInfoProvider(lazyListState = lazyListState)
        }

    SimpHomeShelf(
        title = accountName.ifBlank { stringResource(R.string.your_youtube_playlists) },
        subtitle = stringResource(R.string.your_youtube_playlists),
        avatarUrl = accountImageUrl,
        onMoreClick = { navController.navigate("account") },
        modifier = modifier,
    ) {
        LazyRow(
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = accountPlaylists,
                key = { it.id },
            ) { item ->
                HomeItemContentPlaylist(
                    title = item.title,
                    subtitle = item.author?.name ?: item.songCountText ?: stringResource(R.string.playlist),
                    thumbnailUrl = item.thumbnail,
                    onClick = { navController.navigate("online_playlist/${item.id}") },
                )
            }
        }
    }
}

@Composable
fun ForgottenFavoritesShelf(
    forgottenFavorites: List<Song>,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val snapLayoutInfoProvider =
        remember(lazyListState) {
            SnapLayoutInfoProvider(lazyListState = lazyListState)
        }

    SimpHomeShelf(
        title = stringResource(R.string.forgotten_favorites),
        modifier = modifier,
    ) {
        LazyRow(
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = forgottenFavorites,
                key = { it.id },
            ) { song ->
                val isActive = song.id == mediaMetadata?.id
                val onPlay: () -> Unit = {
                    if (isActive) {
                        playerConnection.player.togglePlayPause()
                    } else {
                        playerConnection.playQueue(
                            if (song.song.isLocal) {
                                ListQueue(items = listOf(song.toMediaItem()))
                            } else {
                                YouTubeQueue.radio(song.toMediaMetadata())
                            },
                        )
                    }
                }
                val onLongClickAction: () -> Unit = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    menuState.show {
                        SongMenu(
                            originalSong = song,
                            navController = navController,
                            onDismiss = menuState::dismiss,
                        )
                    }
                }
                if (song.song.isVideo) {
                    HomeItemVideo(
                        title = song.song.title,
                        subtitle = song.artists.joinToString { it.name },
                        thumbnailUrl = song.song.thumbnailUrl,
                        onClick = onPlay,
                        onLongClick = onLongClickAction,
                    )
                } else {
                    HomeItemSong(
                        title = song.song.title,
                        subtitle = song.artists.joinToString { it.name },
                        thumbnailUrl = song.song.thumbnailUrl,
                        isExplicit = song.song.explicit,
                        onClick = onPlay,
                        onLongClick = onLongClickAction,
                    )
                }
            }
        }
    }
}

@Composable
fun SimilarRecommendationsShelf(
    recommendation: SimilarRecommendation,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val snapLayoutInfoProvider =
        remember(lazyListState) {
            SnapLayoutInfoProvider(lazyListState = lazyListState)
        }
    val titleItem = recommendation.title
    val database = LocalDatabase.current

    SimpHomeShelf(
        title = titleItem.title,
        subtitle = stringResource(R.string.similar_to),
        avatarUrl = titleItem.thumbnailUrl,
        onHeaderClick = {
            when (titleItem) {
                is Song -> titleItem.album?.id?.let { navController.navigate("album/$it") }
                is Album -> navController.navigate("album/${titleItem.id}")
                is Artist -> navController.navigate("artist/${titleItem.id}")
                is Playlist -> {}
            }
        },
        modifier = modifier,
    ) {
        LazyRow(
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = recommendation.items,
                key = { it.id },
            ) { item ->
                when (item) {
                    is ArtistItem -> {
                        HomeItemArtist(
                            title = item.title,
                            thumbnailUrl = item.thumbnail,
                            onClick = { navController.navigate("artist/${item.id}") },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    YouTubeArtistMenu(
                                        artist = item,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                        )
                    }

                    is SongItem -> {
                        val isVideo = item.isLandscapeThumbnail
                        val isActive = item.id == mediaMetadata?.id
                        val onPlay: () -> Unit = {
                            if (isActive) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    YouTubeQueue(
                                        item.endpoint ?: WatchEndpoint(videoId = item.id),
                                        item.toMediaMetadata(),
                                    ),
                                )
                            }
                        }
                        val onLongClickAction: () -> Unit = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                YouTubeSongMenu(
                                    song = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        }
                        val subtitle = listOfNotNull(
                            item.artists.joinToString(", ") { it.name }.takeIf { it.isNotBlank() },
                            item.durationText,
                        ).joinToString(" • ").ifBlank { item.artists.joinToString { it.name } }

                        if (isVideo) {
                            HomeItemVideo(
                                title = item.title,
                                subtitle = subtitle,
                                thumbnailUrl = item.thumbnail,
                                onClick = onPlay,
                                onLongClick = onLongClickAction,
                            )
                        } else {
                            HomeItemSong(
                                title = item.title,
                                subtitle = subtitle,
                                thumbnailUrl = item.thumbnail,
                                isExplicit = item.explicit,
                                onClick = onPlay,
                                onLongClick = onLongClickAction,
                            )
                        }
                    }

                    is AlbumItem -> {
                        HomeItemContentPlaylist(
                            title = item.title,
                            subtitle = item.year?.toString() ?: item.artists?.joinToString { it.name } ?: "",
                            thumbnailUrl = item.thumbnail,
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
                            onPlayClick = {
                                scope.launch(Dispatchers.IO) {
                                    var albumWithSongs = database.albumWithSongs(item.id).first()
                                    if (albumWithSongs?.songs.isNullOrEmpty()) {
                                        YouTube.album(item.id).onSuccess { albumPage ->
                                            database.transaction { insert(albumPage) }
                                            albumWithSongs = database.albumWithSongs(item.id).first()
                                        }.onFailure { reportException(it) }
                                    }
                                    albumWithSongs?.let {
                                        withContext(Dispatchers.Main) {
                                            playerConnection.playQueue(LocalAlbumRadio(it))
                                        }
                                    } ?: run {
                                        withContext(Dispatchers.Main) {
                                            playerConnection.playQueue(
                                                YouTubeQueue(
                                                    WatchEndpoint(playlistId = item.playlistId),
                                                ),
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }

                    is PlaylistItem -> {
                        if (item.isLandscapeThumbnail) {
                            HomeItemVideo(
                                title = item.title,
                                subtitle = item.author?.name ?: item.songCountText ?: stringResource(R.string.playlist),
                                thumbnailUrl = item.thumbnail,
                                onClick = { navController.navigate("online_playlist/${item.id}") },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        YouTubePlaylistMenu(
                                            playlist = item,
                                            coroutineScope = scope,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                            )
                        } else {
                            HomeItemContentPlaylist(
                                title = item.title,
                                subtitle = item.author?.name ?: item.songCountText ?: stringResource(R.string.playlist),
                                thumbnailUrl = item.thumbnail,
                                onClick = { navController.navigate("online_playlist/${item.id}") },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        YouTubePlaylistMenu(
                                            playlist = item,
                                            coroutineScope = scope,
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
    }
}

@Composable
fun HomePageSectionShelf(
    section: HomePage.Section,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val database = LocalDatabase.current
    val snapLayoutInfoProvider =
        remember(lazyListState) {
            SnapLayoutInfoProvider(lazyListState = lazyListState)
        }

    val onMoreClick: (() -> Unit)? =
        section.endpoint?.let { endpoint ->
            {
                when {
                    endpoint.browseId == "FEmusic_moods_and_genres" -> {
                        navController.navigate(Screens.MoodAndGenres.route)
                    }
                    endpoint.isArtistEndpoint -> {
                        navController.navigate("artist/${endpoint.browseId}")
                    }
                    endpoint.isAlbumEndpoint -> {
                        navController.navigate("album/${endpoint.browseId}")
                    }
                    endpoint.isPlaylistEndpoint -> {
                        navController.navigate("online_playlist/${endpoint.browseId}")
                    }
                    else -> {
                        val route = if (endpoint.params != null) {
                            "browse/${endpoint.browseId}?params=${endpoint.params}"
                        } else {
                            "browse/${endpoint.browseId}"
                        }
                        navController.navigate(route)
                    }
                }
            }
        }

    SimpHomeShelf(
        title = section.title,
        subtitle = section.label,
        avatarUrl = if (section.endpoint?.isArtistEndpoint == true) section.thumbnail else null,
        onMoreClick = onMoreClick,
        modifier = modifier,
    ) {
        LazyRow(
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = section.items,
                key = { it.id },
            ) { item ->
                when (item) {
                    is ArtistItem -> {
                        HomeItemArtist(
                            title = item.title,
                            thumbnailUrl = item.thumbnail,
                            onClick = { navController.navigate("artist/${item.id}") },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    YouTubeArtistMenu(
                                        artist = item,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                        )
                    }

                    is SongItem -> {
                        val isVideo = item.isLandscapeThumbnail
                        val isActive = item.id == mediaMetadata?.id
                        val onPlay: () -> Unit = {
                            if (isActive) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    YouTubeQueue(
                                        item.endpoint ?: WatchEndpoint(videoId = item.id),
                                        item.toMediaMetadata(),
                                    ),
                                )
                            }
                        }
                        val onLongClickAction: () -> Unit = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                YouTubeSongMenu(
                                    song = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        }
                        val subtitle = listOfNotNull(
                            item.artists.joinToString(", ") { it.name }.takeIf { it.isNotBlank() },
                            item.durationText,
                        ).joinToString(" • ").ifBlank { item.artists.joinToString { it.name } }

                        if (isVideo) {
                            HomeItemVideo(
                                title = item.title,
                                subtitle = subtitle,
                                thumbnailUrl = item.thumbnail,
                                onClick = onPlay,
                                onLongClick = onLongClickAction,
                            )
                        } else {
                            HomeItemSong(
                                title = item.title,
                                subtitle = subtitle,
                                thumbnailUrl = item.thumbnail,
                                isExplicit = item.explicit,
                                onClick = onPlay,
                                onLongClick = onLongClickAction,
                            )
                        }
                    }

                    is AlbumItem -> {
                        HomeItemContentPlaylist(
                            title = item.title,
                            subtitle = item.year?.toString() ?: item.artists?.joinToString { it.name } ?: "",
                            thumbnailUrl = item.thumbnail,
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
                            onPlayClick = {
                                scope.launch(Dispatchers.IO) {
                                    var albumWithSongs = database.albumWithSongs(item.id).first()
                                    if (albumWithSongs?.songs.isNullOrEmpty()) {
                                        YouTube.album(item.id).onSuccess { albumPage ->
                                            database.transaction { insert(albumPage) }
                                            albumWithSongs = database.albumWithSongs(item.id).first()
                                        }.onFailure { reportException(it) }
                                    }
                                    albumWithSongs?.let {
                                        withContext(Dispatchers.Main) {
                                            playerConnection.playQueue(LocalAlbumRadio(it))
                                        }
                                    } ?: run {
                                        withContext(Dispatchers.Main) {
                                            playerConnection.playQueue(
                                                YouTubeQueue(
                                                    WatchEndpoint(playlistId = item.playlistId),
                                                ),
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }

                    is PlaylistItem -> {
                        if (item.isLandscapeThumbnail) {
                            HomeItemVideo(
                                title = item.title,
                                subtitle = item.author?.name ?: item.songCountText ?: stringResource(R.string.playlist),
                                thumbnailUrl = item.thumbnail,
                                onClick = { navController.navigate("online_playlist/${item.id}") },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        YouTubePlaylistMenu(
                                            playlist = item,
                                            coroutineScope = scope,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                },
                            )
                        } else {
                            HomeItemContentPlaylist(
                                title = item.title,
                                subtitle = item.author?.name ?: item.songCountText ?: stringResource(R.string.playlist),
                                thumbnailUrl = item.thumbnail,
                                onClick = { navController.navigate("online_playlist/${item.id}") },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    menuState.show {
                                        YouTubePlaylistMenu(
                                            playlist = item,
                                            coroutineScope = scope,
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
    }
}

// ==========================================
// 7. SimpMusic Effects: Scrim, Gradient, Scrolling
// ==========================================

@Composable
fun LazyListState.isScrollingUp(): State<Boolean> {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }

    LaunchedEffect(Unit) {
        snapshotFlow { layoutInfo.totalItemsCount }.collect {
            previousIndex = firstVisibleItemIndex
            previousScrollOffset = firstVisibleItemScrollOffset
        }
    }

    return remember(this) {
        derivedStateOf {
            if (firstVisibleItemIndex > 0) {
                if (previousIndex != firstVisibleItemIndex) {
                    previousIndex > firstVisibleItemIndex
                } else {
                    previousScrollOffset >= firstVisibleItemScrollOffset
                }.also {
                    previousIndex = firstVisibleItemIndex
                    previousScrollOffset = firstVisibleItemScrollOffset
                }
            } else {
                true
            }
        }
    }
}

fun Modifier.angledGradientBackground(
    colors: List<Color>,
    degrees: Float,
) = this.then(
    if (colors.size < 2) {
        Modifier
    } else {
        Modifier.drawBehind {
            val (x, y) = size
            val gamma = kotlin.math.atan2(y, x)

            if (gamma == 0f || gamma == (Math.PI / 2).toFloat()) {
                return@drawBehind
            }

            val degreesNormalised = (degrees % 360).let { if (it < 0) it + 360 else it }
            val alpha = (degreesNormalised * Math.PI / 180).toFloat()

            val gradientLength =
                when (alpha) {
                    in 0f..gamma, in ((2 * Math.PI - gamma).toFloat())..((2 * Math.PI).toFloat()) -> {
                        x / kotlin.math.cos(alpha)
                    }
                    in gamma..((Math.PI - gamma).toFloat()) -> {
                        y / kotlin.math.sin(alpha)
                    }
                    in ((Math.PI - gamma).toFloat())..((Math.PI + gamma).toFloat()) -> {
                        x / -kotlin.math.cos(alpha)
                    }
                    in ((Math.PI + gamma).toFloat())..((2 * Math.PI - gamma).toFloat()) -> {
                        y / -kotlin.math.sin(alpha)
                    }
                    else -> {
                        kotlin.math.hypot(x, y)
                    }
                }

            val centerOffsetX = kotlin.math.cos(alpha) * gradientLength / 2
            val centerOffsetY = kotlin.math.sin(alpha) * gradientLength / 2

            drawRect(
                brush =
                    Brush.linearGradient(
                        colors = colors,
                        start = Offset(center.x - centerOffsetX, center.y - centerOffsetY),
                        end = Offset(center.x + centerOffsetX, center.y + centerOffsetY),
                    ),
                size = size,
            )
        }
    },
)

fun smoothScrimBrush(
    from: Color,
    to: Color,
    startFraction: Float = 0f,
    endFraction: Float = 1f,
    startY: Float = 0f,
    endY: Float = Float.POSITIVE_INFINITY,
    steps: Int = 24,
): Brush =
    Brush.verticalGradient(
        colorStops =
            Array(steps + 1) { i ->
                val t = i / steps.toFloat()
                val position = startFraction + (endFraction - startFraction) * t
                position to androidx.compose.ui.graphics.lerp(from, to, t * t * (3f - 2f * t))
            },
        startY = startY,
        endY = endY,
    )

fun artworkScrimBrush(
    color: Color,
    steps: Int = 24,
): Brush = smoothScrimBrush(from = color.copy(alpha = 0f), to = color, steps = steps)
