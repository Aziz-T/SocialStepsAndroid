package com.hms.socialsteps.domain.repository

import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Friendship

class FriendshipRepository {

    fun upsertOrDeleteFriendship(friendship: Friendship, friendship2: Friendship, isDelete: Boolean, callback: (isSuccessful: Resource<Boolean>) -> Unit) {
        CloudDbWrapper.upsertOrDeleteFriendShip(friendship, friendship2, isDelete) { result ->
            callback(result)
        }
    }

    fun queryFriendShipStatus(userId: String, visitedUserId: String, friendship: ((Resource<Friendship>)-> Unit)) {
        CloudDbWrapper.queryFriendShipStatus(userId, visitedUserId) { result ->
            friendship(result)
        }
    }

    fun queryFriendRequest(userId: String, friendship: ((Resource<List<Friendship>>) -> Unit)) {
        CloudDbWrapper.queryFriendRequests(userId) { result ->
            friendship(result)
        }
    }

}