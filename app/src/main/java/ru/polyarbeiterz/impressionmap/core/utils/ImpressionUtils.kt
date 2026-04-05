package ru.polyarbeiterz.impressionmap.core.utils

import ru.polyarbeiterz.impressionmap.data.dto.ImpressionDto
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal

fun ImpressionDto.toLocal(): ImpressionLocal =
    ImpressionLocal(
        latitude = this.latitude,
        longitude = this.longitude,
        title = this.title,
        description = this.description,
        onServer = true,
    )

fun ImpressionLocal.toServerDto(): ImpressionDto =
    ImpressionDto(
        latitude = this.latitude,
        longitude = this.longitude,
        title = this.title,
        description = this.description,
        onServer = this.onServer
    )