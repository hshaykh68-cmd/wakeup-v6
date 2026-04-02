package com.wakeup.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.wakeup.app.data.local.dao.AlarmDao
import com.wakeup.app.data.local.dao.SleepDao
import com.wakeup.app.data.local.dao.WakeHistoryDao
import com.wakeup.app.data.local.entity.AlarmEntity
import com.wakeup.app.data.local.entity.SleepSession
import com.wakeup.app.data.local.entity.WakeHistoryEntity

@Database(
    entities = [AlarmEntity::class, WakeHistoryEntity::class, SleepSession::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class WakeUpDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun wakeHistoryDao(): WakeHistoryDao
    abstract fun sleepDao(): SleepDao

    companion object {
        const val DATABASE_NAME = "wakeup_database"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE alarms ADD COLUMN photoReferencePath TEXT")
                database.execSQL("ALTER TABLE alarms ADD COLUMN photoReferenceHash TEXT")
                database.execSQL("ALTER TABLE alarms ADD COLUMN barcodeValue TEXT")
                database.execSQL("ALTER TABLE alarms ADD COLUMN barcodeFormat INTEGER")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add pending_intent_id column with default value 0
                database.execSQL("ALTER TABLE alarms ADD COLUMN pending_intent_id INTEGER NOT NULL DEFAULT 0")
                // Create index for faster lookup
                database.execSQL("CREATE INDEX IF NOT EXISTS index_alarms_pending_intent_id ON alarms(pending_intent_id)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS sleep_sessions (
                    id TEXT PRIMARY KEY NOT NULL,
                    alarm_id TEXT,
                    start_time INTEGER NOT NULL,
                    end_time INTEGER,
                    sleep_duration_minutes INTEGER NOT NULL DEFAULT 0,
                    deep_sleep_minutes INTEGER NOT NULL DEFAULT 0,
                    light_sleep_minutes INTEGER NOT NULL DEFAULT 0,
                    awake_minutes INTEGER NOT NULL DEFAULT 0,
                    sleep_quality_score REAL NOT NULL DEFAULT 0,
                    smart_wake_used INTEGER NOT NULL DEFAULT 0,
                    smart_wake_triggered INTEGER NOT NULL DEFAULT 0,
                    woke_in_smart_window INTEGER NOT NULL DEFAULT 0,
                    snooze_count INTEGER NOT NULL DEFAULT 0,
                    mission_completed INTEGER NOT NULL DEFAULT 0,
                    notes TEXT,
                    created_at INTEGER NOT NULL
                )""")
            }
        }
    }
}
