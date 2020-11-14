package com.example.cameraapp.helper

import android.app.AlertDialog
import android.content.Context
import com.example.cameraapp.R

object DialogHelper {

    fun showAlert(context: Context, msg: String){
        val dialog = AlertDialog.Builder(context).apply {
            setTitle("${context.resources.getString(R.string.error_msg)}: $msg")
            setPositiveButton(context.resources.getString(R.string.pdf_alert_button)) { _, _ ->
                this.setOnDismissListener {
                    it.dismiss()
                }
            }
        }
        dialog.show()
    }
}