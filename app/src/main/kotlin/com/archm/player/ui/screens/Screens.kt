

package com.archm.player.ui.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.archm.player.R

@Immutable
sealed class Screens(
    @StringRes val titleId: Int,
    @DrawableRes val iconIdInactive: Int,
    @DrawableRes val iconIdActive: Int,
    val route: String,
) {
    object Home : Screens(
        titleId = R.string.home,
        iconIdInactive = R.drawable.home_outlined,
        iconIdActive = R.drawable.home_filled,
        route = "home",
    )

    object New : Screens(
        titleId = R.string.nav_new,
        iconIdInactive = R.drawable.explore_outlined,
        iconIdActive = R.drawable.explore_outlined,
        route = "explore",
    )

    object Search : Screens(
        titleId = R.string.search,
        iconIdInactive = R.drawable.search_outlined,
        iconIdActive = R.drawable.search_filled,
        route = "search",
    )

    object Mix : Screens(
        titleId = R.string.nav_mix,
        iconIdInactive = R.drawable.sensors,
        iconIdActive = R.drawable.sensors,
        route = "mix",
    )

    object Library : Screens(
        titleId = R.string.filter_library,
        iconIdInactive = R.drawable.library_outlined,
        iconIdActive = R.drawable.library_filled,
        route = "library",
    )

    object ListenTogether : Screens(
        titleId = R.string.together,
        iconIdInactive = R.drawable.group_outlined,
        iconIdActive = R.drawable.group_filled,
        route = "listen_together",
    )

    object MoodAndGenres : Screens(
        titleId = R.string.mood_and_genres,
        iconIdInactive = R.drawable.style,
        iconIdActive = R.drawable.style,
        route = "mood_and_genres",
    )

    object Recents : Screens(
        titleId = R.string.recents,
        iconIdInactive = R.drawable.history,
        iconIdActive = R.drawable.history,
        route = NavRoutes.recents,
    )

    companion object {
        val MainScreens = listOf(Home, New, Search, Mix, Library)
        val SignedOutMainScreens = listOf(Home, New, Search, Library)
        val TvMainScreens = listOf(Home, New, Search, Library)
    }
}
