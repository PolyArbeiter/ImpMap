package ru.polyarbeiterz.impressionmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "host",
    indices = [
        Index(value = ["ip", "port"], unique = true)
    ]
)
data class Host(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String? = null,
    @ColumnInfo(name = "ip") val ip: String? = null,
    @ColumnInfo(name = "port") val port: Int? = null,
)
