package com.hms.socialsteps.ui

import android.app.Application
import com.besirkaraoglu.hmfcoroutineextensions.BuildConfig
//import com.hms.socialsteps.BuildConfig
import com.hms.socialsteps.domain.repository.CloudDbWrapper
import com.huawei.hms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class SocialStepsApplication :Application() {
    val TAG = "SocialStepsApplication"

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG)
            Timber.plant(Timber.DebugTree())
        CloudDbWrapper.initialize(this) {
            Timber.tag(TAG).i("Application %s", it.toString())
        }
        MapsInitializer.initialize(this)
    }
}