package com.hms.socialsteps.data.di

import com.google.firebase.storage.StorageReference
import com.hms.socialsteps.data.DirectionApis
import com.hms.socialsteps.domain.repository.*
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.function.AGConnectFunction
import com.huawei.hms.hihealth.ActivityRecordsController
import com.huawei.hms.hihealth.DataController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideLoginRepository(agConnectAuth: AGConnectAuth) = LoginRepository(agConnectAuth)

    @Provides
    @Singleton
    fun provideHealthAuthRepository() = HealthAuthRepository()

    @Provides
    @Singleton
    fun provideMapRepository(directionApis: DirectionApis) = MapRepository(directionApis)

    @Provides
    @Singleton
    fun provideUserRepository() = UserRepository()

    @Provides
    @Singleton
    fun provideActivityRepository() = ActivityRepository()

    @Provides
    @Singleton
    fun provideFriendshipRepository() = FriendshipRepository()

    @Provides
    @Singleton
    fun providePostRepository() = PostRepository()

    @Provides
    @Singleton
    fun provideCloudStorageRepository(reference: StorageReference) =
        CloudStorageRepository(reference)

    @Provides
    @Singleton
    fun provideHealthDataRepository(
        dataController: DataController,
        activityRecordsController: ActivityRecordsController
    ) =
        HealthDataRepository(dataController, activityRecordsController)

    @Provides
    @Singleton
    fun provideLikeRepository() =
        LikeRepository()

    @Provides
    @Singleton
    fun provideNotificationRepository() =
        NotificationRepository(AGConnectFunction.getInstance())

    @Provides
    @Singleton
    fun provideTokenRepository() =
        TokenRepository()


    @Provides
    @Singleton
    fun provideLikesCountRepository() =
        LikesCountRepository()

    @Provides
    @Singleton
    fun provideCommentsRepository() =
        CommentsRepository()

    @Provides
    @Singleton
    fun provideGroupStatisticsRepository() =
        GroupStatisticsRepository()

    @Provides
    @Singleton
    fun provideMessageRepository() =
        MessageRepository()
}