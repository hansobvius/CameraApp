package com.thiagodev.camera

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

    private var lastAnalyzedTimestamp = 0L

    fun ByteBuffer.toByteArray(): ByteArray{
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalyzedTimestamp >=
            TimeUnit.SECONDS.toMillis(1)) {
            val buffer = image.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()
            Log.d("CameraXApp", "Average luminosity: $luma")
            listener(luma)
            lastAnalyzedTimestamp = currentTimestamp
        }
    }
}