

package com.archm.player.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.BrowseEndpoint
import com.archm.player.db.MusicDatabase
import com.archm.player.utils.reportException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.archm.player.utils.Wikipedia
import com.archm.player.utils.AppleMusicAboutAlbum
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel
@Inject
constructor(
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val albumId = savedStateHandle.get<String>("albumId")!!
    val playlistId = MutableStateFlow("")
    val albumWithSongs =
        database
            .albumWithSongs(albumId)
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    var otherVersions = MutableStateFlow<List<AlbumItem>>(emptyList())
    var releasesForYou = MutableStateFlow<List<AlbumItem>>(emptyList())
    var moreByArtist = MutableStateFlow<List<AlbumItem>>(emptyList())
    var moreByArtistEndpoint = MutableStateFlow<BrowseEndpoint?>(null)
    var description = MutableStateFlow<String?>(null)
    var descriptionRuns = MutableStateFlow<List<com.music.innertube.models.Run>?>(null)

    private var fetchedArtistId: String? = null

    private fun fetchArtistReleases(artistId: String) {
        if (fetchedArtistId == artistId) return
        fetchedArtistId = artistId
        viewModelScope.launch(Dispatchers.IO) {
            YouTube.artist(artistId).onSuccess { artistPage ->
                val albumsSection = artistPage.sections.firstOrNull { section ->
                    section.title.contains("album", ignoreCase = true)
                } ?: artistPage.sections.firstOrNull { section ->
                    section.items.any { item -> item is AlbumItem }
                }
                val artistAlbums = albumsSection?.items?.filterIsInstance<AlbumItem>()
                    ?: artistPage.sections.flatMap { it.items }.filterIsInstance<AlbumItem>()
                moreByArtist.value = artistAlbums
                moreByArtistEndpoint.value = albumsSection?.moreEndpoint

                val artistEntity = database.getArtistById(artistId)
                if (artistEntity?.thumbnailUrl == null) {
                    database.query {
                        getArtistById(artistId)?.let { currentArtist ->
                            update(currentArtist, artistPage)
                        }
                    }
                }
            }.onFailure { reportException(it) }
        }
    }

    init {
        viewModelScope.launch {
            val album = database.album(albumId).first()
            val initialArtistId = album?.artists?.firstOrNull()?.id
                ?: database.albumWithSongs(albumId).first()?.artists?.firstOrNull()?.id
            if (initialArtistId != null) {
                fetchArtistReleases(initialArtistId)
            }
            if (album?.description != null) {
                description.value = album.description
            }
            YouTube
                .album(albumId)
                .onSuccess {
                    playlistId.value = it.album.playlistId
                    otherVersions.value = it.otherVersions
                    releasesForYou.value = it.releasesForYou
                    if (it.description != null) {
                        description.value = it.description
                    }
                    descriptionRuns.value = it.descriptionRuns
                    database.transaction {
                        if (album == null) {
                            insert(it)
                        } else {
                            update(album.album, it, album.artists)
                        }
                    }

                    val primaryArtistId = it.album.artists?.firstOrNull()?.id ?: initialArtistId
                    if (primaryArtistId != null) {
                        fetchArtistReleases(primaryArtistId)
                    }
                    
                    if (description.value == null && descriptionRuns.value == null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            val artistName = album?.artists?.firstOrNull()?.name 
                                ?: database.albumWithSongs(albumId).first()?.artists?.firstOrNull()?.name
                            val wikiDescription = Wikipedia.fetchAlbumInfo(it.album.title, artistName)
                            if (wikiDescription != null) {
                                description.value = wikiDescription
                                val currentAlbum = database.album(albumId).first()
                                if (currentAlbum != null) {
                                    database.query {
                                        update(currentAlbum.album.copy(description = wikiDescription))
                                    }
                                }
                            } else {
                                val appleDescription = AppleMusicAboutAlbum.fetchAlbumDescription(it.album.title, artistName)
                                if (appleDescription != null) {
                                    description.value = appleDescription
                                    val currentAlbum = database.album(albumId).first()
                                    if (currentAlbum != null) {
                                        database.query {
                                            update(currentAlbum.album.copy(description = appleDescription))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }.onFailure {
                    reportException(it)
                    if (it.message?.contains("NOT_FOUND") == true) {
                        val albumToDelete = album?.album
                        if (albumToDelete != null) {
                            database.query {
                                delete(albumToDelete)
                            }
                        }
                    }
                }
        }
    }
}
