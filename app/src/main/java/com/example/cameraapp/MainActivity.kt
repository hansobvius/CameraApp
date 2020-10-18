package com.example.cameraapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.cameraapp.camera.LuminosityAnalyzer
import java.io.File
import java.util.concurrent.Executors

private const val REQUEST_CODE_PERMISSIONS = 10

private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private val executor = Executors.newSingleThreadExecutor()
    private val luminosityAnalyzer = LuminosityAnalyzer()
    private lateinit var viewFinder: TextureView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewFinder = findViewById(R.id.view_finder)

        if(allPermissionsGranted()){
            viewFinder.post{startCamera()}
        }else{
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                viewFinder.post{startCamera()}
            }else{
                Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera(){
        val previewConfig = PreviewConfig.Builder().apply{
            setTargetResolution(Size(640, 480))
        }.build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener{
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        val imageCaptureConfig = ImageCaptureConfig.Builder().apply{
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
        }.build()

        val imageCapture = ImageCapture(imageCaptureConfig)
        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
            val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")

            imageCapture.takePicture(file, executor,
                object : ImageCapture.OnImageSavedListener {
                    override fun onError(
                        imageCaptureError: ImageCapture.ImageCaptureError,
                        message: String,
                        exc: Throwable?) {
                            val msg = "Photo capture failed: $message"
                            Log.e("CameraXApp", msg, exc)
                            viewFinder.post {
                                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onImageSaved(file: File) {
                        val msg = "Photo capture succeeded: ${file.absolutePath}"
                        Log.d("CameraXApp", msg)
                        viewFinder.post {
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                })
        }

        val analyzerConfig = ImageAnalysisConfig.Builder().apply{
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply{
            setAnalyzer(executor, luminosityAnalyzer)
        }

        CameraX.bindToLifecycle(this, preview, imageCapture, analyzerUseCase)
    }

    private fun updateTransform(){
        val matrix = Matrix()

        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        val rotateDegrees = when(viewFinder.display.rotation){
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }

        matrix.postRotate(-rotateDegrees.toFloat(), centerX, centerY)

        viewFinder.setTransform(matrix)
    }
}



