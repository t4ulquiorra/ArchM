

package com.archm.player.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.MarqueeAnimationMode
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
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
import com.archm.player.ui.component.shimmer.GridItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.ui.utils.backToMain
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.viewmodels.BrowseViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    browseId: String?,
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val title by viewModel.title.collectAsState()
    val items by viewModel.items.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val lazyGridState = rememberLazyGridState()
    val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

    Box(Modifier.fillMaxSize()) {
        val insetsPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
        LazyVerticalGrid(
            state = lazyGridState,
            columns = GridCells.Adaptive(minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = insetsPadding.calculateTopPadding() + 8.dp,
                bottom = 120.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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
                                is AlbumItem -> navController.navigate("album/${item.id}")
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

        TopAppBar(
            title = {
                Text(
                    text = title.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
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
                    CombinedIconButton(
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
