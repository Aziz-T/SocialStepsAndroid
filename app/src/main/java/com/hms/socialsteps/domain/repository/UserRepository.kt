package com.hms.socialsteps.domain.repository

import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Users

class UserRepository {

    fun queryUsers(isRankQuery: Boolean, userList: (Resource<ArrayList<Users>>) -> Unit) {
        CloudDbWrapper.queryUsers(isRankQuery) { result ->
            userList(result)
        }
    }

    fun queryUser(userId: String, userList: ((Resource<Users>) -> Unit)) {
        CloudDbWrapper.queryUser(userId) { result ->
            userList(result)
        }
    }

    fun upsertUser(users: Users, callback: (isSuccessful: Resource<Boolean>) -> Unit) {
        CloudDbWrapper.upsertUser(users) { result ->
            callback(result)
        }
    }

    fun queryFriendsIds(userId: String, callback: (isSuccessful: Resource<List<String>>) -> Unit) {
        CloudDbWrapper.queryFriendIds(userId) { result ->
            callback(result)
        }
    }
}