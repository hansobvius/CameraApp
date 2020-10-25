package com.example.cameraapp.camera

import android.content.Context
import java.io.File

object FileObject {

    fun createFile(context: Context, path: String): File{
        val file = File(context.externalMediaDirs.first(), path)
        return file
    }

    fun getFile(): String?{
        return null
    }
}