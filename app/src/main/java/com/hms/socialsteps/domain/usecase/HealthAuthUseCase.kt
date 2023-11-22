package com.hms.socialsteps.domain.usecase

import android.content.Intent
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.domain.repository.HealthAuthRepository
import com.huawei.hms.hihealth.SettingController

class HealthAuthUseCase(
    private val healthAuthRepository: HealthAuthRepository
) {

    fun execute(data: Intent, mSettingController: SettingController): Resource<String> {
        return healthAuthRepository.requestHealthAuth(data, mSettingController)
    }
}
