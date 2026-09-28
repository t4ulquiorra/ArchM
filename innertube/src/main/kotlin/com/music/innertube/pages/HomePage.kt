package com.music.innertube.pages

import com.music.innertube.models.Album
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.Artist
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.BrowseEndpoint
import com.music.innertube.models.GridRenderer
import com.music.innertube.models.MusicCarouselShelfRenderer
import com.music.innertube.models.MusicShelfRenderer
import com.music.innertube.models.MusicTwoRowItemRenderer
import com.music.innertube.models.PlaylistItem
import com.music.innertube.models.SectionListRenderer
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.models.YTItem
import com.music.innertube.models.oddElements
import com.music.innertube.models.filterExplicit
import com.music.innertube.models.filterVideoSongs
import com.music.innertube.models.extractPlaylistAuthor
import com.music.innertube.models.extractPlaylistSongCount

data class HomePage(
    val chips: List<Chip>?,
    val sections: List<Section>,
    val continuation: String? = null,
) {
    data class Chip(
        val title: String,
        val endpoint: BrowseEndpoint?,
        val deselectEndPoint: BrowseEndpoint?,
    ) {
        companion object {
            fun fromChipCloudChipRenderer(renderer: SectionListRenderer.Header.ChipCloudRenderer.Chip): Chip? {
                return Chip(
                    title = renderer.chipCloudChipRenderer.text?.runs?.firstOrNull()?.text ?: return null,
                    endpoint = renderer.chipCloudChipRenderer.navigationEndpoint.browseEndpoint,
                    deselectEndPoint = renderer.chipCloudChipRenderer.onDeselectedCommand?.browseEndpoint,
                )
            }
        }
    }

    data class Section(
        val title: String,
        val label: String?,
        val thumbnail: String?,
        val endpoint: BrowseEndpoint?,
        val items: List<YTItem>,
    ) {
        val isUnplayableStation: Boolean
            get() = title.equals("Listen together", ignoreCase = true) ||
                title.contains("listen together", ignoreCase = true)

        companion object {
            fun fromMusicCarouselShelfRenderer(renderer: MusicCarouselShelfRenderer): Section? {
                val rawTitle = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs
                    ?.joinToString("") { it.text }?.takeIf { it.isNotBlank() }
                    ?: renderer.header?.musicCarouselShelfBasicHeaderRenderer?.title?.runs?.firstOrNull()?.text
                    ?: return null
                val title = if (rawTitle.startsWith("Similar to ", ignoreCase = true)) {
                    rawTitle.replaceFirst("Similar to ", "More like ", ignoreCase = true)
                } else if (rawTitle.contains("Similar to ", ignoreCase = true)) {
                    rawTitle.replace("Similar to ", "More like ", ignoreCase = true)
                } else {
                    rawTitle
                }
                val label = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.strapline?.runs?.firstOrNull()?.text
                if (title.equals("Listen together", ignoreCase = true) ||
                    title.contains("listen together", ignoreCase = true)
                ) {
                    return null
                }
                val thumbnail = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.thumbnail?.musicThumbnailRenderer?.getThumbnailUrl()
                val endpoint = renderer.header?.musicCarouselShelfBasicHeaderRenderer?.moreContentButton?.buttonRenderer?.navigationEndpoint?.browseEndpoint
                var items = renderer.contents.mapNotNull { content ->
                    content.musicTwoRowItemRenderer?.let { fromMusicTwoRowItemRenderer(it) }
                        ?: content.musicResponsiveListItemRenderer?.let { SearchSummaryPage.fromMusicResponsiveListItemRenderer(it) }
                }
                if (title.equals("Forgotten favorites", ignoreCase = true) ||
                    title.equals("Fresh finds, old favorites", ignoreCase = true)
                ) {
                    items = items.filterNot { item ->
                        item.id == "LM" || item.id == "VLLM" ||
                            (item is PlaylistItem && item.title.equals("Liked Music", ignoreCase = true))
                    }
                }
                if (items.isEmpty()) {
                    return null
                }
                return Section(
                    title = title,
                    label = label,
                    thumbnail = thumbnail,
                    endpoint = endpoint,
                    items = items,
                )
            }

            fun fromMusicShelfRenderer(renderer: MusicShelfRenderer): Section? {
                val rawTitle = renderer.title?.runs
                    ?.joinToString("") { it.text }?.takeIf { it.isNotBlank() }
                    ?: renderer.title?.runs?.firstOrNull()?.text
                    ?: return null
                val title = if (rawTitle.startsWith("Similar to ", ignoreCase = true)) {
                    rawTitle.replaceFirst("Similar to ", "More like ", ignoreCase = true)
                } else if (rawTitle.contains("Similar to ", ignoreCase = true)) {
                    rawTitle.replace("Similar to ", "More like ", ignoreCase = true)
                } else {
                    rawTitle
                }
                if (title.equals("Listen together", ignoreCase = true) ||
                    title.contains("listen together", ignoreCase = true)
                ) {
                    return null
                }
                val endpoint = renderer.bottomEndpoint?.browseEndpoint
                    ?: renderer.moreContentButton?.buttonRenderer?.navigationEndpoint?.browseEndpoint
                val items = renderer.contents.orEmpty().mapNotNull { content ->
                    content.musicResponsiveListItemRenderer?.let {
                        SearchSummaryPage.fromMusicResponsiveListItemRenderer(it)
                    }
                }
                if (items.isEmpty()) return null
                return Section(
                    title = title,
                    label = null,
                    thumbnail = null,
                    endpoint = endpoint,
                    items = items,
                )
            }

            fun fromGridRenderer(renderer: GridRenderer): Section? {
                val title = renderer.header?.gridHeaderRenderer?.title?.runs
                    ?.joinToString("") { it.text }?.takeIf { it.isNotBlank() }
                    ?: renderer.header?.gridHeaderRenderer?.title?.runs?.firstOrNull()?.text
                    ?: return null
                val items = renderer.items.mapNotNull { it.musicTwoRowItemRenderer }.mapNotNull { fromMusicTwoRowItemRenderer(it) }
                if (items.isEmpty()) return null
                return Section(
                    title = title,
                    label = null,
                    thumbnail = null,
                    endpoint = null,
                    items = items,
                )
            }

            private fun fromMusicTwoRowItemRenderer(renderer: MusicTwoRowItemRenderer): YTItem? {
                return when {
                    renderer.isSong -> {
                        val subtitleRuns = renderer.subtitle?.runs?.oddElements() ?: return null
                        val bestThumb = renderer.thumbnailRenderer.musicThumbnailRenderer?.getBestThumbnail()
                        SongItem(
                            id = renderer.navigationEndpoint.watchEndpoint?.videoId ?: return null,
                            title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                            artists = subtitleRuns.filter { run ->
                                run.navigationEndpoint?.browseEndpoint?.browseId?.startsWith("UC") == true ||
                                (run.navigationEndpoint?.browseEndpoint != null && 
                                 run.navigationEndpoint.browseEndpoint.browseId.startsWith("MPREb_") != true)
                            }.map { run ->
                                Artist(
                                    name = run.text,
                                    id = run.navigationEndpoint?.browseEndpoint?.browseId
                                )
                            }.ifEmpty {
                                subtitleRuns.firstOrNull()?.let { run -> 
                                    listOf(Artist(name = run.text, id = null)) 
                                } ?: emptyList()
                            },
                            album = subtitleRuns.firstOrNull { 
                                it.navigationEndpoint?.browseEndpoint?.browseId?.startsWith("MPREb_") == true 
                            }?.let {
                                Album(
                                    name = it.text,
                                    id = it.navigationEndpoint?.browseEndpoint?.browseId ?: return@let null
                                )
                            },
                            duration = null,
                            musicVideoType = renderer.musicVideoType,
                            thumbnail = bestThumb?.normalizedUrl
                                ?: renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl()
                                ?: return null,
                            explicit = renderer.subtitleBadges?.any {
                                it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                            } == true,
                            endpoint = renderer.navigationEndpoint.watchEndpoint,
                            thumbnailWidth = bestThumb?.width,
                            thumbnailHeight = bestThumb?.height,
                        )
                    }
                    renderer.isAlbum -> {
                        val bestThumb = renderer.thumbnailRenderer.musicThumbnailRenderer?.getBestThumbnail()
                        val browseId = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null
                        val playlistId = renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                            ?.musicPlayButtonRenderer?.playNavigationEndpoint
                            ?.watchPlaylistEndpoint?.playlistId
                            ?: renderer.navigationEndpoint.watchPlaylistEndpoint?.playlistId
                            ?: browseId.removePrefix("MPREb_")
                        AlbumItem(
                            browseId = browseId,
                            playlistId = playlistId,
                            title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                            artists = renderer.subtitle?.runs?.oddElements()?.drop(1)?.map {
                                Artist(
                                    name = it.text,
                                    id = it.navigationEndpoint?.browseEndpoint?.browseId
                                )
                            },
                            year = null,
                            thumbnail = bestThumb?.normalizedUrl
                                ?: renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                            explicit = renderer.subtitleBadges?.find {
                                it.musicInlineBadgeRenderer?.icon?.iconType == "MUSIC_EXPLICIT_BADGE"
                            } != null,
                            thumbnailWidth = bestThumb?.width,
                            thumbnailHeight = bestThumb?.height,
                        )
                    }

                    renderer.isPlaylist || renderer.navigationEndpoint.watchPlaylistEndpoint != null -> {
                        val bestThumb = renderer.thumbnailRenderer.musicThumbnailRenderer?.getBestThumbnail()
                        val browseId = renderer.navigationEndpoint.browseEndpoint?.browseId
                            ?: renderer.thumbnailOverlay?.musicItemThumbnailOverlayRenderer?.content
                                ?.musicPlayButtonRenderer?.playNavigationEndpoint?.watchPlaylistEndpoint?.playlistId
                            ?: renderer.navigationEndpoint.watchPlaylistEndpoint?.playlistId
                            ?: return null
                        val playlistId = browseId.removePrefix("VL")
                        val playEndpoint = renderer.thumbnailOverlay
                            ?.musicItemThumbnailOverlayRenderer?.content
                            ?.musicPlayButtonRenderer?.playNavigationEndpoint
                            ?.watchPlaylistEndpoint
                            ?: renderer.navigationEndpoint.watchPlaylistEndpoint
                            ?: WatchEndpoint(playlistId = playlistId)
                        val shuffleEndpoint = renderer.menu?.menuRenderer?.items?.find {
                            it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE"
                        }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint
                        val radioEndpoint = renderer.menu?.menuRenderer?.items?.find {
                            it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
                        }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint

                        PlaylistItem(
                            id = playlistId,
                            title = renderer.title.runs?.firstOrNull()?.text ?: return null,
                            author = renderer.subtitle.extractPlaylistAuthor(),
                            songCountText = renderer.subtitle.extractPlaylistSongCount(),
                            thumbnail = bestThumb?.normalizedUrl
                                ?: renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                            playEndpoint = playEndpoint,
                            shuffleEndpoint = shuffleEndpoint,
                            radioEndpoint = radioEndpoint,
                            thumbnailWidth = bestThumb?.width,
                            thumbnailHeight = bestThumb?.height,
                        )
                    }

                    renderer.isArtist -> {
                        val bestThumb = renderer.thumbnailRenderer.musicThumbnailRenderer?.getBestThumbnail()
                        ArtistItem(
                            id = renderer.navigationEndpoint.browseEndpoint?.browseId ?: return null,
                            title = renderer.title.runs?.lastOrNull()?.text ?: return null,
                            thumbnail = bestThumb?.normalizedUrl
                                ?: renderer.thumbnailRenderer.musicThumbnailRenderer?.getThumbnailUrl() ?: return null,
                            shuffleEndpoint = renderer.menu?.menuRenderer?.items?.find {
                                it.menuNavigationItemRenderer?.icon?.iconType == "MUSIC_SHUFFLE"
                            }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint,
                            radioEndpoint = renderer.menu?.menuRenderer?.items?.find {
                                it.menuNavigationItemRenderer?.icon?.iconType == "MIX"
                            }?.menuNavigationItemRenderer?.navigationEndpoint?.watchPlaylistEndpoint,
                            thumbnailWidth = bestThumb?.width,
                            thumbnailHeight = bestThumb?.height,
                        )
                    }

                    else -> null
                }
            }
        }
    }

    fun filterExplicit(enabled: Boolean = true) =
        if (enabled) {
            copy(sections = sections.map {
                it.copy(items = it.items.filterExplicit())
            })
        } else this

    fun filterVideoSongs(disableVideos: Boolean = false) =
        if (disableVideos) {
            copy(sections = sections.map { section ->
                section.copy(items = section.items.filterVideoSongs(true))
            })
        } else this
}
