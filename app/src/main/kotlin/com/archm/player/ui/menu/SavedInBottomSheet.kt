package com.archm.player.ui.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.archm.player.LocalDatabase
import com.archm.player.LocalSyncUtils
import com.archm.player.R
import com.archm.player.constants.ListThumbnailSize
import com.archm.player.constants.PlaylistSortType
import com.archm.player.constants.ThumbnailCornerRadius
import com.archm.player.db.entities.Playlist
import com.archm.player.db.entities.PlaylistEntity
import com.archm.player.db.entities.PlaylistSongMap
import com.archm.player.db.entities.Song
import com.archm.player.db.entities.SongEntity
import com.archm.player.models.MediaMetadata
import com.archm.player.models.toMediaMetadata
import com.archm.player.ui.component.CustomSnackbarManager
import com.archm.player.ui.component.PlaylistThumbnail
import com.music.innertube.YouTube
import com.music.innertube.models.SongItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

/**
 * Selection indicator for the right side of playlist rows:
 * - When NOT selected: Circular outlined '+' icon (24.dp)
 * - When IS selected: Solid circle filled with accent color containing checkmark '✓' (24.dp)
 */
@Composable
fun PlaylistSelectionIndicator(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isSelected) {
        Box(
            modifier = modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(24.dp)
                .border(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

/**
 * Spotify-style multi-playlist picker bottom sheet.
 * Supports passing SongEntity, SongItem, or MediaMetadata.
 */
@Composable
fun SavedInBottomSheet(
    song: Song,
    onDismiss: () -> Unit,
) {
    SavedInBottomSheet(
        song = song.song,
        onDismiss = onDismiss,
    )
}

@Composable
fun SavedInBottomSheet(
    song: SongEntity? = null,
    songItem: SongItem? = null,
    mediaMetadata: MediaMetadata? = null,
    onDismiss: () -> Unit,
) {
    val database = LocalDatabase.current
    val syncUtils = LocalSyncUtils.current
    val coroutineScope = rememberCoroutineScope()

    val targetSongId = song?.id ?: songItem?.id ?: mediaMetadata?.id ?: return
    val targetTitle = song?.title ?: songItem?.title ?: mediaMetadata?.title ?: ""

    val dbSong by database.song(targetSongId).collectAsState(initial = null)
    val rawPlaylists by database.playlists(PlaylistSortType.NAME, false).collectAsState(initial = emptyList())
    val userPlaylists = remember(rawPlaylists) {
        rawPlaylists.filter { it.playlist.isEditable || it.playlist.bookmarkedAt != null }
    }

    var isLiked by remember(targetSongId) { mutableStateOf<Boolean?>(null) }
    var initialIsLiked by remember(targetSongId) { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(dbSong, song) {
        if (isLiked == null) {
            val likedVal = song?.liked ?: dbSong?.song?.liked ?: false
            isLiked = likedVal
            initialIsLiked = likedVal
        }
    }

    val selectedPlaylistIds = remember { mutableStateListOf<String>() }
    val initialPlaylistIds = remember { mutableSetOf<String>() }
    var isInitialized by remember { mutableStateOf(false) }

    val existingMaps by produceState<List<PlaylistSongMap>?>(initialValue = null, targetSongId) {
        withContext(Dispatchers.IO) {
            value = database.playlistSongMaps(targetSongId)
        }
    }

    LaunchedEffect(existingMaps) {
        if (existingMaps != null && !isInitialized) {
            val ids = existingMaps!!.map { it.playlistId }
            initialPlaylistIds.addAll(ids)
            selectedPlaylistIds.clear()
            selectedPlaylistIds.addAll(ids)
            isInitialized = true
        }
    }

    var showNewPlaylistDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()),
    ) {
        // 1. Header: Left "Cancel", Right "Done"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        // Ensure song exists in database
                        val existingSong = database.song(targetSongId).firstOrNull()
                        if (existingSong == null) {
                            val meta = mediaMetadata
                                ?: songItem?.toMediaMetadata()
                                ?: song?.toMediaMetadata()
                            if (meta != null) {
                                database.transaction {
                                    insert(meta)
                                }
                            }
                        }

                        val toAdd = selectedPlaylistIds.toSet() - initialPlaylistIds
                        val toRemove = initialPlaylistIds - selectedPlaylistIds.toSet()
                        val initialLikedVal = initialIsLiked ?: false
                        val currentLikedVal = isLiked ?: false
                        val likedChanged = (currentLikedVal != initialLikedVal)

                        // 1. Additions
                        for (plId in toAdd) {
                            val pl = userPlaylists.firstOrNull { it.id == plId }
                            database.query {
                                insert(
                                    PlaylistSongMap(
                                        songId = targetSongId,
                                        playlistId = plId,
                                        position = pl?.songCount ?: 0,
                                    )
                                )
                            }
                            pl?.playlist?.browseId?.let { browseId ->
                                YouTube.addToPlaylist(browseId, targetSongId)
                            }
                        }

                        // 2. Removals
                        val currentMaps = database.playlistSongMaps(targetSongId)
                        for (plId in toRemove) {
                            val map = currentMaps.firstOrNull { it.playlistId == plId }
                            if (map != null) {
                                database.query {
                                    delete(map)
                                }
                            }
                            val pl = userPlaylists.firstOrNull { it.id == plId }
                            if (pl?.playlist?.browseId != null && map?.setVideoId != null) {
                                YouTube.removeFromPlaylist(pl.playlist.browseId!!, targetSongId, map.setVideoId!!)
                            }
                        }

                        // 3. Like / Unlike
                        if (likedChanged) {
                            val s = database.song(targetSongId).firstOrNull()?.song
                            if (currentLikedVal) {
                                val updated = s?.toggleLike() ?: song?.toggleLike()
                                if (updated != null) {
                                    database.query {
                                        update(updated)
                                    }
                                    syncUtils.likeSong(updated)
                                } else {
                                    YouTube.likeVideo(targetSongId, true)
                                }
                            } else {
                                if (s != null) {
                                    val updated = s.copy(liked = false, likedDate = null)
                                    database.query {
                                        update(updated)
                                    }
                                    syncUtils.likeSong(updated)
                                }
                                YouTube.likeVideo(targetSongId, false)
                            }
                        }

                        // 4. Determine summary message for custom snackbar
                        val summaryText = when {
                            toAdd.isNotEmpty() -> {
                                if (toAdd.size == 1) {
                                    val plName = userPlaylists.firstOrNull { it.id == toAdd.first() }?.playlist?.name ?: "playlist"
                                    "Saved to $plName"
                                } else {
                                    "Saved to ${toAdd.size} playlists"
                                }
                            }
                            likedChanged && currentLikedVal -> "Added to Liked Songs"
                            likedChanged && !currentLikedVal -> "Removed from Liked Songs"
                            toRemove.isNotEmpty() -> {
                                if (toRemove.size == 1) {
                                    val plName = userPlaylists.firstOrNull { it.id == toRemove.first() }?.playlist?.name ?: "playlist"
                                    "Removed from $plName"
                                } else {
                                    "Removed from ${toRemove.size} playlists"
                                }
                            }
                            else -> "Changes saved"
                        }

                        withContext(Dispatchers.Main) {
                            CustomSnackbarManager.show(summaryText)
                            onDismiss()
                        }
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.done),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // 2. Subheader row: Left "Saved in" (bold titleMedium), Right "+ New playlist" (accent color)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 2.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Saved in",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            TextButton(onClick = { showNewPlaylistDialog = true }) {
                Text(
                    text = "+ New playlist",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        // 3. Playlists & Liked Songs list
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            // Pinned Top Item: "Liked Songs"
            item(key = "pinned_liked_songs") {
                val isSongLiked = isLiked == true
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isLiked = !isSongLiked }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(ListThumbnailSize)
                            .clip(RoundedCornerShape(ThumbnailCornerRadius))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4A00E0),
                                        Color(0xFF8E2DE2),
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.favorite),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = stringResource(R.string.liked),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = stringResource(R.string.auto_playlist),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    PlaylistSelectionIndicator(isSelected = isSongLiked)
                }
            }

            // User Library Playlists
            items(
                items = userPlaylists,
                key = { it.id },
            ) { playlist ->
                val isSelected = playlist.id in selectedPlaylistIds
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) {
                                selectedPlaylistIds.remove(playlist.id)
                            } else {
                                selectedPlaylistIds.add(playlist.id)
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(ListThumbnailSize)
                            .clip(RoundedCornerShape(ThumbnailCornerRadius)),
                        contentAlignment = Alignment.Center,
                    ) {
                        PlaylistThumbnail(
                            thumbnails = playlist.thumbnails,
                            size = ListThumbnailSize,
                            placeHolder = {
                                Icon(
                                    painter = painterResource(R.drawable.queue_music),
                                    contentDescription = null,
                                    tint = LocalContentColor.current.copy(alpha = 0.7f),
                                    modifier = Modifier.size(ListThumbnailSize / 2),
                                )
                            },
                            shape = RoundedCornerShape(ThumbnailCornerRadius),
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = playlist.playlist.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = pluralStringResource(R.plurals.n_song, playlist.songCount, playlist.songCount),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    PlaylistSelectionIndicator(isSelected = isSelected)
                }
            }
        }
    }

    if (showNewPlaylistDialog) {
        NewPlaylistDialog(
            initialName = targetTitle,
            onDismiss = { showNewPlaylistDialog = false },
            onConfirm = { playlistName ->
                coroutineScope.launch(Dispatchers.IO) {
                    // 1. Ensure song is in DB
                    val existingSong = database.song(targetSongId).firstOrNull()
                    if (existingSong == null) {
                        val meta = mediaMetadata
                            ?: songItem?.toMediaMetadata()
                            ?: song?.toMediaMetadata()
                        if (meta != null) {
                            database.transaction {
                                insert(meta)
                            }
                        }
                    }

                    // 2. Create playlist entity in Room DB
                    val playlistEntity = PlaylistEntity(
                        name = playlistName,
                        bookmarkedAt = LocalDateTime.now(),
                        isEditable = true,
                    )
                    database.query {
                        insert(playlistEntity)
                        insert(
                            PlaylistSongMap(
                                songId = targetSongId,
                                playlistId = playlistEntity.id,
                                position = 0,
                            )
                        )
                    }

                    withContext(Dispatchers.Main) {
                        selectedPlaylistIds.add(playlistEntity.id)
                        initialPlaylistIds.add(playlistEntity.id)
                        CustomSnackbarManager.show("Created playlist $playlistName")
                        showNewPlaylistDialog = false
                    }
                }
            }
        )
    }
}

/**
 * "+ New Playlist" Dialog:
 * - Title: "Give your playlist a name" (centered, bold)
 * - OutlinedTextField pre-filled with song title and text pre-selected (TextRange(0, song.title.length))
 * - Actions: "Cancel" and "Create" (accent-colored button)
 */
@Composable
fun NewPlaylistDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialName,
                selection = TextRange(0, initialName.length),
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Give your playlist a name",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = textFieldValue.text.trim()
                    if (trimmed.isNotEmpty()) {
                        onConfirm(trimmed)
                    }
                },
                enabled = textFieldValue.text.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(24.dp),
    )
}
