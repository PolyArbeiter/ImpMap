package ru.polyarbeiterz.impressionmap.data.dto
data class ImpressionResponse(
    val localId: Long,
    val userId: Long,
    val title: String,
    val description: String,
    val date: String,
    val latitude: Float,
    val longitude: Float,
    val media: List<MediaResponse> = emptyList()
)

