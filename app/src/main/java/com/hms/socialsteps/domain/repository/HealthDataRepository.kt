package com.hms.socialsteps.domain.repository

import android.util.Log
import com.hms.socialsteps.core.util.Resource
import com.huawei.hmf.tasks.Task
import com.huawei.hms.hihealth.ActivityRecordsController
import com.huawei.hms.hihealth.DataController
import com.huawei.hms.hihealth.data.ActivityRecord
import com.huawei.hms.hihealth.data.DataType
import com.huawei.hms.hihealth.data.SampleSet
import com.huawei.hms.hihealth.options.ActivityRecordReadOptions
import com.huawei.hms.hihealth.result.ActivityRecordReply
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class HealthDataRepository @Inject constructor(
    private val dataController: DataController,
    private val activityRecordsController: ActivityRecordsController
) {

    fun getDailyCaloriesFromHealthKit(result: (Resource<String>)-> Unit) {
        val todaySummationTask: Task<SampleSet> =
            dataController.readTodaySummation(DataType.DT_CONTINUOUS_CALORIES_BURNT)

        todaySummationTask.addOnSuccessListener { sampleSet ->
            if (sampleSet.samplePoints.isNullOrEmpty()) {
                result(Resource.Empty())
            }else {
                result(Resource.Success(sampleSet.samplePoints[0].fieldValues["calories_total(f)"].toString()))
            }
        }.addOnFailureListener { exception ->
           result(Resource.Error(exception.message.toString()))
        }
    }

    fun getDailyStepsFromHealthKit(result: (Resource<String>)-> Unit) {
        val todaySummationTask: Task<SampleSet> =
            dataController.readTodaySummation(DataType.DT_CONTINUOUS_STEPS_DELTA)

        todaySummationTask.addOnSuccessListener { sampleSet ->
            if (sampleSet.samplePoints.isNullOrEmpty()) {
                result(Resource.Empty())
            } else {
                result(Resource.Success(sampleSet.samplePoints[0].fieldValues["steps(i)"].toString()))
            }
        }.addOnFailureListener { exception ->
            result(Resource.Error(exception.message.toString()))
        }
    }

    fun getDailyActivitiesFromHealthKit(result: (Resource<List<ActivityRecord>>) -> Unit) {
        val cal = Calendar.getInstance()
        val now = Date()
        cal.time = now
        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = cal.timeInMillis

        val readOption =
            ActivityRecordReadOptions.Builder().setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                .readActivityRecordsFromAllApps()
                .build()

        val getTask = activityRecordsController.getActivityRecord(readOption)
        getTask.addOnSuccessListener { activityRecordReply ->
            result(Resource.Success(activityRecordReply.activityRecords))
        }.addOnFailureListener {
            result(Resource.Error(it.message.toString()))
        }
    }
}