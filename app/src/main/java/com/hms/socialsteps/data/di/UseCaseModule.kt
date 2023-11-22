package com.hms.socialsteps.data.di

import android.content.Context
import com.hms.socialsteps.domain.repository.LoginRepository
import com.hms.socialsteps.domain.usecase.LoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import com.hms.socialsteps.domain.repository.HealthAuthRepository
import com.hms.socialsteps.domain.usecase.HealthAuthUseCase
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Provides
    @Singleton
    fun provideLoginUseCase(@ApplicationContext applicationContext: Context,
                            loginRepository: LoginRepository
    ) = LoginUseCase(applicationContext, loginRepository)

    @Provides
    @Singleton
    fun provideHealthAuthUseCase(
        healthAuthRepository: HealthAuthRepository
    ) = HealthAuthUseCase(healthAuthRepository)
}