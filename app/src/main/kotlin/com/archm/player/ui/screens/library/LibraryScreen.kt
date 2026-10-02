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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.navigation.NavController
import com.archm.player.R
import com.archm.player.constants.ChipSortTypeKey
import com.archm.player.constants.DarkModeKey
import com.archm.player.constants.LibraryFilter
import com.archm.player.constants.PureBlackKey
import com.archm.player.ui.screens.settings.DarkMode
import com.archm.player.utils.rememberEnumPreference
import com.archm.player.utils.rememberPreference
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

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor),
    ) {
        val selectedTabIndex = pagerState.currentPage.coerceIn(0, libraryFilters.lastIndex)

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .wrapContentWidth()
                            .padding(horizontal = 16.dp),
                        color = Color.White,
                        height = 2.5.dp,
                    )
                }
            },
            divider = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            libraryFilters.forEachIndexed { page, filter ->
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
                val iconRes =
                    when (filter) {
                        LibraryFilter.LIBRARY -> R.drawable.graphic_eq
                        LibraryFilter.PLAYLISTS -> R.drawable.queue_music
                        LibraryFilter.SONGS -> R.drawable.music_note
                        LibraryFilter.ARTISTS -> R.drawable.person
                        LibraryFilter.ALBUMS -> R.drawable.album
                        else -> R.drawable.music_note
                    }
                Tab(
                    selected = selected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp,
                                ),
                                color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                            )
                        }
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
