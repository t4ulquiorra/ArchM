package com.archm.player.playback

import com.archm.player.db.entities.SongEntity

interface ISyncUtils {
    fun likeSong(song: SongEntity)
}
