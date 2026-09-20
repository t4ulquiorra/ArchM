/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.palette.graphics.Palette
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.toBitmap
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.constants.QuickPicks
import com.archm.player.db.entities.Album
import com.archm.player.db.entities.Artist
import com.archm.player.db.entities.Playlist
import com.archm.player.db.entities.Song
import com.archm.player.home.HomeAction
import com.archm.player.home.HomeScreenState
import com.archm.player.home.HomeUiState
import com.archm.player.models.MediaMetadata
import com.archm.player.playback.PlayerConnection
import com.archm.player.ui.component.ExpressivePullToRefreshBox
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.MenuState
import com.archm.player.viewmodels.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    headerScrollConnection: NestedScrollConnection? = null,
    snackbarHostState: androidx.compose.material3.SnackbarHostState? = null,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()

    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop =
        backStackEntry
            ?.savedStateHandle
            ?.getStateFlow("scrollToTop", false)
            ?.collectAsStateWithLifecycle()

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazyListState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    val successState = screenState as? HomeScreenState.Success
    val uiState = successState?.uiState
    val selectedChip = uiState?.selectedChip

    LaunchedEffect(uiState?.homePage?.continuation) {
        val continuation = uiState?.homePage?.continuation ?: return@LaunchedEffect
        snapshotFlow {
            val layoutInfo = lazyListState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisibleIndex != null && lastVisibleIndex >= layoutInfo.totalItemsCount - 3
        }.collect { shouldLoadMore ->
            if (shouldLoadMore) {
                viewModel.onAction(HomeAction.LoadMore(continuation))
            }
        }
    }

    if (selectedChip != null) {
        BackHandler {
            viewModel.onAction(HomeAction.SelectChip(selectedChip))
        }
    }

    LaunchedEffect(uiState?.showCategoryChips, selectedChip) {
        if (uiState?.showCategoryChips == false && selectedChip != null) {
            viewModel.onAction(HomeAction.SelectChip(selectedChip))
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .then(
                    if (headerScrollConnection != null) {
                        Modifier.nestedScroll(headerScrollConnection)
                    } else {
                        Modifier
                    },
                ),
    ) {
        when (val state = screenState) {
            HomeScreenState.Loading -> {
                HomeStatePane(
                    iconResId = null,
                    messageResId = null,
                    showLoadingIndicator = true,
                )
            }

            HomeScreenState.Empty -> {
                HomeStatePane(
                    iconResId = R.drawable.music_note,
                    messageResId = R.string.no_results_found,
                    actionResId = R.string.retry,
                    onAction = { viewModel.onAction(HomeAction.Refresh) },
                )
            }

            is HomeScreenState.Error -> {
                HomeStatePane(
                    iconResId = R.drawable.info,
                    messageResId = state.messageResId,
                    actionResId = R.string.retry,
                    onAction = { viewModel.onAction(HomeAction.Refresh) },
                )
            }

            is HomeScreenState.Success -> {
                HomeContent(
                    uiState = state.uiState,
                    mediaMetadata = mediaMetadata,
                    isPlaying = isPlaying,
                    navController = navController,
                    playerConnection = playerConnection,
                    menuState = menuState,
                    haptic = haptic,
                    scope = scope,
                    lazyListState = lazyListState,
                    onAction = viewModel::onAction,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeStatePane(
    @DrawableRes iconResId: Int?,
    @StringRes messageResId: Int?,
    modifier: Modifier = Modifier,
    @StringRes actionResId: Int? = null,
    showLoadingIndicator: Boolean = false,
    onAction: (() -> Unit)? = null,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .fillMaxSize()
                .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            if (showLoadingIndicator) {
                LoadingIndicator()
            } else {
                iconResId?.let {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp),
                    )
                }
                messageResId?.let {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(it),
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (actionResId != null && onAction != null) {
                    Spacer(Modifier.height(20.dp))
                    FilledTonalButton(onClick = onAction) {
                        Text(stringResource(actionResId))
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    mediaMetadata: MediaMetadata?,
    isPlaying: Boolean,
    navController: NavController,
    playerConnection: PlayerConnection,
    menuState: MenuState,
    haptic: HapticFeedback,
    scope: CoroutineScope,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    onAction: (HomeAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val remoteQuickPicks =
        uiState
            .takeIf { it.quickPicksMode == QuickPicks.QUICK_PICKS }
            ?.remoteQuickPicks

    val isScrollingUp by lazyListState.isScrollingUp()
    var topAppBarHeightPx by rememberSaveable { mutableIntStateOf(0) }

    val firstThumbnailUrl =
        remember(uiState) {
            remoteQuickPicks?.items?.firstOrNull()?.thumbnail
                ?: uiState.quickPicks.firstOrNull()?.song?.thumbnailUrl
                ?: uiState.keepListening.firstOrNull()?.let {
                    when (it) {
                        is Song -> it.song.thumbnailUrl
                        is Album -> it.album.thumbnailUrl
                        is Artist -> it.artist.thumbnailUrl
                        is Playlist -> it.thumbnails.firstOrNull()
                    }
                }
                ?: uiState.homePage?.sections?.firstOrNull()?.items?.firstOrNull()?.thumbnail
        }

    val context = LocalContext.current
    val defaultBg = MaterialTheme.colorScheme.background
    var topHeaderColor by remember { mutableStateOf(defaultBg) }
    val animatedColor by animateColorAsState(topHeaderColor, tween(500), label = "topHeaderColor")

    LaunchedEffect(firstThumbnailUrl) {
        if (firstThumbnailUrl != null) {
            withContext(Dispatchers.IO) {
                try {
                    val request =
                        ImageRequest.Builder(context)
                            .data(firstThumbnailUrl)
                            .size(100, 100)
                            .allowHardware(false)
                            .build()
                    val result = context.imageLoader.execute(request)
                    val bitmap = result.image?.toBitmap()
                    if (bitmap != null) {
                        val palette = Palette.from(bitmap).generate()
                        val dom = palette.getDominantColor(android.graphics.Color.TRANSPARENT)
                        if (dom != android.graphics.Color.TRANSPARENT) {
                            val c = Color(dom)
                            topHeaderColor =
                                Color(
                                    red = (c.red * 0.35f).coerceIn(0f, 1f),
                                    green = (c.green * 0.35f).coerceIn(0f, 1f),
                                    blue = (c.blue * 0.35f).coerceIn(0f, 1f),
                                    alpha = 1f,
                                )
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        ExpressivePullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onAction(HomeAction.Refresh) },
            indicatorOffset = with(LocalDensity.current) { topAppBarHeightPx.toDp() },
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                state = lazyListState,
                contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues(),
                modifier = Modifier.fillMaxSize(),
            ) {
                // Item 0: Ambient Hero Backdrop + Account Layout + Quick Picks
                item(key = "home_hero_backdrop") {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier =
                                Modifier
                                    .matchParentSize()
                                    .angledGradientBackground(listOf(animatedColor, defaultBg), 25f),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .align(Alignment.BottomCenter)
                                        .background(artworkScrimBrush(defaultBg)),
                            )
                        }
                        Column(modifier = Modifier.padding(horizontal = 15.dp)) {
                            Spacer(Modifier.height(with(LocalDensity.current) { topAppBarHeightPx.toDp() }))
                            Spacer(Modifier.height(8.dp))
                            if (uiState.accountName.isNotBlank()) {
                                AccountLayout(
                                    accountName = uiState.accountName,
                                    url = uiState.accountImageUrl,
                                    onClick = { navController.navigate("account") },
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                            if (remoteQuickPicks?.items?.isNotEmpty() == true || uiState.quickPicks.isNotEmpty()) {
                                SimpQuickPicks(
                                    quickPicks = uiState.quickPicks,
                                    remoteQuickPicks = remoteQuickPicks,
                                    mediaMetadata = mediaMetadata,
                                    isPlaying = isPlaying,
                                    navController = navController,
                                    playerConnection = playerConnection,
                                    menuState = menuState,
                                    haptic = haptic,
                                )
                            }
                        }
                    }
                }

                // Speed Dial Section
                if (uiState.speedDialItems.isNotEmpty()) {
                    item(key = "home_speed_dial") {
                        Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 6.dp)) {
                            Text(
                                text = stringResource(R.string.speed_dial),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 4.dp),
                            )
                            SpeedDialSection(
                                speedDialItems = uiState.speedDialItems,
                                mediaMetadata = mediaMetadata,
                                isPlaying = isPlaying,
                                navController = navController,
                                playerConnection = playerConnection,
                                menuState = menuState,
                                haptic = haptic,
                                scope = scope,
                            )
                        }
                    }
                }

                // Keep Listening Section
                if (uiState.keepListening.isNotEmpty()) {
                    item(key = "home_keep_listening") {
                        Box(modifier = Modifier.padding(horizontal = 15.dp)) {
                            KeepListeningShelf(
                                keepListening = uiState.keepListening,
                                mediaMetadata = mediaMetadata,
                                isPlaying = isPlaying,
                                navController = navController,
                                playerConnection = playerConnection,
                                menuState = menuState,
                                haptic = haptic,
                                scope = scope,
                            )
                        }
                    }
                }

                // Your YouTube Playlists Section
                if (uiState.accountPlaylists.isNotEmpty()) {
                    item(key = "home_account_playlists") {
                        Box(modifier = Modifier.padding(horizontal = 15.dp)) {
                            AccountPlaylistsShelf(
                                accountPlaylists = uiState.accountPlaylists,
                                accountName = uiState.accountName,
                                accountImageUrl = uiState.accountImageUrl,
                                navController = navController,
                            )
                        }
                    }
                }

                // Forgotten Favorites Section
                if (uiState.forgottenFavorites.isNotEmpty()) {
                    item(key = "home_forgotten_favorites") {
                        Box(modifier = Modifier.padding(horizontal = 15.dp)) {
                            ForgottenFavoritesShelf(
                                forgottenFavorites = uiState.forgottenFavorites,
                                mediaMetadata = mediaMetadata,
                                isPlaying = isPlaying,
                                navController = navController,
                                playerConnection = playerConnection,
                                menuState = menuState,
                                haptic = haptic,
                            )
                        }
                    }
                }

                // Similar Recommendations
                items(
                    items = uiState.similarRecommendations,
                    key = { "similar_${it.title.id}" },
                ) { recommendation ->
                    Box(modifier = Modifier.padding(horizontal = 15.dp)) {
                        SimilarRecommendationsShelf(
                            recommendation = recommendation,
                            mediaMetadata = mediaMetadata,
                            isPlaying = isPlaying,
                            navController = navController,
                            playerConnection = playerConnection,
                            menuState = menuState,
                            haptic = haptic,
                            scope = scope,
                        )
                    }
                }

                // Remote YouTube Music Sections
                itemsIndexed(
                    items = uiState.homePage?.sections.orEmpty(),
                    key = { index, section -> "remote_${section.endpoint?.browseId ?: section.title}_$index" },
                ) { _, section ->
                    Box(modifier = Modifier.padding(horizontal = 15.dp)) {
                        HomePageSectionShelf(
                            section = section,
                            mediaMetadata = mediaMetadata,
                            isPlaying = isPlaying,
                            navController = navController,
                            playerConnection = playerConnection,
                            menuState = menuState,
                            haptic = haptic,
                            scope = scope,
                        )
                    }
                }

                // Loading More
                if (uiState.isLoadingMore) {
                    item(key = "home_loading_more") {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                        ) {
                            LoadingIndicator()
                        }
                    }
                }

                // Bottom spacer for comfortable end-of-list spacing
                item(key = "home_bottom_spacer") {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

        // Sticky Top App Bar & Category Chips Overlay
        AnimatedContent(
            targetState = lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0,
            transitionSpec = {
                fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
            },
            label = "HomeTopBarOverlay",
        ) { isAtTop ->
            Column(
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .then(
                            if (isAtTop) {
                                Modifier.background(Color.Transparent)
                            } else {
                                Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f))
                            },
                        ).onGloballyPositioned { coordinates ->
                            topAppBarHeightPx = coordinates.size.height
                        },
            ) {
                AnimatedVisibility(
                    visible = isScrollingUp,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    HomeTopAppBar(navController = navController)
                }
                AnimatedVisibility(
                    visible = !isScrollingUp,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Spacer(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.statusBars),
                    )
                }
                if (uiState.showCategoryChips && uiState.homePage?.chips?.isNotEmpty() == true) {
                    HomeCategoryChips(
                        chips = uiState.homePage.chips,
                        selectedChip = uiState.selectedChip,
                        onChipSelected = { onAction(HomeAction.SelectChip(it)) },
                    )
                }
            }
        }
    }
}
