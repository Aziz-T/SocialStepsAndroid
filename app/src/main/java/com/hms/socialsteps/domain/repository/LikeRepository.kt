package com.hms.socialsteps.domain.repository

import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Likes
import com.hms.socialsteps.data.model.Users

class LikeRepository {

    fun upsertLike(likes: Likes, callback: (isSuccessful: Resource<Boolean>) -> Unit) {
        CloudDbWrapper.upsertLike(likes) { result ->
            callback(result)
        }
    }

    fun queryLikes(callback: (isSuccessful: Resource<List<Likes>>) -> Unit){
        CloudDbWrapper.queryLikes { result ->
            callback(result)
        }
    }

    fun deleteLike(like: Likes, callback: (isSuccessful: Resource<Boolean>) -> Unit) {
        CloudDbWrapper.deleteLike(like) { result ->
            callback(result)
        }
    }

    fun queryLikeId(userId: String, postId: String, callback: (isSuccessful: Resource<Likes>) -> Unit){
        CloudDbWrapper.queryLikeId(userId, postId){ result ->
            callback(result)
        }
    }

}