/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.archm.player.ui.component.FixedTopStrip
import com.archm.player.ui.component.LargeTitleHeader
import com.archm.player.ui.component.getSharedTopClearance
import com.archm.player.ui.component.rememberTopBarTitleAlphas
import kotlin.math.roundToInt
import com.archm.player.R
import com.archm.player.constants.ChipSortTypeKey
import com.archm.player.constants.DarkModeKey
import com.archm.player.constants.LibraryFilter
import com.archm.player.constants.PureBlackKey
import com.archm.player.ui.screens.settings.DarkMode
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.utils.rememberPreference
import com.archm.player.ui.theme.Marble
import kotlinx.coroutines.launch

internal val LibraryHeaderContentPadding = 0.dp
internal val LibraryPullToRefreshIndicatorOffset = 0.dp

@Composable
fun LibraryScreen(navController: NavController) {
    val pureBlackEnabled by rememberPreference(PureBlackKey, defaultValue = false)
    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }
    val pureBlack = remember(pureBlackEnabled, useDarkTheme) {
        pureBlackEnabled && useDarkTheme
    }

    val defaultFilter by rememberEnumPreference(ChipSortTypeKey, LibraryFilter.LIBRARY)
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

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(defaultFilter, libraryFilters) {
        val selectedFilter = defaultFilter.takeIf { it in libraryFilters } ?: LibraryFilter.LIBRARY
        val selectedPage = libraryFilters.indexOf(selectedFilter).takeIf { it >= 0 } ?: 0
        if (pagerState.currentPage != selectedPage) {
            pagerState.scrollToPage(selectedPage)
        }
    }

    val backgroundColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.background

    val topClearance = getSharedTopClearance()
    val density = LocalDensity.current
    var largeTitleHeight by remember { mutableStateOf(65.dp) }
    val maxCollapseDp = remember(largeTitleHeight) { largeTitleHeight }
    val maxCollapsePx = with(density) { maxCollapseDp.toPx() }

    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    val nestedScrollConnection = remember(maxCollapsePx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0f && headerOffsetPx > -maxCollapsePx) {
                    val newOffset = (headerOffsetPx + delta).coerceIn(-maxCollapsePx, 0f)
                    val consumed = newOffset - headerOffsetPx
                    headerOffsetPx = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0f && headerOffsetPx < 0f) {
                    val newOffset = (headerOffsetPx + delta).coerceIn(-maxCollapsePx, 0f)
                    val consumed = newOffset - headerOffsetPx
                    headerOffsetPx = newOffset
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }
        }
    }

    val scrollOffset = -headerOffsetPx
    val isScrolledAway = scrollOffset >= (maxCollapsePx - 2f).coerceAtLeast(0f)
    val titleAlphas = rememberTopBarTitleAlphas(
        scrollOffset = scrollOffset,
        isScrolledAway = isScrolledAway,
    )
    val collapseDp = with(density) { (-headerOffsetPx).toDp() }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .nestedScroll(nestedScrollConnection),
    ) {
        FixedTopStrip(
            title = "Library",
            alpha = titleAlphas.smallTitleAlpha,
            navController = navController,
            backgroundColor = backgroundColor,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = topClearance),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height((largeTitleHeight - collapseDp).coerceAtLeast(0.dp))
                        .clipToBounds(),
            ) {
                LargeTitleHeader(
                    title = "Library",
                    alpha = titleAlphas.largeTitleAlpha,
                    horizontalPadding = 16.dp,
                    bottomSpacer = 8.dp,
                    modifier =
                        Modifier
                            .offset { IntOffset(0, headerOffsetPx.roundToInt()) }
                            .onGloballyPositioned {
                                if (largeTitleHeight == 65.dp && it.size.height > 0) {
                                    with(density) { largeTitleHeight = it.size.height.toDp() }
                                }
                            },
                )
            }

            val selectedTabIndex = pagerState.currentPage.coerceIn(0, libraryFilters.lastIndex)
            val tabListState = rememberLazyListState()
            LaunchedEffect(selectedTabIndex) {
                tabListState.animateScrollToItem(selectedTabIndex)
            }

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(backgroundColor)
                        .padding(top = 8.dp, bottom = 8.dp),
            ) {
                LazyRow(
                    state = tabListState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    itemsIndexed(libraryFilters) { page, filter ->
                        val selected = selectedTabIndex == page
                        val label =
                            when (filter) {
                                LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                                LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                                LibraryFilter.SONGS -> stringResource(R.string.songs)
                                LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                                LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                                else -> filter.name
                            }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) Marble else Color(0xFF262626),
                            contentColor = if (selected) Color.Black else Color.White,
                            modifier = Modifier.clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            },
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }
                }
            }

        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
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
    }
}
}
