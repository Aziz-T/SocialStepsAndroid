package com.hms.socialsteps.domain.repository

import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Activities
import com.hms.socialsteps.data.model.Users

class ActivityRepository {

    fun queryActivities(userId: String?, activityId: String?, activityList: (Resource<ArrayList<Activities>>) -> Unit) {
        CloudDbWrapper.queryActivities(userId, activityId) { result ->
            activityList(result)
        }
    }

    fun upsertActivity(activities: List<Activities>, callback: (isSuccessful: Resource<Boolean>) -> Unit) {
        CloudDbWrapper.upsertActivities(activities) { result ->
            callback(result)
        }
    }
}