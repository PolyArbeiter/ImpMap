package ru.polyarbeiterz.impressionmap.data.service

import ru.polyarbeiterz.impressionmap.data.dao.HostDao
import ru.polyarbeiterz.impressionmap.data.entity.Host
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HostService @Inject constructor(
    private val hostDao: HostDao
) {
    suspend fun getAll(): List<Host> {
        return hostDao.getAll()
    }

    suspend fun insertAll(vararg hosts: Host) {
        hostDao.insertAll(*hosts)
    }
}