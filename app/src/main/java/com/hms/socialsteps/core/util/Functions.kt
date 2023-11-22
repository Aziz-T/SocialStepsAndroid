package com.hms.socialsteps.core.util

    fun getMessagingId(id1: String, id2: String): String{
        return if (id1>id2) "$id1$id2" else "$id2$id1"
    }