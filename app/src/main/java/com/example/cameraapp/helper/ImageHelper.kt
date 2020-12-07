package com.example.cameraapp.helper

import android.graphics.Bitmap
import android.graphics.Matrix
import android.widget.ImageView
import coil.load
import com.example.cameraapp.viewModel.CameraViewModel

object ImageHelper {

    fun imageLoader(imageView: ImageView, bitmap: Bitmap){
        imageView.load(bitmap)
    }

    fun createBitmap(bitmapValue: Bitmap?, imageMatrix: Matrix): Bitmap? =
        Bitmap.createBitmap(
            bitmapValue!!,
            CameraViewModel.X_PIXEL,
            CameraViewModel.Y_PIXEL,
            bitmapValue.width,
            bitmapValue.height,
            imageMatrix,
            true
        )
}