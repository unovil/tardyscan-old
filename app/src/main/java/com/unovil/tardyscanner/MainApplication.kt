package com.unovil.tardyscanner

import android.app.Application
import com.unovil.tardyscanner.koin.initKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        this.initKoin()
    }
}