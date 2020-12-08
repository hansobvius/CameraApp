package com.thiagodev.camera

import android.app.Application
import com.thiagodev.camera.di.ServiceLocator

class CameraApplication: Application() {

    lateinit var service: ServiceLocator

    override fun onCreate() {
        super.onCreate()
        service = ServiceLocator()
    }
}