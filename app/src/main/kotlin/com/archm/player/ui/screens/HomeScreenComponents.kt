/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

@file:OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class,
)

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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.carousel.HorizontalCenteredHeroCarousel
import androidx.compose.material3.carousel.rememberCarouselState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.archm.player.viewmodels.HomeViewModel
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
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
fun MainTopBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    accountName: String = "",
    accountImageUrl: String? = null,
    titleContent: (@Composable () -> Unit)? = null,
) {
    val hour =
        remember {
            val date = java.time.LocalTime.now()
            date.hour
        }
    val greeting =
        when (hour) {
            in 6..12 -> stringResource(R.string.good_morning)
            in 13..17 -> stringResource(R.string.good_afternoon)
            in 18..23 -> stringResource(R.string.good_evening)
            else -> stringResource(R.string.good_night)
        }
    val homeViewModel: HomeViewModel = hiltViewModel()
    val vmAccountName by homeViewModel.accountName.collectAsState()
    val vmAccountImageUrl by homeViewModel.accountImageUrl.collectAsState()

    val effectiveName = accountName.ifBlank { vmAccountName }
    val effectiveImageUrl = accountImageUrl ?: vmAccountImageUrl

    val displayName =
        if (effectiveName.isNotBlank() && !effectiveName.equals("Guest", ignoreCase = true)) {
            effectiveName
        } else {
            stringResource(R.string.app_name)
        }

    TopAppBar(
        windowInsets =
            TopAppBarDefaults.windowInsets.exclude(
                TopAppBarDefaults.windowInsets.only(WindowInsetsSides.Start),
            ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { navController.navigate("account") },
            ) {
                AsyncImage(
                    model =
                        ImageRequest.Builder(LocalContext.current)
                            .data(effectiveImageUrl)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .diskCacheKey(effectiveImageUrl)
                            .crossfade(true)
                            .build(),
                    placeholder = painterResource(R.drawable.person),
                    error = painterResource(R.drawable.person),
                    contentDescription = stringResource(R.string.account),
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                )
                if (titleContent != null) {
                    titleContent()
                } else {
                    Column(
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
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
fun HomeTopAppBar(
    navController: NavController,
    modifier: Modifier = Modifier,
    accountName: String = "",
    accountImageUrl: String? = null,
    titleContent: (@Composable () -> Unit)? = null,
) = MainTopBar(
    navController = navController,
    modifier = modifier,
    accountName = accountName,
    accountImageUrl = accountImageUrl,
    titleContent = titleContent,
)

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
// 2. SimpMusic Quick Picks Components
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

data class QuickPicksCarouselItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val thumbnailUrl: String?,
    val onClick: () -> Unit,
    val onLongClick: () -> Unit,
)

fun Song.toQuickPicksCarouselItem(
    playerConnection: PlayerConnection,
    navController: NavController,
    menuState: MenuState,
    haptic: HapticFeedback,
    mediaMetadata: MediaMetadata?,
): QuickPicksCarouselItem {
    val isActive = id == mediaMetadata?.id
    return QuickPicksCarouselItem(
        id = id,
        title = song.title,
        subtitle = artists.joinToString { it.name },
        thumbnailUrl = song.thumbnailUrl,
        onClick = {
            if (isActive) {
                playerConnection.player.togglePlayPause()
            } else {
                playerConnection.playQueue(
                    if (song.isLocal) {
                        ListQueue(items = listOf(toMediaItem()))
                    } else {
                        YouTubeQueue.radio(toMediaMetadata())
                    },
                )
            }
        },
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
                SongMenu(
                    originalSong = this@toQuickPicksCarouselItem,
                    navController = navController,
                    onDismiss = menuState::dismiss,
                )
            }
        },
    )
}

fun YTItem.toQuickPicksCarouselItem(
    playerConnection: PlayerConnection,
    navController: NavController,
    menuState: MenuState,
    haptic: HapticFeedback,
    mediaMetadata: MediaMetadata?,
): QuickPicksCarouselItem {
    val isActive = id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id)
    val itemTitle =
        when (this) {
            is SongItem -> title
            is AlbumItem -> title
            is ArtistItem -> title
            is PlaylistItem -> title
        }
    val itemSubtitle =
        when (this) {
            is SongItem -> artists.joinToString { it.name }
            is AlbumItem -> artists?.joinToString { it.name }.orEmpty()
            is ArtistItem -> ""
            is PlaylistItem -> author?.name.orEmpty()
        }
    return QuickPicksCarouselItem(
        id = id,
        title = itemTitle,
        subtitle = itemSubtitle,
        thumbnailUrl = thumbnail,
        onClick = {
            when (this) {
                is SongItem -> {
                    if (isActive) {
                        playerConnection.player.togglePlayPause()
                    } else {
                        playerConnection.playQueue(
                            YouTubeQueue(
                                endpoint ?: WatchEndpoint(videoId = id),
                                toMediaMetadata(),
                            ),
                        )
                    }
                }
                is AlbumItem -> navController.navigate("album/$id")
                is ArtistItem -> navController.navigate("artist/$id")
                is PlaylistItem -> navController.navigate("online_playlist/$id")
            }
        },
        onLongClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            menuState.show {
                when (this@toQuickPicksCarouselItem) {
                    is SongItem ->
                        YouTubeSongMenu(
                            song = this@toQuickPicksCarouselItem,
                            navController = navController,
                            onDismiss = menuState::dismiss,
                        )
                    is AlbumItem ->
                        YouTubeAlbumMenu(
                            albumItem = this@toQuickPicksCarouselItem,
                            navController = navController,
                            onDismiss = menuState::dismiss,
                        )
                    is ArtistItem ->
                        YouTubeArtistMenu(
                            artist = this@toQuickPicksCarouselItem,
                            onDismiss = menuState::dismiss,
                        )
                    is PlaylistItem ->
                        YouTubePlaylistMenu(
                            playlist = this@toQuickPicksCarouselItem,
                            onDismiss = menuState::dismiss,
                        )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuickPicksCarousel(
    items: List<QuickPicksCarouselItem>,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val distinctItems = remember(items) { items.distinctBy { it.id } }
    if (distinctItems.isEmpty()) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val heroHeight =
            when {
                maxWidth >= 840.dp -> 210.dp
                maxWidth >= 600.dp -> 195.dp
                else -> 185.dp
            }
        val heroMaxWidth = (maxWidth - 32.dp).coerceAtMost(360.dp)
        val density = LocalDensity.current
        val requestWidthPx = with(density) { heroMaxWidth.roundToPx().coerceAtLeast(1) }
        val requestHeightPx = with(density) { heroHeight.roundToPx().coerceAtLeast(1) }

        HorizontalCenteredHeroCarousel(
            state = rememberCarouselState { distinctItems.size },
            maxItemWidth = heroMaxWidth,
            itemSpacing = 10.dp,
            contentPadding = contentPadding,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(heroHeight),
        ) { index ->
            val item = distinctItems[index]
            val isActive = item.id == mediaMetadata?.id || item.id == mediaMetadata?.album?.id
            val context = LocalContext.current
            val imageRequest =
                remember(item.thumbnailUrl, requestWidthPx, requestHeightPx) {
                    ImageRequest
                        .Builder(context)
                        .data(item.thumbnailUrl)
                        .size(Size(requestWidthPx, requestHeightPx))
                        .crossfade(true)
                        .build()
                }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .maskClip(MaterialTheme.shapes.extraLarge)
                        .maskBorder(
                            BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
                            ),
                            MaterialTheme.shapes.extraLarge,
                        ).focusable()
                        .combinedClickable(
                            onClick = item.onClick,
                            onLongClick = item.onLongClick,
                        ),
            ) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    0f to Color.Transparent,
                                    0.48f to Color.Black.copy(alpha = 0.08f),
                                    1f to Color.Black.copy(alpha = 0.84f),
                                ),
                            ),
                )

                if (isActive && isPlaying) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape,
                        tonalElevation = 2.dp,
                        modifier =
                            Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(32.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(R.drawable.volume_up),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier =
                        Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                ) {
                    Text(
                        text = item.title,
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (item.subtitle.isNotBlank()) {
                        Text(
                            text = item.subtitle,
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                ),
                            color = Color.White.copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickPicksCarouselShelf(
    section: HomePage.Section,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier,
) {
    val carouselItems =
        remember(section.items, mediaMetadata, isPlaying) {
            section.items.map {
                it.toQuickPicksCarouselItem(
                    playerConnection = playerConnection,
                    navController = navController,
                    menuState = menuState,
                    haptic = haptic,
                    mediaMetadata = mediaMetadata,
                )
            }
        }

    if (carouselItems.isEmpty()) return

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
                        val encodedBrowseId = android.net.Uri.encode(endpoint.browseId)
                        val encodedParams = endpoint.params?.takeIf { it.isNotBlank() }?.let { android.net.Uri.encode(it) }
                        val encodedTitle = section.title.takeIf { it.isNotBlank() }?.let { android.net.Uri.encode(it) }

                        val route =
                            buildString {
                                append("browse/")
                                append(encodedBrowseId)
                                val queryParams = mutableListOf<String>()
                                if (encodedParams != null) queryParams.add("params=$encodedParams")
                                if (encodedTitle != null) queryParams.add("title=$encodedTitle")
                                if (queryParams.isNotEmpty()) {
                                    append("?")
                                    append(queryParams.joinToString("&"))
                                }
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
        onHeaderClick = onMoreClick,
        onMoreClick = onMoreClick,
        modifier = modifier,
    ) {
        QuickPicksCarousel(
            items = carouselItems,
            mediaMetadata = mediaMetadata,
            isPlaying = isPlaying,
        )
    }
}

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
    val carouselItems =
        remember(remoteQuickPicks, quickPicks, mediaMetadata, isPlaying) {
            if (remoteQuickPicks?.items?.isNotEmpty() == true) {
                remoteQuickPicks.items.map {
                    it.toQuickPicksCarouselItem(
                        playerConnection = playerConnection,
                        navController = navController,
                        menuState = menuState,
                        haptic = haptic,
                        mediaMetadata = mediaMetadata,
                    )
                }
            } else {
                quickPicks.map {
                    it.toQuickPicksCarouselItem(
                        playerConnection = playerConnection,
                        navController = navController,
                        menuState = menuState,
                        haptic = haptic,
                        mediaMetadata = mediaMetadata,
                    )
                }
            }
        }

    if (carouselItems.isEmpty()) return

    SimpHomeShelf(
        title = remoteQuickPicks?.title ?: stringResource(R.string.quick_picks),
        subtitle = stringResource(R.string.let_s_start_with_a_radio),
        modifier = modifier,
    ) {
        QuickPicksCarousel(
            items = carouselItems,
            mediaMetadata = mediaMetadata,
            isPlaying = isPlaying,
        )
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
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W800,
                        ),
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

private val CardGrey = Color(0xFF9D9D9D)
private val CardMarbleWhite = Color(0xFFFFFFFF)
private val CardPureWhite = Color(0xFFFFFFFF)

val HomeVideoCardWidth: Dp = 216.dp
val HomeVideoThumbnailHeight: Dp = 116.dp
val HomeVideoPodHeight: Dp = 180.dp

// Square card: 126dp wide matching ArtistScreen
val HomeSquareCardThumbSize: Dp = 126.dp

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
    thumbSize: Dp = HomeSquareCardThumbSize,
    onPlayClick: (() -> Unit)? = null,
    typeLabel: String = "Playlist",
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

    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier =
            modifier
                .width(thumbSize)
                .height(180.dp)
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
                ).padding(start = 5.dp, top = 5.dp, end = 5.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
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
                            .padding(6.dp)
                            .size(32.dp)
                            .bouncyClickable(onClick = onPlayClick)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
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
                text = typeLabel,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = CardGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = CardMarbleWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(3.dp))

            val sanitizedSubtitle = subtitle?.takeUnless {
                it.isBlank() || it.equals(typeLabel, ignoreCase = true) || it.equals("Playlist", ignoreCase = true)
            }

            if (sanitizedSubtitle != null) {
                Text(
                    text = sanitizedSubtitle,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.5.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    color = CardGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(2.dp))
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
    thumbSize: Dp = HomeSquareCardThumbSize,
    onPlayClick: (() -> Unit)? = null,
    typeLabel: String = "Song",
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

    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier =
            modifier
                .width(thumbSize)
                .height(180.dp)
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
                ).padding(start = 5.dp, top = 5.dp, end = 5.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
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
                            .padding(6.dp)
                            .size(32.dp)
                            .bouncyClickable(onClick = onPlayClick)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
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
                text = typeLabel,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = CardGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = CardMarbleWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(3.dp))

            if (subtitle.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (isExplicit) {
                        Text(
                            text = "E",
                            style =
                                MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            color = CardGrey,
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
                                fontSize = 9.5.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                        color = CardGrey,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

/**
 * Circular avatar Related Artists carousel item — circular pod container blending into screen background,
 * no visible border, and 5dp inner padding matching standard square card pods.
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
    avatarSize: Dp = HomeSquareCardThumbSize,
    isSingleLine: Boolean = false,
    labelSpacing: Dp = 8.dp,
    typeLabel: String = "Artist",
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberBouncyScale(
        interactionSource = interactionSource,
        targetShrinkScale = 0.97f,
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioMediumBouncy,
    )

    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier =
            modifier
                .width(avatarSize)
                .height(180.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(cardShape)
                .background(MaterialTheme.colorScheme.background)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(start = 5.dp, top = 5.dp, end = 5.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = thumbnailUrl?.resize(480, 480) ?: thumbnailUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 3.dp),
        ) {
            Text(
                text = typeLabel,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = CardGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = CardPureWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(3.dp))

            if (!subscribers.isNullOrBlank()) {
                Text(
                    text = subscribers,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.5.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    color = CardGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(2.dp))
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
    cardWidth: Dp = HomeVideoCardWidth,
    onPlayClick: (() -> Unit)? = null,
    typeLabel: String = "Video",
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

    val cardShape = RoundedCornerShape(16.dp)

    Column(
        modifier =
            modifier
                .width(cardWidth)
                .height(180.dp)
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
                .padding(start = 5.dp, top = 5.dp, end = 5.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(116.dp)
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
                            .padding(6.dp)
                            .size(32.dp)
                            .bouncyClickable(onClick = onPlayClick)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.play),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
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
                text = typeLabel,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = CardGrey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = title,
                style =
                    MaterialTheme.typography.labelLarge.copy(
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = CardMarbleWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(3.dp))

            val sanitizedSubtitle = subtitle?.takeUnless {
                it.isBlank() || it.equals(typeLabel, ignoreCase = true) || it.equals("Video", ignoreCase = true)
            }

            if (sanitizedSubtitle != null) {
                Text(
                    text = sanitizedSubtitle,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontSize = 9.5.sp,
                            lineHeight = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                    color = CardGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}
// ==========================================
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
    val maxRows = 2
    val maxItems = columns * maxRows // 4 items in Portrait, 8 items in Landscape

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
        displayItems.chunked(columns).take(maxRows).forEach { rowItems ->
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
        val distinctKeepListening = remember(keepListening) {
            keepListening.distinctBy { item ->
                when (item) {
                    is Song -> "song_${item.id}"
                    is Album -> "album_${item.id}"
                    is Artist -> "artist_${item.id}"
                    is Playlist -> "playlist_${item.id}"
                }
            }
        }
        LazyRow(
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = distinctKeepListening,
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
                            subtitle = item.artists.joinToString { it.name }.ifBlank { item.album.year?.toString() },
                            thumbnailUrl = item.album.thumbnailUrl,
                            typeLabel = "Album",
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
                            subtitle = pluralStringResource(R.plurals.n_song, item.songCount, item.songCount),
                            thumbnailUrl = item.thumbnails.firstOrNull(),
                            typeLabel = "Playlist",
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
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = accountPlaylists.distinctBy { it.id },
                key = { "ap_${it.id}" },
            ) { item ->
                HomeItemContentPlaylist(
                    title = item.title,
                    subtitle = item.author?.name ?: item.songCountText,
                    thumbnailUrl = item.thumbnail,
                    typeLabel = "Playlist",
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
    val shelfItems =
        remember(forgottenFavorites) {
            forgottenFavorites.filterNot { song ->
                song.id == "LM" || song.id == "VLLM" ||
                    song.song.title.equals("Liked Music", ignoreCase = true)
            }
        }

    if (shelfItems.isEmpty()) return

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
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = shelfItems.distinctBy { it.id },
                key = { "ff_${it.id}" },
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
    val distinctRecommendationItems = remember(recommendation.items) {
        recommendation.items.distinctBy { it.id }
    }

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
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = distinctRecommendationItems,
                key = { "${recommendation.title.id}_${it.id}" },
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
                            subtitle = item.artists?.joinToString { it.name } ?: item.year?.toString(),
                            thumbnailUrl = item.thumbnail,
                            typeLabel = "Album",
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
                                subtitle = item.author?.name ?: item.songCountText,
                                thumbnailUrl = item.thumbnail,
                                typeLabel = "Video",
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
                                subtitle = item.author?.name ?: item.songCountText,
                                thumbnailUrl = item.thumbnail,
                                typeLabel = "Playlist",
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
    if (section.title.equals("Quick picks", ignoreCase = true) ||
        section.title.contains("quick pick", ignoreCase = true) ||
        section.title.equals("Listen again", ignoreCase = true) ||
        section.title.contains("listen again", ignoreCase = true)
    ) {
        QuickPicksCarouselShelf(
            section = section,
            mediaMetadata = mediaMetadata,
            isPlaying = isPlaying,
            navController = navController,
            playerConnection = playerConnection,
            menuState = menuState,
            haptic = haptic,
            modifier = modifier,
        )
        return
    }

    val lazyListState = rememberLazyListState()
    val database = LocalDatabase.current
    val snapLayoutInfoProvider =
        remember(lazyListState) {
            SnapLayoutInfoProvider(lazyListState = lazyListState)
        }

    val isTargetShelf =
        section.title.equals("Forgotten favorites", ignoreCase = true) ||
            section.title.equals("Fresh finds, old favorites", ignoreCase = true)

    val shelfItems =
        remember(section.items, isTargetShelf) {
            if (isTargetShelf) {
                section.items.filterNot { item ->
                    item.id == "LM" || item.id == "VLLM" ||
                        (item is PlaylistItem && item.title.equals("Liked Music", ignoreCase = true))
                }
            } else {
                section.items
            }
        }

    if (shelfItems.isEmpty()) return

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
                        val encodedBrowseId = android.net.Uri.encode(endpoint.browseId)
                        val encodedParams = endpoint.params?.takeIf { it.isNotBlank() }?.let { android.net.Uri.encode(it) }
                        val encodedTitle = section.title.takeIf { it.isNotBlank() }?.let { android.net.Uri.encode(it) }

                        val route = buildString {
                            append("browse/")
                            append(encodedBrowseId)
                            val queryParams = mutableListOf<String>()
                            if (encodedParams != null) queryParams.add("params=$encodedParams")
                            if (encodedTitle != null) queryParams.add("title=$encodedTitle")
                            if (queryParams.isNotEmpty()) {
                                append("?")
                                append(queryParams.joinToString("&"))
                            }
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
        onHeaderClick = onMoreClick,
        onMoreClick = onMoreClick,
        modifier = modifier,
    ) {
        LazyRow(
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(snapLayoutInfoProvider),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(
                items = shelfItems.distinctBy { it.id },
                key = { "${section.title}_${it.id}" },
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
                            subtitle = item.artists?.joinToString { it.name } ?: item.year?.toString(),
                            thumbnailUrl = item.thumbnail,
                            typeLabel = "Album",
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
                                subtitle = item.author?.name ?: item.songCountText,
                                thumbnailUrl = item.thumbnail,
                                typeLabel = "Video",
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
                                subtitle = item.author?.name ?: item.songCountText,
                                thumbnailUrl = item.thumbnail,
                                typeLabel = "Playlist",
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
