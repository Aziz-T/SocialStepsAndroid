package com.hms.socialsteps.utils

import retrofit2.Response
import timber.log.Timber

abstract class BaseDataSource {
    val TAG = "BaseDataSource"
    protected suspend fun <T> getResult(call: suspend () -> Response<T>): SDResource<T> {
        try {
            val response = call()
            if (response.isSuccessful) {
                val body = response.body()
                return SDResource.success(body)
            }
            return error(" ${response.code()} ${response.message()}")
        } catch (e: Exception) {
            return error(e.message ?: e.toString())
        }
    }

    private fun <T> error(message: String): SDResource<T> {
        Timber.tag(TAG).e(message)
        return SDResource.error("Network call has failed for a following reason: $message")
    }

}