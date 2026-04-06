package ru.polyarbeiterz.impressionmap.data.service

import kotlinx.coroutines.flow.Flow
import ru.polyarbeiterz.impressionmap.data.dao.ImpressionDao
import ru.polyarbeiterz.impressionmap.data.entity.Host
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImpressionService @Inject constructor(
    private val impDao: ImpressionDao
) {
    fun getAll(): Flow<List<ImpressionLocal>> {
        return impDao.getAll()
    }

    suspend fun insertAll(vararg impressionLocals: ImpressionLocal) {
        impDao.insertAll(*impressionLocals)
    }
}