package com.hms.socialsteps.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.besirkaraoglu.hmfcoroutineextensions.BuildConfig
//import com.hms.socialsteps.BuildConfig
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

const val TAG = "SDNetwork"

fun <T> performGetOperation(networkCall: suspend () -> SDResource<T>): LiveData<SDResource<T>> = liveData(
    Dispatchers.IO) {
    emit(SDResource.loading())

    val responseStatus = networkCall.invoke()
    log("Network request begins")
    try {
        if (responseStatus.status == SDResource.Status.SUCCESS ) {
            if(responseStatus.data != null){
                emit(SDResource.success(responseStatus.data))
            }else{
                emit(SDResource.success())
            }
            log("Network request success")
        }else if (responseStatus.status == SDResource.Status.ERROR) {
            emit(SDResource.error(responseStatus.message!!))
            logError("Network request error")
        }
    }catch (e: Exception) {
        logError("Network request error")
        if (e.message != null)
            emit(SDResource.error(e.message!!))
    }


}

private fun log(logMessage: String) {
    onDebug {
        Timber.tag(TAG).i( logMessage)
    }
}

private fun logError(logMessage: String) {
    onDebug {
        Timber.tag(TAG).e( logMessage)
    }
}

private fun onDebug(block: () -> Unit) {
    if (BuildConfig.DEBUG) {
        block.invoke()
    }
}