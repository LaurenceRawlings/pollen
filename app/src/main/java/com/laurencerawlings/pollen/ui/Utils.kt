package com.laurencerawlings.pollen.ui

import android.view.View
import com.google.android.material.snackbar.Snackbar
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class Utils {
    companion object {
        fun showSnackbar(message: String, view: View) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
        }

        private fun stringToDate(date: String): Date? {
            var sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT)
            return try {
                sdf.parse(date)
            } catch (_: ParseException) {
                try {
                    sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.ms'Z'", Locale.ROOT)
                    sdf.parse(date)
                } catch (_: ParseException) {
                    try {
                        sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
                        sdf.parse(date.split("+")[0])
                    } catch (_: ParseException) {
                        null
                    }
                }
            }
        }

        fun hoursPassed(date: String): Int {
            val now = Date()
            val then = stringToDate(date)
            val diffInMS: Long = kotlin.math.abs(now.time - (then?.time ?: 0))

            return TimeUnit.HOURS.convert(diffInMS, TimeUnit.MILLISECONDS).toInt()
        }
    }
}