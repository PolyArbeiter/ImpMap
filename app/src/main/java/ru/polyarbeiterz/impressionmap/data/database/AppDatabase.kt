package ru.polyarbeiterz.impressionmap.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.polyarbeiterz.impressionmap.data.dao.HostDao
import ru.polyarbeiterz.impressionmap.data.dao.ImpressionDao
import ru.polyarbeiterz.impressionmap.data.dao.MediaDao
import ru.polyarbeiterz.impressionmap.data.entity.Host
import ru.polyarbeiterz.impressionmap.data.entity.ImpressionLocal
import ru.polyarbeiterz.impressionmap.data.entity.MediaLocal

@Database(
    entities = [ImpressionLocal::class, MediaLocal::class, Host::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun impressionDao(): ImpressionDao
    abstract fun hostDao(): HostDao
    abstract fun mediaDao(): MediaDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS `host` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `name` TEXT NOT NULL,
                `ip` TEXT NOT NULL,
                `port` INTEGER NOT NULL
            )
        """.trimIndent())
    }
}