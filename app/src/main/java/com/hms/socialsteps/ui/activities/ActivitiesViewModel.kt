package com.hms.socialsteps.ui.activities

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.*
import com.hms.socialsteps.core.util.Constants.RUNNING
import com.hms.socialsteps.core.util.Constants.WALKING
import com.hms.socialsteps.data.model.Activities
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
class ActivitiesViewModel
@Inject constructor(
    private val userRepository: UserRepository,
    private val activityRepository: ActivityRepository,
    private val healthDataRepository: HealthDataRepository,
    private val userPreference: UserPreference
): ViewModel() {
    val TAG = "ActivitiesViewModel"
    private var isCaloriesFetched = false
    private var isActivitiesFetched = false

    private val _activities: MutableLiveData<Resource<ArrayList<Activities>>> = MutableLiveData()
    val activities: LiveData<Resource<ArrayList<Activities>>> get() = _activities

    private val _user: MutableLiveData<Resource<Users>> = MutableLiveData()
    val user : LiveData<Resource<Users>> get() = _user

    private val _upsertResult = SingleLiveEvent<Resource<Boolean>>()
    val upsertResult get() = _upsertResult

    private val _dailyCalories = MutableLiveData<Resource<Boolean>>()
    val dailyCalories get() = _dailyCalories

    private val _isOldDataFetched = MutableLiveData<Boolean>()
    val isOldDataFetched = _isOldDataFetched

    private val _healthActivities = MutableLiveData<Resource<Boolean>>()
    val healthActivities get() = _healthActivities

    private val isLastCaloriesFetched = MutableLiveData<Boolean>()
    private val isLastActivitiesFetched = MutableLiveData<Boolean>()

    private val _isDailyStepsFetched: MutableLiveData<Resource<Boolean>> = MutableLiveData()
    val isDailyStepsFetched: LiveData<Resource<Boolean>> get() = _isDailyStepsFetched

    val mediatorLiveData = MediatorLiveData<Boolean>()

    init {
        mediatorLiveData.addSource(
            isLastCaloriesFetched
        ) { dailyCalories ->
            if (dailyCalories != null) {
                if (dailyCalories) {
                    isCaloriesFetched = true
                    mediatorLiveData.value = isCaloriesFetched && isActivitiesFetched
                }
            }
        }
        mediatorLiveData.addSource(
            isLastActivitiesFetched
        ) { activities ->
            if (activities != null) {
                if (activities) {
                    isActivitiesFetched = true
                    mediatorLiveData.value = isCaloriesFetched && isActivitiesFetched
                }
            }
        }
    }



    fun upsertUser(user: Users) {
        _upsertResult.value = Resource.Loading()
        userRepository.upsertUser(user){
            _upsertResult.value = it
        }
    }

    fun getLatestActivities() {
        val upsertedActivities = ArrayList<Activities>()
        var isActivityExist = false
        if (activities.value is Resource.Empty){
            healthDataRepository.getDailyActivitiesFromHealthKit { activityRecordsList ->
                if (activityRecordsList is Resource.Error){
                    _healthActivities.value = Resource.Error(activityRecordsList.message.toString())
                }else {
                    if (activityRecordsList.data!!.isEmpty()) {
                        isLastActivitiesFetched.value = true
                    } else {
                        for (healthActivity in activityRecordsList.data) {
                            if (healthActivity.activityType == WALKING || healthActivity.activityType.startsWith(RUNNING)){
                                addCurrentActivityToList(healthActivity, upsertedActivities)
                            }
                        }
                        activityRepository.upsertActivity(upsertedActivities) {
                            if (it is Resource.Success) {
                                activityRepository.queryActivities(userPreference.getStoredUser().uid, null) {activities ->
                                    _activities.value = activities
                                    isLastActivitiesFetched.value = true
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            healthDataRepository.getDailyActivitiesFromHealthKit { activityRecordsList ->
                if (activityRecordsList is Resource.Error) {
                    _healthActivities.value = Resource.Error(activityRecordsList.message.toString())
                }else {
                    for (healthActivity in activityRecordsList.data!!) {
                        for (cloudActivity in activities.value!!.data!!) {
                            if (healthActivity.id == cloudActivity.activityId) {
                                isActivityExist = true
                            }
                        }
                        if (!isActivityExist) {
                            if (healthActivity.activityType == WALKING || healthActivity.activityType.startsWith(RUNNING)){
                                addCurrentActivityToList(healthActivity, upsertedActivities)
                                isActivityExist = false
                            }
                        }
                        isActivityExist = false
                    }
                    activityRepository.upsertActivity(upsertedActivities) {
                        if (it is Resource.Success) {
                            activityRepository.queryActivities(userPreference.getStoredUser().uid, null) {activities ->
                                _activities.value = activities
                                isLastActivitiesFetched.value = true
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addCurrentActivityToList(healthActivity: ActivityRecord, upsertedActivities: ArrayList<Activities>) {
        val currentActivity = Activities()
        currentActivity.apply {
            activityId = healthActivity.id
            uid = userPreference.getStoredUser().uid
            activityType = if (healthActivity.activityType == WALKING) {
                "walk"
            }else {
                "run"
            }
            activityStartTime = convertMillisecondsToDate(healthActivity.getStartTime(TimeUnit.MILLISECONDS))
            duration = convertMilliSecondsToHours(healthActivity.getDurationTime(TimeUnit.MILLISECONDS))
            calories = healthActivity.activitySummary.dataSummary[0].fieldValues["calories_total(f)"].toString().toFloat().toInt()
            totalDistance = healthActivity.activitySummary.dataSummary[2].fieldValues["distance(f)"].toString().toFloat().roundToInt().toString()
            totalStep = healthActivity.activitySummary.dataSummary[1].fieldValues["steps(i)"].toString()
        }
        upsertedActivities.add(currentActivity)
    }

    fun getActivities(userId: String?, activityId: String?) {
        _activities.value = Resource.Loading()
        activityRepository.queryActivities(userId, activityId) { activities ->
            _activities.value = activities
            _isOldDataFetched.value = true
        }
    }


    fun calculateCpiProgress(caloriesTarget: Int, caloriesIntake: Int, caloriesBurned: Int): Int {
        return if (caloriesBurned > caloriesIntake) {
            0
        }else {
            (100 * (caloriesIntake - caloriesBurned)) / caloriesTarget
        }
    }

    fun getDailyCaloriesFromHealthKit() {
        _dailyCalories.value = Resource.Loading()
        healthDataRepository.getDailyCaloriesFromHealthKit { result ->
            if (result is Resource.Success) {
                Log.e(TAG, "success")   ///bu kisimlar kaldirilacak
                Log.e(TAG, "result ${result}")
                Log.e(TAG, "result data ${result.data}")
                Log.e(TAG, "result message ${result.message}")
                Log.e(TAG, "daily calories empty ${_dailyCalories.value}")
                if (result.data?.toFloat()?.toInt() != userPreference.getStoredUser().caloriesBurned.toInt()) {
                    val user = userPreference.getStoredUser()
                    user.caloriesBurned = result.data?.toFloat()?.toInt().toString()
                    user.lastDailyCaloriesUpdatedTime = getCurrentDate()
                    userRepository.upsertUser(user) {
                        _dailyCalories.value = it
                    }
                }else {
                    _dailyCalories.value = Resource.Success(true)
                }
            } else if (result is Resource.Error) {
                Log.e(TAG, "daily calories error ${_dailyCalories.value}")
                _dailyCalories.value = Resource.Error(result.message.toString())
            }else if (result is Resource.Empty) {
                Log.e(TAG, "empty")
                Log.e(TAG, "result ${result}")
                Log.e(TAG, "result data ${result.data}")
                Log.e(TAG, "result message ${result.message}")
                Log.e(TAG, "daily calories empty ${_dailyCalories.value}")
                _dailyCalories.value = Resource.Empty()
            }
            isLastCaloriesFetched.value = true
        }
    }

    fun getDailyStepsFromHealthKit() {
        var currentValue: Int
        _isDailyStepsFetched.value = Resource.Loading()
        healthDataRepository.getDailyStepsFromHealthKit() { result ->
            if (result is Resource.Success) {
                currentValue = if (userPreference.getStoredUser().dailyStepsCount == null) {
                    0
                } else {
                    userPreference.getStoredUser().dailyStepsCount.toInt()
                }
                if (result.data?.toInt() != currentValue) {
                    val user = userPreference.getStoredUser()
                    user.dailyStepsCount = result.data?.toInt().toString()
                    user.lastDailyStepsUpdatedTime = getCurrentDate()
                    userRepository.upsertUser(user) { upsertResult ->
                        _isDailyStepsFetched.value = upsertResult
                    }
                }else {
                    _isDailyStepsFetched.value = Resource.Success(true)
                }
            } else if (result is Resource.Error) {
                _isDailyStepsFetched.value = Resource.Error(result.message.toString())
            }else if (result is Resource.Empty) {
                _isDailyStepsFetched.value = Resource.Empty()
            }
        }
    }

    fun calculateCpiProgress(stepsTarget: Int, stepsValue: Int): Int {
        return if (stepsTarget == 0) {
            0
        }else {
            (100 * (stepsValue)) / stepsTarget
        }

    }
}