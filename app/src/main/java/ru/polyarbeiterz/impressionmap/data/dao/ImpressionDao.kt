package ru.polyarbeiterz.impressionmap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal

@Dao
interface ImpressionDao {
    @Query("SELECT * FROM impressionlocal")
    fun getAll(): Flow<List<ImpressionLocal>>

    @Query("SELECT * FROM impressionlocal WHERE id=:impressionId LIMIT 1")
    fun getById(impressionId: Int): Flow<ImpressionLocal>

    @Query("SELECT * FROM impressionlocal WHERE id in (:impressionIds)")
    fun loadAllByIds(impressionIds: IntArray): Flow<List<ImpressionLocal>>

    @Insert
    suspend fun insertAll(vararg impressionLocals: ImpressionLocal)

    @Update
    suspend fun update(impression: ImpressionLocal)

    @Delete
    suspend fun delete(impressionLocal: ImpressionLocal)
}