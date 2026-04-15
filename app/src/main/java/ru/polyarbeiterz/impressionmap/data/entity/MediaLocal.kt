package ru.polyarbeiterz.impressionmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "media_items",
    foreignKeys = [
        ForeignKey(
            entity = ImpressionLocal::class,
            parentColumns = ["id"],
            childColumns = ["impression_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MediaLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "impression_id") val impressionId: Int,
    @ColumnInfo(name = "file_data") val fileData: ByteArray,
    @ColumnInfo(name = "media_type") val mediaType: MediaType,
    @ColumnInfo(name = "mime_type") val mimeType: String,
) {
    override fun equals(other: Any?) = other is MediaLocal && id == other.id
    override fun hashCode() = id.hashCode()
    enum class MediaType {
        IMAGE, VIDEO
    }
}