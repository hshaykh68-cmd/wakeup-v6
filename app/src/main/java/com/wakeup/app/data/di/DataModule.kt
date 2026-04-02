package com.wakeup.app.data.di

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.wakeup.app.data.alarm.AlarmSchedulerImpl
import com.wakeup.app.data.local.WakeUpDatabase
import com.wakeup.app.data.repository.AlarmRepositoryImpl
import com.wakeup.app.data.repository.SettingsRepositoryImpl
import com.wakeup.app.data.repository.StatsRepositoryImpl
import com.wakeup.app.data.repository.WakeHistoryRepositoryImpl
import com.wakeup.app.domain.repository.AlarmRepository
import com.wakeup.app.domain.repository.SettingsRepository
import com.wakeup.app.domain.repository.StatsRepository
import com.wakeup.app.domain.repository.WakeHistoryRepository
import com.wakeup.app.data.repository.WidgetStateRepositoryImpl
import com.wakeup.app.data.repository.SleepSoundRepositoryImpl
import com.wakeup.app.data.repository.OEMSettingsRepositoryImpl
import com.wakeup.app.domain.repository.WidgetStateRepository
import com.wakeup.app.domain.repository.SleepSoundRepository
import com.wakeup.app.domain.repository.OEMSettingsRepository
import com.wakeup.app.core.service.HapticsControllerImpl
import com.wakeup.app.domain.service.HapticsController
import com.wakeup.app.core.service.AlarmLabelSuggestionsProviderImpl
import com.wakeup.app.domain.service.AlarmLabelSuggestionsProvider
import com.wakeup.app.domain.usecase.AlarmScheduler
import com.wakeup.app.data.migration.DataStoreMigration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.SecretKey
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        @Named("db_passphrase") passphrase: ByteArray
    ): WakeUpDatabase {
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context,
            WakeUpDatabase::class.java,
            WakeUpDatabase.DATABASE_NAME
        )
        .openHelperFactory(factory)
        .addMigrations(WakeUpDatabase.MIGRATION_1_2, WakeUpDatabase.MIGRATION_2_3, WakeUpDatabase.MIGRATION_3_4)
        .build()
    }

    @Provides
    @Singleton
    @Named("db_passphrase")
    fun provideDatabasePassphrase(@ApplicationContext context: Context): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val keyAlias = "wakeup_db_key"
        
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGen = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore"
            )
            keyGen.init(
                KeyGenParameterSpec.Builder(keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
            )
            keyGen.generateKey()
        }
        
        val key = keyStore.getKey(keyAlias, null) as SecretKey
        // Derive a deterministic passphrase from the hardware key
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(key)
        return mac.doFinal("wakeup_database".toByteArray())
    }

    @Provides
    @Singleton
    fun provideAlarmDao(database: WakeUpDatabase) = database.alarmDao()

    @Provides
    @Singleton
    fun provideWakeHistoryDao(database: WakeUpDatabase) = database.wakeHistoryDao()

    @Provides
    @Singleton
    @Named("settings")
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("wakeup_settings_prefs") }
        )
    }

    @Provides
    @Singleton
    @Named("stats")
    fun provideStatsDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("wakeup_stats_prefs") }
        )
    }

    @Provides
    @Singleton
    fun provideDataStore(@Named("settings") settingsDataStore: DataStore<Preferences>): DataStore<Preferences> {
        return settingsDataStore
    }

    @Provides
    @Singleton
    fun provideAlarmRepository(alarmRepositoryImpl: AlarmRepositoryImpl): AlarmRepository {
        return alarmRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): SettingsRepository {
        return settingsRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideStatsRepository(statsRepositoryImpl: StatsRepositoryImpl): StatsRepository {
        return statsRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideWakeHistoryRepository(wakeHistoryRepositoryImpl: WakeHistoryRepositoryImpl): WakeHistoryRepository {
        return wakeHistoryRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideAlarmScheduler(alarmSchedulerImpl: AlarmSchedulerImpl): AlarmScheduler {
        return alarmSchedulerImpl
    }

    @Provides
    @Singleton
    fun provideWidgetStateRepository(widgetStateRepositoryImpl: WidgetStateRepositoryImpl): WidgetStateRepository {
        return widgetStateRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideSleepSoundRepository(sleepSoundRepositoryImpl: SleepSoundRepositoryImpl): SleepSoundRepository {
        return sleepSoundRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideOEMSettingsRepository(oemSettingsRepositoryImpl: OEMSettingsRepositoryImpl): OEMSettingsRepository {
        return oemSettingsRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideHapticsController(hapticsControllerImpl: HapticsControllerImpl): HapticsController {
        return hapticsControllerImpl
    }

    @Provides
    @Singleton
    fun provideAlarmLabelSuggestionsProvider(
        alarmLabelSuggestionsProviderImpl: AlarmLabelSuggestionsProviderImpl
    ): AlarmLabelSuggestionsProvider {
        return alarmLabelSuggestionsProviderImpl
    }

    @Provides
    @Singleton
    fun provideDataStoreMigration(
        @ApplicationContext context: Context,
        @Named("settings") settingsDataStore: DataStore<Preferences>,
        @Named("stats") statsDataStore: DataStore<Preferences>
    ): DataStoreMigration {
        return DataStoreMigration(context, settingsDataStore, statsDataStore)
    }
}
