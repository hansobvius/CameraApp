package com.thiagodev.camera

import android.net.Uri
import java.io.File

interface CameraImplementation {

    fun openCamera()

    fun imageCapture()

    fun imageAnalyzer()

    fun takePicture(callback: (uri: Uri) -> Unit)
}