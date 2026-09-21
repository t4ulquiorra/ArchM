

package com.archm.player.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.archm.player.LocalDatabase
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.constants.GridItemSize
import com.archm.player.constants.GridItemsSizeKey
import com.archm.player.constants.GridThumbnailHeight
import com.archm.player.playback.queues.LocalAlbumRadio
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.ui.component.AlbumPlayButton
import com.archm.player.ui.component.ChipsRow
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.LongClickIconButton
import com.archm.player.ui.component.formatReleaseSubtitle
import com.archm.player.ui.component.rememberBouncyScale
import com.archm.player.ui.component.shimmer.GridItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.utils.backToMain
import com.archm.player.ui.utils.resize
import com.archm.player.utils.joinByBullet
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.utils.reportException
import com.archm.player.ui.screens.library.rememberArtworkCardColor
import com.archm.player.viewmodels.AccountContentType
import com.archm.player.viewmodels.AccountViewModel
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.WatchEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current

    val coroutineScope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()

    val playlists by viewModel.playlists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val selectedContentType by viewModel.selectedContentType.collectAsState()
    val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

    Box(modifier = Modifier.fillMaxSize()) {
        val insetsPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()

        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = (insetsPadding.calculateTopPadding() + 8.dp).coerceAtLeast(0.dp),
                bottom = 120.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                ChipsRow(
                    chips = listOf(
                        AccountContentType.PLAYLISTS to stringResource(R.string.filter_playlists),
                        AccountContentType.ALBUMS to stringResource(R.string.filter_albums),
                        AccountContentType.ARTISTS to stringResource(R.string.filter_artists),
                    ),
                    currentValue = selectedContentType,
                    onValueUpdate = { viewModel.setSelectedContentType(it) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            when (selectedContentType) {
                AccountContentType.PLAYLISTS -> {
                    items(
                        items = playlists.orEmpty().distinctBy { it.id },
                        key = { it.id },
                    ) { item ->
                        AccountPlaylistItem(
                            item = item,
                            onClick = {
                                navController.navigate("online_playlist/${item.id}")
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    YouTubePlaylistMenu(
                                        playlist = item,
                                        coroutineScope = coroutineScope,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .animateItem(),
                        )
                    }

                    if (playlists == null) {
                        items(8) {
                            ShimmerHost(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .animateItem(),
                            ) {
                                GridItemPlaceHolder(fillMaxWidth = true)
                            }
                        }
                    }
                }

                AccountContentType.ALBUMS -> {
                    items(
                        items = albums.orEmpty().distinctBy { it.id },
                        key = { it.id },
                    ) { item ->
                        AccountAlbumItem(
                            item = item,
                            onClick = {
                                val releaseType = item.explicitType ?: when {
                                    Regex("""\bEP\b""", RegexOption.IGNORE_CASE).containsMatchIn(item.title) -> "EP"
                                    Regex("""\bSingle\b""", RegexOption.IGNORE_CASE).containsMatchIn(item.title) -> "Single"
                                    else -> null
                                }
                                navController.navigate(buildAlbumRoute(item.id, releaseType))
                            },
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
                                coroutineScope.launch(Dispatchers.IO) {
                                    var albumWithSongs = database.albumWithSongs(item.id).first()
                                    if (albumWithSongs?.songs.isNullOrEmpty()) {
                                        YouTube.album(item.id).onSuccess { albumPage ->
                                            database.transaction { insert(albumPage) }
                                            albumWithSongs = database.albumWithSongs(item.id).first()
                                        }.onFailure { reportException(it) }
                                    }
                                    albumWithSongs?.let {
                                        withContext(Dispatchers.Main) {
                                            playerConnection?.playQueue(LocalAlbumRadio(it))
                                        }
                                    } ?: run {
                                        withContext(Dispatchers.Main) {
                                            playerConnection?.playQueue(
                                                YouTubeQueue(
                                                    WatchEndpoint(playlistId = item.playlistId),
                                                ),
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .padding(4.dp)
                                .animateItem(),
                        )
                    }

                    if (albums == null) {
                        items(8) {
                            ShimmerHost(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .animateItem(),
                            ) {
                                GridItemPlaceHolder(fillMaxWidth = true)
                            }
                        }
                    }
                }

                AccountContentType.ARTISTS -> {
                    items(
                        items = artists.orEmpty().distinctBy { it.id },
                        key = { it.id },
                    ) { item ->
                        AccountArtistItem(
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
                    }

                    if (artists == null) {
                        items(8) {
                            ShimmerHost(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .animateItem(),
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth(0.7f)
                                            .height(14.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        TopAppBar(
            scrollBehavior = scrollBehavior,
            title = {
                Text(
                    text = stringResource(R.string.account),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                )
            },
            navigationIcon = {
                Box(Modifier.padding(horizontal = 5.dp)) {
                    LongClickIconButton(
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
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                navigationIconContentColor = Color.White,
                titleContentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountPlaylistItem(
    item: PlaylistItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(cardShape)
            .background(Color(0xFF141414))
            .border(1.dp, Color.White.copy(alpha = 0.12f), cardShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(start = innerPadding.coerceAtLeast(0.dp), top = innerPadding.coerceAtLeast(0.dp), end = innerPadding.coerceAtLeast(0.dp), bottom = 12.dp.coerceAtLeast(0.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = item.thumbnail?.resize(540, 540),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(),
            )
            val subtitle = joinByBullet(item.author?.name, item.songCountText) ?: stringResource(R.string.playlist)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountAlbumItem(
    item: AlbumItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(cardShape)
            .background(Color(0xFF141414))
            .border(1.dp, Color.White.copy(alpha = 0.12f), cardShape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(start = innerPadding.coerceAtLeast(0.dp), top = innerPadding.coerceAtLeast(0.dp), end = innerPadding.coerceAtLeast(0.dp), bottom = 12.dp.coerceAtLeast(0.dp)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = item.thumbnail?.resize(540, 540),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            AlbumPlayButton(
                visible = true,
                onClick = onPlayClick,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 3.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .basicMarquee(),
            )
            val releaseSubtitle = formatReleaseSubtitle(item)
            val subtitle = joinByBullet(item.artists?.joinToString { it.name }, releaseSubtitle ?: item.year?.toString())
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountArtistItem(
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
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
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
            color = Color.White,
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

