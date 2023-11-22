package com.hms.socialsteps.domain.repository

import android.util.Log
import com.google.firebase.storage.StorageReference
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Users
import com.huawei.agconnect.auth.AGConnectAuth
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import javax.inject.Inject

class CloudStorageRepository
@Inject constructor(
    private val imagesReference: StorageReference
) {


    fun uploadImage(inputStream: InputStream, user: Users) = channelFlow<Resource<String>> {
        try {
            val uid = user.uid
            val ref = imagesReference.child(uid).child("$uid.jpg")
            val downloadUrl = ref
                .putStream(inputStream).await()
                .storage.downloadUrl.await()

            user.photo = downloadUrl.toString()
            CloudDbWrapper.upsertUser(user) {
                if (it is Resource.Success){
                    trySend(Resource.Success(downloadUrl.toString()))
                } else {
                    throw Exception(it.message ?: "Upsert task failed!")
                }
            }
        } catch (e: Exception) {
            trySend(Resource.Error(e.message ?: "Upload task failed!"))
            e.printStackTrace()
        }

        awaitClose {}
    }
}