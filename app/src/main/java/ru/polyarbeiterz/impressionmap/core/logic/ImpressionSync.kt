package ru.polyarbeiterz.impressionmap.core.logic

import ru.polyarbeiterz.impressionmap.core.service.ImpressionCoreService
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
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
    val impressionCoreService: ImpressionCoreService
) {
    suspend fun synchronizeImpressions() {
        // get remote and local
        val remote = impressionCoreService.getRemoteAll(withMedia = true)
        val local = impressionCoreService.getLocalAll(withMedia = true)

        // intersect by id
        val inCommon = remote.map { it.localId }.intersect(local.map { it.localId })

        // update existing remote and local
        inCommon.forEach { id ->
            val localById = local.find{imp -> imp.localId == id} ?: return@forEach
            val remoteById = remote.find{imp -> imp.localId == id} ?: return@forEach
            // TODO(update logic)
        }

        // send local to server
        local
            .filter { imp -> !inCommon.contains(imp.localId) && imp.onServer }
            .forEach {
                impressionCoreService.createImpressionWithMediaRemote(it)
            }

        // send remote to local
        remote
            .filter { !inCommon.contains(it.localId) }
            .forEach { impressionCoreService.createImpressionWithMediaLocal(it) }
    }
}
