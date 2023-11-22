package com.hms.socialsteps.ui.visitedprofile.visitedusertargetinformation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.data.model.Activities
import com.hms.socialsteps.data.model.Friendship
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.ActivityRepository
import com.hms.socialsteps.domain.repository.HealthDataRepository
import com.hms.socialsteps.domain.repository.UserRepository
import com.huawei.hms.hihealth.data.ActivityRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class VisitedUserTargetViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    val TAG = "UserTargetViewModel"

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

    fun calculateCpiProgress(stepsTarget: Int, stepsValue: Int): Int {
        return if (stepsTarget == 0) {
            0
        } else {
            (100 * (stepsValue)) / stepsTarget
        }

    }

    fun calculateCpiProgress(caloriesTarget: Int, caloriesIntake: Int, caloriesBurned: Int): Int {
        return if (caloriesBurned > caloriesIntake) {
            0
        } else {
            (100 * (caloriesIntake - caloriesBurned)) / caloriesTarget
        }
    }
}