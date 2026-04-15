package ru.polyarbeiterz.impressionmap.data.dto

import ru.polyarbeiterz.impressionmap.data.entity.MediaLocal
import ru.polyarbeiterz.impressionmap.data.entity.MediaLocal.MediaType

data class MediaDto(
    val id: Int,
    val impressionId: Int,
    val fileData: ByteArray,
    val mediaType: MediaType,
    val mimeType: String
) {
    override fun equals(other: Any?) = other is MediaLocal && id == other.id
    override fun hashCode() = id.hashCode()
}

