package ru.polyarbeiterz.impressionmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Impression(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "latitude") val latitude: Double? = null,
    @ColumnInfo(name = "longitude") val longitude: Double? = null,
    @ColumnInfo(name = "date") val date: Long? = null,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "on_server") val onServer: Boolean? = null,
    @ColumnInfo(name = "on_localhost") val onLocalhost: Boolean? = null,
    @ColumnInfo(name = "image_path") val imagePath: String? = null
)