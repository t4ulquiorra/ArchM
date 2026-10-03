package com.archm.player.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.archm.player.R
import com.archm.player.ui.theme.Marble

val SharedTopBarHeight: Dp = 58.dp

@Composable
fun getSharedTopClearance(): Dp {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    return statusBarTop + SharedTopBarHeight
}

@Composable
fun LargeTitleHeader(
    title: String,
    alpha: Float = 1f,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
    bottomSpacer: Dp = 16.dp,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 34.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White.copy(alpha = alpha),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.15f)),
        )
        Spacer(Modifier.height(bottomSpacer))
    }
}

@Composable
fun FixedTopStrip(
    title: String,
    alpha: Float,
    navController: NavController,
    modifier: Modifier = Modifier,
    showNavigationIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .zIndex(2f)
                .background(backgroundColor)
                .drawBehind {
                    if (alpha > 0.01f) {
                        val strokePx = 1.dp.toPx()
                        val y = size.height - strokePx / 2f
                        drawLine(
                            color = Color.White.copy(alpha = 0.15f * alpha),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = strokePx,
                        )
                    }
                }
                .statusBarsPadding(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = SharedTopBarHeight)
                    .padding(start = if (showNavigationIcon) 4.dp else 16.dp, end = 4.dp),
        ) {
            if (showNavigationIcon) {
                IconButton(onClick = { onBackClick?.invoke() ?: navController.navigateUp() }) {
                    Icon(
                        painter = painterResource(R.drawable.arrow_back),
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = alpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = stringResource(R.string.more_options),
                        tint = Marble,
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.account)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.person),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            navController.navigate("account")
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.history)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.history),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            navController.navigate("history")
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.music_together)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.multi_user),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            navController.navigate("listen_together_from_topbar") {
                                launchSingleTop = true
                            }
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.settings)) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.settings),
                                contentDescription = null,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            navController.navigate("settings")
                        },
                    )
                }
            }
        }
    }
}

data class TopBarTitleAlphas(
    val largeTitleAlpha: Float,
    val smallTitleAlpha: Float,
)

@Composable
fun rememberTopBarTitleAlphas(lazyListState: LazyListState): TopBarTitleAlphas {
    val density = LocalDensity.current
    val largeTitleFadeStartPx = with(density) { 8.dp.toPx() }
    val largeTitleFadeEndPx = with(density) { 46.dp.toPx() }
    val smallTitleFadeStartPx = with(density) { 50.dp.toPx() }
    val smallTitleFadeRangePx = with(density) { 20.dp.toPx() }

    val largeTitleAlpha by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex > 0) {
                0f
            } else {
                val offset = lazyListState.firstVisibleItemScrollOffset.toFloat()
                if (offset <= largeTitleFadeStartPx) {
                    1f
                } else {
                    (1f - (offset - largeTitleFadeStartPx) / (largeTitleFadeEndPx - largeTitleFadeStartPx)).coerceIn(0f, 1f)
                }
            }
        }
    }

    val smallTitleAlpha by remember {
        derivedStateOf {
            if (lazyListState.firstVisibleItemIndex > 0) {
                1f
            } else {
                val offset = lazyListState.firstVisibleItemScrollOffset.toFloat()
                if (offset <= smallTitleFadeStartPx) {
                    0f
                } else {
                    ((offset - smallTitleFadeStartPx) / smallTitleFadeRangePx).coerceIn(0f, 1f)
                }
            }
        }
    }

    return TopBarTitleAlphas(largeTitleAlpha, smallTitleAlpha)
}

@Composable
fun rememberTopBarTitleAlphas(lazyGridState: LazyGridState): TopBarTitleAlphas {
    val density = LocalDensity.current
    val largeTitleFadeStartPx = with(density) { 8.dp.toPx() }
    val largeTitleFadeEndPx = with(density) { 46.dp.toPx() }
    val smallTitleFadeStartPx = with(density) { 50.dp.toPx() }
    val smallTitleFadeRangePx = with(density) { 20.dp.toPx() }

    val largeTitleAlpha by remember {
        derivedStateOf {
            if (lazyGridState.firstVisibleItemIndex > 0) {
                0f
            } else {
                val offset = lazyGridState.firstVisibleItemScrollOffset.toFloat()
                if (offset <= largeTitleFadeStartPx) {
                    1f
                } else {
                    (1f - (offset - largeTitleFadeStartPx) / (largeTitleFadeEndPx - largeTitleFadeStartPx)).coerceIn(0f, 1f)
                }
            }
        }
    }

    val smallTitleAlpha by remember {
        derivedStateOf {
            if (lazyGridState.firstVisibleItemIndex > 0) {
                1f
            } else {
                val offset = lazyGridState.firstVisibleItemScrollOffset.toFloat()
                if (offset <= smallTitleFadeStartPx) {
                    0f
                } else {
                    ((offset - smallTitleFadeStartPx) / smallTitleFadeRangePx).coerceIn(0f, 1f)
                }
            }
        }
    }

    return TopBarTitleAlphas(largeTitleAlpha, smallTitleAlpha)
}

@Composable
fun rememberTopBarTitleAlphas(
    scrollOffset: Float,
    isScrolledAway: Boolean,
): TopBarTitleAlphas {
    val density = LocalDensity.current
    val largeTitleFadeStartPx = with(density) { 8.dp.toPx() }
    val largeTitleFadeEndPx = with(density) { 46.dp.toPx() }
    val smallTitleFadeStartPx = with(density) { 50.dp.toPx() }
    val smallTitleFadeRangePx = with(density) { 20.dp.toPx() }

    val largeTitleAlpha by remember(scrollOffset, isScrolledAway) {
        derivedStateOf {
            if (isScrolledAway) {
                0f
            } else {
                if (scrollOffset <= largeTitleFadeStartPx) {
                    1f
                } else {
                    (1f - (scrollOffset - largeTitleFadeStartPx) / (largeTitleFadeEndPx - largeTitleFadeStartPx)).coerceIn(0f, 1f)
                }
            }
        }
    }

    val smallTitleAlpha by remember(scrollOffset, isScrolledAway) {
        derivedStateOf {
            if (isScrolledAway) {
                1f
            } else {
                if (scrollOffset <= smallTitleFadeStartPx) {
                    0f
                } else {
                    ((scrollOffset - smallTitleFadeStartPx) / smallTitleFadeRangePx).coerceIn(0f, 1f)
                }
            }
        }
    }

    return TopBarTitleAlphas(largeTitleAlpha, smallTitleAlpha)
}
