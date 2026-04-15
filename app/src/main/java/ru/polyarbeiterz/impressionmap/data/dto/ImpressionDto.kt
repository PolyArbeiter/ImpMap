package ru.polyarbeiterz.impressionmap.data.dto

data class ImpressionDto(
    val latitude: Float? = null,
    val longitude: Float? = null,
    val date: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val onServer: Boolean = true,
)

