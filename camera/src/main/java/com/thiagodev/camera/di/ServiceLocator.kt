package com.thiagodev.camera.di

import com.thiagodev.camera.components.CaptureImage
import com.thiagodev.camera.components.LuminosityAnalyzer
import com.thiagodev.camera.components.PreviewImage

class ServiceLocator {

    val captureImage: CaptureImage = CaptureImage()
    val previewImage: PreviewImage = PreviewImage()
    val luminosityAnalyzer: LuminosityAnalyzer = LuminosityAnalyzer {  }
}