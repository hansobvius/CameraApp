package com.thiagodev.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@SuppressLint("RestrictedApi")
abstract class CameraXComponent<B>(): Fragment(), CameraImplementation, LifecycleOwner
        where B: ViewDataBinding{

    private var executor: ExecutorService? = null
    private var isGranted: Boolean = false
    private var viewFinder: TextureView? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var analyzerUseCase: ImageAnalysis? = null
    private var mContext: Context? = null
    private var bitLens: Boolean = true

    open lateinit var binding: B

    abstract fun getViewBinding(): B

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = getViewBinding().apply {
            this.lifecycleOwner
        }
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    open fun initViewFinder(view: TextureView, context: Context){
        this.viewFinder = view
        this.mContext = context
        viewFinder.apply{
            when{
                allPermissionsGranted() -> this!!.post{openCamera()}
                else -> ActivityCompat.requestPermissions(this@CameraXComponent.activity!!, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
                )
            }

            this!!.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                updateTransform(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                openCamera()
            }else{
                Toast.makeText(mContext, "Permission not granted", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(mContext!!, it) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Preview Image
     */
    override fun openCamera(){
        CameraX.isInitialized().apply {
            if(this) CameraX.unbindAll()
        }
        this.initializeExecutor()
        this.imageCapture()
        this.imageAnalyzer()
        val previewConfig = PreviewConfig.Builder().apply{
            setTargetResolution(Size(
                (binding as ViewDataBinding).root.width,
                (binding as ViewDataBinding).root.height)
            )
        }.build()

        preview = Preview(previewConfig).also {
            it.setOnPreviewOutputUpdateListener{
                viewFinder?.let{ view ->
                    val parent = view.parent as ViewGroup
                    parent.removeView(view)
                    parent.addView(view, 0)
                    view.setSurfaceTexture(it.surfaceTexture)
                    updateTransform(view)
                }
            }
        }

        this@CameraXComponent.let{
            CameraX.getCameraWithLensFacing(getCameraOfChoice())
            CameraX.bindToLifecycle(this, preview, imageCapture, analyzerUseCase)
        }
    }

    /**
     * Image Analyzes
     */
    override fun imageAnalyzer() {
        val analyzerConfig = ImageAnalysisConfig.Builder().apply{
            setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        this.analyzerUseCase = ImageAnalysis(analyzerConfig).apply{
            setAnalyzer(executor!!, LuminosityAnalyzer())
        }
    }

    /**
     * Take picture
     */
    override fun takePicture(){
        val file = FileObject.createFile(mContext!!, "${System.currentTimeMillis()}.jpg")

        imageCapture!!.takePicture(file, executor!!, object : ImageCapture.OnImageSavedListener {

                override fun onError(
                    imageCaptureError: ImageCapture.ImageCaptureError,
                    message: String,
                    exc: Throwable?) {
                    val msg = "Photo capture failed: $message"
                    Log.e("CameraXApp", msg, exc)
                    viewFinder!!.post {
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onImageSaved(file: File) {
                    val msg = "Photo capture succeeded: ${file.absolutePath}"
                    Log.d("CameraXApp", msg)
                    viewFinder!!.post {
                        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    override fun imageCapture() {
        val imageCaptureConfig = ImageCaptureConfig.Builder().apply{
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
        }.build()
        this.imageCapture = ImageCapture(imageCaptureConfig)
    }

    private fun updateTransform(viewFinder: TextureView){
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

    fun flipCamera(){
        bitLens = !bitLens
        executor!!.shutdownNow()
        openCamera()
    }

    private fun initializeExecutor() {
        executor = Executors.newSingleThreadExecutor()
    }

    private fun getCameraOfChoice(): CameraX.LensFacing{
        var lens: CameraX.LensFacing? = null
        if(bitLens){
            lens = CameraX.LensFacing.FRONT
        }else{
            lens = CameraX.LensFacing.BACK
        }
        return lens
    }

    companion object{
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}