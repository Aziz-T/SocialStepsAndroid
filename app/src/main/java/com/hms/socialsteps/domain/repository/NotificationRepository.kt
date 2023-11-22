package com.hms.socialsteps.domain.repository

import com.google.gson.Gson
import com.hms.socialsteps.core.util.Constants.COMMENT
import com.hms.socialsteps.core.util.Constants.LIKE
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.convertMillisecondsToDate
import com.hms.socialsteps.data.model.GroupInformation
import com.hms.socialsteps.data.model.MessageItem
import com.hms.socialsteps.data.model.Notifications
import com.hms.socialsteps.data.model.Users
import com.huawei.agconnect.function.AGConnectFunction
import timber.log.Timber
import javax.inject.Inject

class NotificationRepository
@Inject constructor(
    private val function: AGConnectFunction
) {
    companion object {
        const val TAG = "NotificationRepository"
    }

    fun upsertNotification(
        postId: String,
        postOwnerId: String,
        loggedUser: Users,
        notificationType: String?,
        mNotification: Notifications?,
        groupInformation: GroupInformation? = null,
        callback: (isSuccessful: Resource<Boolean>) -> Unit
    ) {
        var notification = Notifications()
        if (mNotification == null) {
            if (notificationType == LIKE) {
                notification.notificationID = loggedUser.uid + postId
            } else if (notificationType == COMMENT) {
                notification.notificationID = loggedUser.uid + System.currentTimeMillis().toString()
            } else {

                notification.notificationID =
                    (groupInformation?.groupID ?: "") + System.currentTimeMillis().toString()
                notification.additionalString = groupInformation?.groupName ?: ""
            }
            notification.notificationType = notificationType
            notification.user1 = loggedUser.uid
            notification.user1Photo = loggedUser.photo
            notification.user1Name = loggedUser.username
            notification.user2 = postOwnerId
            notification.postID = postId
            notification.notificationDate = convertMillisecondsToDate(System.currentTimeMillis())
            notification.isRead = false
            notification.recordCreationTime = System.currentTimeMillis().toString()
        } else {
            mNotification.isRead = true
            notification = mNotification
        }
        CloudDbWrapper.upsertNotification(notification) { result ->
            if (result.data == true && notificationType != null) pushNotification(
                loggedUser,
                postOwnerId,
                notificationType,
                groupInformation
            )
            callback(result)
        }
    }

    fun queryNotifications(notificationsList: (Resource<ArrayList<Notifications>>) -> Unit) {
        CloudDbWrapper.queryNotifications { result ->
            notificationsList(result)
        }
    }

    fun deleteNotification(
        notification: Notifications,
        callback: (isSuccessful: Resource<Boolean>) -> Unit
    ) {
        CloudDbWrapper.deleteNotification(notification) {
            callback(it)
        }
    }

    private fun pushNotification(
        loggedUser: Users,
        uid: String,
        notificationType: String,
        groupInformation: GroupInformation? = null
    ) {
        CloudDbWrapper.queryToken(uid) {
            if (it is Resource.Success) {
                val map: HashMap<String, String> = HashMap()
                map["mToken"] = it.data!!.token
                map["photoUrl"] = loggedUser.photo ?: ""
                map["content"] =
                    if (notificationType == LIKE) "${loggedUser.username} has liked your post."
                    else if (notificationType == COMMENT) "${loggedUser.username} has commented on your post."
                    else "${loggedUser.username} added you to ${groupInformation?.groupName}."
                Timber.tag(TAG).d("pushNotification: ${Gson().toJson(map)}")
                function.wrap("like-comment-push-\$latest").call(map)
                    .addOnCompleteListener { pushRes ->
                        if (pushRes.isSuccessful) {
                            Timber.tag(TAG).d("pushNotification: push message success.")
                            Timber.tag(TAG).d("pushNotification: ${pushRes.result.value}")
                        } else {
                            Timber.tag(TAG).e(
                                "pushNotification: push message failed! ${pushRes.exception.message}"
                            )
                        }
                    }
            } else {
                Timber.tag(TAG).e("pushNotification: User token not found!")
            }
        }
    }

    /**
     * Notification push function for messaging
     */
    fun pushNotification(
        loggedUser: Users, uid: String,
        messageItem: MessageItem
    ) {
        CloudDbWrapper.queryToken(uid) {
            if (it is Resource.Success) {
                val map: HashMap<String, String> = HashMap()
                map["mToken"] = it.data!!.token
                map["photoUrl"] = loggedUser.photo ?: ""
                map["content"] = "${loggedUser.username}: ${messageItem.text}"
                Timber.tag(TAG).d("pushNotification: ${Gson().toJson(map)}")
                function.wrap("like-comment-push-\$latest").call(map)
                    .addOnCompleteListener { pushRes ->
                        if (pushRes.isSuccessful) {
                            Timber.tag(TAG).d("pushNotification: push message success.")
                            Timber.tag(TAG).d("pushNotification: ${pushRes.result.value}")
                        } else {
                            Timber.tag(TAG).e(
                                "pushNotification: push message failed! ${pushRes.exception.message}"
                            )
                        }
                    }
            } else {
                Timber.tag(TAG).e("pushNotification: User token not found!")
            }
        }
    }
}