package ru.polyarbeiterz.impressionmap.core.utils

import android.graphics.Bitmap
import androidx.core.graphics.scale
import java.io.ByteArrayOutputStream
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

fun compressImageToByteArray(
    bitmap: Bitmap,
    quality: Int,
    maxWidth: Int = 1024,
    maxHeight: Int = 1024
): ByteArray {
    // Scale down if too large
    val scaledBitmap = if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
        val ratio = minOf(
            maxWidth.toFloat() / bitmap.width,
            maxHeight.toFloat() / bitmap.height
        )
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()
        bitmap.scale(newWidth, newHeight)
    } else {
        bitmap
    }

    val stream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}