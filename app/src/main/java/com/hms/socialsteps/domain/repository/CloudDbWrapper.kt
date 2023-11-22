package com.hms.socialsteps.domain.repository

import android.content.Context
import com.besirkaraoglu.hmfcoroutineextensions.await
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.core.util.Constants.STEP
import com.hms.socialsteps.core.util.Constants
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.*
import com.huawei.agconnect.AGCRoutePolicy
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hmf.tasks.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import timber.log.Timber

class CloudDbWrapper {

    companion object {
        private const val TAG = "CloudDBWrapper"
        private const val CloudDBLog = "Cloud DB Zone is null, try re-open it"
        private var userPreference: UserPreference? = null

        private var cloudDB: AGConnectCloudDB? = null
        private var config: CloudDBZoneConfig? = null
        var cloudDBZone: CloudDBZone? = null
        var instance: AGConnectInstance? = null

        fun initialize(
            context: Context,
            cloudDbInitializeResponse: (Boolean) -> Unit
        ) {
            userPreference = UserPreference(context)

            if (cloudDBZone != null) {
                cloudDbInitializeResponse(true)
                return
            }

            AGConnectCloudDB.initialize(context)

            instance = AGConnectInstance.buildInstance(
                AGConnectOptionsBuilder().setRoutePolicy(AGCRoutePolicy.GERMANY).build(context)
            )

            cloudDB = AGConnectCloudDB.getInstance(
                AGConnectInstance.getInstance(),
                AGConnectAuth.getInstance()
            )

            cloudDB?.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())

            config = CloudDBZoneConfig(
                Constants.CloudDBZoneName,
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC
            )

            config?.persistenceEnabled = true
            val task = cloudDB?.openCloudDBZone2(config!!, true)
            task?.addOnSuccessListener {
                Timber.tag(TAG).i("Open cloudDBZone success")
                cloudDBZone = it
                cloudDbInitializeResponse(true)
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Open cloudDBZone failed for %s", it.message)
                cloudDbInitializeResponse(false)
            }

        }

        /**
         * Extension function for query tasks
         **/
        private fun <T : CloudDBZoneObject> CloudDBZoneQuery<T>.query(): Task<CloudDBZoneSnapshot<T>> =
            cloudDBZone!!.executeQuery(
                this,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

        /**
         * Extension function for subscription tasks
         */
        private fun <T : CloudDBZoneObject> CloudDBZoneQuery<T>.subscribe(listener: OnSnapshotListener<T>): ListenerHandler =
            cloudDBZone!!.subscribeSnapshot(
                this,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                listener
            )

        fun queryUser(userId: String, result: (Resource<Users>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val queryUsers = CloudDBZoneQuery.where(Users::class.java)
                .contains(UsersEditFields.UID, userId)

            val queryTask = cloudDBZone?.executeQuery(
                queryUsers,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val usersList = arrayListOf<Users>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val cUser = snapshot.snapshotObjects.next()
                        usersList.add(cUser)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query failed"))
                } finally {
                    if (usersList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(usersList[0]))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed"))
            }
        }

        fun queryPostLikeCount(postId: String, result: (Resource<LikesCount>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val queryPostLikesCount = CloudDBZoneQuery.where(LikesCount::class.java)
                .contains("postId", postId)

            val queryTask = cloudDBZone?.executeQuery(
                queryPostLikesCount,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val postLikesCountList = arrayListOf<LikesCount>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val postLikesCount = snapshot.snapshotObjects.next()
                        postLikesCountList.add(postLikesCount)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query failed"))
                } finally {
                    if (postLikesCountList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(postLikesCountList[0]))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed"))
            }
        }

        fun queryFriendRequests(userId: String, result: (Resource<List<Friendship>>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val queryFriendRequests = CloudDBZoneQuery.where(Friendship::class.java)
                .equalTo(FriendShipEditFields.USER1, userId)
                .equalTo(FriendShipEditFields.STATUS, "2")
                .orderByDesc(FriendShipEditFields.SENT_REQUEST_DATE)

            val queryTask = cloudDBZone?.executeQuery(
                queryFriendRequests,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )
            queryTask?.addOnSuccessListener { snapshot ->
                val friendRequestList = arrayListOf<Friendship>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val friendRequest = snapshot.snapshotObjects.next()
                        friendRequestList.add(friendRequest)
                    }
                } catch (e: AGConnectCloudDBException) {
                    result(Resource.Error(e.message ?: "Query failed!"))
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                } finally {
                    if (friendRequestList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(friendRequestList))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                result(Resource.Error(it.message ?: "Query failed!"))
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
            }

        }

        fun queryUsers(isRankQuery: Boolean, result: (Resource<ArrayList<Users>>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            var queryUsers = CloudDBZoneQuery.where(Users::class.java)

            if (isRankQuery) {
                queryUsers = CloudDBZoneQuery.where(Users::class.java)
                    .equalTo(UsersEditFields.PERMISSION_TO_SHARE_INFO, true)
            }

            val queryTask = cloudDBZone?.executeQuery(
                queryUsers,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val usersList = arrayListOf<Users>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val user = snapshot.snapshotObjects.next()
                        usersList.add(user)
                    }
                } catch (e: AGConnectCloudDBException) {
                    result(Resource.Error(e.message ?: "Query failed!"))
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                } finally {
                    if (usersList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(usersList))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                result(Resource.Error(it.message ?: "Query failed!"))
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
            }
        }

        fun upsertUser(users: Users, result: (isSuccessful: Resource<Boolean>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val upsertTask = cloudDBZone!!.executeUpsert(users)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                userPreference?.setStoredUser(users)
                Timber.tag(TAG).i("Upsert $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertData: ${it.message}")
                result(Resource.Error(it.message ?: " Upsert failed!", false))
            }
        }

        fun upsertGroupInformation(
            groupInformation: GroupInformation,
            result: (isSuccessful: Resource<Boolean>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val upsertTask = cloudDBZone!!.executeUpsert(groupInformation)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("Upsert $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertData: ${it.message}")
                result(Resource.Error(it.message ?: " Upsert failed!", false))
            }
        }

        fun upsertGroupMembers(
            groupMembers: ArrayList<GroupMembers>,
            result: (isSuccessful: Resource<Boolean>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val upsertTask = cloudDBZone!!.executeUpsert(groupMembers)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("Upsert $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertData: ${it.message}")
                result(Resource.Error(it.message ?: " Upsert failed!", false))
            }
        }

        fun deleteGroupMember(
            groupMember: GroupMembers,
            result: (isSuccessful: Resource<Any>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val deleteTask = cloudDBZone!!.executeDelete(groupMember)
            deleteTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("Delete $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("DeleteData: ${it.message}")
                result(Resource.Error(it.message ?: " Delete failed!", false))
            }
        }

        /**
         * Deletes the [GroupMembers] object by given user id and group id.
         */
        suspend fun deleteGroupMemberByUserIdFlow(userId: String, groupId: String) = flow<Resource<Any?>> {
            try {
                emit(Resource.Loading())
                if (cloudDBZone == null) {
                    Timber.tag(TAG).w(CloudDBLog)
                    throw Exception(CloudDBLog)
                }

                val queryGroupMembers = CloudDBZoneQuery.where(GroupMembers::class.java)
                    .equalTo(GroupMembersEditFields.USER_ID, userId).and()
                    .equalTo(GroupMembersEditFields.GROUP_ID, groupId)
                    .query().await().asList()

                val deleteOperation = cloudDBZone!!.executeDelete(queryGroupMembers).await()
                emit(Resource.Success(deleteOperation))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Delete failed!"))
            }
        }

        /**
         * Returns a list of [GroupInformation] that includes given user id.
         */
        suspend fun queryGroupsByUserIdAsFlow(userId: String) =
            flow<Resource<List<GroupInformation>>> {
                try {
                    emit(Resource.Loading())
                    if (cloudDBZone == null) {
                        Timber.tag(TAG).w(CloudDBLog)
                        throw Exception(CloudDBLog)
                    }
                    val userGroupsResult = CloudDBZoneQuery.where(GroupMembers::class.java)
                        .equalTo(GroupMembersEditFields.USER_ID, userId)
                        .query().await().asList()


                    if (userGroupsResult.isEmpty()) {
                        emit(Resource.Empty())
                        return@flow
                    }

                    val groupIdList = mutableListOf<String>()
                    userGroupsResult.forEach {
                        groupIdList.add(it.groupID)
                    }

                    val groupsResult = CloudDBZoneQuery.where(GroupInformation::class.java)
                        .`in`(GroupInformationEditFields.GROUP_ID, groupIdList.toTypedArray())
                        .query().await().asList()

                    if (groupsResult.isEmpty()) emit(Resource.Empty())
                    else emit(Resource.Success(groupsResult))

                } catch (e: Exception) {
                    emit(Resource.Error(e.message ?: "Query failed!"))
                }
            }

        /**
         * Returns list of [Group] by given user id.
         */
        suspend fun getGroupsByUserIdFlow(userId: String) = flow<Resource<List<Group>>> {
            try {
                emit(Resource.Loading())
                if (cloudDBZone == null) {
                    Timber.tag(TAG).w(CloudDBLog)
                    throw Exception(CloudDBLog)
                }
                //query user's groups
                val userGroups = CloudDBZoneQuery.where(GroupMembers::class.java)
                    .equalTo(GroupMembersEditFields.USER_ID, userId)
                    .query().await().asList()


                if (userGroups.isEmpty()) {
                    emit(Resource.Empty())
                    return@flow
                }

                val groupIdList = mutableListOf<String>()
                userGroups.forEach {
                    groupIdList.add(it.groupID)
                }

                //query user's groups' information
                val groupsInfo = CloudDBZoneQuery.where(GroupInformation::class.java)
                    .`in`(GroupInformationEditFields.GROUP_ID, groupIdList.toTypedArray())
                    .query().await().asList()

                //query members of groups
                val memberList = CloudDBZoneQuery.where(GroupMembers::class.java)
                    .`in`(GroupMembersEditFields.GROUP_ID, groupIdList.toTypedArray())
                    .query().await().asList()

                //get userIds to query Users
                val memberIdList = mutableListOf<String>()
                memberList.forEach { member ->
                    if (!memberIdList.contains(member.userID))
                        memberIdList.add(member.userID)
                }

                val users = CloudDBZoneQuery.where(Users::class.java)
                    .`in`(UsersEditFields.UID, memberIdList.toTypedArray())
                    .query().await().asList()

                //change data to 0 if it's older than 1 day
                users.forEach {
                    if (it.lastDailyCaloriesUpdatedTime == null || getDateFromString(getCurrentDate()).time - getDateFromString(it.lastDailyCaloriesUpdatedTime).time > 86400000)
                        it.caloriesBurned = 0.toString()
                    if (it.lastDailyStepsUpdatedTime == null || getDateFromString(getCurrentDate()).time - getDateFromString(it.lastDailyStepsUpdatedTime).time > 86400000)
                        it.dailyStepsCount = 0.toString()
                }

                val groups = mutableListOf<Group>()
                groupsInfo.forEach { groupInfo ->
                    val userList = mutableListOf<Users>()
                    memberList.forEach { member ->
                        if (member.groupID == groupInfo.groupID)
                            users.forEach { user ->
                                if (user.uid == member.userID)
                                    userList.add(user)
                            }
                    }
                    val group = Group(
                        groupInfo, userList
                    )
                    groups.add(group)
                }
                groups.forEach {
                    if (it.groupInformation.targetType == TargetType.STEP.toString())
                        it.members = it.members.sortedByDescending { it1 ->
                            it1.dailyStepsCount.toInt()
                        }
                    else
                        it.members = it.members.sortedByDescending { it1 ->
                            it1.caloriesBurned.toInt()
                        }
                }
                emit(Resource.Success(groups))

            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Query failed!"))
            }
        }


        fun upsertNotification(
            notifications: Notifications,
            result: (isSuccessful: Resource<Boolean>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val upsertTask = cloudDBZone!!.executeUpsert(notifications)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("Upsert $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertData: ${it.message}")
                result(Resource.Error(it.message ?: "Upsert failed!", false))
            }
        }

        fun queryNotifications(result: (Resource<ArrayList<Notifications>>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val queryNotifications = userPreference?.getStoredUser()?.let {
                CloudDBZoneQuery.where(Notifications::class.java).equalTo(
                    NotificationEditFields.USER2,
                    it.uid
                ).orderByDesc(NotificationEditFields.RECORD_CREATION_TIME)
            }

            val queryTask = cloudDBZone?.executeQuery(
                queryNotifications!!,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val notificationsList = arrayListOf<Notifications>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val notification = snapshot.snapshotObjects.next()
                        notificationsList.add(notification)
                    }
                } catch (e: AGConnectCloudDBException) {
                    result(Resource.Error(e.message ?: "Query failed!"))
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                } finally {
                    if (notificationsList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(notificationsList))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                result(Resource.Error(it.message ?: "Query failed!"))
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
            }
        }

        fun deleteNotification(
            notifications: Notifications,
            result: (isSuccessful: Resource<Boolean>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val deleteTask = cloudDBZone!!.executeDelete(notifications)
            deleteTask.addOnSuccessListener {
                Timber.tag(TAG).i("Delete $it records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("deleteData: ${it.message}")
                result(Resource.Error(it.message ?: "Delete failed!", false))
            }
        }

        fun upsertLike(likes: Likes, result: (isSuccessful: Resource<Boolean>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val upsertTask = cloudDBZone!!.executeUpsert(likes)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("Upsert $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertData: ${it.message}")
                result(Resource.Error(it.message ?: " Upsert failed!", false))
            }
        }

        fun upsertLikeCount(
            likesCount: LikesCount,
            result: (isSuccessful: Resource<Boolean>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
            }

            val upsertTask = cloudDBZone!!.executeUpsert(likesCount)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("Upsert $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertData: ${it.message}")
                result(Resource.Error(it.message ?: " Upsert failed!", false))
            }
        }

        fun upsertActivities(
            activities: List<Activities>,
            result: (isSuccessful: Resource<Boolean>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val upsertTask = cloudDBZone!!.executeUpsert(activities)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("Upsert $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertData: ${it.message}")
                result(Resource.Error(it.message ?: " Upsert failed!", false))
            }
        }

        fun upsertOrDeleteFriendShip(
            friendship: Friendship,
            friendship2: Friendship,
            isDelete: Boolean,
            result: (isSuccessful: Resource<Boolean>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val upsertOrDeleteTask: Task<Int> = if (isDelete) {
                cloudDBZone!!.executeDelete(friendship)
            } else {
                cloudDBZone!!.executeUpsert(friendship)
            }
            upsertOrDeleteTask.addOnSuccessListener {
                val upsertOrDeleteTask2: Task<Int> = if (isDelete) {
                    cloudDBZone!!.executeDelete(friendship2)
                } else {
                    cloudDBZone!!.executeUpsert(friendship2)
                }
                upsertOrDeleteTask2.addOnSuccessListener { cloudDBZoneResult ->
                    Timber.tag(TAG).i("Upsert or Delete $cloudDBZoneResult records successful")
                    result(Resource.Success(true))
                }.addOnFailureListener {
                    Timber.tag(TAG).e("upsertOrDeleteData: ${it.message}")
                    result(Resource.Error(it.message ?: " Upsert or Delete failed!", false))
                }
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertOrDeleteData: ${it.message}")
                result(Resource.Error(it.message ?: " Upsert or Delete failed!", false))
            }
        }

        fun queryFriendShipStatus(
            userId: String,
            visitedUserId: String,
            result: (Resource<Friendship>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val queryFriendshipStatus = CloudDBZoneQuery.where(Friendship::class.java)
                .equalTo(FriendShipEditFields.USER1, userId)
                .equalTo(FriendShipEditFields.USER2, visitedUserId)

            val queryTask = cloudDBZone?.executeQuery(
                queryFriendshipStatus,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                var friendShipRecord: Friendship? = null
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        friendShipRecord = snapshot.snapshotObjects.next()
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query failed"))
                } finally {
                    if (friendShipRecord == null) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(friendShipRecord))
                    }
                    snapshot.release()
                }
            }
        }

        fun upsertPost(posts: Posts, result: (isSuccessful: Resource<Boolean>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val upsertTask = cloudDBZone!!.executeUpsert(posts)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("Upsert $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertData: ${it.message}")
                result(Resource.Error(it.message ?: " Upsert failed!", false))
            }
        }

        fun queryActivities(
            userId: String?,
            activityId: String?,
            result: (Resource<ArrayList<Activities>>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            var queryActivities: CloudDBZoneQuery<Activities>?

            queryActivities = userId?.let {
                CloudDBZoneQuery.where(Activities::class.java)
                    .contains(ActivitiesEditFields.UID, it)
            }

            if (activityId != null) {
                queryActivities = CloudDBZoneQuery.where(Activities::class.java)
                    .contains(ActivitiesEditFields.ACTIVITY_ID, activityId)
            }

            val queryTask = cloudDBZone?.executeQuery(
                queryActivities!!,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val activitiesList = arrayListOf<Activities>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val activity = snapshot.snapshotObjects.next()
                        activitiesList.add(activity)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query Failed!"))
                } finally {
                    if (activitiesList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(activitiesList))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed!"))
            }
        }

        fun queryGroup(groupID: String, result: (Resource<List<GroupInformation>>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val queryGroups = CloudDBZoneQuery.where(GroupInformation::class.java)
                .contains(GroupInformationEditFields.GROUP_ID, groupID)

            val queryTask = cloudDBZone?.executeQuery(
                queryGroups,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )
            queryTask?.addOnSuccessListener { snapshot ->
                val groupList = arrayListOf<GroupInformation>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val groupInformation = snapshot.snapshotObjects.next()
                        groupList.add(groupInformation)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query Failed!"))
                } finally {
                    if (groupList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(groupList))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed!"))
            }

        }

        fun queryFriendIds(userId: String, result: (Resource<List<String>>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val queryFriends = CloudDBZoneQuery.where(Friendship::class.java)
                .contains(FriendShipEditFields.USER1, userId)
                .equalTo(FriendShipEditFields.STATUS, "3")

            val queryTask = cloudDBZone?.executeQuery(
                queryFriends,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val friendsList = arrayListOf<String>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val friendship = snapshot.snapshotObjects.next()
                        friendsList.add(friendship.user2)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query Failed!"))
                } finally {
                    if (friendsList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(friendsList))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed!"))
            }
        }

        fun queryPosts(postId: String?, result: (Resource<List<Posts>>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            var queryPosts = CloudDBZoneQuery.where(Posts::class.java)
                .orderByDesc(PostsEditFields.SHARED_DATE_MILLIS)

            if (postId != null) {
                queryPosts = CloudDBZoneQuery.where(Posts::class.java)
                    .equalTo(PostsEditFields.POST_ID, postId)
            }
            val queryTask = cloudDBZone?.executeQuery(
                queryPosts,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val postsList = mutableListOf<Posts>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val post = snapshot.snapshotObjects.next()
                        postsList.add(post)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query Failed!"))
                } finally {
                    if (postsList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(postsList))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed!"))
            }
        }

        fun queryUserPosts(userId: String?, result: (Resource<List<Posts>>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            var queryPosts = CloudDBZoneQuery.where(Posts::class.java)
                .orderByDesc(PostsEditFields.SHARED_DATE_MILLIS)

            if (userId != null) {
                queryPosts = CloudDBZoneQuery.where(Posts::class.java)
                    .equalTo(PostsEditFields.USER_ID, userId)
            }
            val queryTask = cloudDBZone?.executeQuery(
                queryPosts,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val postsList = mutableListOf<Posts>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val post = snapshot.snapshotObjects.next()
                        postsList.add(post)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query Failed!"))
                } finally {
                    if (postsList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(postsList))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed!"))
            }
        }

        fun queryLikes(result: (Resource<List<Likes>>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val queryPosts = CloudDBZoneQuery.where(Likes::class.java)

            val queryTask = cloudDBZone?.executeQuery(
                queryPosts,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val likesList = mutableListOf<Likes>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val post = snapshot.snapshotObjects.next()
                        likesList.add(post)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query Failed!"))
                } finally {
                    if (likesList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(likesList))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed!"))
            }
        }

        fun upsertToken(token: Tokens, result: (Resource<Any?>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val upsertTask = cloudDBZone!!.executeUpsert(token)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("Upsert $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertData: ${it.message}")
                result(Resource.Error(it.message ?: " Upsert failed!", false))
            }
        }

        fun deleteLike(
            like: Likes,
            result: (isSuccessful: Resource<Boolean>) -> Unit
        ) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val deleteTask: Task<Int> =
                cloudDBZone!!.executeDelete(like)

            deleteTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("delete $cloudDBZoneResult records successful")
                result(Resource.Success(true))
            }.addOnFailureListener {
                Timber.tag(TAG).e("delete like : ${it.message}")
                result(Resource.Error(it.message ?: "delete like failed!", false))
            }

        }

        fun queryLikeId(userId: String, postId: String, result: (Resource<Likes>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val queryLikes = CloudDBZoneQuery.where(Likes::class.java)
                .contains("postId", postId)
                .equalTo("userId", userId)

            val queryTask = cloudDBZone?.executeQuery(
                queryLikes,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val likeList = arrayListOf<Likes>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val like = snapshot.snapshotObjects.next()
                        likeList.add(like)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query failed"))
                } finally {
                    if (likeList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(likeList[0]))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed"))
            }
        }

        fun queryToken(userId: String, result: (Resource<Tokens>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val queryToken = CloudDBZoneQuery.where(Tokens::class.java)
                .equalTo(TokensFields.USER_ID, userId)

            val queryTask = cloudDBZone?.executeQuery(
                queryToken,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val tokensList = mutableListOf<Tokens>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val tokens = snapshot.snapshotObjects.next()
                        tokensList.add(tokens)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query Failed!"))
                } finally {
                    if (tokensList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(tokensList[0]))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed!"))
            }
        }

        fun queryComments(result: (Resource<List<Comments>>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }

            val queryComments = CloudDBZoneQuery.where(Comments::class.java)

            val queryTask = cloudDBZone?.executeQuery(
                queryComments,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
            )

            queryTask?.addOnSuccessListener { snapshot ->
                val commentsList = mutableListOf<Comments>()
                try {
                    while (snapshot.snapshotObjects.hasNext()) {
                        val comment = snapshot.snapshotObjects.next()
                        commentsList.add(comment)
                    }
                } catch (e: AGConnectCloudDBException) {
                    Timber.tag(TAG).e("processQueryResultExc: %s", e.message)
                    result(Resource.Error(e.message ?: "Query Failed!"))
                } finally {
                    if (commentsList.isEmpty()) {
                        result(Resource.Empty())
                    } else {
                        result(Resource.Success(commentsList))
                    }
                    snapshot.release()
                }
            }?.addOnFailureListener {
                Timber.tag(TAG).e("Fail processQueryResult: %s", it.message)
                result(Resource.Error(it.message ?: "Query failed!"))
            }
        }

        fun insertComment(comment: Comments, result: (isSuccessful: Resource<Comments>) -> Unit) {
            if (cloudDBZone == null) {
                Timber.tag(TAG).w(CloudDBLog)
                result(Resource.Error(CloudDBLog))
            }
            val upsertTask = cloudDBZone!!.executeUpsert(comment)
            upsertTask.addOnSuccessListener { cloudDBZoneResult ->
                Timber.tag(TAG).i("Upsert $cloudDBZoneResult records successful")
                result(Resource.Success(comment))
            }.addOnFailureListener {
                Timber.tag(TAG).e("upsertData: ${it.message}")
                result(Resource.Error(it.message ?: " Upsert failed!"))
            }
        }

        suspend fun getCommentDataChanges(postId: String): Flow<Resource<List<Comments>>> =
            withContext(Dispatchers.IO) {
                callbackFlow {
                    val snapshotQuery = CloudDBZoneQuery.where(Comments::class.java)
                        .equalTo(CommentEditFields.POST_ID, postId)
                    val subscription = cloudDBZone!!.subscribeSnapshot(snapshotQuery,
                        CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                        object : OnSnapshotListener<Comments> {
                            override fun onSnapshot(
                                snapshot: CloudDBZoneSnapshot<Comments>?,
                                e: AGConnectCloudDBException?
                            ) {
                                if (e != null) {
                                    Timber.tag(TAG).w("onSnapshot:(getObject)%s", e.message)
                                    trySend(Resource.Error(e.message!!))
                                    return
                                }

                                try {
                                    val snapShotObjects = snapshot?.upsertedObjects
                                    if (snapShotObjects != null) {
                                        val commentList = arrayListOf<Comments>()
                                        while (snapShotObjects.hasNext()) {
                                            commentList.add(snapShotObjects.next())
                                        }
                                        trySend(Resource.Success(commentList))
                                    }
                                } catch (e: Exception) {
                                    trySend(Resource.Error(e.message!!))
                                } finally {
                                    snapshot?.release()
                                }
                            }
                        })
                    awaitClose {
                        subscription.remove()
                    }
                }
            }

        /**
         * Creates and subscribes a snapshot for [MessageItem] and returns [DirectMessage].
         */
        suspend fun getMessagesSnapshot(senderId: String, receiverId: String) =
            withContext(Dispatchers.IO){
                 callbackFlow<Resource<DirectMessage>>{
                    var subscription: ListenerHandler? = null
                    try {
                        trySend(Resource.Loading())
                        val queryList = listOf<String>(senderId, receiverId)
                        val users = CloudDBZoneQuery.where(Users::class.java)
                            .`in`(UsersEditFields.UID, queryList.toTypedArray())
                            .query().await().asList()

                        subscription = CloudDBZoneQuery.where(MessageItem::class.java)
                            .equalTo(MessageItemEditFields.MESSAGING_ID, getMessagingId(senderId,receiverId))
                            .subscribe { snapshot, e ->
                                if (e != null) {
                                    trySend(Resource.Error(e.message ?: "Snapshot failed!"))
                                    return@subscribe
                                }
                                val directMessage = DirectMessage(users, snapshot.snapshotObjects.asList())
                                trySend(Resource.Success(directMessage))
                            }
                    } catch (e: Exception) {
                        trySend(Resource.Error(e.message ?: "Query failed!"))
                    }
                     awaitClose { subscription?.remove() }
                }
            }

        /**
         * Returns a list of [LastMessage] for given id.
         */
        suspend fun getLastMessages(userId: String) = flow<Resource<List<LastMessage>>> {
            try {
                val lastMessages = CloudDBZoneQuery.where(MessagingItem::class.java)
                    .equalTo(MessagingItemEditFields.USER1, userId).or()
                    .equalTo(MessagingItemEditFields.USER2, userId)
                    .query().await().asList().sortedByDescending { it.lastUpdateTime }

                if (lastMessages.isEmpty()){
                    emit(Resource.Empty())
                    return@flow
                }

                val userIdList = mutableListOf<String>()
                lastMessages.forEach {
                    if (!userIdList.contains(it.user1))
                        userIdList.add(it.user1)
                    if (!userIdList.contains(it.user2))
                        userIdList.add(it.user2)
                }
                userIdList.remove(userId)

                val users = CloudDBZoneQuery.where(Users::class.java)
                    .`in`(UsersEditFields.UID, userIdList.toTypedArray())
                    .query().await().asList()

                if (users.isEmpty()) {
                    emit(Resource.Empty())
                    return@flow
                }

                val dmList = mutableListOf<LastMessage>()
                lastMessages.forEach {
                    if (it.user1 == userId) {
                        val user = users.find { it1 -> it1.uid == it.user2 }
                        dmList.add(
                            LastMessage(
                                user!!, it
                            )
                        )
                    } else if (it.user2 == userId) {
                        val user = users.find { it1 -> it1.uid == it.user1 }
                        dmList.add(
                            LastMessage(
                                user!!, it
                            )
                        )
                    }
                }

                emit(Resource.Success(dmList))
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Query failed!"))
            }

        }

        suspend fun upsertMessage(messageItem: MessageItem) = flow<Resource<Any?>> {
            try {
                emit(Resource.Loading())
                cloudDBZone!!.executeUpsert(messageItem).await()
                val messagingItem = MessagingItem().apply {
                    lastMessage = messageItem.text
                    messagingId = messageItem.messagingId
                    lastUpdateTime = messageItem.sendTime
                    user1 = messageItem.senderId
                    user2 = messageItem.receiverId
                }
                cloudDBZone!!.executeUpsert(messagingItem).await()

                emit(Resource.Success(null))

            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Process failed!"))
            }
        }

    }

}
