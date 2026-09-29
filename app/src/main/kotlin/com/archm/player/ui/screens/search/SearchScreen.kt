/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.music.innertube.models.BrowseEndpoint
import com.archm.player.ui.screens.rememberMoodAndGenresArtworkModel
import com.archm.player.ui.screens.rememberMoodAndGenresArtworkUrl
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.archm.player.ui.component.CyclingSearchPlaceholder
import com.archm.player.ui.component.InputFieldHeight
import com.archm.player.viewmodels.HomeViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.extensions.togglePlayPause
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.search.SearchDiscoveryUiModel
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.NavigationTitle
import com.archm.player.ui.component.YouTubeGridItem
import com.archm.player.ui.component.YouTubeListItem
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.component.shimmer.TextPlaceholder
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.menu.YouTubeArtistMenu
import com.archm.player.ui.menu.YouTubeSongMenu
import com.archm.player.ui.screens.MoodAndGenresButton
import com.archm.player.ui.screens.MoodAndGenresButtonHeight
import androidx.compose.foundation.isSystemInDarkTheme
import com.archm.player.constants.DarkMode
import com.archm.player.constants.DarkModeKey
import com.archm.player.constants.PureBlackKey
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.utils.rememberPreference
import com.archm.player.viewmodels.SearchDiscoveryScreenState
import com.archm.player.viewmodels.SearchDiscoveryTab
import com.archm.player.viewmodels.SearchDiscoveryViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    onSearchClick: () -> Unit = {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.set("openSearch", true)
    },
    headerScrollConnection: NestedScrollConnection? = null,
    pureBlack: Boolean = false,
    viewModel: SearchDiscoveryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val pureBlackPref by rememberPreference(PureBlackKey, defaultValue = false)
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }
    val effectivePureBlack = pureBlack || (pureBlackPref && useDarkTheme)
    val backgroundColor = if (effectivePureBlack) Color.Black else MaterialTheme.colorScheme.background
    val lazyListState = rememberLazyListState()
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

    val homeViewModel: HomeViewModel = hiltViewModel()
    val vmAccountImageUrl by homeViewModel.accountImageUrl.collectAsStateWithLifecycle()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .then(
                    // Step 2b: attach the shell's floating-header connection here so Search's
                    // scroll/fling writes Search's own header state and can't leak elsewhere.
                    if (headerScrollConnection != null) {
                        Modifier.nestedScroll(headerScrollConnection)
                    } else {
                        Modifier
                    },
                ),
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding =
                LocalPlayerAwareWindowInsets.current
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                    .asPaddingValues(),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(
                key = "landing_header",
                contentType = "landing_header",
            ) {
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
                            Box(
                                modifier =
                                    Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(30, 30, 30))
                                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.person),
                                    contentDescription = stringResource(R.string.account),
                                    tint = Color.White.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp),
                                )
                                if (!vmAccountImageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model =
                                            ImageRequest.Builder(LocalContext.current)
                                                .data(vmAccountImageUrl)
                                                .diskCachePolicy(CachePolicy.ENABLED)
                                                .diskCacheKey(vmAccountImageUrl)
                                                .crossfade(true)
                                                .build(),
                                        contentDescription = stringResource(R.string.account),
                                        contentScale = ContentScale.Crop,
                                        modifier =
                                            Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                    )
                                }
                            }

                            Text(
                                text = stringResource(R.string.search),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (effectivePureBlack) Color.White else MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                )
            }

            stickyHeader(
                key = "landing_sticky_bar",
                contentType = "landing_sticky_bar",
            ) {
                Surface(
                    color = backgroundColor,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(35, 35, 38),
                        contentColor = Color.White,
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
                        modifier =
                            Modifier
                                .windowInsetsPadding(WindowInsets.statusBars)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxWidth()
                                .height(InputFieldHeight)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onSearchClick()
                                },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.search),
                                contentDescription = stringResource(R.string.search),
                                tint = Color.White.copy(alpha = 0.7f),
                            )
                            CyclingSearchPlaceholder(
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            when (val currentState = state) {
                SearchDiscoveryScreenState.Loading -> {
                    item(
                        key = "search_loading",
                        contentType = "search_loading",
                    ) {
                        SearchDiscoveryLoading(modifier = Modifier.animateItem())
                    }
                }

                SearchDiscoveryScreenState.Empty -> {
                    item(
                        key = "search_empty",
                        contentType = "search_empty",
                    ) {
                        SearchStateMessage(
                            message = stringResource(R.string.no_results_found),
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                is SearchDiscoveryScreenState.Error -> {
                    item(
                        key = "search_error",
                        contentType = "search_error",
                    ) {
                        SearchStateMessage(
                            message = stringResource(currentState.messageResId),
                            action = {
                                Button(onClick = viewModel::retry) {
                                    Text(stringResource(R.string.retry_button))
                                }
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }

                is SearchDiscoveryScreenState.Success -> {
                    val moodAndMomentsItems = if (currentState.data.moodAndMoments.isNotEmpty()) {
                        currentState.data.moodAndMoments
                    } else {
                        currentState.data.moodAndGenres
                    }
                    val genresItems = currentState.data.genres

                    if (moodAndMomentsItems.isNotEmpty()) {
                        item(
                            key = "search_moods_title",
                            contentType = "section_title",
                        ) {
                            NavigationTitle(
                                title = "Moods & moments",
                                modifier = Modifier.animateItem(),
                            )
                        }
                        items(
                            items = moodAndMomentsItems.chunked(2),
                            key = { chunk -> "mood_chunk_" + chunk.joinToString { it.title } },
                            contentType = { "mood_genre_row" },
                        ) { rowItems ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 5.dp)
                                        .animateItem(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                for (item in rowItems) {
                                    MoodCategoryCard(
                                        title = item.title,
                                        stripeColor = item.stripeColor,
                                        endpoint = item.endpoint,
                                        onClick = {
                                            navController.navigate("youtube_browse/${item.endpoint.browseId}?params=${item.endpoint.params}")
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    if (genresItems.isNotEmpty()) {
                        item(
                            key = "search_genres_title",
                            contentType = "section_title",
                        ) {
                            NavigationTitle(
                                title = "Genres",
                                modifier =
                                    Modifier
                                        .padding(top = 12.dp)
                                        .animateItem(),
                            )
                        }
                        items(
                            items = genresItems.chunked(2),
                            key = { chunk -> "genre_chunk_" + chunk.joinToString { it.title } },
                            contentType = { "mood_genre_row" },
                        ) { rowItems ->
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 5.dp)
                                        .animateItem(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                for (item in rowItems) {
                                    MoodCategoryCard(
                                        title = item.title,
                                        stripeColor = item.stripeColor,
                                        endpoint = item.endpoint,
                                        onClick = {
                                            navController.navigate("youtube_browse/${item.endpoint.browseId}?params=${item.endpoint.params}")
                                        },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getMoodCategoryColor(title: String, stripeColor: Long): Color {
    if (stripeColor != 0L) {
        val color = Color(stripeColor)
        if (color != Color.Transparent && color != Color.Black && color != Color(30, 30, 30)) {
            return color
        }
    }
    val cleanTitle = title.trim().lowercase()
    return when {
        cleanTitle.contains("chill") -> Color(0xFF287884)
        cleanTitle.contains("commute") -> Color(0xFF4C5D73)
        cleanTitle.contains("energize") -> Color(0xFFBA5D00)
        cleanTitle.contains("sad") || cleanTitle.contains("heartbreak") -> Color(0xFF4A5568)
        cleanTitle.contains("feel good") -> Color(0xFF9E6500)
        cleanTitle.contains("romance") || cleanTitle.contains("love") -> Color(0xFF8F2D56)
        cleanTitle.contains("focus") -> Color(0xFF2E4057)
        cleanTitle.contains("workout") -> Color(0xFFB33E2B)
        cleanTitle.contains("party") -> Color(0xFF7A2062)
        cleanTitle.contains("sleep") -> Color(0xFF1E3A5F)
        cleanTitle.contains("rock") -> Color(0xFF7F2626)
        cleanTitle.contains("pop") -> Color(0xFF8C3060)
        cleanTitle.contains("hip-hop") || cleanTitle.contains("rap") -> Color(0xFF6A3D18)
        cleanTitle.contains("indie") || cleanTitle.contains("alternative") -> Color(0xFF2D5A47)
        cleanTitle.contains("dance") || cleanTitle.contains("electronic") -> Color(0xFF1D5A6E)
        cleanTitle.contains("r&b") || cleanTitle.contains("soul") -> Color(0xFF5C2B4E)
        cleanTitle.contains("country") -> Color(0xFF7D4E2D)
        cleanTitle.contains("jazz") -> Color(0xFF4A3E56)
        cleanTitle.contains("classical") -> Color(0xFF3D4E5B)
        cleanTitle.contains("metal") -> Color(0xFF36393F)
        cleanTitle.contains("k-pop") -> Color(0xFF852D68)
        cleanTitle.contains("latin") -> Color(0xFF8A4018)
        cleanTitle.contains("bollywood") || cleanTitle.contains("hindi") -> Color(0xFF8A3040)
        cleanTitle.contains("punjabi") -> Color(0xFF854B10)
        cleanTitle.contains("90s") -> Color(0xFF3D5A80)
        cleanTitle.contains("80s") -> Color(0xFF5C4D7D)
        cleanTitle.contains("70s") -> Color(0xFF6E4D2B)
        else -> {
            val palette = listOf(
                Color(0xFF287884), Color(0xFF4C5D73), Color(0xFFBA5D00),
                Color(0xFF4A5568), Color(0xFF9E6500), Color(0xFF8F2D56),
                Color(0xFF2E4057), Color(0xFFB33E2B), Color(0xFF7A2062),
                Color(0xFF1E3A5F), Color(0xFF2D5A47), Color(0xFF5C2B4E),
            )
            palette[kotlin.math.abs(title.hashCode()) % palette.size]
        }
    }
}

@Composable
fun MoodCategoryCard(
    title: String,
    stripeColor: Long,
    endpoint: BrowseEndpoint,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val artworkUrl = rememberMoodAndGenresArtworkUrl(endpoint)
    val artworkModel = rememberMoodAndGenresArtworkModel(endpoint, artworkUrl)
    val cardBg = remember(title, stripeColor) { getMoodCategoryColor(title, stripeColor) }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(84.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(cardBg)
                .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                .clickable(onClick = onClick),
    ) {
        if (artworkModel != null) {
            AsyncImage(
                model = artworkModel,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 10.dp, y = 14.dp)
                        .size(64.dp)
                        .rotate(25f)
                        .clip(RoundedCornerShape(6.dp)),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp, end = 68.dp, bottom = 12.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchDiscoveryTabs(
    selectedTab: SearchDiscoveryTab,
    onTabSelected: (SearchDiscoveryTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember { SearchDiscoveryTab.entries }
    PrimaryTabRow(
        selectedTabIndex = tabs.indexOf(selectedTab),
        modifier = modifier,
        containerColor = Color.Transparent,
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        painter =
                            painterResource(
                                when (tab) {
                                    SearchDiscoveryTab.EXPLORE -> R.drawable.explore_outlined
                                    SearchDiscoveryTab.SUGGESTIONS -> R.drawable.auto_awesome
                                },
                            ),
                        contentDescription = null,
                    )
                },
                text = {
                    Text(
                        text =
                            stringResource(
                                when (tab) {
                                    SearchDiscoveryTab.EXPLORE -> R.string.explore
                                    SearchDiscoveryTab.SUGGESTIONS -> R.string.suggestions
                                },
                            ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Composable
private fun SearchMoodAndGenresGrid(
    data: SearchDiscoveryUiModel,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        val columnCount = (maxWidth.value / MoodAndGenresMinCellWidth.value).toInt().coerceAtLeast(1)
        val rowCount = ((data.moodAndGenres.size + columnCount - 1) / columnCount).coerceAtLeast(1)

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = MoodAndGenresMinCellWidth),
            contentPadding = PaddingValues(6.dp),
            userScrollEnabled = false,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height((MoodAndGenresButtonHeight + 12.dp) * rowCount + 12.dp),
        ) {
            items(
                items = data.moodAndGenres,
                key = { item -> "${item.title}:${item.endpoint.browseId}:${item.endpoint.params}" },
                contentType = { "mood_genres_item" },
            ) { item ->
                MoodAndGenresButton(
                    title = item.title,
                    stripeColor = item.stripeColor,
                    endpoint = item.endpoint,
                    onClick = {
                        navController.navigate("youtube_browse/${item.endpoint.browseId}?params=${item.endpoint.params}")
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                )
            }
        }
    }
}

private val MoodAndGenresMinCellWidth = 180.dp

private val SuggestedSongGroupHorizontalPadding = 12.dp
private val SuggestedSongGroupVerticalPadding = 2.dp
private val SuggestedSongGroupItemSpacing = 2.dp
private val SuggestedSongGroupLargeCorner = 28.dp
private val SuggestedSongGroupSmallCorner = 6.dp

private fun segmentedSuggestedSongShape(
    index: Int,
    count: Int,
): Shape {
    val large = SuggestedSongGroupLargeCorner
    val small = SuggestedSongGroupSmallCorner
    return when {
        count <= 1 -> {
            RoundedCornerShape(large)
        }

        index == 0 -> {
            RoundedCornerShape(
                topStart = large,
                topEnd = large,
                bottomEnd = small,
                bottomStart = small,
            )
        }

        index == count - 1 -> {
            RoundedCornerShape(
                topStart = small,
                topEnd = small,
                bottomEnd = large,
                bottomStart = large,
            )
        }

        else -> {
            RoundedCornerShape(small)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuggestedSongsSection(
    songs: List<SongItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    if (songs.isEmpty()) return

    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()

    SectionContainer(
        title = stringResource(R.string.stats_unique_songs),
        modifier = modifier,
    ) {
        val visibleSongs = remember(songs) { songs.take(6) }

        Column(
            verticalArrangement = Arrangement.spacedBy(SuggestedSongGroupItemSpacing),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = SuggestedSongGroupHorizontalPadding,
                        vertical = SuggestedSongGroupVerticalPadding,
                    ),
        ) {
            visibleSongs.forEachIndexed { index, song ->
                val isActive = song.id == mediaMetadata?.id
                Card(
                    shape = segmentedSuggestedSongShape(index = index, count = visibleSongs.size),
                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                if (isActive) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (isActive) {
                                        playerConnection.player.togglePlayPause()
                                    } else {
                                        playerConnection.playQueue(
                                            YouTubeQueue(
                                                endpoint = song.endpoint ?: WatchEndpoint(videoId = song.id),
                                                preloadItem = song.toMediaMetadata(),
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
                            ),
                ) {
                    YouTubeListItem(
                        item = song,
                        albumIndex = index + 1,
                        viewCountText = song.viewCountText,
                        containerColor = Color.Transparent,
                        color = Color.Transparent,
                        isActive = isActive,
                        isPlaying = isPlaying,
                        isSwipeable = false,
                        showActiveContainer = false,
                        trailingContent = {
                            YouTubeSongMenuButton(song = song, navController = navController)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TrendingAlbumsSection(
    albums: List<AlbumItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    if (albums.isEmpty()) return

    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    NavigationTitle(
        title = stringResource(R.string.top_albums),
        modifier = modifier,
    )
    val distinctAlbums = remember(albums) { albums.distinctBy { it.id } }
    LazyRow(
        contentPadding = LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal).asPaddingValues(),
    ) {
        items(
            items = distinctAlbums,
            key = { album -> "trending_album_${album.id}" },
            contentType = { "trending_album" },
        ) { album ->
            YouTubeGridItem(
                item = album,
                isActive = mediaMetadata?.album?.id == album.id,
                isPlaying = isPlaying,
                coroutineScope = coroutineScope,
                onClick = {
                    navController.navigate("album/${album.id}")
                },
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
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SuggestedArtistsSection(
    artists: List<ArtistItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    if (artists.isEmpty()) return

    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current

    NavigationTitle(
        title = stringResource(R.string.stats_unique_artists),
        modifier = modifier,
    )
    val distinctArtists = remember(artists) { artists.distinctBy { it.id } }
    LazyRow(
        contentPadding = LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal).asPaddingValues(),
    ) {
        items(
            items = distinctArtists,
            key = { artist -> "trending_artist_${artist.id}" },
            contentType = { "trending_artist" },
        ) { artist ->
            YouTubeGridItem(
                item = artist,
                modifier =
                    Modifier
                        .combinedClickable(
                            onClick = {
                                navController.navigate("artist/${artist.id}")
                            },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                menuState.show {
                                    YouTubeArtistMenu(
                                        artist = artist,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                        ).animateItem(),
            )
        }
    }
}

@Composable
private fun SectionContainer(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    NavigationTitle(
        title = title,
        modifier = modifier,
    )
    content()
}

@Composable
private fun YouTubeSongMenuButton(
    song: SongItem,
    navController: NavController,
) {
    val menuState = LocalMenuState.current
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
}

@Composable
private fun SearchDiscoveryLoading(modifier: Modifier = Modifier) {
    ShimmerHost(
        modifier = modifier.fillMaxWidth(),
    ) {
        TextPlaceholder(
            height = 56.dp,
            modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
        )
        TextPlaceholder(
            height = 28.dp,
            modifier =
                Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .width(180.dp),
        )
        repeat(6) {
            TextPlaceholder(
                height = 84.dp,
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SearchStateMessage(
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.search_off),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            androidx.compose.foundation.layout
                .Row(content = action)
        }
    }
}
