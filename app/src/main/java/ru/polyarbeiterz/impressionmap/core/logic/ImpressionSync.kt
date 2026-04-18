package ru.polyarbeiterz.impressionmap.core.logic

import ru.polyarbeiterz.impressionmap.core.utils.toLocal
import ru.polyarbeiterz.impressionmap.data.dto.ImpressionDto
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.data.service.ImpressionBackendService
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import javax.inject.Inject
import javax.inject.Singleton

fun List<ImpressionLocal>.filterAndSaveImpressionsWithCoords(
    savedImpressionLocals: MutableSet<ImpressionLocal>
) = this.filter { imp ->
        imp.longitude != null &&
        imp.latitude != null
    }.forEach { imp ->
        savedImpressionLocals.add(imp)
    }

@Singleton
class ImpressionSynchronizer @Inject constructor(
    val retrofitService: ImpressionBackendService,
    val impressionService: ImpressionService,
) {

    suspend fun synchronize(
        local: Iterable<ImpressionDto>,
        remote: Iterable<ImpressionDto>
    ) {
        local.filter { !remote.contains(it) && it.onServer }.forEach {
            retrofitService.createImpression(it)
        }

        remote.filter { imp -> !local.filter { it.onServer }.contains(imp) }.forEach {
            impressionService.insertAll(it.toLocal())
        }
    }
}
