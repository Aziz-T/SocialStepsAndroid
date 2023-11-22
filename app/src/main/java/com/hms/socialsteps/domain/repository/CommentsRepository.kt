package com.hms.socialsteps.domain.repository

import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Comments
import java.util.UUID

class CommentsRepository {
    fun getComments(result: (Resource<List<Comments>>) -> Unit) {
        CloudDbWrapper.queryComments(result)
    }

    suspend fun getSnapShotComments(postId: String) = CloudDbWrapper.getCommentDataChanges(postId)

    fun insertComment(comment:String,postId:String,userId:String, username: String, userPhoto: String?, result: (Resource<Comments>) -> Unit){
        val mComment = Comments()
        mComment.commentId=UUID.randomUUID().toString()
        mComment.postId=postId
        mComment.userId=userId
        mComment.userName = username
        if (userPhoto.isNullOrEmpty()) {
            mComment.userPhoto = ""
        }else {
            mComment.userPhoto = userPhoto
        }
        mComment.userPhoto = userPhoto
        mComment.commentContent= comment
        CloudDbWrapper.insertComment(mComment,result)

    }
}