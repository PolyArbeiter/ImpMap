package ru.polyarbeiterz.impressionmap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal

@Dao
interface ImpressionDao {
    @Query("SELECT * FROM impressionlocal")
    suspend fun getAll(): List<ImpressionLocal>

    @Query("SELECT * FROM impressionlocal WHERE id in (:impressionIds)")
    suspend fun loadAllByIds(impressionIds: IntArray): List<ImpressionLocal>

    @Insert
    suspend fun insertAll(vararg impressionLocals: ImpressionLocal)

    @Delete
    suspend fun delete(impressionLocal: ImpressionLocal)
}