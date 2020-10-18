package com.example.cameraapp.camera

import android.content.Context
import java.io.File

object FileObject {

    fun inputWrite(context: Context, path: String): File{
        val file = File(context.externalMediaDirs.first(), path)
        return file
    }
}