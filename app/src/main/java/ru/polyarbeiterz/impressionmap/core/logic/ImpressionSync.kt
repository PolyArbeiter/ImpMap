package ru.polyarbeiterz.impressionmap.core.logic

import ru.polyarbeiterz.impressionmap.core.utils.toLocal
import ru.polyarbeiterz.impressionmap.core.utils.toServerDto
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.data.service.ImpressionBackendService
import ru.polyarbeiterz.impressionmap.data.service.ImpressionService
import ru.polyarbeiterz.impressionmap.data.service.MediaService
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
    val mediaService: MediaService
) {
    suspend fun synchronizeImpressions(now: List<ImpressionLocal>) {
        // get remote and local
        val remote = retrofitService.getAllImpressions().body() ?: emptySet()
        val local = now.map { it.toServerDto() }.toSet()

        // get sync
        val same = remote.intersect(local)

        // set local not sync
        local.filter { imp ->
            !same.contains(imp) && imp.onServer
        }.forEach { imp ->
            retrofitService.createImpression(imp)
        }

        // save remote not sync
        remote.filter { imp ->
            !same.contains(imp)
        }.forEach { imp ->
            impressionService.insertAll(imp.toLocal())
        }
    }
}
