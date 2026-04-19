package ru.polyarbeiterz.impressionmap.core.utils

import java.sql.Date
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

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