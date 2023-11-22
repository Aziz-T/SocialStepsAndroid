package com.hms.socialsteps.domain.repository

import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Friendship
import com.hms.socialsteps.data.model.LikesCount
import com.hms.socialsteps.data.model.Users

class LikesCountRepository {

    fun upsertPostLikeCount(likesCount: LikesCount, callback: (isSuccessful: Resource<Boolean>) -> Unit) {
        CloudDbWrapper.upsertLikeCount(likesCount) { result ->
            callback(result)
        }
    }

    fun queryPostLikeCount(postId: String, postLikesCountList: ((Resource<LikesCount>) -> Unit)) {
        CloudDbWrapper.queryPostLikeCount(postId) { result ->
            postLikesCountList(result)
        }
    }


}