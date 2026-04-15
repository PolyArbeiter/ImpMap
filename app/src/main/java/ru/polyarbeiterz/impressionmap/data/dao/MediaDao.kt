package ru.polyarbeiterz.impressionmap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.polyarbeiterz.impressionmap.data.entity.MediaLocal

@Dao
interface MediaDao {
    @Insert
    suspend fun insert(mediaItem: MediaLocal): Long

    @Delete
    suspend fun delete(mediaItem: MediaLocal)

    @Query("SELECT * FROM media_items WHERE impression_id = :impressionId")
    fun getByImpId(impressionId: Int): Flow<List<MediaLocal>>

    @Query("DELETE FROM media_items WHERE impression_id = :impressionId")
    suspend fun deleteByImpId(impressionId: Int)
}