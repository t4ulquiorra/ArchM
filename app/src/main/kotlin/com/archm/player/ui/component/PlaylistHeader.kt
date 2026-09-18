package com.archm.player.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.archm.player.R

/**
 * Redesigned header with a static blurred atmosphere background and
 * a floating square artwork that smoothly scales down by 50% on scroll
 * before naturally moving off-screen.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PlaylistHeader(
    backdropThumbnail: Any?,
    lazyListState: LazyListState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onBackLongClick: () -> Unit = onBack,
    artworkThumbnail: Any? = backdropThumbnail,
    collapseThreshold: Dp = 140.dp,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundOverlay: (@Composable BoxScope.() -> Unit)? = null,
    artworkOverlay: (@Composable BoxScope.() -> Unit)? = null,
    artworkContent: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.screenWidthDp < configuration.screenHeightDp
    val artworkSize = if (isPortrait) 200.dp else 140.dp
    val density = LocalDensity.current
    val maxScrollPx = with(density) { collapseThreshold.toPx() }

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        // ====================================================================
        // Layer 1: Static Blurred Atmosphere (Background)
        // ====================================================================
        Box(
            modifier = Modifier
                .matchParentSize()
                .clipToBounds(),
        ) {
            AsyncImage(
                model = backdropThumbnail,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(40.dp),
            )

            backgroundOverlay?.invoke(this)

            // Dark scrim overlay: Color.Black.copy(alpha = 0.45f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
            )

            // Smooth vertical fade into pure black at the bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                        ),
                    ),
            )
        }

        // ====================================================================
        // Layer 2: Floating Square Artwork &
        // Layer 3: Scroll-Driven Scaling Physics (Two-Phase Motion)
        // ====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(top = 56.dp, bottom = 20.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        val currentScrollOffset = if (lazyListState.firstVisibleItemIndex > 0) {
                            maxScrollPx
                        } else {
                            lazyListState.firstVisibleItemScrollOffset.toFloat()
                        }
                        val scrollRatio = (currentScrollOffset / maxScrollPx).coerceIn(0f, 1f)
                        val artworkScale = 1.0f - (0.50f * scrollRatio) // Scales 1.0 -> 0.50
                        scaleX = artworkScale
                        scaleY = artworkScale
                        transformOrigin = TransformOrigin(pivotFractionX = 0.5f, pivotFractionY = 1.0f)
                    }
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .size(artworkSize),
            ) {
                if (artworkContent != null) {
                    artworkContent()
                } else {
                    AsyncImage(
                        model = artworkThumbnail ?: backdropThumbnail,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                artworkOverlay?.invoke(this)
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }

        // Floating circular Back button at top-left
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 8.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center,
        ) {
            LongClickIconButton(
                onClick = onBack,
                onLongClick = onBackLongClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_back),
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Floating circular action pill at top-right
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 8.dp)
                .windowInsetsPadding(WindowInsets.statusBars)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.45f)),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

@Composable
fun CollapsingHeader(
    backdropThumbnail: Any?,
    lazyListState: LazyListState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onBackLongClick: () -> Unit = onBack,
    artworkThumbnail: Any? = backdropThumbnail,
    collapseThreshold: Dp = 140.dp,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundOverlay: (@Composable BoxScope.() -> Unit)? = null,
    artworkOverlay: (@Composable BoxScope.() -> Unit)? = null,
    artworkContent: (@Composable BoxScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    PlaylistHeader(
        backdropThumbnail = backdropThumbnail,
        lazyListState = lazyListState,
        onBack = onBack,
        modifier = modifier,
        onBackLongClick = onBackLongClick,
        artworkThumbnail = artworkThumbnail,
        collapseThreshold = collapseThreshold,
        actions = actions,
        backgroundOverlay = backgroundOverlay,
        artworkOverlay = artworkOverlay,
        artworkContent = artworkContent,
        content = content,
    )
}
