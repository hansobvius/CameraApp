package com.example.cameraapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.cameraapp.databinding.FragmentCameraBinding
import com.example.cameraapp.viewModel.CameraViewModel
import com.thiagodev.camera.CameraXComponent

class CameraFragment: CameraXComponent<FragmentCameraBinding>(), LifecycleOwner {

    private lateinit var viewModel: CameraViewModel

    override fun getViewBinding() = FragmentCameraBinding.inflate(LayoutInflater.from(this.requireActivity()))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)
    }

    override fun onStart(){
        super.onStart()
        binding.let{ view ->
            initViewFinder(view.fragmentTextureView, this.requireContext())
        }
    }

    override fun onResume(){
        super.onResume()
        this@CameraFragment.apply{
            initCameraActions()
            observerEvents()
        }
    }

    private fun observerEvents(){
        viewModel.viewModelFile.observe(this, Observer {
            it?.let{
                binding.apply{

                }
            }
        })
    }

    private fun initCameraActions(){
        binding.captureButton.setOnClickListener {
            Log.i("TEST", "clicked")
            takePicture()
        }
    }
}