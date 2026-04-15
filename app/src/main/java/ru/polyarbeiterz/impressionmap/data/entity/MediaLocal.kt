package ru.polyarbeiterz.impressionmap.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

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
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
    ) {
    override fun equals(other: Any?) = other is MediaLocal && id == other.id
    override fun hashCode() = id.hashCode()
}

enum class MediaType {
    IMAGE, VIDEO
}

data class ImpressionWithMedia(
    @Embedded val impression: ImpressionLocal,
    @Relation(
        parentColumn = "id",
        entityColumn = "impression_id"
    )
    val mediaItems: List<MediaLocal> = emptyList()
)