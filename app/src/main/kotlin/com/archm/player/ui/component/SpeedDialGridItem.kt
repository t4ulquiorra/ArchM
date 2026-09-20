package com.archm.player.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archm.player.R
import com.archm.player.db.entities.Album
import com.archm.player.db.entities.Artist
import com.archm.player.db.entities.LocalItem
import com.archm.player.db.entities.Playlist
import com.archm.player.db.entities.Song
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.YTItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SpeedDialPodLayout(
    title: String,
    thumbnailUrl: String?,
    isArtist: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    isPinned: Boolean = false,
) {
    val cardShape = RoundedCornerShape(8.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(cardShape)
                .background(Color(0xFF141414))
                .border(1.dp, Color.White.copy(alpha = 0.12f), cardShape)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
    ) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            ItemThumbnail(
                thumbnailUrl = thumbnailUrl,
                isActive = isActive,
                isPlaying = isPlaying,
                shape = if (isArtist) CircleShape else RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp),
                modifier = if (isArtist) Modifier.padding(4.dp).fillMaxSize() else Modifier.fillMaxSize(),
            )
        }

        Text(
            text = title,
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                ),
            color = if (isActive) MaterialTheme.colorScheme.primary else Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 10.dp, end = 8.dp),
        )

        if (isPinned) {
            Icon(
                painter = painterResource(R.drawable.ic_push_pin),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier =
                    Modifier
                        .padding(end = 8.dp)
                        .size(14.dp),
            )
        }
    }
}

@Composable
fun SpeedDialPodItem(
    item: LocalItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    isPinned: Boolean = false,
) {
    val title =
        when (item) {
            is Song -> item.title
            is Album -> item.title
            is Artist -> item.title
            is Playlist -> item.title
        }
    val thumbnailUrl =
        when (item) {
            is Song -> item.song.thumbnailUrl
            is Album -> item.album.thumbnailUrl
            is Artist -> item.artist.thumbnailUrl
            is Playlist -> item.thumbnails.firstOrNull()
        }
    val isArtist = item is Artist

    SpeedDialPodLayout(
        title = title,
        thumbnailUrl = thumbnailUrl,
        isArtist = isArtist,
        onClick = onClick,
        onLongClick = onLongClick,
        isActive = isActive,
        isPlaying = isPlaying,
        isPinned = isPinned,
        modifier = modifier,
    )
}

@Composable
fun SpeedDialPodItem(
    item: YTItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    isPinned: Boolean = false,
) {
    SpeedDialPodLayout(
        title = item.title,
        thumbnailUrl = item.thumbnail,
        isArtist = item is ArtistItem,
        onClick = onClick,
        onLongClick = onLongClick,
        isActive = isActive,
        isPlaying = isPlaying,
        isPinned = isPinned,
        modifier = modifier,
    )
}

@Composable
fun SpeedDialGridItem(
    item: YTItem,
    modifier: Modifier = Modifier,
    isPinned: Boolean = false,
    isActive: Boolean = false,
    isPlaying: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    SpeedDialPodItem(
        item = item,
        onClick = onClick ?: {},
        onLongClick = onLongClick,
        isActive = isActive,
        isPlaying = isPlaying,
        isPinned = isPinned,
        modifier = modifier,
    )
}
