package ru.polyarbeiterz.impressionmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ImpressionLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "latitude") val latitude: Float? = null,
    @ColumnInfo(name = "longitude") val longitude: Float? = null,
    @ColumnInfo(name = "date") val date: Long? = null,
    @ColumnInfo(name = "description") val description: String? = "",
    @ColumnInfo(name = "title") val title: String? = null,
    @ColumnInfo(name = "on_server") val onServer: Boolean = false,
    @ColumnInfo(name = "image_path") val imagePath: String? = null
)