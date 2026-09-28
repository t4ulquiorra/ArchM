package com.archm.player.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.archm.player.LocalDatabase
import com.archm.player.LocalPlayerAwareWindowInsets
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.db.entities.ActivityLogEntity
import com.archm.player.db.entities.EventWithSong
import com.archm.player.db.entities.Song
import com.archm.player.extensions.toMediaItem
import com.archm.player.extensions.togglePlayPause
import com.archm.player.models.MediaMetadata
import com.archm.player.playback.PlayerConnection
import com.archm.player.playback.queues.ListQueue
import com.archm.player.playback.queues.YouTubeQueue
import com.archm.player.ui.component.DefaultDialog
import com.archm.player.ui.component.LocalMenuState
import com.archm.player.ui.component.bouncyClickable
import com.archm.player.ui.menu.SongMenu
import com.archm.player.viewmodels.DateGroup
import com.archm.player.viewmodels.PlayGroup
import com.archm.player.viewmodels.RecentsViewModel
import com.music.innertube.models.WatchEndpoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(
    navController: NavController,
    viewModel: RecentsViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val playerConnection = LocalPlayerConnection.current ?: return
    val database = LocalDatabase.current
    val menuState = LocalMenuState.current

    val recentActivity by viewModel.recentActivity.collectAsState()
    val recentlyPlayed by viewModel.recentlyPlayed.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val amoledBg = Color.Black

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(amoledBg),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.recents),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    ),
                    color = Color.White,
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = "Back",
                        tint = Color.White,
                    )
                }
            },
            actions = {
                if (pagerState.currentPage == 0 && recentActivity.isNotEmpty()) {
                    IconButton(onClick = { showClearConfirmDialog = true }) {
                        Icon(
                            painter = painterResource(R.drawable.delete_history),
                            contentDescription = stringResource(R.string.clear_recents),
                            tint = Color.White.copy(alpha = 0.8f),
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = amoledBg,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White,
            ),
        )

        // AMOLED-native Tab Row
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = amoledBg,
            contentColor = Color.White,
            indicator = { tabPositions ->
                if (pagerState.currentPage < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp,
                    )
                }
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f)),
                )
            },
        ) {
            val tabs = listOf(
                stringResource(R.string.recent_activity),
                stringResource(R.string.recently_played),
            )
            tabs.forEachIndexed { index, title ->
                val selected = pagerState.currentPage == index
                Tab(
                    selected = selected,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
                        )
                    },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            when (page) {
                0 -> RecentActivityTab(
                    recentActivity = recentActivity,
                    navController = navController,
                    playerConnection = playerConnection,
                    onDeleteItem = { id -> viewModel.deleteRecentActivity(id) },
                )
                1 -> RecentlyPlayedTab(
                    dateGroups = recentlyPlayed,
                    navController = navController,
                    playerConnection = playerConnection,
                )
            }
        }
    }

    if (showClearConfirmDialog) {
        DefaultDialog(
            onDismiss = { showClearConfirmDialog = false },
            title = { Text(stringResource(R.string.clear_recents), color = Color.White) },
            buttons = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(stringResource(android.R.string.cancel))
                }
                TextButton(
                    onClick = {
                        viewModel.clearAllRecentActivity()
                        showClearConfirmDialog = false
                    },
                ) {
                    Text(
                        stringResource(R.string.clear_recents),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
        ) {
            Text(
                text = "Are you sure you want to clear your recent activity?",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun RecentActivityTab(
    recentActivity: List<ActivityLogEntity>,
    navController: NavController,
    playerConnection: PlayerConnection,
    onDeleteItem: (Long) -> Unit,
) {
    if (recentActivity.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.history),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White.copy(alpha = 0.3f),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "No recent activity",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
        return
    }

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()

    var selectedItemForMenu by remember { mutableStateOf<ActivityLogEntity?>(null) }

    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = LocalPlayerAwareWindowInsets.current
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            .asPaddingValues(),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(
            items = recentActivity,
            key = { "${it.id}_${it.entityType}_${it.entityId}" },
        ) { item ->
            RecentActivityItemRow(
                item = item,
                isActive = item.entityType == "SONG" && item.entityId == mediaMetadata?.id,
                isPlaying = isPlaying,
                onClick = {
                    when (item.entityType) {
                        "ARTIST" -> navController.navigate("artist/${item.entityId}")
                        "ALBUM" -> navController.navigate("album/${item.entityId}")
                        "PLAYLIST" -> {
                            if (item.entityId.startsWith("LP")) {
                                navController.navigate("local_playlist/${item.entityId}")
                            } else {
                                navController.navigate("online_playlist/${item.entityId}")
                            }
                        }
                        "SONG" -> {
                            if (mediaMetadata?.id == item.entityId) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    YouTubeQueue(
                                        WatchEndpoint(videoId = item.entityId),
                                        MediaMetadata(
                                            id = item.entityId,
                                            title = item.title,
                                            artists = item.subtitle?.split(", ")?.map { MediaMetadata.Artist(id = null, name = it) } ?: emptyList(),
                                            duration = 0,
                                            thumbnailUrl = item.thumbnailUrl,
                                        ),
                                    ),
                                )
                            }
                        }
                    }
                },
                onMoreClick = { selectedItemForMenu = item },
            )
        }
    }

    selectedItemForMenu?.let { item ->
        DefaultDialog(
            onDismiss = { selectedItemForMenu = null },
            title = {
                Text(
                    text = item.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            },
            buttons = {
                TextButton(onClick = { selectedItemForMenu = null }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Open option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val target = selectedItemForMenu
                            selectedItemForMenu = null
                            if (target != null) {
                                when (target.entityType) {
                                    "ARTIST" -> navController.navigate("artist/${target.entityId}")
                                    "ALBUM" -> navController.navigate("album/${target.entityId}")
                                    "PLAYLIST" -> {
                                        if (target.entityId.startsWith("LP")) {
                                            navController.navigate("local_playlist/${target.entityId}")
                                        } else {
                                            navController.navigate("online_playlist/${target.entityId}")
                                        }
                                    }
                                    "SONG" -> {
                                        if (mediaMetadata?.id == target.entityId) {
                                            playerConnection.player.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                YouTubeQueue(
                                                    WatchEndpoint(videoId = target.entityId),
                                                    MediaMetadata(
                                                        id = target.entityId,
                                                        title = target.title,
                                                        artists = target.subtitle?.split(", ")?.map { MediaMetadata.Artist(id = null, name = it) } ?: emptyList(),
                                                        duration = 0,
                                                        thumbnailUrl = target.thumbnailUrl,
                                                    ),
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(
                            when (item.entityType) {
                                "ARTIST" -> R.drawable.person
                                "ALBUM" -> R.drawable.album
                                "PLAYLIST" -> R.drawable.queue_music
                                else -> R.drawable.play
                            }
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = when (item.entityType) {
                            "ARTIST" -> "View Artist"
                            "ALBUM" -> "View Album"
                            "PLAYLIST" -> "View Playlist"
                            else -> "Play Song"
                        },
                        color = Color.White,
                        fontSize = 15.sp,
                    )
                }

                // Delete option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onDeleteItem(item.id)
                            selectedItemForMenu = null
                        }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        text = "Remove from recent activity",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivityItemRow(
    item: ActivityLogEntity,
    isActive: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isArtist = item.entityType == "ARTIST"
    val shape = if (isArtist) CircleShape else RoundedCornerShape(10.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(shape)
                .background(Color(0xFF1E1E1E)),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.thumbnailUrl)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                placeholder = painterResource(
                    when (item.entityType) {
                        "ARTIST" -> R.drawable.person
                        "ALBUM" -> R.drawable.album
                        "PLAYLIST" -> R.drawable.queue_music
                        else -> R.drawable.music_note
                    }
                ),
                error = painterResource(
                    when (item.entityType) {
                        "ARTIST" -> R.drawable.person
                        "ALBUM" -> R.drawable.album
                        "PLAYLIST" -> R.drawable.queue_music
                        else -> R.drawable.music_note
                    }
                ),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            if (isActive && isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.volume_up),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.5.sp,
                ),
                color = if (isActive) MaterialTheme.colorScheme.primary else Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(2.dp))
            val subtitle = item.subtitle ?: when (item.entityType) {
                "ARTIST" -> "Artist"
                "ALBUM" -> "Album"
                "PLAYLIST" -> "Playlist"
                else -> "Song"
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color.White.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.more_vert),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun RecentlyPlayedTab(
    dateGroups: List<DateGroup>,
    navController: NavController,
    playerConnection: PlayerConnection,
) {
    if (dateGroups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.history),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = Color.White.copy(alpha = 0.3f),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "No playback history",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.6f),
                )
            }
        }
        return
    }

    val expandedGroupIds = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        state = rememberLazyListState(),
        contentPadding = LocalPlayerAwareWindowInsets.current
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            .asPaddingValues(),
        modifier = Modifier.fillMaxSize(),
    ) {
        dateGroups.forEach { dateGroup ->
            item(key = "header_${dateGroup.dateLabel}") {
                Text(
                    text = dateGroup.dateLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    ),
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }

            items(
                items = dateGroup.groups,
                key = { it.id },
            ) { group ->
                val isExpanded = expandedGroupIds[group.id] ?: false
                AccordionPlayGroupItem(
                    group = group,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedGroupIds[group.id] = !isExpanded
                    },
                    navController = navController,
                    playerConnection = playerConnection,
                )
            }
        }
    }
}

@Composable
private fun AccordionPlayGroupItem(
    group: PlayGroup,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    navController: NavController,
    playerConnection: PlayerConnection,
) {
    val coroutineScope = rememberCoroutineScope()
    val menuState = LocalMenuState.current
    val isArtist = group.parentType == "Artist"
    val parentShape = if (isArtist) CircleShape else RoundedCornerShape(10.dp)

    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ChevronRotation",
    )

    val currentMediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isEffectivelyPlaying.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF121212)),
    ) {
        // Accordion Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(parentShape)
                    .background(Color(0xFF222222)),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(group.thumbnailUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    placeholder = painterResource(if (isArtist) R.drawable.person else R.drawable.album),
                    error = painterResource(if (isArtist) R.drawable.person else R.drawable.album),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = group.parentName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                val countText = if (group.events.size == 1) "1 song played" else "${group.events.size} songs played"
                Text(
                    text = "$countText • ${group.parentType}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.White.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Play group button
            IconButton(
                onClick = {
                    val mediaItems = group.events.map { it.song.toMediaItem() }
                    if (mediaItems.isNotEmpty()) {
                        playerConnection.playQueue(ListQueue(items = mediaItems))
                    }
                },
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = "Play group",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Chevron expand toggle
            IconButton(
                onClick = onToggleExpand,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.expand_more),
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.rotate(rotationAngle),
                )
            }
        }

        // Expanded track list
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.07f)),
                )

                group.events.forEachIndexed { index, eventWithSong ->
                    val song = eventWithSong.song
                    val isTrackActive = currentMediaMetadata?.id == song.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isTrackActive) {
                                    playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.playQueue(
                                        ListQueue(
                                            items = group.events.map { it.song.toMediaItem() },
                                            startIndex = index,
                                        ),
                                    )
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF222222)),
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(song.thumbnailUrl)
                                    .crossfade(true)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                                placeholder = painterResource(R.drawable.music_note),
                                error = painterResource(R.drawable.music_note),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                            if (isTrackActive && isPlaying) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.volume_up),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = song.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.5.sp,
                                    fontWeight = if (isTrackActive) FontWeight.Bold else FontWeight.Medium,
                                ),
                                color = if (isTrackActive) MaterialTheme.colorScheme.primary else Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val artistSubtitle = song.artists.joinToString(", ") { it.name }
                            if (artistSubtitle.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = artistSubtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = Color.White.copy(alpha = 0.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                menuState.show {
                                    SongMenu(
                                        originalSong = song,
                                        event = eventWithSong.event,
                                        navController = navController,
                                        onDismiss = menuState::dismiss,
                                    )
                                }
                            },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.more_vert),
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}
