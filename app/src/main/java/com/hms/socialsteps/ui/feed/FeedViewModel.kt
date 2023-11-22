package com.hms.socialsteps.ui.feed

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.Constants.LIKE
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.SingleLiveEvent
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.*
import com.hms.socialsteps.domain.repository.*
import com.hms.socialsteps.data.model.Comments
import com.hms.socialsteps.data.model.Likes
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.LikeRepository
import com.hms.socialsteps.domain.repository.PostRepository
import com.hms.socialsteps.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class FeedViewModel
@Inject constructor(
    private val userRepository: UserRepository,
    private val userPreference: UserPreference,
    private val likeRepository: LikeRepository,
    private val likesCountRepository: LikesCountRepository,
    private val postRepository: PostRepository,
    private val commentsRepository: CommentsRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    val TAG = "FeedViewModel"

    private val _userRegistered = MutableLiveData<Resource<Users>>()
    val userRegistered get() = _userRegistered

    private val _upsertNotificationResult = MutableLiveData<Resource<Boolean>>()
    val upsertNotificationResult get() = _upsertNotificationResult

    private val _comments = MutableLiveData<Resource<List<Comments>>>()
    val comments get() = _comments

    private val _postList = MutableLiveData<Resource<List<Posts>>>()
    val postList get() = _postList

    private val _likes = SingleLiveEvent<Resource<List<Likes>>>()
    val likes get() = _likes

    var isLikeFetched = MutableLiveData<Boolean>(false)
    var isPostFetched = MutableLiveData<Boolean>(false)
    var isCommentFetched = MutableLiveData<Boolean>(false)

    val mediatorLiveData = MediatorLiveData<Boolean>()

    init {
        mediatorLiveData.addSource(
            likes
        ) { likes ->
            if (likes != null) {
                when (likes) {
                    is Resource.Success -> {
                        isLikeFetched.value = true
                        mediatorLiveData.value = isLikeFetched.value!! && isPostFetched.value!! && isCommentFetched.value!!
                    }
                    is Resource.Empty -> {
                        isLikeFetched.value = true
                    }
                    is Resource.Error -> {
                        isLikeFetched.value = false
                    }
                    is Resource.Loading -> {
                    }
                }
            }
        }

        mediatorLiveData.addSource(
            postList
        ) { postList ->
            if (postList != null) {
                when (postList) {
                    is Resource.Success -> {
                        isPostFetched.value = true
                        mediatorLiveData.value = isLikeFetched.value!! && isPostFetched.value!! && isCommentFetched.value!!
                    }
                    is Resource.Empty -> {

                    }
                    is Resource.Error -> {
                        isPostFetched.value = false
                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }

        mediatorLiveData.addSource(
            comments
        ) { comments ->
            if (comments != null) {
                when (comments) {
                    is Resource.Success -> {
                        isCommentFetched.value = true
                        mediatorLiveData.value = isLikeFetched.value!! && isPostFetched.value!! && isCommentFetched.value!!
                    }
                    is Resource.Empty -> {

                    }
                    is Resource.Error -> {

                    }
                    is Resource.Loading -> {

                    }
                }
            }
        }
    }

    fun checkIfUserRegistered(userId: String) {
        _userRegistered.value = Resource.Loading()
        userRepository.queryUser(userId) { mUserList ->
            _userRegistered.value = mUserList
        }
    }

    fun getUserFeed() {
        _postList.value = Resource.Loading()
        userRepository.queryFriendsIds(userPreference.getStoredUser().uid) { result ->
            when (result) {
                is Resource.Empty -> {
                    _postList.value = Resource.Empty()
                    mediatorLiveData.value = true
                }
                is Resource.Error -> {
                    _postList.value = Resource.Error(
                        result.message ?: "Error on getting friends!"
                    )
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    Timber.tag(TAG).d("getUserFeed: Friends Count:${result.data!!.size}")
                    getFeed(result.data!!)
                    getLikes()
                    getComments()
                }
            }
        }
    }

    private fun getFeed(data: List<String>) {
        postRepository.queryPosts(null) { result ->
            when (result) {
                is Resource.Empty -> {
                    _postList.value = Resource.Empty()
                }
                is Resource.Error -> {
                    _postList.value = result
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    val postList = mutableListOf<Posts>()
                    result.data!!.forEach { post ->
                        if (data.contains(post.userId))
                            postList.add(post)
                    }
                    _postList.value = Resource.Success(postList)
                }
            }
        }
    }

    private fun getLikes() {
        likeRepository.queryLikes { result ->
            _likes.value = result
        }
    }

    private fun getComments() {
        commentsRepository.getComments { result ->
            _comments.value = result
        }
    }

    private val _likeResult = MutableLiveData<Resource<Boolean>>()
    fun upsertLike(postId: String, userId: String) {
        _likeResult.value = Resource.Loading()
        val like = Likes()
        like.id = UUID.randomUUID().toString()
        like.postId = postId
        like.userId = userPreference.getStoredUser().uid!!

        likeRepository.upsertLike(like) {
            if (it is Resource.Success) {
                upsertNotification(postId, userId, null, LIKE)
            }
            _likeResult.value = it
        }
        likeRepository.upsertLike(like) {}
    }

    fun deleteLike(userId: String, postId: String) {
        likeRepository.queryLikeId(userId, postId) { result ->
            if (result is Resource.Success) {
                val like = Likes()
                like.id = result.data!!.id
                like.postId = postId
                like.userId = userPreference.getStoredUser().uid!!
                likeRepository.deleteLike(like) {}
            }
        }
    }

    private val _likesCountResult = MutableLiveData<Resource<LikesCount>>()
    fun upsertLikeCount(postId: String, isLiked: Boolean) {
        _likesCountResult.value = Resource.Loading()
        likesCountRepository.queryPostLikeCount(postId) {
            if (it is Resource.Success) {
                Timber.tag(TAG).d("upsertLikeCount: get like count.")
                val likeCount = it.data!!.likesCount
                val likesCount = LikesCount()
                likesCount.postId = postId
                if (isLiked)
                    likesCount.likesCount = likeCount + 1
                else
                    likesCount.likesCount = likeCount - 1
                likesCountRepository.upsertPostLikeCount(likesCount) {
                    if (it is Resource.Success) Timber.tag(TAG)
                        .d("upsertLikeCount: like count updated.")
                }
            }
            if (it is Resource.Empty) {
                val firstLikesCount = LikesCount()
                firstLikesCount.postId = postId
                firstLikesCount.likesCount = 1
                likesCountRepository.upsertPostLikeCount(firstLikesCount) {
                    if (it is Resource.Success) Timber.tag(TAG).d(
                        "upsertLikeCount: like count updated for first like of the post."
                    )
                }
            }

            _likesCountResult.value = it
        }

    }

    fun upsertNotification(
        postId: String,
        userId: String,
        notifications: Notifications?,
        notificationType: String?
    ) {
        _upsertNotificationResult.value = Resource.Loading()
        notificationRepository.upsertNotification(
            postId,
            userId,
            userPreference.getStoredUser(),
            notificationType,
            notifications
        ) {
            _upsertNotificationResult.value = it
        }
    }
}