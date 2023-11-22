package com.hms.socialsteps.ui.statistics.friendstatistics

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.Constants.STEP
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.core.util.getCurrentDate
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FriendStatisticsViewModel
@Inject constructor(
    private val userRepository: UserRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    private val _friendList = MutableLiveData<Resource<List<String>?>>()
    val friendList get() = _friendList

    private val _userList = MutableLiveData<Resource<ArrayList<Users>>>()
    val userList get() = _userList

    var selectedFilter = STEP

    fun getUserFriends() {
        _friendList.value = Resource.Loading()
        userRepository.queryFriendsIds(userPreference.getStoredUser().uid) { result ->
            when (result) {
                is Resource.Empty -> {
                    _friendList.value = Resource.Empty()
                }
                is Resource.Error -> {
                    _friendList.value = Resource.Error(
                        result.message ?: "Error on getting friends!"
                    )
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    _friendList.value = Resource.Success(result.data)
                }
            }
        }
    }

    fun getFriendsStatistics(friendList: List<String>) {
        _userList.value = Resource.Loading()

        userRepository.queryUsers(true) { result ->
            when (result) {
                is Resource.Empty -> {
                    _userList.value = Resource.Empty()
                }
                is Resource.Error -> {
                    _userList.value = result
                }
                is Resource.Loading -> {}
                is Resource.Success -> {
                    val userList = arrayListOf<Users>()
                    result.data!!.forEach { user ->
                        if (friendList.contains(user.uid)) {
                            if (user.lastDailyStepsUpdatedTime != null) {
                                if (getCurrentDate().substring(0, 9) != user.lastDailyStepsUpdatedTime.substring(0, 9)) {
                                    user.dailyStepsCount = "0"
                                }
                            } else{
                                user.dailyStepsCount = "0"
                            }
                            if (user.lastDailyCaloriesUpdatedTime != null) {
                                if (getCurrentDate().substring(0, 9) != user.lastDailyCaloriesUpdatedTime.substring(0, 9)) {
                                    user.caloriesBurned = "0"
                                }
                            } else{
                                user.caloriesBurned = "0"
                            }

                            userList.add(user)

                        }
                    }
                    _userList.value = Resource.Success(userList)
                }
            }
        }
    }
}