package com.hms.socialsteps.ui.statistics.groupstatistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hms.socialsteps.core.util.Constants.GROUP_ADD
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.SingleLiveEvent
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.Group
import com.hms.socialsteps.data.model.GroupInformation
import com.hms.socialsteps.data.model.GroupMembers
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.GroupStatisticsRepository
import com.hms.socialsteps.domain.repository.NotificationRepository
import com.hms.socialsteps.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupStatisticsViewModel
@Inject constructor(
    private val userRepository: UserRepository,
    private val userPreference: UserPreference,
    private val groupStatisticsRepository: GroupStatisticsRepository,
    private val notificationRepository: NotificationRepository
) :ViewModel() {

    private val _friendList = MutableLiveData<Resource<List<String>?>>()
    val friendList get() = _friendList

    private val _userList = MutableLiveData<Resource<ArrayList<Users>>>()
    val userList get() = _userList

    private val _groupList = SingleLiveEvent<Resource<List<GroupInformation>>>()
    val groupList get() = _groupList

    private val _upsertGroupInfoResult = SingleLiveEvent<Resource<Boolean>>()
    val upsertGroupInfoResult get() = _upsertGroupInfoResult

    private val _upsertGroupMembersResult = SingleLiveEvent<Resource<Boolean>>()
    val upsertGroupMembersResult get() = _upsertGroupMembersResult

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
                            userList.add(user)
                        }
                    }
                    _userList.value = Resource.Success(userList)
                }
            }
        }
    }

    fun upsertGroup(groupInformation: GroupInformation) {
        _upsertGroupInfoResult.postValue(Resource.Loading())
        groupStatisticsRepository.upsertGroupInformation(groupInformation) { result ->
            _upsertGroupInfoResult.postValue(result)
        }
    }

    fun upsertGroupMembers(groupMembers: ArrayList<GroupMembers>) {
        _upsertGroupMembersResult.postValue(Resource.Loading())
        groupStatisticsRepository.upsertGroupMembers(groupMembers) { result ->
            _upsertGroupMembersResult.postValue(result)
        }
    }

    fun queryGroup(groupID: String) {
        _groupList.postValue(Resource.Loading())

        groupStatisticsRepository.queryGroup(groupID) { result ->
            _groupList.postValue(result)
        }
    }

    private val _usersGroupsResult = MutableLiveData<Resource<List<Group>>>()
    val usersGroupsResult get() = _usersGroupsResult

    fun queryUserGroups() = viewModelScope.launch{
        groupStatisticsRepository.queryGroupsByUserId(
            userPreference.getStoredUser().uid).collect{
                _usersGroupsResult.postValue(it)
            }
    }

    private val _groupNotificationResult = MutableLiveData<Resource<Boolean>>()
    val groupNotification get() = _groupNotificationResult

    fun sendGroupNotification(list: ArrayList<GroupMembers>, groupInformation: GroupInformation){
        _groupNotificationResult.value = Resource.Loading()
        list.forEach {
            notificationRepository.upsertNotification("",it.userID,
                userPreference.getStoredUser(),GROUP_ADD,null, groupInformation){ result ->
                _groupNotificationResult.value = result
            }
        }
    }
}