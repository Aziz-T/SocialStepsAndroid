package com.hms.socialsteps.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.CloudDbWrapper
import com.hms.socialsteps.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject constructor(
        private val userRepository: UserRepository
    ): ViewModel() {

    private val _userListLiveData: MutableLiveData<Resource<ArrayList<Users>>> = MutableLiveData()
    val userListLiveData: LiveData<Resource<ArrayList<Users>>> get() = _userListLiveData


    fun getUserList() {
        _userListLiveData.value = Resource.Loading()
        userRepository.queryUsers(false) {
            _userListLiveData.value = it
        }
    }
}