
package com.hms.socialsteps.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.SingleLiveEvent
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.Comments
import com.hms.socialsteps.data.model.Likes
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel
@Inject constructor(
    private val userRepository: UserRepository,
    private val healthDataRepository: HealthDataRepository,
    private val likeRepository: LikeRepository,
    private val commentsRepository: CommentsRepository,
    private val postRepository: PostRepository,
    private val userPreference: UserPreference
) : ViewModel() {
    val TAG = "ProfileViewModel"

    private val _user: MutableLiveData<Resource<Users>> = MutableLiveData()
    val user: LiveData<Resource<Users>> get() = _user

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
        userRepository.queryUser(userId) {
            _user.value = it
        }
    }

    private fun getFeed(data: String?) {
        postRepository.queryUserPosts(userPreference.getStoredUser().uid) { result ->
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
                    getFeed(userPreference.getStoredUser().uid)
                    getLikes()
                    getComments()
                }
            }
        }
    }

}