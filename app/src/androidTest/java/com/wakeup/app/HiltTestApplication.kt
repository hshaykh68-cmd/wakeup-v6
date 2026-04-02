package com.wakeup.app

import android.app.Application
import dagger.hilt.android.testing.CustomTestApplication

/**
 * Custom test application for Hilt instrumentation tests.
 */
@CustomTestApplication(WakeUpApplication::class)
interface HiltTestApplication
