package com.hms.socialsteps.ui.comment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hms.socialsteps.core.util.Constants.COMMENT
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.Comments
import com.hms.socialsteps.data.model.Notifications
import com.hms.socialsteps.data.model.LikesCount
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.domain.repository.CloudDbWrapper
import com.hms.socialsteps.domain.repository.CommentsRepository
import com.hms.socialsteps.domain.repository.NotificationRepository
import com.hms.socialsteps.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CommentViewModel
@Inject constructor(
    private val commentsRepository: CommentsRepository,
    private val postRepository: PostRepository,
    private val userPreference: UserPreference,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    val TAG = "CommentViewModel"

    private val _commentList = MutableLiveData<Resource<List<Comments>>>()
    val commentList get() = _commentList
    private val _insertResult = MutableLiveData<Resource<Comments>>()
    val insertResult get() = _insertResult

    private val _postList = MutableLiveData<Resource<List<Posts>>>()
    val postList get() = _postList

    fun getPost(postId: String) {
        postRepository.queryPosts(postId) { result ->
            _postList.value = Resource.Loading()
            when(result) {
                is Resource.Error -> {
                    _postList.value = Resource.Error(result.message.toString())
                }
                is Resource.Success -> {
                    _postList.value = Resource.Success(result.data!!)
                }
                else ->{}
            }
        }
    }

    suspend fun getSnapshotComments(postId: String) {
        viewModelScope.launch {
            commentsRepository.getSnapShotComments(postId).collect(){
                commentList.value = it
            }
        }
    }

    fun insertComment(comment:String,postId:String, userId: String){
        _insertResult.value = Resource.Loading()
        commentsRepository.insertComment(comment,postId,userPreference.getStoredUser().uid, userPreference.getStoredUser().username,
        userPreference.getStoredUser().photo){
            _insertResult.value=it
            if (_insertResult.value is Resource.Success) {
                upsertNotification(postId, userId, null, COMMENT)
            }
        }
    }

    private fun upsertNotification(
        postId: String,
        userId: String,
        notifications: Notifications?,
        notificationType: String?
    ) {
        notificationRepository.upsertNotification(
            postId,
            userId,
            userPreference.getStoredUser(),
            notificationType,
            notifications
        ) {
        }
    }

}