package com.thiagodev.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

typealias LumaListener = (luma: Double) -> Unit

@SuppressLint("RestrictedApi")
abstract class CameraXComponent<B> : Fragment(), CameraImplementation, LifecycleOwner
        where B: ViewDataBinding{

    private lateinit var mContext: Context
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var viewFinder: PreviewView? = null
    private var bitLens: Boolean = true

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    open lateinit var binding: B

    abstract fun getViewBinding(): B

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = getViewBinding().apply {
            this.lifecycleOwner
        }
        return binding.root
    }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Log.i("TEST", "USER CAMERA PERMISSION: ${requestCode}")
                openCamera()
            } else{
                this@CameraXComponent.requireActivity().finish()
            }
        }
    }

    override fun onStart(){
        super.onStart()
        this.initializeExecutor()
    }

    @SuppressLint("RestrictedApi")
    open fun initViewFinder(view: PreviewView, context: Context, file: File){
        outputDirectory = file
        this.mContext = context
        this.viewFinder = view
        when{
            allPermissionsGranted() -> {
                Log.i("TEST", "initViewFinder isAllPermission: ${allPermissionsGranted()}")
                openCamera()  }
            else -> {
                Log.i("TEST", "Permission not granted}")
                ActivityCompat.requestPermissions(
                    this@CameraXComponent.activity!!,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS)
            }
        }
    }

    /**
     * Preview Image
     */
    override fun openCamera(){
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
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

                preview?.setSurfaceProvider(viewFinder!!.createSurfaceProvider(camera?.cameraInfo))
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(mContext))
    }

    /**
     * Image Analyzes
     */
    override fun imageAnalyzer() {
        imageAnalyzer = ImageAnalysis.Builder().build().also {
                it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                    Log.d(TAG, "Average luminosity: $luma")
                })
            }
    }

    /**
     * Take picture
     */
    override fun takePicture(callback: (file: Uri) -> Unit, onError: (error: ImageCaptureException) -> Unit){
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(mContext), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                    onError(exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    callback(savedUri)
                }
            })
    }

    override fun imageCapture() {
        imageCapture = ImageCapture.Builder().build()
    }

    fun flipCamera(){
        cameraExecutor.let{
            it.shutdownNow()
            bitLens = !bitLens
            openCamera()
        }

    }

    private fun initializeExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun getLensOfChoice(): Int =
        if(bitLens) CameraSelector.LENS_FACING_BACK
        else CameraSelector.LENS_FACING_FRONT

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(this.requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    fun updateTransform(view: PreviewView): Matrix{
        val matrix = Matrix()

        val centerX = view.width / 2f
        val centerY = view.height / 2f

        val rotateDegrees = when(view.display.rotation){
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> -90
            Surface.ROTATION_180 -> -180
            Surface.ROTATION_270 -> -270
            else -> null
        }

        rotateDegrees?.let { rotate -> matrix.postRotate(-90F, centerX, centerY) }

        return matrix
    }

    companion object {
        private const val TAG = "CameraApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}