package ru.polyarbeiterz.impressionmap.core.logic

import ru.polyarbeiterz.impressionmap.data.dto.ImpressionResponse
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal

data class Impression(
    val localId: Long,
    val userId: Long?,
    val latitude: Float,
    val longitude: Float,
    val date: Long,
    val title: String,
    val description: String,
    val onServer: Boolean,
    val media: List<Media> = emptyList()
    //    val timeCreated: Long? = null,
    //    val timeModified: Long? = null,
)

fun Impression.toLocal() =
    ImpressionLocal(
        id = this.localId.toInt(),
        userId = this.userId?.toInt(),
        latitude = this.latitude,
        longitude = this.longitude,
        title = this.title,
        description = this.description,
        date = this.date,
        onServer = this.onServer
    )

fun ImpressionResponse.toCore(mediaList: List<Media> = emptyList()): Impression {

    return Impression(
        localId = this.localId,
        userId = this.userId,
        latitude = this.latitude,
        longitude = this.longitude,
        date = this.date,
        title = this.title,
        description = this.description,
        onServer = true,
        media = mediaList
    )
}

fun ImpressionLocal.toCore(mediaList: List<Media> = emptyList()): Impression {

    return Impression(
        localId = this.id.toLong(),
        userId = null,
        latitude = this.latitude!!,
        longitude = this.longitude!!,
        date = this.date!!,
        title = this.title!!,
        description = this.description!!,
        onServer = this.onServer,
        media = mediaList
    )
}
