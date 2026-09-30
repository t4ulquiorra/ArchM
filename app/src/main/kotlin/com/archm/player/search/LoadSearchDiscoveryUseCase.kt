/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.search

import androidx.compose.runtime.Immutable
import com.google.common.collect.ImmutableList
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.SongItem
import com.music.innertube.pages.MoodAndGenres
import com.archm.player.repository.SearchDiscoveryData
import com.archm.player.repository.SearchDiscoveryRepository
import javax.inject.Inject

class LoadSearchDiscoveryUseCase
    @Inject
    constructor(
        private val repository: SearchDiscoveryRepository,
    ) {
        suspend operator fun invoke(): Result<SearchDiscoveryUiModel> =
            repository.loadDiscovery().map(::mapToUiModel)

        fun getCached(): SearchDiscoveryUiModel? =
            repository.getCachedDiscovery()?.let(::mapToUiModel)

        private fun mapToUiModel(data: SearchDiscoveryData): SearchDiscoveryUiModel {
            val chartItems = data.chartSections.flatMap { section -> section.items }

            return SearchDiscoveryUiModel(
                moodAndMoments = ImmutableList.copyOf(data.moodAndMoments),
                genres = ImmutableList.copyOf(data.genres),
                moodAndGenres = ImmutableList.copyOf(data.moodAndGenres),
                suggestedSongs =
                    ImmutableList.copyOf(
                        data
                            .suggestedSongs
                            .distinctBy { item -> item.id }
                            .take(MaxDiscoveryItems),
                    ),
                trendingAlbums =
                    ImmutableList.copyOf(
                        (
                            chartItems.filterIsInstance<AlbumItem>() +
                                data.newReleaseAlbums +
                                data.searchedAlbums
                        ).distinctBy { item -> item.id }.take(MaxDiscoveryItems),
                    ),
                suggestedArtists =
                    ImmutableList.copyOf(
                        data
                            .suggestedArtists
                            .distinctBy { item -> item.id }
                            .take(MaxDiscoveryItems),
                    ),
            )
        }

        private companion object {
            const val MaxDiscoveryItems = 12
        }
    }

@Immutable
data class SearchDiscoveryUiModel(
    val moodAndMoments: ImmutableList<MoodAndGenres.Item> = ImmutableList.of(),
    val genres: ImmutableList<MoodAndGenres.Item> = ImmutableList.of(),
    val moodAndGenres: ImmutableList<MoodAndGenres.Item>,
    val suggestedSongs: ImmutableList<SongItem>,
    val trendingAlbums: ImmutableList<AlbumItem>,
    val suggestedArtists: ImmutableList<ArtistItem>,
) {
    val isEmpty: Boolean
        get() = moodAndMoments.isEmpty() && genres.isEmpty() && moodAndGenres.isEmpty() && suggestedSongs.isEmpty() && trendingAlbums.isEmpty() && suggestedArtists.isEmpty()
}
