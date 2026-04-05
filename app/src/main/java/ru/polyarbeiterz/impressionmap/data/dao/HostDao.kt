package ru.polyarbeiterz.impressionmap.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.polyarbeiterz.impressionmap.data.entity.Host



@Dao
interface HostDao {

    @Query("SELECT * FROM host")
    fun getAll(): Flow<List<Host>>

    @Insert
    suspend fun insertAll(vararg servers: Host)

    @Delete
    suspend fun deleteAll(vararg servers: Host)

}