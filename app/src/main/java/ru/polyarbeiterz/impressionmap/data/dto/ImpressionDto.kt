package ru.polyarbeiterz.impressionmap.data.dto

data class ImpressionDto(
    val userId: Long? = null,
    val timeCreated: Long? = null,
    val timeModified: Long? = null,
    val latitude: Float? = null,
    val longitude: Float? = null,
    val date: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val onServer: Boolean = true,
)

