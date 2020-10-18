package com.example.cameraapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.widget.Toast
import androidx.camera.core.Preview
import androidx.camera.core.PreviewConfig
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

open class CameraComponent<BINDING>: Fragment(), CameraImplementation
        where BINDING: androidx.databinding.ViewDataBinding{

    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    private var isGranted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                Toast.makeText(this@CameraComponent.requireContext(), "Permission not granted", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all{
        ContextCompat.checkSelfPermission(this@CameraComponent.requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }


    override fun openCamera(){
        val previewConfig = PreviewConfig.Builder().apply{
            setTargetResolution(Size(640, 480))
        }.build()

        val preview = Preview(previewConfig)
    }

    companion object{
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}