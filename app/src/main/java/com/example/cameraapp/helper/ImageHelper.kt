package com.example.cameraapp.helper

import android.graphics.Bitmap
import android.widget.ImageView
import coil.load

object ImageHelper {

    fun imageLoader(imageView: ImageView, bitmap: Bitmap){
        imageView.load(bitmap)
    }
}