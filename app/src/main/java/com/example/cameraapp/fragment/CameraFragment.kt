package com.example.cameraapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.example.cameraapp.camera.CameraComponent
import com.example.cameraapp.databinding.FragmentCameraBinding

class CameraFragment: CameraComponent<FragmentCameraBinding>(), LifecycleOwner {

    private var binding: FragmentCameraBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentCameraBinding.inflate(inflater).apply{
            this.lifecycleOwner = this@CameraFragment
        }
        return binding!!.root
    }

    override fun onStart(){
        super.onStart()
        binding?.let{ view ->
            initViewFinder(view.fragmentTextureView, this.requireContext())
        }
    }

    override fun onResume(){
        super.onResume()
        this.initCameraActions()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun initCameraActions(){
        binding!!.captureButton.setOnClickListener { takePicture() }
    }
}