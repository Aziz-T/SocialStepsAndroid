package com.hms.socialsteps.domain.repository

import android.content.Intent
import com.hms.socialsteps.core.util.Resource
import com.huawei.hms.hihealth.SettingController

class HealthAuthRepository {
    fun requestHealthAuth(data: Intent, mSettingController: SettingController): Resource<String> {
        val result = mSettingController.parseHealthKitAuthResultFromIntent(data)
            ?: return Resource.Error("Authorization Fail")
        return if (result.isSuccess) {
            Resource.Success("Authorization success")
        }else {
            Resource.Error("Authorization Fail, errorCode: " + result.errorCode)
        }
    }
}