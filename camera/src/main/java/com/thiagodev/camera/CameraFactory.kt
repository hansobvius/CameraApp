package com.thiagodev.camera

import android.app.Activity
import androidx.fragment.app.Fragment
import com.thiagodev.camera.ui.CameraActivity
import com.thiagodev.camera.ui.CameraFragment

abstract class CameraFactory {

    init{

    }

    inline fun <reified T> factory(): T?{
        return when(T::class){
            is Fragment -> CameraFragment() as T
            is Activity -> CameraActivity() as T
            else -> null
        }
    }
}