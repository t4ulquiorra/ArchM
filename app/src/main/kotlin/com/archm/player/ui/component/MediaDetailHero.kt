package com.archm.player.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.archm.player.R
import com.archm.player.ui.utils.fadingEdge

@Composable
public fun MediaDetailPrimaryActions(
    isAdded: Boolean,
    contentColor: Color,
    contrastingColor: Color,
    @StringRes addContentDescription: Int,
    @StringRes removeContentDescription: Int,
    onShuffle: (() -> Unit)?,
    onPlay: (() -> Unit)?,
    onToggleAdd: (() -> Unit)?,
    modifier: Modifier = Modifier,
    additionalActions: (@Composable RowScope.(Color) -> Unit)? = null,
) {
    val secondaryButtonColors =
        IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = contentColor.copy(alpha = 0.16f),
            contentColor = contentColor,
            disabledContainerColor = contentColor.copy(alpha = 0.08f),
            disabledContentColor = contentColor.copy(alpha = 0.38f),
        )
    val actionScrollState = rememberScrollState()
    val actionScrollMaxValue = actionScrollState.maxValue

    LaunchedEffect(actionScrollMaxValue) {
        if (
            actionScrollMaxValue > 0 &&
            actionScrollMaxValue != Int.MAX_VALUE &&
            actionScrollState.value == 0
        ) {
            actionScrollState.scrollTo(actionScrollMaxValue / 2)
        }
    }

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .widthIn(max = MediaDetailContentMaxWidth),
    ) {
        val actionViewportWidth = maxWidth

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fadingEdge(horizontal = MediaDetailActionEdgeFade)
                    .horizontalScroll(actionScrollState),
        ) {
            MediaDetailBalancedActionLayout(
                actionRowScope = this,
                modifier = Modifier.widthIn(min = actionViewportWidth),
            ) {
                onShuffle?.let { shuffle ->
                    FilledTonalIconButton(
                        onClick = shuffle,
                        shape = CircleShape,
                        colors = secondaryButtonColors,
                        modifier =
                            Modifier
                                .layoutId(MediaDetailActionLayoutId.Shuffle)
                                .size(MediaDetailSecondaryActionSize),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.shuffle),
                            contentDescription = stringResource(R.string.shuffle),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                onPlay?.let { play ->
                    val playButtonHeight = 56.dp
                    Button(
                        onClick = play,
                        shape = RoundedCornerShape(percent = 50),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = contentColor,
                                contentColor = contrastingColor,
                            ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                        modifier =
                            Modifier
                                .layoutId(MediaDetailActionLayoutId.Play)
                                .heightIn(min = playButtonHeight),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.play),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.play),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                onToggleAdd?.let { toggleAdd ->
                    FilledTonalIconButton(
                        onClick = toggleAdd,
                        shape = CircleShape,
                        colors = secondaryButtonColors,
                        modifier =
                            Modifier
                                .layoutId(MediaDetailActionLayoutId.ToggleAdd)
                                .size(MediaDetailSecondaryActionSize),
                    ) {
                        Icon(
                            painter = painterResource(if (isAdded) R.drawable.done else R.drawable.add),
                            contentDescription =
                                stringResource(
                                    if (isAdded) removeContentDescription else addContentDescription,
                                ),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }

                additionalActions?.invoke(this, contentColor)
            }
        }
    }
}

@Composable
private fun MediaDetailBalancedActionLayout(
    actionRowScope: RowScope,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Layout(
        content = { content(actionRowScope) },
        modifier = modifier,
    ) { measurables, constraints ->
        val actionSpacing = MediaDetailActionSpacing.roundToPx()
        val shuffleActionIndex = measurables.indexOfFirst { it.layoutId == MediaDetailActionLayoutId.Shuffle }
        val playActionIndex = measurables.indexOfFirst { it.layoutId == MediaDetailActionLayoutId.Play }
        val toggleAddActionIndex = measurables.indexOfFirst { it.layoutId == MediaDetailActionLayoutId.ToggleAdd }
        val placeables =
            measurables.map { measurable ->
                measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
            }
        val shuffleAction = placeables.getOrNull(shuffleActionIndex)
        val playAction = placeables.getOrNull(playActionIndex)
        val toggleAddAction = placeables.getOrNull(toggleAddActionIndex)
        val otherActions =
            placeables.filterIndexed { index, _ ->
                index != shuffleActionIndex &&
                    index != playActionIndex &&
                    index != toggleAddActionIndex
            }
        val centeredContentWidth =
            placeables.sumOf { it.width } +
                actionSpacing * (placeables.size - 1).coerceAtLeast(0)
        val leftOtherActionCount = otherActions.size / 2
        val leftActions =
            buildList {
                addAll(otherActions.take(leftOtherActionCount))
                if (shuffleAction != null) {
                    add(shuffleAction)
                }
            }
        val rightActions =
            buildList {
                if (toggleAddAction != null) {
                    add(toggleAddAction)
                }
                addAll(otherActions.drop(leftOtherActionCount))
            }
        val leftActionsWidth =
            leftActions.sumOf { it.width } +
                actionSpacing * (leftActions.size - 1).coerceAtLeast(0)
        val rightActionsWidth =
            rightActions.sumOf { it.width } +
                actionSpacing * (rightActions.size - 1).coerceAtLeast(0)
        val balancedContentWidth =
            if (playAction == null) {
                centeredContentWidth
            } else {
                val sideSpacing = if (leftActions.isEmpty() && rightActions.isEmpty()) 0 else actionSpacing
                playAction.width + 2 * (maxOf(leftActionsWidth, rightActionsWidth) + sideSpacing)
            }
        val layoutWidth =
            if (constraints.hasBoundedWidth) {
                constraints.maxWidth
            } else {
                balancedContentWidth.coerceAtLeast(constraints.minWidth)
            }
        val contentHeight = placeables.maxOfOrNull { it.height } ?: 0
        val layoutHeight =
            if (constraints.hasBoundedHeight) {
                contentHeight.coerceIn(constraints.minHeight, constraints.maxHeight)
            } else {
                contentHeight.coerceAtLeast(constraints.minHeight)
            }

        layout(layoutWidth, layoutHeight) {
            if (playAction == null) {
                var actionX = (layoutWidth - centeredContentWidth) / 2
                placeables.forEach { action ->
                    action.placeRelative(
                        x = actionX,
                        y = (layoutHeight - action.height) / 2,
                    )
                    actionX += action.width + actionSpacing
                }
                return@layout
            }

            val playActionX = (layoutWidth - playAction.width) / 2
            var leftActionX = playActionX - actionSpacing - leftActionsWidth
            var rightActionX = playActionX + playAction.width + actionSpacing

            leftActions.forEach { action ->
                action.placeRelative(
                    x = leftActionX,
                    y = (layoutHeight - action.height) / 2,
                )
                leftActionX += action.width + actionSpacing
            }
            rightActions.forEach { action ->
                action.placeRelative(
                    x = rightActionX,
                    y = (layoutHeight - action.height) / 2,
                )
                rightActionX += action.width + actionSpacing
            }
            playAction.placeRelative(
                x = playActionX,
                y = (layoutHeight - playAction.height) / 2,
            )
        }
    }
}

@Composable
public fun MediaDetailAction(
    @StringRes contentDescription: Int,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
    content: @Composable () -> Unit,
) {
    val actionDescription = stringResource(contentDescription)
    val colors =
        if (isDestructive) {
            IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.16f),
                contentColor = MaterialTheme.colorScheme.error,
                disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f),
                disabledContentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.38f),
            )
        } else {
            IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = contentColor.copy(alpha = 0.16f),
                contentColor = contentColor,
                disabledContainerColor = contentColor.copy(alpha = 0.08f),
                disabledContentColor = contentColor.copy(alpha = 0.38f),
            )
        }

    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        colors = colors,
        modifier =
            modifier
                .size(MediaDetailActionSize)
                .semantics { this.contentDescription = actionDescription },
    ) {
        content()
    }
}

@Composable
public fun MediaDetailIconAction(
    @DrawableRes icon: Int,
    @StringRes contentDescription: Int,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDestructive: Boolean = false,
) {
    MediaDetailAction(
        contentDescription = contentDescription,
        contentColor = contentColor,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        isDestructive = isDestructive,
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(22.dp),
        )
    }
}

private val MediaDetailContentMaxWidth = 720.dp
private val MediaDetailActionSpacing = 12.dp
private val MediaDetailActionEdgeFade = 20.dp
private val MediaDetailSecondaryActionSize = 52.dp
private val MediaDetailActionSize = 48.dp

private enum class MediaDetailActionLayoutId {
    Shuffle,
    Play,
    ToggleAdd,
}
