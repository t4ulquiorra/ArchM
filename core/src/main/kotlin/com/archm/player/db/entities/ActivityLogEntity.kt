package com.archm.player.db.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Immutable
@Entity(
    tableName = "activity_log",
    indices = [
        Index(value = ["entityId", "entityType"], unique = true),
        Index(value = ["timestamp"]),
    ],
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityId: String,
    val entityType: String,
    val title: String,
    val subtitle: String? = null,
    val thumbnailUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)
