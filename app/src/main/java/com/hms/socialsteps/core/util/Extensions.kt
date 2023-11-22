package com.hms.socialsteps.core.util

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.hms.socialsteps.R
import com.huawei.agconnect.cloud.database.CloudDBZoneObject
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.sn.lib.NestedProgress
import java.text.SimpleDateFormat
import java.util.*

fun Context.showToastLong(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun Context.showToastShort(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.loadImage(imageUrl: String?, imageView: ImageView) =
    Glide.with(this)
        .load(imageUrl)
        .error(R.drawable.ic_profile_placeholder)
        .placeholder(R.drawable.ic_profile_placeholder)
        .into(imageView)

fun Fragment.loadImage(imageUrl: String?, imageView: ImageView) =
    Glide.with(this)
        .load(imageUrl)
        .placeholder(R.drawable.ic_profile_placeholder)
        .into(imageView)

fun Fragment.loadImage(imageUrl: Int, imageView: ImageView) =
    Glide.with(this)
        .load(imageUrl)
        .placeholder(R.drawable.ic_profile_placeholder)
        .into(imageView)

fun Fragment.navigateTo(id: Int) =
    findNavController().navigate(id)

fun Fragment.navigateTo(id: Int, bundle: Bundle) =
    findNavController().navigate(id, bundle)

fun Fragment.showSnackBarShort(text: String) =
    Snackbar.make(requireView(), text, Snackbar.LENGTH_SHORT)
        .show()

fun Context.showAlertDialog(title: String?, message: String?) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    builder.setTitle(title)
    builder.setMessage(message)
    builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
        dialog.dismiss()
    }
    builder.show()
}

fun Fragment.showProgressBar(progressBar: NestedProgress) {
    progressBar.visibility = View.VISIBLE
    requireActivity().window.setFlags(
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    )
}

fun Fragment.hideProgressBar(progressBar: NestedProgress) {
    progressBar.visibility = View.GONE
    requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
}

fun getCurrentDate(): String {
    val simpleDate = SimpleDateFormat("dd/M/yyyy HH:mm")
    return simpleDate.format(Date()).toString()
}

fun <T : CloudDBZoneObject> CloudDBZoneSnapshot<T>.asList(): kotlin.collections.List<T> {
    val list = mutableListOf<T>()
    try {
        while (this.snapshotObjects.hasNext()) {
            val cObj = this.snapshotObjects.next()
            list.add(cObj)
        }
    } catch (e: AGConnectCloudDBException) {
        throw Exception(e)
    }
    return list
}

fun <T : CloudDBZoneObject> CloudDBZoneObjectList<T>.asList(): kotlin.collections.List<T> {
    val list = mutableListOf<T>()
    try {
        while (this.hasNext()) {
            val cObj = this.next()
            list.add(cObj)
        }
    } catch (e: AGConnectCloudDBException) {
        throw Exception(e)
    }
    return list
}