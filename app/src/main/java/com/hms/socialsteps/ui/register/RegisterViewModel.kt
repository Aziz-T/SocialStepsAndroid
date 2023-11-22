package com.hms.socialsteps.ui.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.CloudDbWrapper
import com.hms.socialsteps.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel
    @Inject constructor(
        private val userRepository: UserRepository
    ): ViewModel() {
    val TAG = "RegisterViewModel"

    private val _registerResult = MutableLiveData<Resource<Boolean>>()
    val registerResult get() = _registerResult

    fun registerUser(user: Users) {
        _registerResult.value = Resource.Loading()
        userRepository.upsertUser(user){
            _registerResult.value = it
        }
    }


    fun calculateTargetCalories(gender: String, weight: Int, height: Int, age: Int, exerciseFrequency: Int, weightPurpose: String): String {

        val bmrMultiply = setBMR(exerciseFrequency)
        var bmr = 0.0

        when(gender) {
            "Male" -> {
                bmr = 66.5 + (13.75 * weight) + (5.003 * height) - (6.75 * age)
            }
            "Female" -> {
                bmr = 655.1 + (9.563 * weight) + (1.850 * height) - (4.676 * age)
            }
        }
        return when(weightPurpose) {
            "Gain" -> ((bmr * bmrMultiply) + 500).toInt().toString()
            "Lose" -> ((bmr * bmrMultiply) - 500).toInt().toString()
            else -> {
                return "0"
            }
        }

    }

    private fun setBMR(exerciseFrequency: Int): Double{

        return when(exerciseFrequency) {
            R.id.rbSedentary -> 1.2
            R.id.rbLightlyActive -> 1.375
            R.id.rbModeratelyActive -> 1.55
            R.id.rbVeryActive -> 1.725
            R.id.rbExtraActive -> 1.9
            else -> {
                return 0.0
            }
        }

    }
}