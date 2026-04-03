package ru.polyarbeiterz.impressionmap.data.service

import ru.polyarbeiterz.impressionmap.data.dao.ImpressionDao
import ru.polyarbeiterz.impressionmap.data.entity.Impression
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImpressionService @Inject constructor(
    private val impDao: ImpressionDao
) {
    suspend fun getAll(): List<Impression> {
        return impDao.getAll()
    }

    suspend fun insertAll(vararg impressions: Impression) {
        impDao.insertAll(*impressions)
    }
}