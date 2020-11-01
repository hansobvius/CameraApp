package com.thiagodev.camera

import android.content.Context
import java.io.File

object FileObject {

    fun createFile(context: Context, path: String): File = File(context.externalMediaDirs.first(), path)

    fun getFile(): String?{
        return null
    }
}