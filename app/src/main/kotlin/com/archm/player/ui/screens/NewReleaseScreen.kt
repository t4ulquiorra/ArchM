

package com.archm.player.ui.screens

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import com.archm.player.ui.component.FixedTopStrip
import com.archm.player.ui.component.LargeTitleHeader
import com.archm.player.ui.component.getSharedTopClearance
import com.archm.player.ui.component.rememberTopBarTitleAlphas
import com.archm.player.ui.theme.Marble
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.constants.GridThumbnailHeight
import com.archm.player.ui.component.CombinedIconButton
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.YouTubeGridItem
import com.archm.player.ui.component.shimmer.GridItemPlaceHolder
import com.archm.player.ui.component.shimmer.ShimmerHost
import com.archm.player.ui.menu.YouTubeAlbumMenu
import com.archm.player.ui.utils.backToMain
import com.archm.player.viewmodels.NewReleaseContent
import com.archm.player.viewmodels.NewReleaseUiState
import com.archm.player.viewmodels.NewReleaseViewModel
import com.music.innertube.models.AlbumItem
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReleaseScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showNavigationIcon: Boolean = false,
    viewModel: NewReleaseViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by rememberSaveable { mutableStateOf(NewReleaseTab.All) }

    val gridState = rememberLazyGridState()
    val playerAwarePadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()

    val topClearance = getSharedTopClearance()
    val titleAlphas = rememberTopBarTitleAlphas(gridState)

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
    ) {
        FixedTopStrip(
            title = "New",
            alpha = titleAlphas.smallTitleAlpha,
            navController = navController,
            showNavigationIcon = showNavigationIcon,
            onBackClick = { navController.navigateUp() },
        )
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(150))
            },
            modifier = Modifier.fillMaxSize(),
            label = "NewReleaseContent",
        ) { state ->
            when (state) {
                NewReleaseUiState.Loading -> {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(minSize = GridThumbnailHeight + 24.dp),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = topClearance,
                            bottom = playerAwarePadding.calculateBottomPadding() + 16.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(12) {
                            ShimmerHost {
                                GridItemPlaceHolder(fillMaxWidth = true, modifier = Modifier.padding(4.dp))
                            }
                        }
                    }
                }

                is NewReleaseUiState.Success -> {
                    NewReleaseGridContent(
                        content = state.content,
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        gridState = gridState,
                        topPadding = topClearance,
                        largeTitleAlpha = titleAlphas.largeTitleAlpha,
                        bottomPadding = playerAwarePadding.calculateBottomPadding() + 16.dp,
                        activeAlbumId = mediaMetadata?.album?.id,
                        isPlaying = isPlaying,
                        coroutineScope = coroutineScope,
                        onReleaseClick = { album -> navController.navigate("album/${album.id}") },
                        onReleaseLongClick = { album ->
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                YouTubeAlbumMenu(
                                    albumItem = album,
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                        onRefresh = viewModel::retry,
                    )
                }

                NewReleaseUiState.Error -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(top = topClearance)
                                .padding(horizontal = 24.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.error),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "New releases are temporarily unavailable",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Could not load this YouTube Music section. Try again later.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = viewModel::retry,
                            ) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }

                NewReleaseUiState.Empty -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(top = topClearance)
                                .padding(horizontal = 24.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.no_results_found),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = viewModel::retry,
                            ) {
                                Text(stringResource(R.string.refresh))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Immutable
private enum class NewReleaseTab(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val contentType: String,
) {
    All(
        titleRes = R.string.filter_all,
        icon = Icons.Outlined.LibraryMusic,
        contentType = "new_release_all_grid_item",
    ),
    Albums(
        titleRes = R.string.albums,
        icon = Icons.Outlined.Album,
        contentType = "new_release_album_grid_item",
    ),
    Singles(
        titleRes = R.string.singles,
        icon = Icons.Outlined.MusicNote,
        contentType = "new_release_single_grid_item",
    ),
    Ep(
        titleRes = R.string.ep,
        icon = Icons.Outlined.QueueMusic,
        contentType = "new_release_ep_grid_item",
    ),
}

@Immutable
private data class NewReleaseSection(
    val tab: NewReleaseTab,
    val releases: List<AlbumItem>,
)

@Composable
private fun NewReleaseGridContent(
    content: NewReleaseContent,
    selectedTab: NewReleaseTab,
    onTabSelected: (NewReleaseTab) -> Unit,
    gridState: LazyGridState,
    topPadding: androidx.compose.ui.unit.Dp,
    largeTitleAlpha: Float,
    bottomPadding: androidx.compose.ui.unit.Dp,
    activeAlbumId: String?,
    isPlaying: Boolean,
    coroutineScope: CoroutineScope,
    onReleaseClick: (AlbumItem) -> Unit,
    onReleaseLongClick: (AlbumItem) -> Unit,
    onRefresh: () -> Unit,
) {
    val allSections =
        remember(content) {
            content.releaseSections()
        }
    val releases =
        remember(content, selectedTab) {
            if (selectedTab == NewReleaseTab.All) emptyList() else content.releasesFor(selectedTab)
        }

    val contentPadding = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        top = topPadding,
        bottom = bottomPadding,
    )

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = GridThumbnailHeight + 24.dp),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {

        item(
            key = "new_release_large_title",
            span = { GridItemSpan(maxLineSpan) },
            contentType = "new_release_large_title",
        ) {
            LargeTitleHeader(
                title = "New",
                alpha = largeTitleAlpha,
                horizontalPadding = 0.dp,
                bottomSpacer = 8.dp,
            )
        }

        item(
            key = "new_release_chips",
            span = { GridItemSpan(maxLineSpan) },
            contentType = "new_release_chips",
        ) {
            NewReleaseChipsRow(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
            )
        }

        if (selectedTab == NewReleaseTab.All) {
            allSections.forEach { section ->
                item(
                    key = "new_release_section_header_${section.tab.name}",
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "new_release_section_header",
                ) {
                    NewReleaseSectionHeader(
                        title = stringResource(section.tab.titleRes),
                        count = section.releases.size,
                    )
                }

                item(
                    key = "new_release_section_${section.tab.name}",
                    span = { GridItemSpan(maxLineSpan) },
                    contentType = "new_release_horizontal_section",
                ) {
                    NewReleaseHorizontalSection(
                        releases = section.releases,
                        contentType = section.tab.contentType,
                        activeAlbumId = activeAlbumId,
                        isPlaying = isPlaying,
                        coroutineScope = coroutineScope,
                        onReleaseClick = onReleaseClick,
                        onReleaseLongClick = onReleaseLongClick,
                    )
                }
            }
        } else if (releases.isEmpty()) {
            item(
                key = "new_release_empty_${selectedTab.name}",
                span = { GridItemSpan(maxLineSpan) },
                contentType = "new_release_empty",
            ) {
                NewReleaseCategoryEmptyState(onRefresh = onRefresh)
            }
        } else {
            items(
                items = releases,
                key = { it.id },
                contentType = { selectedTab.contentType },
            ) { album ->
                YouTubeGridItem(
                    item = album,
                    isActive = activeAlbumId == album.id,
                    isPlaying = isPlaying,
                    fillMaxWidth = true,
                    coroutineScope = coroutineScope,
                    modifier = Modifier.padding(4.dp).animateItem(),
                    onClick = { onReleaseClick(album) },
                    onLongClick = { onReleaseLongClick(album) },
                )
            }
        }
    }
}

@Composable
private fun NewReleaseChipsRow(
    selectedTab: NewReleaseTab,
    onTabSelected: (NewReleaseTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tabs = remember { NewReleaseTab.entries.toList() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tabs.forEach { tab ->
            val selected = tab == selectedTab
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (selected) Marble else Color(0xFF262626),
                contentColor = if (selected) Color.Black else Color.White,
                modifier = Modifier.clickable { onTabSelected(tab) },
            ) {
                Text(
                    text = stringResource(tab.titleRes),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun NewReleaseSectionHeader(
    title: String,
    count: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 12.dp, end = 4.dp, bottom = 4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NewReleaseHorizontalSection(
    releases: List<AlbumItem>,
    contentType: String,
    activeAlbumId: String?,
    isPlaying: Boolean,
    coroutineScope: CoroutineScope,
    onReleaseClick: (AlbumItem) -> Unit,
    onReleaseLongClick: (AlbumItem) -> Unit,
) {
    LazyHorizontalGrid(
        rows = GridCells.Fixed(1),
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .height(216.dp),
    ) {
        items(
            items = releases,
            key = { it.id },
            contentType = { contentType },
        ) { album ->
            YouTubeGridItem(
                item = album,
                isActive = activeAlbumId == album.id,
                isPlaying = isPlaying,
                fillMaxWidth = false,
                coroutineScope = coroutineScope,
                modifier = Modifier.padding(4.dp).animateItem(),
                onClick = { onReleaseClick(album) },
                onLongClick = { onReleaseLongClick(album) },
            )
        }
    }
}

@Composable
private fun NewReleaseCategoryEmptyState(onRefresh: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 56.dp),
    ) {
        Text(
            text = stringResource(R.string.no_releases_found),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRefresh,
        ) {
            Text(stringResource(R.string.refresh))
        }
    }
}

private fun NewReleaseContent.releasesFor(tab: NewReleaseTab): List<AlbumItem> =
    when (tab) {
        NewReleaseTab.All -> emptyList()
        NewReleaseTab.Albums -> albums
        NewReleaseTab.Singles -> singles
        NewReleaseTab.Ep -> eps
    }

private fun NewReleaseContent.releaseSections(): List<NewReleaseSection> =
    buildList {
        if (albums.isNotEmpty()) {
            add(NewReleaseSection(NewReleaseTab.Albums, albums))
        }
        if (singles.isNotEmpty()) {
            add(NewReleaseSection(NewReleaseTab.Singles, singles))
        }
        if (eps.isNotEmpty()) {
            add(NewReleaseSection(NewReleaseTab.Ep, eps))
        }
    }

