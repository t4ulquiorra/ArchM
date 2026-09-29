

/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarDefaults.TonalElevation
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import com.archm.player.constants.AppBarHeight
import kotlin.math.max

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay

@Composable
fun CyclingSearchPlaceholder(
    modifier: Modifier = Modifier,
) {
    val placeholders = remember {
        listOf(
            "Search for songs...",
            "Search for artists...",
            "Search for albums...",
            "Search for podcasts...",
        )
    }
    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(2800)
            currentIndex = (currentIndex + 1) % placeholders.size
        }
    }

    AnimatedContent(
        targetState = placeholders[currentIndex],
        transitionSpec = {
            (slideInVertically { height -> height / 2 } + fadeIn(tween(400)))
                .togetherWith(slideOutVertically { height -> -height / 2 } + fadeOut(tween(400)))
        },
        label = "cyclingSearchPlaceholder",
        modifier = modifier,
    ) { targetText ->
        Text(
            text = targetText,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@ExperimentalMaterial3Api
@Composable
fun TopSearch(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(10.dp),
    colors: SearchBarColors =
        SearchBarDefaults.colors(
            containerColor = Color(35, 35, 38),
        ),
    tonalElevation: Dp = 0.dp,
    windowInsets: WindowInsets = WindowInsets.statusBars,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    focusRequester: FocusRequester = remember { FocusRequester() },
    leftFocusRequester: FocusRequester? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            if (active) {
                modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
            } else {
                modifier.fillMaxWidth()
            },
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(35, 35, 38),
                contentColor = Color.White,
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.08f)),
                tonalElevation = tonalElevation,
                modifier =
                    Modifier
                        .windowInsetsPadding(windowInsets)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(InputFieldHeight),
            ) {
                SearchBarInputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onSearch,
                    active = active,
                    onActiveChange = onActiveChange,
                    modifier = Modifier.fillMaxWidth().height(InputFieldHeight),
                    enabled = enabled,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    colors =
                        TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White.copy(alpha = 0.7f),
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                    interactionSource = interactionSource,
                    focusRequester = focusRequester,
                    leftFocusRequester = leftFocusRequester,
                )
            }
        }

        if (active) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f),
                content = content,
            )
        }
    }

    BackHandler(enabled = active) {
        onActiveChange(false)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBarInputField(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: (String) -> Unit,
    active: Boolean,
    onActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    colors: TextFieldColors,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    focusRequester: FocusRequester = remember { FocusRequester() },
    leftFocusRequester: FocusRequester? = null,
) {
    val coercedQuery = remember(query) { query.coerceTextRanges() }
    val focused = interactionSource.collectIsFocusedAsState().value
    val textColor =
        LocalTextStyle.current.color.takeOrElse {
            if (focused) colors.focusedTextColor else colors.unfocusedTextColor
        }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .height(InputFieldHeight),
    ) {
        if (leadingIcon != null) {
            Spacer(Modifier.width(SearchBarIconOffsetX))
            leadingIcon()
        }

        BasicTextField(
            value = coercedQuery,
            onValueChange = { onQueryChange(it.coerceTextRanges()) },
            modifier =
                Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .then(
                        if (leftFocusRequester != null) {
                            Modifier.focusProperties {
                                left = leftFocusRequester
                            }
                        } else {
                            Modifier
                        },
                    ).pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(pass = PointerEventPass.Initial)
                            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
                            if (upEvent != null) {
                                onActiveChange(true)
                            }
                        }
                    }.semantics {
                        contentDescription = "Search"
                        if (active) {
                            stateDescription = "Suggestions available"
                        }
                    }.onKeyEvent {
                        if (it.key == Key.Enter) {
                            onSearch(coercedQuery.text)
                            return@onKeyEvent true
                        }
                        false
                    },
            enabled = enabled,
            singleLine = true,
            textStyle = LocalTextStyle.current.merge(TextStyle(color = textColor)),
            cursorBrush = SolidColor(colors.cursorColor),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch(coercedQuery.text) }),
            interactionSource = interactionSource,
            decorationBox = @Composable { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = coercedQuery.text,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    placeholder = placeholder,
                    shape = SearchBarDefaults.inputFieldShape,
                    colors = colors,
                    contentPadding = PaddingValues(),
                    container = {},
                )
            },
        )

        if (trailingIcon != null) {
            trailingIcon()
            Spacer(Modifier.width(SearchBarIconOffsetX))
        }
    }
}

private fun TextFieldValue.coerceTextRanges(): TextFieldValue {
    val textLength = text.length
    val coercedSelection = selection.coerceInText(textLength)
    val coercedComposition = composition?.coerceInText(textLength)
    return if (coercedSelection == selection && coercedComposition == composition) {
        this
    } else {
        copy(selection = coercedSelection, composition = coercedComposition)
    }
}

private fun TextRange.coerceInText(textLength: Int): TextRange =
    TextRange(
        start = minOf(start, end).coerceIn(0, textLength),
        end = maxOf(start, end).coerceIn(0, textLength),
    )

// Measurement specs
val InputFieldHeight = 48.dp
private val SearchBarCornerRadius: Dp = InputFieldHeight / 2
internal val SearchBarVerticalPadding: Dp = 8.dp
internal val SearchBarHorizontalPadding: Dp = 12.dp
val SearchBarIconOffsetX: Dp = 4.dp
private const val AnimationDurationMillis: Int = 300
