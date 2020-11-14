package com.example.cameraapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.cameraapp.R
import com.example.cameraapp.databinding.FragmentCameraBinding
import com.example.cameraapp.helper.DialogHelper
import com.example.cameraapp.helper.ImageHelper
import com.example.cameraapp.viewModel.CameraViewModel
import com.thiagodev.camera.CameraXComponent
import kotlinx.android.synthetic.main.fragment_camera.view.*
import java.io.File

class CameraFragment: CameraXComponent<FragmentCameraBinding>(), LifecycleOwner {

    private lateinit var viewModel: CameraViewModel

    override fun getViewBinding() = FragmentCameraBinding.inflate(LayoutInflater.from(this.requireActivity()))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(CameraViewModel::class.java)
    }

    override fun onStart(){
        super.onStart()
        binding.apply{
            initViewFinder(
                this.fragmentTextureView,
                this@CameraFragment.requireContext(),
                getOutputDirectory())
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
        viewModel.apply{
            viewModelFile.observe(viewLifecycleOwner, {
                it?.let{
                    binding.apply{
                        ImageHelper.imageLoader(this.previewBottomImage, it)
                    }
                }
            })
            error.observe(viewLifecycleOwner, {
                it?.let{
                    DialogHelper.showAlert(this@CameraFragment.requireContext(), it.toString())
                }
            })
        }
    }

    private fun initCameraActions(){
        binding.apply{
            previewBottomImage.visibility = View.VISIBLE
            cameraButton.apply{
                visibility = View.VISIBLE
                setOnClickListener{ flipCamera() }
            }
            captureButton.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    takePicture(
                        callback = {
                            it.let{
                                    viewModel.getFileImage(it, updateTransform(binding.fragmentTextureView))
                            }
                        },
                        onError ={

                        }
                    )
                }
            }
        }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = this.requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists()) mediaDir
        else this.requireContext().filesDir
    }
}