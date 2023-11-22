package com.hms.socialsteps.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.LiveNetworkMonitor
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.Activities
import com.hms.socialsteps.data.model.Group
import com.hms.socialsteps.data.model.Tokens
import com.hms.socialsteps.domain.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject constructor(
        private val tokenRepository: TokenRepository,
        private val userPreference: UserPreference,
        val networkMonitor: LiveNetworkMonitor
    ): ViewModel() {

    var uId : String = ""

    private val _activity: MutableLiveData<Activities> = MutableLiveData()
    val activity: LiveData<Activities> get() = _activity

    fun setActivity(activity: Activities){
        _activity.value = activity
    }

    private val _tokenResult = MutableLiveData<Resource<Any?>>()
    val tokenResult get() = _tokenResult

    fun setToken(mToken: String){
        _tokenResult.postValue(Resource.Loading())
        val token = Tokens()
        token.token = mToken
        token.userId = userPreference.getStoredUser().uid
        tokenRepository.upsertToken(token){
            _tokenResult.postValue(it)
        }
    }

    private val _selectedGroup = MutableLiveData<Group>()
    val selectedGroup get() = _selectedGroup

    fun setSelectedGroup(group: Group){
        _selectedGroup.value = group
    }

}