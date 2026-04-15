package ru.polyarbeiterz.impressionmap.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.polyarbeiterz.impressionmap.data.database.AppDatabase
import ru.polyarbeiterz.impressionmap.data.database.MIGRATION_1_2

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun getDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "impression_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    fun getImpressionDao(
        database: AppDatabase
    ) = database.impressionDao()

    @Provides
    fun getHostDao(
        database: AppDatabase
    ) = database.hostDao()

    @Provides
    fun getMediaDao(
        database: AppDatabase
    ) = database.mediaDao()

}