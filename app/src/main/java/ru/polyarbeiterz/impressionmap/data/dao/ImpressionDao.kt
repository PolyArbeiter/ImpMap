package ru.polyarbeiterz.impressionmap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ru.polyarbeiterz.impressionmap.data.entity.Impression

@Dao
interface ImpressionDao {
    @Query("SELECT * FROM impression")
    suspend fun getAll(): List<Impression>

    @Query("SELECT * FROM impression WHERE id in (:impressionIds)")
    suspend fun loadAllByIds(impressionIds: IntArray): List<Impression>

    @Insert
    suspend fun insertAll(vararg impressions: Impression)

    @Delete
    suspend fun delete(impression: Impression)
}