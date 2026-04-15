package ru.polyarbeiterz.impressionmap.data.service

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import ru.polyarbeiterz.impressionmap.data.dao.MediaDao
import ru.polyarbeiterz.impressionmap.data.entity.MediaLocal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaService @Inject constructor(
    private val mediaDao: MediaDao
) {
    fun getByImpressionId(impressionId: Int): Flow<List<MediaLocal>> {
        return mediaDao.getByImpId(impressionId)
            .catch { e ->
                Log.e("DATABASE", "Error reading media for impression ID=$impressionId", e)
            }
    }

    suspend fun insert(mediaItem: MediaLocal): Long {
        return mediaDao.insert(mediaItem)
    }

    suspend fun insertAll(vararg mediaItems: MediaLocal) {
        mediaItems.forEach { mediaDao.insert(it) }
    }

    suspend fun delete(mediaItem: MediaLocal) {
        mediaDao.delete(mediaItem)
    }

    suspend fun deleteByImpressionId(impressionId: Int) {
        mediaDao.deleteByImpId(impressionId)
    }
}