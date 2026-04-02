package com.wakeup.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.wakeup.app.data.migration.DataStoreMigration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class WakeUpApplication : Application() {

    @Inject
    lateinit var dataStoreMigration: DataStoreMigration

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        
        // Migrate DataStore data from legacy shared store to separate stores
        applicationScope.launch {
            dataStoreMigration.migrateIfNeeded()
        }
        
        // Initialize AdMob on background thread to avoid blocking startup
        applicationScope.launch {
            MobileAds.initialize(this@WakeUpApplication) {}
        }
    }
}
