@file:Suppress("BlockingMethodInNonBlockingContext")

package com.example.cameraapp.viewModel

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.impl.utils.Exif
import androidx.lifecycle.*
import kotlinx.coroutines.*
import java.io.File
import java.io.IOException

class CameraViewModel: ViewModel() {

    private val _viewModelFile =  MutableLiveData<Bitmap>()
    val viewModelFile: LiveData<Bitmap> get() = _viewModelFile

    private val _error = MutableLiveData<ImageCaptureException>()
    val error: LiveData<ImageCaptureException> get() = _error

    private val _bitmapError = MutableLiveData<Boolean>()
    val bitmapError: LiveData<Boolean> get() = _bitmapError

    private val scope: CoroutineScope by lazy{
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    @SuppressLint("RestrictedApi")
    fun getFileImage(uri: Uri, imageMatrix: Matrix){
        viewModelScope.launch {
            val file = File(uri.path!!)
            file.exists().also {
                if(it){
                    try{
                        val bitmapValue: Bitmap? = BitmapFactory.decodeFile(uri.path)
                        val bitmap = createBitmap(
                            bitmapValue!!,
                            X_PIXEL,
                            Y_PIXEL,
                            bitmapValue.width,
                            bitmapValue.height,
                            imageMatrix,
                            true)
                        bitmap?.let { btm -> _viewModelFile.value = btm }
                    }catch (e: IOException){
                        _bitmapError.value = IS_BITMAP_ERROR
                        e.stackTrace
                    }
                }
            }
        }
    }

    companion object{
        const val X_PIXEL = 0
        const val Y_PIXEL = 0
        const val IS_BITMAP_ERROR = true
    }
}