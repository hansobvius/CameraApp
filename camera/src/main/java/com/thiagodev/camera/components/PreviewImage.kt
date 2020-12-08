package com.thiagodev.camera.components

import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService

class PreviewImage {

    private lateinit var mContext: Context
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var viewFinder: PreviewView? = null
    private var bitLens: Boolean = true
    private lateinit var cameraExecutor: ExecutorService

    fun imageCapture() {
        imageCapture = ImageCapture.Builder().build()
    }

    fun imageAnalyzer() {
        imageAnalyzer = ImageAnalysis.Builder().build().also {
            it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                Log.d(TAG, "Average luminosity: $luma")
            })
        }
    }

    fun openCamera(lifecycleOwner: LifecycleOwner){
        this.imageCapture()
        this.imageAnalyzer()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(mContext)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder().build()

            val cameraSelector = CameraSelector.Builder().requireLensFacing(getLensOfChoice()).build()

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture, imageAnalyzer)

                preview?.setSurfaceProvider(viewFinder!!.createSurfaceProvider(camera?.cameraInfo))
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(mContext))
    }

    private fun getLensOfChoice(): Int =
        if(bitLens) CameraSelector.LENS_FACING_BACK
        else CameraSelector.LENS_FACING_FRONT

    companion object {
        private const val TAG = "CameraApp"
    }
}