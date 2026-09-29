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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.extensions.togglePlayPause
import com.music.innertube.models.*
import com.archm.player.models.toMediaMetadata
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.YouTubeListItem
import com.archm.player.ui.menu.*
import com.archm.player.viewmodels.OnlineSearchSuggestionViewModel

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OnlineSearchScreen(
    query: String,
    onQueryChange: (TextFieldValue) -> Unit,
    navController: NavController,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
    pureBlack: Boolean,
    viewModel: OnlineSearchSuggestionViewModel = hiltViewModel(),
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val haptic = LocalHapticFeedback.current
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    val lazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        snapshotFlow { lazyListState.firstVisibleItemScrollOffset }
            .drop(1)
            .collect {
                keyboardController?.hide()
            }
    }

    LaunchedEffect(query) {
        viewModel.updateQuery(query)
    }

    val backgroundColor = if (pureBlack) Color.Black else MaterialTheme.colorScheme.background
    val distinctResultItems = remember(viewState.items) { viewState.items.distinctBy { it.id } }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(backgroundColor),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            state = lazyListState,
            contentPadding =
                PaddingValues(
                    top = 12.dp,
                    bottom =
                        WindowInsets.systemBars
                            .only(WindowInsetsSides.Bottom)
                            .asPaddingValues()
                            .calculateBottomPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(SearchRowSpacing),
            modifier =
                Modifier
                    .widthIn(max = SearchContentMaxWidth)
                    .fillMaxSize(),
        ) {
            if (query.isEmpty()) {
                item(
                    key = "recently_searched_header",
                    contentType = "section_header",
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 12.dp, top = 2.dp, bottom = 2.dp)
                                .animateItem(),
                    ) {
                        Text(
                            text = "Recently Searched",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurface,
                        )

                        if (viewState.history.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearHistory() }) {
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }

                itemsIndexed(
                    items = viewState.history,
                    key = { _, history -> "history_${history.query}" },
                    contentType = { _, _ -> "history" },
                ) { index, history ->
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .animateItem(),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSearch(history.query)
                                        onDismiss()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(30, 30, 30))
                                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.history),
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(20.dp),
                                )
                            }

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = history.query,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = stringResource(R.string.search),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (pureBlack) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteHistory(history) },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = stringResource(R.string.remove_from_history),
                                    tint = if (pureBlack) Color.White.copy(alpha = 0.62f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp),
                                )
                            }

                            IconButton(
                                onClick = {
                                    onQueryChange(TextFieldValue(history.query, TextRange(history.query.length)))
                                },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.arrow_top_left),
                                    contentDescription = stringResource(R.string.search),
                                    tint = if (pureBlack) Color.White.copy(alpha = 0.62f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier =
                                        Modifier
                                            .size(18.dp)
                                            .graphicsLayer { scaleX = -1f },
                                )
                            }
                        }

                        if (index < viewState.history.lastIndex) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(start = 16.dp)
                                        .height(0.5.dp)
                                        .background(Color.White.copy(alpha = 0.28f)),
                            )
                        }
                    }
                }
            } else {
                if (viewState.suggestions.isNotEmpty()) {
                    val top5Suggestions = remember(viewState.suggestions) { viewState.suggestions.take(5) }
                    itemsIndexed(
                        items = top5Suggestions,
                        key = { _, suggestion -> "suggestion_$suggestion" },
                        contentType = { _, _ -> "suggestion" },
                    ) { index, suggestion ->
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clickable {
                                            onSearch(suggestion)
                                            onDismiss()
                                        }
                                        .padding(horizontal = 16.dp),
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.search),
                                    contentDescription = null,
                                    tint = if (pureBlack) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )

                                Spacer(Modifier.width(14.dp))

                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (pureBlack) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )

                                IconButton(
                                    onClick = {
                                        onQueryChange(TextFieldValue(suggestion, TextRange(suggestion.length)))
                                    },
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.arrow_top_left),
                                        contentDescription = stringResource(R.string.search),
                                        tint = if (pureBlack) Color.White.copy(alpha = 0.62f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier =
                                            Modifier
                                                .size(18.dp)
                                                .graphicsLayer { scaleX = -1f },
                                    )
                                }
                            }

                            if (index < top5Suggestions.lastIndex) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp)
                                            .height(0.5.dp)
                                            .background(Color.White.copy(alpha = 0.28f)),
                                )
                            }
                        }
                    }
                }

                if (viewState.items.isNotEmpty()) {
                    item(
                        key = "top_results_header",
                        contentType = "section_header",
                    ) {
                        SearchSectionHeader(
                            title = stringResource(R.string.top_results),
                            pureBlack = pureBlack,
                            modifier = Modifier.animateItem(),
                        )
                    }

                    itemsIndexed(
                        items = distinctResultItems,
                        key = { _, item -> "top_result_${item.id}" },
                        contentType = { _, item -> item::class },
                    ) { index, item ->
                        val currentMediaId = mediaMetadata?.id ?: playerConnection.player.currentMediaItem?.mediaId
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                        ) {
                            YouTubeListItem(
                                item = item,
                                containerColor = Color.Transparent,
                                color = Color.Transparent,
                                showActiveContainer = true,
                                isActive =
                                    when (item) {
                                        is SongItem -> item.id == currentMediaId
                                        is AlbumItem -> mediaMetadata?.album?.id == item.id
                                        else -> false
                                    },
                                isPlaying = isPlaying,
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            menuState.show {
                                                when (item) {
                                                    is SongItem -> {
                                                        YouTubeSongMenu(
                                                            song = item,
                                                            navController = navController,
                                                            onDismiss = {
                                                                menuState.dismiss()
                                                                onDismiss()
                                                            },
                                                        )
                                                    }

                                                    is AlbumItem -> {
                                                        YouTubeAlbumMenu(
                                                            albumItem = item,
                                                            navController = navController,
                                                            onDismiss = {
                                                                menuState.dismiss()
                                                                onDismiss()
                                                            },
                                                        )
                                                    }

                                                    is ArtistItem -> {
                                                        YouTubeArtistMenu(
                                                            artist = item,
                                                            onDismiss = {
                                                                menuState.dismiss()
                                                                onDismiss()
                                                            },
                                                        )
                                                    }

                                                    is PlaylistItem -> {
                                                        YouTubePlaylistMenu(
                                                            playlist = item,
                                                            coroutineScope = coroutineScope,
                                                            onDismiss = {
                                                                menuState.dismiss()
                                                                onDismiss()
                                                            },
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.more_vert),
                                            contentDescription = null,
                                        )
                                    }
                                },
                                modifier =
                                    Modifier
                                        .combinedClickable(
                                            onClick = {
                                                when (item) {
                                                    is SongItem -> {
                                                        if (item.id == mediaMetadata?.id) {
                                                            playerConnection.player.togglePlayPause()
                                                        } else {
                                                            playerConnection.playQueue(
                                                                YouTubeQueue.radio(item.toMediaMetadata()),
                                                            )
                                                            onDismiss()
                                                        }
                                                    }

                                                    is AlbumItem -> {
                                                        navController.navigate("album/${item.id}")
                                                        onDismiss()
                                                    }

                                                    is ArtistItem -> {
                                                        navController.navigate("artist/${item.id}")
                                                        onDismiss()
                                                    }

                                                    is PlaylistItem -> {
                                                        navController.navigate("online_playlist/${item.id}")
                                                        onDismiss()
                                                    }
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    when (item) {
                                                        is SongItem -> {
                                                            YouTubeSongMenu(
                                                                song = item,
                                                                navController = navController,
                                                                onDismiss = {
                                                                    menuState.dismiss()
                                                                    onDismiss()
                                                                },
                                                            )
                                                        }

                                                        is AlbumItem -> {
                                                            YouTubeAlbumMenu(
                                                                albumItem = item,
                                                                navController = navController,
                                                                onDismiss = {
                                                                    menuState.dismiss()
                                                                    onDismiss()
                                                                },
                                                            )
                                                        }

                                                        is ArtistItem -> {
                                                            YouTubeArtistMenu(
                                                                artist = item,
                                                                onDismiss = {
                                                                    menuState.dismiss()
                                                                    onDismiss()
                                                                },
                                                            )
                                                        }

                                                        is PlaylistItem -> {
                                                            YouTubePlaylistMenu(
                                                                playlist = item,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = {
                                                                    menuState.dismiss()
                                                                    onDismiss()
                                                                },
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                        ),
                            )
                            if (index < distinctResultItems.lastIndex) {
                                Box(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp)
                                            .height(0.5.dp)
                                            .background(Color.White.copy(alpha = 0.28f)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(
    title: String,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color =
            if (pureBlack) {
                Color.White.copy(alpha = 0.72f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    start = SearchHorizontalPadding + 4.dp,
                    top = 16.dp,
                    end = SearchHorizontalPadding + 4.dp,
                    bottom = 6.dp,
                ),
    )
}

private fun segmentedSearchItemShape(
    index: Int,
    count: Int,
): Shape =
    when {
        count <= 1 -> {
            RoundedCornerShape(SearchGroupOuterCorner)
        }

        index == 0 -> {
            RoundedCornerShape(
                topStart = SearchGroupOuterCorner,
                topEnd = SearchGroupOuterCorner,
                bottomEnd = SearchGroupInnerCorner,
                bottomStart = SearchGroupInnerCorner,
            )
        }

        index == count - 1 -> {
            RoundedCornerShape(
                topStart = SearchGroupInnerCorner,
                topEnd = SearchGroupInnerCorner,
                bottomEnd = SearchGroupOuterCorner,
                bottomStart = SearchGroupOuterCorner,
            )
        }

        else -> {
            RoundedCornerShape(SearchGroupInnerCorner)
        }
    }

@Composable
fun SuggestionItem(
    modifier: Modifier = Modifier,
    query: String,
    online: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit = {},
    onFillTextField: () -> Unit,
    pureBlack: Boolean,
    shape: Shape = MaterialTheme.shapes.large,
) {
    val containerColor =
        if (pureBlack) {
            Color.White.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }

    val iconContainerColor =
        if (pureBlack) {
            Color.White.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        }

    val iconTint =
        if (pureBlack) {
            Color.White.copy(alpha = 0.78f)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Surface(
        onClick = onClick,
        shape = shape,
        color = containerColor,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = SearchHorizontalPadding),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = SearchRowMinHeight)
                    .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .size(40.dp)
                        .background(
                            color = iconContainerColor,
                            shape = MaterialTheme.shapes.medium,
                        ),
            ) {
                Icon(
                    painterResource(if (online) R.drawable.search else R.drawable.history),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Text(
                text = query,
                style = MaterialTheme.typography.bodyLarge,
                color = if (pureBlack) Color.White.copy(alpha = 0.92f) else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            if (!online) {
                IconButton(onClick = onDelete) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = stringResource(R.string.remove_from_history),
                        tint =
                            if (pureBlack) {
                                Color.White.copy(alpha = 0.62f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            IconButton(onClick = onFillTextField) {
                Icon(
                    painter = painterResource(R.drawable.arrow_top_left),
                    contentDescription = stringResource(R.string.search),
                    tint =
                        if (pureBlack) {
                            Color.White.copy(alpha = 0.62f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

private val SearchContentMaxWidth = 720.dp
private val SearchHorizontalPadding = 12.dp
private val SearchRowMinHeight = 64.dp
private val SearchRowSpacing = 2.dp
private val SearchGroupOuterCorner = 24.dp
private val SearchGroupInnerCorner = 6.dp
