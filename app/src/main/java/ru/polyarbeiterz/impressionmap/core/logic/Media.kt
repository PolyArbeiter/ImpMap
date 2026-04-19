package ru.polyarbeiterz.impressionmap.core.logic

import ru.polyarbeiterz.impressionmap.data.dto.MediaResponse
import ru.polyarbeiterz.impressionmap.data.entity.MediaLocal
import ru.polyarbeiterz.impressionmap.data.entity.MediaLocal.MediaType

data class Media(
    val id: Long,
    val fileData: ByteArray,
    val mediaType: MediaType,
    val mimeType: String,
)

fun Media.toLocal(impressionId: Long) =
    MediaLocal(
        impressionId = impressionId.toInt(),
        fileData = this.fileData,
        mediaType = this.mediaType,
        mimeType = this.mimeType
    )
fun MediaLocal.toCore() =
    Media(
        id = this.id.toLong(),
        fileData = this.fileData,
        mediaType = this.mediaType,
        mimeType = this.mimeType
    )

fun MediaResponse.toCore(
    fileData: ByteArray,
    mediaType: MediaType,
    mimeType: String
) =
    Media(
        id = this.id,
        fileData = fileData,
        mediaType = mediaType,
        mimeType = mimeType
    )
