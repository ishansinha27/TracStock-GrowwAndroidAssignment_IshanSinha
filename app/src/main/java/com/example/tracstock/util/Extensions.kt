package com.example.tracstock.util

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}
fun View.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_SHORT,
    actionText: String? = null,
    actionListener: View.OnClickListener? = null
) {
    val snackbar = Snackbar.make(this, message, duration)
    if (actionText != null && actionListener != null) {
        snackbar.setAction(actionText, actionListener)
    }
    snackbar.show()
}