package com.example.cameraapp.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.io.File

class CameraViewModel: ViewModel() {

    private val _viewModelFile =  MutableLiveData<Bitmap>()
    val viewModelFile: LiveData<Bitmap> get() = _viewModelFile

    private val scope: CoroutineScope by lazy{
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    fun getFileImage(uri: Uri){
        viewModelScope.launch {
            val file = File(uri.path!!)
            file.exists().also {
                if(it){
                    val bitmapValue: Bitmap? = BitmapFactory.decodeFile(uri.path)
                    bitmapValue?.let { bitmap -> _viewModelFile.value = bitmap }
                }
            }
        }
    }
}