package com.example.cameraapp.viewModel

import android.graphics.Bitmap
import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.io.BufferedOutputStream
import java.io.File

class CameraViewModel: ViewModel() {

    private val _ViewModelFile =  MutableLiveData<Bitmap>()
    val viewModelFile: LiveData<Bitmap> get() = _ViewModelFile

    private val scope: CoroutineScope by lazy{
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}