package com.thiagodev.camera

import android.net.Uri
import androidx.camera.core.ImageCaptureException

interface CameraImplementation {

    fun openCamera()

    fun imageCapture()

    fun imageAnalyzer()

    fun takePicture(callback: (uri: Uri) -> Unit, onError: (error: ImageCaptureException) -> Unit)
}