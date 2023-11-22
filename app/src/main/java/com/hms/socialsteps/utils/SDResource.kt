package com.hms.socialsteps.utils

data class SDResource<out T>(val status: Status,val data: T?, val message: String?){

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> success(data: T? = null): SDResource<T>{
            return SDResource(Status.SUCCESS,data,null)
        }

        fun <T> error(message: String, data: T? = null): SDResource<T>{
            return SDResource(Status.ERROR,data,message)
        }

        fun <T> loading(data: T? = null):SDResource<T>{
            return SDResource(Status.LOADING,data,null)
        }
    }
}