package com.example.cameraapp.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

open class CameraComponent<BINDING>: Fragment(), CameraImplementation, LifecycleOwner
        where BINDING: androidx.databinding.ViewDataBinding{

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private var isGranted: Boolean = false
    private var viewFinder: TextureView? = null
    private var mContext: Context? = null

    open fun initViewFinder(view: TextureView, context: Context){
        this.viewFinder = view
        this.mContext = context
        viewFinder.apply{
            when{
                allPermissionsGranted() ->  this!!.post{openCamera()}
                else -> ActivityCompat.requestPermissions(
                        this@CameraComponent.activity!!, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
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

    override fun openCamera(){
        val previewConfig = PreviewConfig.Builder().apply{
            setTargetResolution(Size(640, 480))
        }.build()

        val preview = Preview(previewConfig)

        preview.setOnPreviewOutputUpdateListener{
            viewFinder?.let{ view ->
                val parent = view.parent as ViewGroup
                parent.removeView(view)
                parent.addView(view, 0)
                view.surfaceTexture = it.surfaceTexture
                updateTransform(view)
            }
        }

        // FIXME - npe on usesCaseBind
        this@CameraComponent.let{
            CameraX.bindToLifecycle(this, preview)
        }
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

    companion object{
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}