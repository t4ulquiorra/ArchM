package com.music.innertube.pages

import com.music.innertube.models.AlbumItem
import com.music.innertube.models.MusicTwoRowItemRenderer

data class LibraryAlbumsPage(
    val albums: List<AlbumItem>,
    val continuation: String?,
) {
    companion object {
        fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): AlbumItem? {
            val subtitleRuns = renderer.subtitle?.runs
            val releaseType = subtitleRuns?.firstOrNull()?.text
            val year = subtitleRuns?.lastOrNull()?.text?.toIntOrNull()
            val explicitType = if (releaseType != null && releaseType != year?.toString() && releaseType != "•") releaseType else null
            return AlbumItem(
                browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
                playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                    ?.musicPlayButtonRenderer?.playNavigationEndpoint
                    ?.watchPlaylistEndpoint?.playlistId ?: return null,
                title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                artists = null,
                year = year,
                thumbnail = renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                explicit = renderer.subtitleBadges?.find {
                    it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                } != null,
                explicitType = explicitType,
            )
        }
    }
}
