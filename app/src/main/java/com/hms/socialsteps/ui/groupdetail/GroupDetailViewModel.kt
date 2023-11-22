package com.hms.socialsteps.ui.groupdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.GroupStatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupDetailViewModel
    @Inject constructor(
    private val groupStatisticsRepository: GroupStatisticsRepository,
    private val userPreference: UserPreference
    ): ViewModel() {

    private val _leaveResult = MutableLiveData<Resource<Any?>>()
    val leaveResult get() = _leaveResult

        fun leaveGroup(userId: String,groupId: String) = viewModelScope.launch{
            groupStatisticsRepository.deleteGroupMemberByUserId(userId,groupId).collect{
                _leaveResult.postValue(it)
            }
        }

    fun getCurrentUser(): Users {
        return userPreference.getStoredUser()
    }
}