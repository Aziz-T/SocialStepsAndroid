package com.hms.socialsteps.domain.repository

import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Likes
import com.hms.socialsteps.data.model.Posts

class PostRepository {
    fun upsertPost(posts: Posts, callback: (isSuccessful: Resource<Boolean>) -> Unit) {
        CloudDbWrapper.upsertPost(posts) { result ->
            callback(result)
        }
    }

    fun queryPosts(postId: String?, callback: (isSuccessful: Resource<List<Posts>>) -> Unit){
        CloudDbWrapper.queryPosts(postId) { result ->
            callback(result)
        }
    }

    fun queryUserPosts(userId: String?, callback: (isSuccessful: Resource<List<Posts>>) -> Unit){
        CloudDbWrapper.queryUserPosts(userId) { result ->
            callback(result)
        }
    }

    fun queryLikes(callback: (isSuccessful: Resource<List<Likes>>) -> Unit){
        CloudDbWrapper.queryLikes { result ->
            callback(result)
        }
    }
}