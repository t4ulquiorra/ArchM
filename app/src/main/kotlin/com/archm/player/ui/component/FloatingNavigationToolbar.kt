/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.ui.component

import android.os.SystemClock
import android.view.ViewConfiguration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.archm.player.ui.screens.Screens
import com.archm.player.ui.theme.DockedDockBackground
import com.archm.player.ui.theme.NavTabSelectedColor
import com.archm.player.ui.theme.NavTabUnselectedColor

val DockedNavBarContentHeight = 56.dp

@Composable
fun FloatingNavigationToolbar(
    items: List<Screens>,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
    isPairedWithMiniPlayer: Boolean = false,
    isSelected: (Screens) -> Boolean,
    onItemClick: (Screens, Boolean) -> Unit,
    onSearchItemDoubleClick: (() -> Unit)? = null,
) {
    DockedNavigationBar(
        items = items,
        pureBlack = pureBlack,
        modifier = modifier,
        isSelected = isSelected,
        onItemClick = onItemClick,
        onSearchItemDoubleClick = onSearchItemDoubleClick,
    )
}

@Composable
fun DockedNavigationBar(
    items: List<Screens>,
    pureBlack: Boolean,
    modifier: Modifier = Modifier,
    isSelected: (Screens) -> Boolean,
    onItemClick: (Screens, Boolean) -> Unit,
    onSearchItemDoubleClick: (() -> Unit)? = null,
) {
    val navContainerColor = if (pureBlack) Color.Black else DockedDockBackground

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = navContainerColor,
        shape = RectangleShape,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DockedNavBarContentHeight),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { screen ->
                    val selected = isSelected(screen)
                    val onDoubleClick = remember(screen, onSearchItemDoubleClick) {
                        if (screen == Screens.Search) onSearchItemDoubleClick else null
                    }
                    val lastClickTime = remember(screen) { mutableLongStateOf(0L) }
                    val onClick = remember(screen, selected, onItemClick, onDoubleClick) {
                        {
                            val currentTime = SystemClock.uptimeMillis()
                            val isDoubleClick = onDoubleClick != null &&
                                currentTime - lastClickTime.longValue <= ViewConfiguration.getDoubleTapTimeout()
                            lastClickTime.longValue = if (isDoubleClick) 0L else currentTime
                            if (isDoubleClick) {
                                onDoubleClick?.invoke()
                                Unit
                            } else {
                                onItemClick(screen, selected)
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 28.dp),
                                onClick = onClick,
                            )
                            .padding(vertical = 4.dp),
                    ) {
                        val iconRes = if (selected) screen.iconIdActive else screen.iconIdInactive
                        val tintColor = if (selected) NavTabSelectedColor else NavTabUnselectedColor

                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = stringResource(screen.titleId),
                            tint = tintColor,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = stringResource(screen.titleId),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                            ),
                            color = tintColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
