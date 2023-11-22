package com.hms.socialsteps.ui.login

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.HuaweiAuthResult
import com.hms.socialsteps.domain.usecase.HealthAuthUseCase
import com.hms.socialsteps.domain.usecase.LoginUseCase
import com.huawei.agconnect.auth.AGConnectUser
import com.huawei.hms.hihealth.SettingController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
): ViewModel() {

    private val signInHuaweiIdLiveData: MutableLiveData<Resource<AGConnectUser?>> =
        MutableLiveData()

    fun getSignInHuaweiIdLiveData(): LiveData<Resource<AGConnectUser?>> = signInHuaweiIdLiveData

    fun signInWithHuaweiId(requestCode: Int, data: Intent?) {
        viewModelScope.launch {
            signInHuaweiIdLiveData.value = Resource.Loading()

            when (val result = loginUseCase.signInWithHuaweiId(requestCode, data)) {
                is HuaweiAuthResult.UserSuccessful -> {
                    signInHuaweiIdLiveData.value = Resource.Success(result.user)
                }
                is HuaweiAuthResult.UserFailure -> {
                    signInHuaweiIdLiveData.value = result.errorMessage?.let { Resource.Error(it) }
                }
            }
        }
    }
}