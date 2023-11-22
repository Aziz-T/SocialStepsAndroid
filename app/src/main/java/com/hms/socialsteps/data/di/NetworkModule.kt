package com.hms.socialsteps.data.di

import android.content.Context
import android.net.ConnectivityManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hms.socialsteps.core.util.Constants
import com.hms.socialsteps.core.util.LiveNetworkMonitor
import com.hms.socialsteps.data.DirectionApis
import com.hms.socialsteps.utils.MapUtils
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.hihealth.ActivityRecordsController
import com.huawei.hms.hihealth.DataController
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.SettingController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideAgcConnectAuth(): AGConnectAuth = AGConnectAuth.getInstance()

    @Singleton
    @Provides
    fun provideSettingController(@ApplicationContext appContext: Context): SettingController =
        HuaweiHiHealth.getSettingController(appContext)

    @Singleton
    @Provides
    fun provideDataController(@ApplicationContext appContext: Context): DataController =
        HuaweiHiHealth.getDataController(appContext)

    @Singleton
    @Provides
    fun provideActivityRecordsController(@ApplicationContext appContext: Context): ActivityRecordsController =
        HuaweiHiHealth.getActivityRecordsController(appContext)

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.MAP_BASE_URL_DIRECTIONS)
            .client(provideClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Singleton
    @Provides
    fun provideClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val url: HttpUrl = chain.request().url().newBuilder()
                    .addQueryParameter("key", MapUtils.API_KEY)
                    .build()
                val request = chain.request().newBuilder()
                    .header("Content-Type", "application/json")
                    .url(url)
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(logger)
            .build()
    }

    @Singleton
    @Provides
    fun provideDirectionApis(retrofit: Retrofit): DirectionApis = retrofit.create(DirectionApis::class.java)

    @Singleton
    @Provides
    fun provideImageStorage(): StorageReference =
        FirebaseStorage.getInstance().reference
            .child(Constants.FIREBASE_IMAGE_STORAGE_REFERENCE_KEY)

    @Singleton
    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Singleton
    @Provides
    fun provideNetworkMonitor(connectivityManager: ConnectivityManager) =
        LiveNetworkMonitor(connectivityManager)

}