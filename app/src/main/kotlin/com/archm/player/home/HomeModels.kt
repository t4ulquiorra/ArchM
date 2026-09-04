package com.archm.player.home

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.google.common.collect.ImmutableList
import com.archm.player.constants.QuickPicks
import com.archm.player.constants.QuickPicksDisplayMode
import com.archm.player.db.entities.LocalItem
import com.archm.player.db.entities.Song
import com.music.innertube.models.PlaylistItem
import com.music.innertube.pages.HomePage
import com.archm.player.models.SimilarRecommendation

sealed interface HomeScreenState {
    data object Loading : HomeScreenState

    @Immutable
    data class Success(
        val uiState: HomeUiState,
    ) : HomeScreenState

    data object Empty : HomeScreenState

    @Immutable
    data class Error(
        @StringRes val messageResId: Int,
    ) : HomeScreenState
}

@Immutable
data class HomeUiState(
    val quickPicks: ImmutableList<Song>,
    val speedDialItems: ImmutableList<LocalItem>,
    val forgottenFavorites: ImmutableList<Song>,
    val keepListening: ImmutableList<LocalItem>,
    val similarRecommendations: ImmutableList<SimilarRecommendation>,
    val accountPlaylists: ImmutableList<PlaylistItem>,
    val homePage: HomePage?,
    val remoteQuickPicks: HomePage.Section?,
    val selectedChip: HomePage.Chip?,
    val accountName: String,
    val accountImageUrl: String?,
    val quickPicksMode: QuickPicks,
    val quickPicksDisplayMode: QuickPicksDisplayMode,
    val showCategoryChips: Boolean,
    val showTonalBackdrop: Boolean,
    val isRefreshing: Boolean,
    val isLoadingMore: Boolean,
)

sealed interface HomeAction {
    data object Refresh : HomeAction

    data class SelectChip(
        val chip: HomePage.Chip?,
    ) : HomeAction

    data class LoadMore(
        val continuation: String?,
    ) : HomeAction
}
