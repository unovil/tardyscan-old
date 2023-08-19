package com.unovil.tardyscanner.koin

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun Application.initKoin(additionalConfiguration: KoinApplication.() -> Unit = {}) {
    startKoin {
        androidContext(this@initKoin)
        modules(constantsModule, supabaseModule)
        additionalConfiguration()
    }
}