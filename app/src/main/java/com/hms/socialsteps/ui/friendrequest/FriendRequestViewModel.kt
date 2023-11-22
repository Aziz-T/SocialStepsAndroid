package com.hms.socialsteps.ui.friendrequest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Friendship
import com.hms.socialsteps.domain.repository.FriendshipRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val friendshipRepository: FriendshipRepository
): ViewModel() {
    val TAG = "FriendRequestViewModel"

    private val _friendRequests: MutableLiveData<Resource<List<Friendship>>> = MutableLiveData()
    val friendRequests: LiveData<Resource<List<Friendship>>> get() = _friendRequests

    private val _upsertOrDeleteResult: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val upsertOrDeleteResult: LiveData<Resource<Boolean>> get() = _upsertOrDeleteResult


    fun getFriendRequests(userId: String) {
        _friendRequests.value = Resource.Loading()
        friendshipRepository.queryFriendRequest(userId) { result ->
            _friendRequests.value = result
        }
    }

    fun replyFriendRequest(friendship: Friendship, isDelete: Boolean) {
        friendship.status = "3"

        val friendshipRecord2 = Friendship()
        friendshipRecord2.apply {
            friendShipId = (friendship.friendShipId).reversed()
            user1 = friendship.user2
            user1name = friendship.user2name
            user2 = friendship.user1
            user2name = friendship.user1name
            status = "3"
            sentRequestDate = System.currentTimeMillis().toString()
        }

        _upsertOrDeleteResult.value = Resource.Loading()
        friendshipRepository.upsertOrDeleteFriendship(friendship, friendshipRecord2, isDelete) {
            _upsertOrDeleteResult.value = it
        }
    }
}