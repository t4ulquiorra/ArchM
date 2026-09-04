/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.ui.screens.library

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.archm.player.R
import com.archm.player.constants.AppBarHeight
import com.archm.player.constants.ChipSortTypeKey
import com.archm.player.constants.DisableBlurKey
import com.archm.player.constants.LibraryFilter
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.utils.rememberPreference

internal val LibraryHeaderContentPadding = 64.dp
internal val LibraryPullToRefreshIndicatorOffset = 0.dp

@Composable
fun LibraryScreen(navController: NavController) {
    val defaultFilter by rememberEnumPreference(ChipSortTypeKey, LibraryFilter.LIBRARY)
    val (disableBlur) = rememberPreference(DisableBlurKey, false)
    val libraryFilters = remember {
        listOf(
            LibraryFilter.LIBRARY,
            LibraryFilter.PLAYLISTS,
            LibraryFilter.SONGS,
            LibraryFilter.ARTISTS,
            LibraryFilter.ALBUMS,
        )
    }

    val pagerState =
        rememberPagerState(
            initialPage = libraryFilters.indexOf(defaultFilter).takeIf { it >= 0 } ?: 0,
        ) { libraryFilters.size }

    val currentFilter = libraryFilters.getOrElse(pagerState.currentPage) { LibraryFilter.LIBRARY }

    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val tonalStart = MaterialTheme.colorScheme.primaryContainer
    val tonalMiddle = MaterialTheme.colorScheme.secondaryContainer

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        if (!disableBlur) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(430.dp)
                        .align(Alignment.TopCenter)
                        .drawWithCache {
                            val brush =
                                Brush.verticalGradient(
                                    0f to tonalStart.copy(alpha = 0.30f),
                                    0.42f to tonalMiddle.copy(alpha = 0.14f),
                                    1f to Color.Transparent,
                                )
                            onDrawBehind { drawRect(brush) }
                        },
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(top = AppBarHeight),
        ) {
            val tabListState = rememberLazyListState()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(defaultFilter, libraryFilters) {
                val selectedFilter = defaultFilter.takeIf { it in libraryFilters } ?: LibraryFilter.LIBRARY
                val selectedPage = libraryFilters.indexOf(selectedFilter).takeIf { it >= 0 } ?: 0
                if (pagerState.currentPage != selectedPage) {
                    pagerState.scrollToPage(selectedPage)
                }
            }

            // Sync Pager -> Preference & lazy list centering
            LaunchedEffect(pagerState.currentPage, libraryFilters) {
                val targetPage = pagerState.currentPage.coerceIn(0, libraryFilters.lastIndex)
                val targetFilter = libraryFilters.getOrElse(targetPage) { LibraryFilter.LIBRARY }

                val tabWidth =
                    when (targetFilter) {
                        LibraryFilter.LIBRARY -> 116.dp
                        LibraryFilter.PLAYLISTS -> 132.dp
                        LibraryFilter.SONGS -> 102.dp
                        LibraryFilter.ARTISTS -> 116.dp
                        LibraryFilter.ALBUMS -> 110.dp
                        else -> 116.dp
                    }
                val screenWidth = configuration.screenWidthDp.dp
                val targetOffsetDp = (screenWidth - tabWidth) / 2
                val targetOffsetPx = with(density) { targetOffsetDp.roundToPx() }

                tabListState.animateScrollToItem(targetPage, scrollOffset = -targetOffsetPx)
            }

            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    when (libraryFilters.getOrElse(page) { LibraryFilter.LIBRARY }) {
                        LibraryFilter.LIBRARY -> {
                            LibraryMixScreen(
                                navController = navController,
                                filterContent = null,
                                selectedTagIds = emptySet(),
                                onTabSelected = { targetFilter ->
                                    coroutineScope.launch {
                                        val targetPage = libraryFilters.indexOf(targetFilter)
                                        pagerState.animateScrollToPage(targetPage.takeIf { it >= 0 } ?: 0)
                                    }
                                },
                            )
                        }

                        LibraryFilter.PLAYLISTS -> {
                            LibraryPlaylistsScreen(
                                navController = navController,
                                filterContent = null,
                                selectedTagIds = emptySet(),
                            )
                        }

                        LibraryFilter.SONGS -> {
                            LibrarySongsScreen(
                                navController = navController,
                                onDeselect = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(0)
                                    }
                                },
                            )
                        }

                        LibraryFilter.ARTISTS -> {
                            LibraryArtistsScreen(
                                navController = navController,
                                onDeselect = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(0)
                                    }
                                },
                            )
                        }

                        LibraryFilter.ALBUMS -> {
                            LibraryAlbumsScreen(
                                navController = navController,
                                onDeselect = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(0)
                                    }
                                },
                            )
                        }

                        else -> Unit
                    }
                }

                LazyRow(
                    state = tabListState,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(
                        items = libraryFilters,
                        key = { filter -> filter.name },
                        contentType = { "library_filter_chip" },
                    ) { filter ->
                        val page = libraryFilters.indexOf(filter)
                        val label =
                            when (filter) {
                                LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                                LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                                LibraryFilter.SONGS -> stringResource(R.string.songs)
                                LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                                LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                                else -> filter.name
                            }
                        val iconRes =
                            when (filter) {
                                LibraryFilter.LIBRARY -> R.drawable.graphic_eq
                                LibraryFilter.PLAYLISTS -> R.drawable.queue_music
                                LibraryFilter.SONGS -> R.drawable.music_note
                                LibraryFilter.ARTISTS -> R.drawable.person
                                LibraryFilter.ALBUMS -> R.drawable.album
                                else -> R.drawable.music_note
                            }
                        ExpressiveTabChip(
                            label = label,
                            iconRes = iconRes,
                            selected = currentFilter == filter,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page)
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
fun ExpressiveTabChip(
    label: String,
    iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue =
            if (isPressed) {
                0.92f
            } else if (selected) {
                1.05f
            } else {
                1.0f
            },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "TabChipScale",
    )

    val bgColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "TabChipBgColor",
    )

    val contentColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "TabChipContentColor",
    )

    Row(
        modifier =
            Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clip(CircleShape)
                .background(bgColor)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = contentColor,
        )
    }
}
