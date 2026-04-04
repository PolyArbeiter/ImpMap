package ru.polyarbeiterz.impressionmap.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.polyarbeiterz.impressionmap.data.entity.Host



@Dao
interface HostDao {

    @Query("SELECT * FROM host")
    suspend fun getAll(): List<Host>

    @Insert
    suspend fun insertAll(vararg servers: Host)

}