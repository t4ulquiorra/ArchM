

package com.archm.player.viewmodels

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archm.player.constants.HideExplicitKey
import com.archm.player.db.MusicDatabase
import com.archm.player.utils.dataStore
import com.archm.player.utils.get
import com.archm.player.utils.reportException
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.filterExplicit
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

enum class AlbumReleaseType {
    ALBUM,
    SINGLE,
    EP,
    ;

    companion object {
        fun fromLabel(label: String?): AlbumReleaseType =
            when (label?.trim()?.lowercase(Locale.ROOT)) {
                "single", "singles" -> SINGLE
                "ep", "eps" -> EP
                else -> ALBUM
            }
    }
}

val AlbumItem.releaseType: AlbumReleaseType
    get() = AlbumReleaseType.fromLabel(explicitType)

@Immutable
data class NewReleaseContent(
    val albums: List<AlbumItem>,
    val singles: List<AlbumItem>,
    val eps: List<AlbumItem>,
) {
    val totalReleases: Int
        get() = albums.size + singles.size + eps.size

    val isEmpty: Boolean
        get() = totalReleases == 0
}

sealed interface NewReleaseUiState {
    data object Loading : NewReleaseUiState

    data class Success(
        val content: NewReleaseContent,
    ) : NewReleaseUiState

    data object Empty : NewReleaseUiState

    data object Error : NewReleaseUiState
}

@HiltViewModel
class NewReleaseViewModel
@Inject
constructor(
    @ApplicationContext val context: Context,
    private val database: MusicDatabase,
) : ViewModel() {
    private val _uiState = MutableStateFlow<NewReleaseUiState>(NewReleaseUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _newReleaseAlbums = MutableStateFlow<List<AlbumItem>>(emptyList())
    val newReleaseAlbums = _newReleaseAlbums.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = NewReleaseUiState.Loading
            YouTube
                .newReleaseAlbums()
                .onSuccess { albums ->
                    val artists: MutableMap<Int, String> = mutableMapOf()
                    val favouriteArtists: MutableMap<Int, String> = mutableMapOf()
                    database.allArtistsByPlayTime().first().let { list ->
                        var favIndex = 0
                        for ((artistsIndex, artist) in list.withIndex()) {
                            artists[artistsIndex] = artist.id
                            if (artist.artist.bookmarkedAt != null) {
                                favouriteArtists[favIndex] = artist.id
                                favIndex++
                            }
                        }
                    }
                    val sorted =
                        albums
                            .sortedBy { album ->
                                val artistIds = album.artists.orEmpty().mapNotNull { it.id }
                                val firstArtistKey =
                                    artistIds.firstNotNullOfOrNull { artistId ->
                                        if (artistId in favouriteArtists.values) {
                                            favouriteArtists.entries.firstOrNull { it.value == artistId }?.key
                                        } else {
                                            artists.entries.firstOrNull { it.value == artistId }?.key
                                        }
                                    } ?: Int.MAX_VALUE
                                firstArtistKey
                            }
                            .filterExplicit(context.dataStore.get(HideExplicitKey, false))
                            .distinctBy { it.id }

                    _newReleaseAlbums.value = sorted
                    val content = sorted.toNewReleaseContent()
                    _uiState.value =
                        if (content.isEmpty) {
                            NewReleaseUiState.Empty
                        } else {
                            NewReleaseUiState.Success(content)
                        }
                }
                .onFailure { t ->
                    reportException(t)
                    _uiState.value = NewReleaseUiState.Error
                }
        }
    }

    private fun List<AlbumItem>.toNewReleaseContent(): NewReleaseContent =
        NewReleaseContent(
            albums = filter { it.releaseType == AlbumReleaseType.ALBUM },
            singles = filter { it.releaseType == AlbumReleaseType.SINGLE },
            eps = filter { it.releaseType == AlbumReleaseType.EP },
        )
}
