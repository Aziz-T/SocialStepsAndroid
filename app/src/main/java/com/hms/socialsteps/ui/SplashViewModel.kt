package com.hms.socialsteps.ui

import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.Users
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userPreference: UserPreference
): ViewModel() {

    fun saveLoggedUser(users: Users) {
        userPreference.setStoredUser(users)
    }
}