package com.hms.socialsteps.ui.visitedprofile.visiteduserprofileinformation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.SingleLiveEvent
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.*
import com.hms.socialsteps.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VisitedUserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val friendshipRepository: FriendshipRepository,
    private val postRepository: PostRepository,
    private val likeRepository: LikeRepository,
    private val commentsRepository: CommentsRepository,
    private val userPreference: UserPreference
): ViewModel() {
    val TAG = "UserProfileViewModel"

    private val _user: MutableLiveData<Resource<Users>> = MutableLiveData()
    val user: LiveData<Resource<Users>> get() = _user

    private val _upsertResult: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val upsertResult: LiveData<Resource<Boolean>> get() = _upsertResult

    private val _friendShip: MutableLiveData<Resource<Friendship>> = MutableLiveData()
    val friendship: LiveData<Resource<Friendship>> get() = _friendShip

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
                        mediatorLiveData.value =
                            isLikeFetched.value!! && isPostFetched.value!! && isCommentFetched.value!!
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
                        mediatorLiveData.value =
                            isLikeFetched.value!! && isPostFetched.value!! && isCommentFetched.value!!
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
                        mediatorLiveData.value =
                            isLikeFetched.value!! && isPostFetched.value!! && isCommentFetched.value!!
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

    fun getUser(userId: String) {
        _user.value = Resource.Loading()
        userRepository.queryUser(userId) { result ->
            _user.value = result
        }
    }

    private fun getFeed(data: String?) {
        postRepository.queryUserPosts(data) { result ->
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
                        if (data!!.contains(post.userId))
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

    fun sendFriendRequest(loggedUserId: String, visitedUserId: String, visitedUserName: String) {
        val friendshipRecord = Friendship()
        friendshipRecord.apply {
            friendShipId = loggedUserId + visitedUserId
            user1 = loggedUserId
            user1name = userPreference.getStoredUser().username
            user1Photo = userPreference.getStoredUser().photo
            user2 = visitedUserId
            user2name = visitedUserName
            status = "1"
            sentRequestDate = System.currentTimeMillis().toString()
        }
        val friendshipRecord2 = Friendship()
        friendshipRecord2.apply {
            friendShipId = (loggedUserId + visitedUserId).reversed()
            user1 = visitedUserId
            user1name = visitedUserName
            user1Photo = userPreference.getStoredUser().photo
            user2 = loggedUserId
            user2name = userPreference.getStoredUser().username
            status = "2"
            sentRequestDate = System.currentTimeMillis().toString()
        }

        _upsertResult.value = Resource.Loading()
        friendshipRepository.upsertOrDeleteFriendship(friendshipRecord, friendshipRecord2, false) {
            _upsertResult.value = it
        }
    }

    fun queryFriendShipStatus(userId: String, visitedUserId: String) {
        _friendShip.value = Resource.Loading()
        Log.e("queryFriendShipStatus", "${userId}, ${visitedUserId}")
        friendshipRepository.queryFriendShipStatus(userId, visitedUserId) {
            Log.e("queryFriendShipStatus itititit", "${it.data}")
            Log.e("query", "${userId}, ${visitedUserId}")
            _friendShip.value = it
        }
    }

    fun getUserFeed(visitedUserUID: String) {
        _postList.value = Resource.Loading()
        userRepository.queryFriendsIds(visitedUserUID) { result ->
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
                    getFeed(visitedUserUID)
                    getLikes()
                    getComments()
                }
            }
        }
    }
}