package com.archm.player.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.archm.player.constants.HideVideoSongsKey
import com.archm.player.db.MusicDatabase
import com.archm.player.db.entities.ActivityLogEntity
import com.archm.player.db.entities.EventWithSong
import com.archm.player.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class PlayGroup(
    val id: String,
    val parentName: String,
    val parentType: String,
    val parentId: String?,
    val thumbnailUrl: String?,
    val events: List<EventWithSong>,
)

data class DateGroup(
    val dateLabel: String,
    val groups: List<PlayGroup>,
)

@HiltViewModel
class RecentsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val database: MusicDatabase,
) : ViewModel() {

    val recentActivity: StateFlow<List<ActivityLogEntity>> =
        database.activityLogDao.getAll()
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val today = LocalDate.now()
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

    val recentlyPlayed: StateFlow<List<DateGroup>> =
        context.dataStore.data
            .map { (try { it[HideVideoSongsKey] } catch (e: Exception) { null }) ?: false }
            .distinctUntilChanged()
            .flatMapLatest { hideVideoSongs ->
                database.events().map { rawEvents ->
                    val filteredEvents = if (hideVideoSongs) {
                        rawEvents.filter { !it.song.song.isVideo }
                    } else {
                        rawEvents
                    }

                    val byDate = filteredEvents.groupBy { it.event.timestamp.toLocalDate() }
                        .toSortedMap(compareByDescending { it })

                    byDate.map { (date, eventsOnDate) ->
                        val daysAgo = ChronoUnit.DAYS.between(date, today).toInt()
                        val label = when (daysAgo) {
                            0 -> "Today"
                            1 -> "Yesterday"
                            else -> date.format(dateFormatter)
                        }

                        val playGroups = mutableListOf<PlayGroup>()
                        var currentParentName: String? = null
                        var currentParentType: String = "Artist"
                        var currentParentId: String? = null
                        var currentThumbnail: String? = null
                        val currentEvents = mutableListOf<EventWithSong>()

                        fun flushGroup(idx: Int) {
                            if (currentEvents.isNotEmpty()) {
                                playGroups.add(
                                    PlayGroup(
                                        id = "${date}_${currentParentName}_${currentEvents.first().event.id}_$idx",
                                        parentName = currentParentName ?: "Unknown",
                                        parentType = currentParentType,
                                        parentId = currentParentId,
                                        thumbnailUrl = currentThumbnail,
                                        events = currentEvents.toList(),
                                    )
                                )
                                currentEvents.clear()
                            }
                        }

                        for (event in eventsOnDate) {
                            val artist = event.song.artists.firstOrNull()
                            val artistName = artist?.name?.ifBlank { null }
                            val album = event.song.album
                            val albumName = album?.title?.ifBlank { null }

                            val pName = artistName ?: albumName ?: "Unknown Artist"
                            val pType = if (artistName != null) "Artist" else if (albumName != null) "Album" else "Artist"
                            val pId = if (artistName != null) artist?.id else album?.id
                            val pThumb = event.song.thumbnailUrl ?: album?.thumbnailUrl

                            if (currentParentName == pName && currentParentType == pType) {
                                currentEvents.add(event)
                            } else {
                                flushGroup(playGroups.size)
                                currentParentName = pName
                                currentParentType = pType
                                currentParentId = pId
                                currentThumbnail = pThumb
                                currentEvents.add(event)
                            }
                        }
                        flushGroup(playGroups.size)

                        DateGroup(
                            dateLabel = label,
                            groups = playGroups,
                        )
                    }
                }
            }
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun deleteRecentActivity(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            database.activityLogDao.delete(id)
        }
    }

    fun clearAllRecentActivity() {
        viewModelScope.launch(Dispatchers.IO) {
            database.activityLogDao.clearAll()
        }
    }
}
