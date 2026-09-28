package com.archm.player.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.archm.player.db.entities.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int = 15): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ActivityLogEntity): Long

    @Query("SELECT id FROM activity_log WHERE entityId = :entityId AND entityType = :entityType LIMIT 1")
    suspend fun findId(entityId: String, entityType: String): Long?

    @Query("SELECT * FROM activity_log WHERE entityId = :entityId AND entityType = :entityType LIMIT 1")
    suspend fun findEntity(entityId: String, entityType: String): ActivityLogEntity?

    @Transaction
    suspend fun logVisit(
        entityId: String,
        entityType: String,
        title: String,
        subtitle: String? = null,
        thumbnailUrl: String? = null,
        timestamp: Long = System.currentTimeMillis(),
    ) {
        val existing = findEntity(entityId, entityType)
        if (existing != null && (timestamp - existing.timestamp) < 5000L && existing.title == title) {
            return
        }
        val existingId = existing?.id ?: 0L
        insert(
            ActivityLogEntity(
                id = existingId,
                entityId = entityId,
                entityType = entityType,
                title = title,
                subtitle = subtitle,
                thumbnailUrl = thumbnailUrl,
                timestamp = timestamp,
            )
        )
    }

    @Query("DELETE FROM activity_log WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM activity_log WHERE entityId = :entityId AND entityType = :entityType")
    suspend fun deleteByEntity(entityId: String, entityType: String)

    @Query("DELETE FROM activity_log")
    suspend fun clearAll()
}
