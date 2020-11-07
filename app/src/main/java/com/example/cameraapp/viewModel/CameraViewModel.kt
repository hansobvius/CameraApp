package com.example.cameraapp.viewModel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream

class CameraViewModel: ViewModel() {

    private val _viewModelFile =  MutableLiveData<Bitmap>()
    val viewModelFile: LiveData<Bitmap> get() = _viewModelFile

    private val _test = MutableLiveData<Int>()
    val test: LiveData<Int> get() = _test

    private val scope: CoroutineScope by lazy{
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    fun getFileImage(uri: Uri, fileName: String){
        viewModelScope.launch {
            val file = File(fileName)
            file.exists().also {
                if(it){
                    val bitmapValue: Bitmap = BitmapFactory.decodeFile(fileName)
                    _viewModelFile.value = bitmapValue
                }
            }
        }
    }
}