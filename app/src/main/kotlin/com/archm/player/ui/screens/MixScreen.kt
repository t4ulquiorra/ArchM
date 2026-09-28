package com.archm.player.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.constants.GridItemSize
import com.archm.player.constants.GridItemsSizeKey
import com.archm.player.constants.GridThumbnailHeight
import com.archm.player.ui.component.ExpressivePullToRefreshBox
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.YouTubeGridItem
import com.archm.player.ui.component.shimmer.GridItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.menu.YouTubePlaylistMenu
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.viewmodels.MixViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MixScreen(
    navController: NavController,
    viewModel: MixViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val mixPlaylists by viewModel.mixPlaylists.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val gridItemSize by rememberEnumPreference(GridItemsSizeKey, GridItemSize.BIG)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.mix_for_you)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        contentWindowInsets = LocalPlayerAwareWindowInsets.current,
    ) { innerPadding ->
        ExpressivePullToRefreshBox(
            isRefreshing = isLoading && mixPlaylists.isNotEmpty(),
            onRefresh = viewModel::load,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = GridThumbnailHeight + if (gridItemSize == GridItemSize.BIG) 24.dp else (-24).dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = mixPlaylists.distinctBy { it.id },
                    key = { it.id },
                ) { playlist ->
                    YouTubeGridItem(
                        item = playlist,
                        isActive = mediaMetadata?.playlist?.id == playlist.id,
                        isPlaying = isPlaying,
                        fillMaxWidth = true,
                        coroutineScope = coroutineScope,
                        onClick = {
                            navController.navigate("online_playlist/${playlist.id}")
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                YouTubePlaylistMenu(
                                    playlist = playlist,
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                    )
                }

                if (mixPlaylists.isEmpty() && isLoading) {
                    items(8) {
                        ShimmerHost {
                            GridItemPlaceHolder(fillMaxWidth = true)
                        }
                    }
                }
            }

            if (mixPlaylists.isEmpty() && !isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.no_mixes_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
