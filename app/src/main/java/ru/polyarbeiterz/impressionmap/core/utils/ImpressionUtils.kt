package ru.polyarbeiterz.impressionmap.core.utils

import ru.polyarbeiterz.impressionmap.data.dto.ImpressionDto
import ru.polyarbeiterz.impressionmap.data.dto.MediaDto
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

fun ImpressionDto.toLocal(): ImpressionLocal =
    ImpressionLocal(
        latitude = this.latitude,
        longitude = this.longitude,
        date = formatDateForLocal(this.date),
        title = this.title,
        description = this.description,
        onServer = true,
    )

fun ImpressionLocal.toServerDto(
    media: List<MediaDto> = emptyList()
): ImpressionDto =
    ImpressionDto(
        latitude = this.latitude,
        longitude = this.longitude,
        date = formatDateForServer(this.date),
        title = this.title,
        description = this.description,
        onServer = this.onServer,
        media = media,
    )

fun formatDateForServer(timestamp: Long?): String {
    val millis = timestamp ?: System.currentTimeMillis()
    val date = Date(millis)
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format.format(date)
}

fun formatDateForLocal(dateString: String?): Long {
    return try {
        Instant.parse(dateString).toEpochMilli()
    } catch (e: Exception) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val localDateTime = LocalDateTime.parse(dateString, formatter)
        localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }
}