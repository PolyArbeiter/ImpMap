package ru.polyarbeiterz.impressionmap.data.service

import kotlinx.coroutines.flow.Flow
import ru.polyarbeiterz.impressionmap.data.dao.HostDao
import ru.polyarbeiterz.impressionmap.data.entity.Host
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostService @Inject constructor(
    private val hostDao: HostDao
) {
    fun getAll(): Flow<List<Host>> {
        return hostDao.getAll()
    }

    suspend fun insertAll(vararg hosts: Host) {
        hostDao.insertAll(*hosts)
    }

    suspend fun deleteAll(vararg hosts: Host) {
        hostDao.deleteAll(*hosts)
    }
}