package com.hms.socialsteps.core.util

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun convertMillisecondsToDate(milliSeconds: Long): String {
    return DateFormat.format("dd/MM/yyyy HH:mm:ss", milliSeconds).toString()
}

fun convertMilliSecondsToHours(milliSeconds: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(milliSeconds).toInt() % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliSeconds).toInt() % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliSeconds).toInt() % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

fun getDateFromString(string: String): Date {
    val format = SimpleDateFormat("dd/M/yyyy HH:mm")
    return format.parse(string) as Date

}
