/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.Player
import coil3.compose.AsyncImage
import com.archm.player.LocalPlayerConnection
import com.archm.player.R
import com.archm.player.constants.MiniPlayerHeight
import com.archm.player.constants.SwipeSensitivityKey
import com.archm.player.constants.SwipeThumbnailKey
import com.archm.player.extensions.togglePlayPause
import com.archm.player.ui.theme.DockedDockBackground
import com.archm.player.ui.theme.NavTabSelectedColor
import com.archm.player.ui.theme.NavTabUnselectedColor
import com.archm.player.utils.rememberPreference
import kotlin.math.roundToInt

@Composable
fun MiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    isPairedWithNavigation: Boolean = false,
    onClick: () -> Unit = {},
) {
    AppleMusicMiniPlayer(
        position = position,
        duration = duration,
        modifier = modifier,
        pureBlack = pureBlack,
        onClick = onClick,
    )
}

@Composable
fun MiniPlayer(
    positionState: MutableLongState,
    durationState: MutableLongState,
    modifier: Modifier = Modifier,
    pureBlack: Boolean = false,
    isPairedWithNavigation: Boolean = false,
    onClick: () -> Unit = {},
) {
    MiniPlayer(
        position = positionState.longValue,
        duration = durationState.longValue,
        modifier = modifier,
        pureBlack = pureBlack,
        isPairedWithNavigation = isPairedWithNavigation,
        onClick = onClick,
    )
}

@Composable
private fun AppleMusicMiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    pureBlack: Boolean,
    onClick: () -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val layoutDirection = LocalLayoutDirection.current
    val coroutineScope = rememberCoroutineScope()
    val swipeSensitivity by rememberPreference(SwipeSensitivityKey, 0.73f)
    val swipeThumbnail by rememberPreference(SwipeThumbnailKey, true)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsStateWithLifecycle()
    val isPlaying by playerConnection.isPlaying.collectAsStateWithLifecycle()
    val playbackState by playerConnection.playbackState.collectAsStateWithLifecycle()
    val canSkipNext by playerConnection.canSkipNext.collectAsStateWithLifecycle()

    val miniPlayerBg = if (pureBlack) Color.Black else DockedDockBackground

    SwipeableMiniPlayerBox(
        modifier = modifier,
        contentMaxWidth = null,
        swipeSensitivity = swipeSensitivity,
        swipeThumbnail = swipeThumbnail,
        playerConnection = playerConnection,
        layoutDirection = layoutDirection,
        coroutineScope = coroutineScope,
        pureBlack = pureBlack,
        useLegacyBackground = false,
    ) { offsetX ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MiniPlayerHeight)
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .background(miniPlayerBg)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                // Left: Square thumbnail (RoundedCornerShape(6.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(30, 30, 30))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    val thumbnailUrl = mediaMetadata?.thumbnailUrl
                    if (thumbnailUrl != null) {
                        AsyncImage(
                            model = thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Icon(
                            painter = painterResource(R.drawable.music_note),
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Middle: Track title in primary text, artist name in secondary text
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = mediaMetadata?.title.orEmpty(),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = NavTabSelectedColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mediaMetadata?.artists?.joinToString { it.name }.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = NavTabUnselectedColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.basicMarquee(),
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right: Only Play/Pause and Next track action icons (plain vector icons without circular container backgrounds)
                val onPlayPause = remember(playbackState, playerConnection) {
                    {
                        if (playbackState == Player.STATE_ENDED) {
                            playerConnection.player.seekTo(0, 0)
                            playerConnection.player.playWhenReady = true
                        } else {
                            playerConnection.player.togglePlayPause()
                        }
                    }
                }

                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(42.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = NavTabSelectedColor,
                    ),
                ) {
                    Icon(
                        painter = painterResource(
                            when {
                                playbackState == Player.STATE_ENDED -> R.drawable.replay
                                isPlaying -> R.drawable.pause
                                else -> R.drawable.play
                            }
                        ),
                        contentDescription = stringResource(
                            if (playbackState == Player.STATE_ENDED || !isPlaying) R.string.play else R.string.pause
                        ),
                        modifier = Modifier.size(26.dp),
                    )
                }

                IconButton(
                    onClick = { playerConnection.seekToNext() },
                    enabled = canSkipNext,
                    modifier = Modifier.size(42.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = NavTabSelectedColor,
                        disabledContentColor = NavTabSelectedColor.copy(alpha = 0.38f),
                    ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.skip_next),
                        contentDescription = stringResource(R.string.next),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // Divider: A crisp 0.5.dp / 1.dp hairline divider running full width between miniplayer and navbar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(Color.White.copy(alpha = 0.08f)),
            )
        }
    }
}
