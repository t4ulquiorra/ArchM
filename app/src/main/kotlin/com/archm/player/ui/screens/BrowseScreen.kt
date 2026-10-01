

package com.archm.player.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.constants.GridItemSize
import com.archm.player.constants.GridItemsSizeKey
import com.archm.player.constants.GridThumbnailHeight
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.ui.component.CombinedIconButton
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.YouTubeGridItem
import com.archm.player.ui.component.rememberBouncyScale
import com.archm.player.ui.component.shimmer.GridItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.ui.utils.backToMain
import com.archm.player.ui.utils.resize
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.viewmodels.BrowseViewModel
import com.archm.player.ui.screens.library.rememberArtworkCardColor
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    browseId: String?,
    initialTitle: String? = null,
    showNavigationIcon: Boolean = true,
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val title by viewModel.title.collectAsState()
    val displayTitle = title?.takeIf { it.isNotBlank() } ?: initialTitle.orEmpty()
    val items by viewModel.items.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()
    val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

    LaunchedEffect(browseId) {
        if (browseId != null && items == null) {
            viewModel.load(browseId, fallbackTitle = initialTitle)
        }
    }

    val density = LocalDensity.current
    val scrollProgress by remember {
        derivedStateOf {
            if (lazyGridState.firstVisibleItemIndex > 0) {
                1f
            } else {
                val offset = lazyGridState.firstVisibleItemScrollOffset.toFloat()
                (offset / with(density) { TitleMorphFadeThresholdDp.toPx() }).coerceIn(0f, 1f)
            }
        }
    }

    Scaffold(
        topBar = {
            if (!showNavigationIcon) {
                MainTopBar(
                    navController = navController,
                    titleAlpha = scrollProgress,
                    titleContent = {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee(),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .appleMusicTopBarGlass(scrollProgress),
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = displayTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .graphicsLayer { alpha = scrollProgress }
                                .basicMarquee(),
                        )
                    },
                    navigationIcon = {
                        if (showNavigationIcon) {
                            Box(Modifier.padding(horizontal = 5.dp)) {
                                CombinedIconButton(
                                    onClick = navController::navigateUp,
                                    onLongClick = navController::backToMain,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_back),
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .appleMusicTopBarGlass(scrollProgress),
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = LocalPlayerAwareWindowInsets.current,
    ) { paddingValues ->
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = paddingValues.calculateTopPadding() + 8.dp,
                bottom = paddingValues.calculateBottomPadding() + 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            // Apple Music Large Header Title
            if (displayTitle.isNotBlank()) {
                item(key = "browse_large_title", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = displayTitle,
                        fontSize = AppleMusicLargeTitleSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                            .padding(top = 4.dp, bottom = 12.dp)
                            .graphicsLayer {
                                alpha = (1f - scrollProgress * 1.5f).coerceIn(0f, 1f)
                            },
                    )
                }
            }
            if (items == null) {
                items(6) {
                    ShimmerHost(
                        modifier = Modifier
                            .padding(4.dp)
                            .animateItem(),
                    ) {
                        GridItemPlaceHolder(fillMaxWidth = true)
                    }
                }
            } else {
                items(
                    items = items.orEmpty().distinctBy { it.id },
                    key = { it.id },
                ) { item ->
                    if (item is ArtistItem) {
                        BrowseArtistItem(
                            item = item,
                            onClick = {
                                navController.navigate("artist/${item.id}")
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    YouTubeArtistMenu(
                                        artist = item,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .animateItem(),
                        )
                    } else {
                        YouTubeGridItem(
                            item = item,
                            isActive = when (item) {
                                is SongItem -> mediaMetadata?.id == item.id
                                is AlbumItem -> mediaMetadata?.album?.id == item.id
                                else -> false
                            },
                            isPlaying = isPlaying,
                            fillMaxWidth = true,
                            coroutineScope = coroutineScope,
                            onClick = {
                                when (item) {
                                    is SongItem -> playerConnection.playQueue(
                                        YouTubeQueue(
                                            item.endpoint ?: WatchEndpoint(videoId = item.id),
                                            item.toMediaMetadata(),
                                        ),
                                    )
                                    is AlbumItem -> {
                                        val releaseType = item.explicitType ?: when {
                                            Regex("""\bEP\b""", RegexOption.IGNORE_CASE).containsMatchIn(item.title) -> "EP"
                                            Regex("""\bSingle\b""", RegexOption.IGNORE_CASE).containsMatchIn(item.title) -> "Single"
                                            else -> null
                                        }
                                        navController.navigate(buildAlbumRoute(item.id, releaseType))
                                    }
                                    is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                                    is ArtistItem -> navController.navigate("artist/${item.id}")
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
                                        is PlaylistItem -> YouTubePlaylistMenu(
                                            playlist = item,
                                            coroutineScope = coroutineScope,
                                            onDismiss = menuState::dismiss,
                                        )
                                        is ArtistItem -> YouTubeArtistMenu(
                                            artist = item,
                                            onDismiss = menuState::dismiss,
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BrowseArtistItem(
    item: ArtistItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardBgColor = rememberArtworkCardColor(
        thumbnailUrl = item.thumbnail,
        fallbackColor = MaterialTheme.colorScheme.surfaceContainerLow,
    )
    val interactionSource = remember { MutableInteractionSource() }
    val scale by rememberBouncyScale(
        interactionSource = interactionSource,
        targetShrinkScale = 0.95f,
        stiffness = Spring.StiffnessMedium,
        dampingRatio = Spring.DampingRatioMediumBouncy,
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(cardBgColor)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), CircleShape)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick,
                )
                .padding(5.dp),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = item.thumbnail?.resize(480, 480),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        }

        Spacer(modifier = Modifier.height(8.dp.coerceAtLeast(0.dp)))

        Text(
            text = item.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .basicMarquee()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        )
    }
}
