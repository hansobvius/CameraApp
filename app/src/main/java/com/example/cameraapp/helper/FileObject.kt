package com.example.cameraapp.helper

import android.app.Activity
import android.content.Context
import java.io.File

object FileObject {

    fun createFile(activity: Activity, path: String): File {
        val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
            File(it,path).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else activity.filesDir
    }

}