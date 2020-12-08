package com.laurencerawlings.pollen.ui

import android.view.View
import com.google.android.material.snackbar.Snackbar

class Utils {
    companion object {
        fun showSnackbar(message: String, view: View) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
        }
    }
}