/*
 * ArchiveTune (2026)
 * © Rukamori — github.com/rukamori
 * GPL-3.0 License | Contributors: see git history
 * Do not remove or alter this notice. - Per GPL-3.0 Section 4 & Section 5
 */

package com.archm.player.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import com.archm.player.db.MusicDatabase
import com.archm.player.db.entities.Artist
import com.archm.player.db.entities.Song
import com.music.innertube.YouTube
import com.music.innertube.models.AlbumItem
import com.music.innertube.models.ArtistItem
import com.music.innertube.models.BrowseEndpoint
import com.music.innertube.models.SongItem
import com.music.innertube.models.WatchEndpoint
import com.music.innertube.pages.ChartsPage
import com.music.innertube.pages.MoodAndGenres
import javax.inject.Inject
import javax.inject.Singleton

data class SearchDiscoveryData(
    val moodAndMoments: List<MoodAndGenres.Item> = emptyList(),
    val genres: List<MoodAndGenres.Item> = emptyList(),
    val moodAndGenres: List<MoodAndGenres.Item>,
    val newReleaseAlbums: List<AlbumItem>,
    val chartSections: List<ChartsPage.ChartSection>,
    val suggestedSongs: List<SongItem>,
    val searchedAlbums: List<AlbumItem>,
    val suggestedArtists: List<ArtistItem>,
)

@Singleton
class SearchDiscoveryRepository
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val database: MusicDatabase,
    ) {
        @Volatile
        private var memoryCachedData: SearchDiscoveryData? = null

        fun getCachedDiscovery(): SearchDiscoveryData? {
            memoryCachedData?.let { return it }
            val diskCached = loadFromDiskCache()
            if (diskCached != null) {
                memoryCachedData = diskCached
            }
            return diskCached
        }

        private fun loadFromDiskCache(): SearchDiscoveryData? {
            return try {
                val file = context.filesDir.resolve(CacheFileName)
                if (!file.exists()) return null
                val jsonStr = file.readText()
                val root = JSONObject(jsonStr)
                val parseItems = { array: JSONArray ->
                    val list = mutableListOf<MoodAndGenres.Item>()
                    for (i in 0 until array.length()) {
                        val obj = array.getJSONObject(i)
                        val browseId = obj.getString("browseId")
                        val params = if (obj.has("params") && !obj.isNull("params")) obj.getString("params") else null
                        val title = obj.getString("title")
                        val stripeColor = obj.optLong("stripeColor", 0L)
                        list.add(
                            MoodAndGenres.Item(
                                title = title,
                                stripeColor = stripeColor,
                                endpoint = BrowseEndpoint(browseId = browseId, params = params),
                            ),
                        )
                    }
                    list
                }
                val moodAndMoments = if (root.has("moodAndMoments")) parseItems(root.getJSONArray("moodAndMoments")) else emptyList()
                val genres = if (root.has("genres")) parseItems(root.getJSONArray("genres")) else emptyList()
                val moodAndGenres = if (root.has("moodAndGenres")) parseItems(root.getJSONArray("moodAndGenres")) else emptyList()

                if (moodAndMoments.isEmpty() && genres.isEmpty() && moodAndGenres.isEmpty()) {
                    null
                } else {
                    SearchDiscoveryData(
                        moodAndMoments = moodAndMoments,
                        genres = genres,
                        moodAndGenres = moodAndGenres,
                        newReleaseAlbums = emptyList(),
                        chartSections = emptyList(),
                        suggestedSongs = emptyList(),
                        searchedAlbums = emptyList(),
                        suggestedArtists = emptyList(),
                    )
                }
            } catch (_: Throwable) {
                null
            }
        }

        private fun saveToDiskCache(data: SearchDiscoveryData) {
            try {
                if (data.moodAndMoments.isEmpty() && data.genres.isEmpty() && data.moodAndGenres.isEmpty()) return
                val root = JSONObject()
                val serializeItems = { items: List<MoodAndGenres.Item> ->
                    val arr = JSONArray()
                    for (item in items) {
                        val obj = JSONObject()
                        obj.put("title", item.title)
                        obj.put("stripeColor", item.stripeColor)
                        obj.put("browseId", item.endpoint.browseId)
                        if (item.endpoint.params != null) {
                            obj.put("params", item.endpoint.params)
                        }
                        arr.put(obj)
                    }
                    arr
                }
                root.put("moodAndMoments", serializeItems(data.moodAndMoments))
                root.put("genres", serializeItems(data.genres))
                root.put("moodAndGenres", serializeItems(data.moodAndGenres))

                val file = context.filesDir.resolve(CacheFileName)
                file.writeText(root.toString())
            } catch (_: Throwable) {
                // Ignore cache write errors
            }
        }
        suspend fun loadDiscovery(): Result<SearchDiscoveryData> =
            withContext(Dispatchers.IO) {
                try {
                    coroutineScope {
                        val moodAndGenresDeferred = async { YouTube.moodAndGenres().getOrNull() }
                        val explorePageDeferred = async { YouTube.explore().getOrNull() }
                        val chartsPageDeferred = async { YouTube.getChartsPage().getOrNull() }
                        val suggestedSongsDeferred = async { loadSuggestedSongs() }
                        val searchedAlbumsDeferred =
                            async {
                                searchItems<AlbumItem>(
                                    query = TopAlbumsQuery,
                                    filter = YouTube.SearchFilter.FILTER_ALBUM,
                                )
                            }
                        val suggestedArtistsDeferred = async { loadSuggestedArtists() }

                        val moodAndGenresResult = moodAndGenresDeferred.await()
                        val explorePage = explorePageDeferred.await()
                        val chartsPage = chartsPageDeferred.await()

                        val moodAndMomentsList = moodAndGenresResult?.find {
                            it.title.contains("Mood", ignoreCase = true) || it.title.contains("moment", ignoreCase = true)
                        }?.items ?: moodAndGenresResult?.getOrNull(0)?.items ?: explorePage?.moodAndGenres.orEmpty()

                        val genresList = moodAndGenresResult?.find {
                            it.title.contains("Genre", ignoreCase = true)
                        }?.items ?: moodAndGenresResult?.getOrNull(1)?.items.orEmpty()

                        val discoveryData =
                            SearchDiscoveryData(
                                moodAndMoments = moodAndMomentsList,
                                genres = genresList,
                                moodAndGenres = explorePage?.moodAndGenres ?: (moodAndMomentsList + genresList),
                                newReleaseAlbums = explorePage?.newReleaseAlbums.orEmpty(),
                                chartSections = chartsPage?.sections.orEmpty(),
                                suggestedSongs = suggestedSongsDeferred.await(),
                                searchedAlbums = searchedAlbumsDeferred.await(),
                                suggestedArtists = suggestedArtistsDeferred.await(),
                            )
                        memoryCachedData = discoveryData
                        saveToDiskCache(discoveryData)
                        Result.success(discoveryData)
                    }
                } catch (throwable: Throwable) {
                    if (throwable is CancellationException) throw throwable
                    Result.failure(throwable)
                }
            }

        private suspend inline fun <reified T> searchItems(
            query: String,
            filter: YouTube.SearchFilter,
        ): List<T> =
            try {
                YouTube
                    .search(
                        query = query,
                        filter = filter,
                    ).getOrThrow()
                    .items
                    .filterIsInstance<T>()
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                emptyList()
            }

        private suspend fun loadSuggestedSongs(): List<SongItem> =
            coroutineScope {
                val seedSongs =
                    database
                        .mostPlayedSongs(
                            fromTimeStamp = AllHistoryTimestamp,
                            limit = MaxHistoryLookupItems,
                        ).first()
                        .filterNot { song -> song.song.isLocal }
                        .take(MaxSuggestionSeedItems)
                val seedSongIds = seedSongs.mapTo(HashSet()) { song -> song.id }

                seedSongs
                    .map { song ->
                        async {
                            loadRelatedSongs(song)
                                .ifEmpty { searchRelatedSongs(song) }
                        }
                    }.awaitAll()
                    .flatten()
                    .filterNot { song -> song.id in seedSongIds }
                    .distinctBy { song -> song.id }
                    .take(MaxSuggestedItems)
            }

        private suspend fun loadRelatedSongs(song: Song): List<SongItem> =
            try {
                val nextResult = YouTube.next(WatchEndpoint(videoId = song.id)).getOrThrow()
                val relatedSongs =
                    nextResult
                        .relatedEndpoint
                        ?.let { endpoint -> YouTube.related(endpoint).getOrNull()?.songs }
                        .orEmpty()
                (relatedSongs + nextResult.items).distinctBy { item -> item.id }
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                emptyList()
            }

        private suspend fun searchRelatedSongs(song: Song): List<SongItem> =
            searchItems(
                query =
                    buildString {
                        append(song.title)
                        song.artists
                            .firstOrNull()
                            ?.name
                            ?.takeIf(String::isNotBlank)
                            ?.let { artistName ->
                                append(' ')
                                append(artistName)
                            }
                    },
                filter = YouTube.SearchFilter.FILTER_SONG,
            )

        private suspend fun loadSuggestedArtists(): List<ArtistItem> =
            coroutineScope {
                val seedArtists =
                    database
                        .mostPlayedArtists(
                            fromTimeStamp = AllHistoryTimestamp,
                            limit = MaxHistoryLookupItems,
                        ).first()
                        .filter { artist -> artist.artist.isYouTubeArtist }
                        .take(MaxSuggestionSeedItems)
                val seedArtistIds = seedArtists.mapTo(HashSet()) { artist -> artist.id }

                seedArtists
                    .map { artist ->
                        async {
                            loadRelatedArtists(artist)
                                .ifEmpty { searchRelatedArtists(artist) }
                        }
                    }.awaitAll()
                    .flatten()
                    .filterNot { artist -> artist.id in seedArtistIds }
                    .distinctBy { artist -> artist.id }
                    .take(MaxSuggestedItems)
            }

        private suspend fun loadRelatedArtists(artist: Artist): List<ArtistItem> =
            try {
                YouTube
                    .artist(artist.id)
                    .getOrThrow()
                    .sections
                    .flatMap { section -> section.items }
                    .filterIsInstance<ArtistItem>()
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                emptyList()
            }

        private suspend fun searchRelatedArtists(artist: Artist): List<ArtistItem> =
            searchItems(
                query = artist.title,
                filter = YouTube.SearchFilter.FILTER_ARTIST,
            )

        private companion object {
            const val AllHistoryTimestamp = 0L
            const val MaxHistoryLookupItems = 36
            const val MaxSuggestionSeedItems = 6
            const val MaxSuggestedItems = 12
            const val TopAlbumsQuery = "top albums"
            const val CacheFileName = "search_discovery_taxonomy.json"
        }
    }
