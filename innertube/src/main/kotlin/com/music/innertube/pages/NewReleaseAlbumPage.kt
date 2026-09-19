package com.music.innertube.pages

import com.music.innertube.models.AlbumItem
import com.music.innertube.models.Artist
import com.music.innertube.models.MusicTwoRowItemRenderer
import com.music.innertube.models.oddElements
import com.music.innertube.models.splitBySeparator

object NewReleaseAlbumPage {
    fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
        val subtitleRuns = renderer.subtitle?.runs
        val releaseTypeCandidate = subtitleRuns?.firstOrNull()?.text
        val year = subtitleRuns?.lastOrNull()?.text?.toIntOrNull()
        val explicitType = subtitleRuns?.map { it.text.trim() }?.firstOrNull { text ->
            text.equals("Single", ignoreCase = true) ||
            text.equals("EP", ignoreCase = true) ||
            text.equals("Album", ignoreCase = true)
        } ?: if (releaseTypeCandidate != null && releaseTypeCandidate != year?.toString() && releaseTypeCandidate != "•") releaseTypeCandidate else null

        return AlbumItem(
            browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
            playlistId =
                renderer.thumbnailOverlay
                    ?.musicItemThumbnailOverlayRenderer
                    ?.content
                    ?.musicPlayButtonRenderer
                    ?.playNavigationEndpoint
                    ?.watchPlaylistEndpoint
                    ?.playlistId ?: return null,
            title =
                renderer.title.runs
                    ?.firstOrNull()
                    ?.text ?: return null,
            artists =
                renderer.subtitle?.runs?.splitBySeparator()?.getOrNull(1)?.oddElements()?.map {
                    Artist(
                        name = it.text,
                        id = it.navigationEndpoint?.browseEndpoint?.browseId,
                    )
                } ?: return null,
            year = year,
            thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
            explicit =
                renderer.subtitleBadges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null,
            explicitType = explicitType,
        )
    }
}
