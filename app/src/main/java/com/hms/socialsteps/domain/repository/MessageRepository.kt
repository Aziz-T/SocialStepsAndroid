package com.hms.socialsteps.domain.repository

import com.hms.socialsteps.data.model.MessageItem

class MessageRepository {


    suspend fun getMessagesSnapshot(senderId: String, receiverId: String) =
        CloudDbWrapper.getMessagesSnapshot(senderId, receiverId)

    suspend fun getLastMessages(userId: String) =
        CloudDbWrapper.getLastMessages(userId)

    suspend fun sendMessage(messageItem: MessageItem) =
        CloudDbWrapper.upsertMessage(messageItem)
}