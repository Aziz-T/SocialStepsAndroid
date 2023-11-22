package com.hms.socialsteps.ui.visitedprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.Friendship
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.FriendshipRepository
import com.hms.socialsteps.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VisitedProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val friendshipRepository: FriendshipRepository,
    private val userPreference: UserPreference
): ViewModel() {
    val TAG = "VisitedProfileViewModel"

    private val _user: MutableLiveData<Resource<Users>> = MutableLiveData()
    val user: LiveData<Resource<Users>> get() = _user

    private val _upsertResult: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val upsertResult: LiveData<Resource<Boolean>> get() = _upsertResult

    private val _friendShip: MutableLiveData<Resource<Friendship>> = MutableLiveData()
    val friendship: LiveData<Resource<Friendship>> get() = _friendShip

    fun getUser(userId: String) {
        _user.value = Resource.Loading()
        userRepository.queryUser(userId) { result ->
            _user.value = result
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
        friendshipRepository.queryFriendShipStatus(userId, visitedUserId) {
            _friendShip.value = it
        }
    }
}