package com.hms.socialsteps.ui.profile.editprofile

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

import androidx.lifecycle.*
import com.hms.socialsteps.R
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.CloudDbWrapper
import com.hms.socialsteps.domain.repository.CloudStorageRepository
import com.hms.socialsteps.domain.repository.UserRepository
import com.hms.socialsteps.domain.usecase.HealthAuthUseCase
import com.huawei.hms.hihealth.SettingController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val healthAuthUseCase: HealthAuthUseCase,
    private val userRepository: UserRepository,
    private val cloudStorageRepository: CloudStorageRepository,
    application: Application
) : AndroidViewModel(application) {
    val TAG = "ProfileViewModel"

    private val _user: MutableLiveData<Resource<Users>> = MutableLiveData()
    val user get() = _user

    private val _upsertResult = MutableLiveData<Resource<Boolean>>()
    val upsertResult get() = _upsertResult

    private var _uploadResult = MutableLiveData<Resource<String>>()
    val uploadResult get() = _uploadResult

    private fun getContext(): Context = getApplication<Application>().applicationContext

    fun setImageUri(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        _uploadResult.postValue(Resource.Loading())
        getContext().contentResolver.let { contentResolver: ContentResolver ->
            //takePersistableUriPermission() is for ACTION_OPEN_DOCUMENT, so any further changes
            //this code could be needed
            /*val readUriPermission: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, readUriPermission)*/
            contentResolver.openInputStream(uri)?.use { inputStream ->
                cloudStorageRepository.uploadImage(inputStream, user.value!!.data!!).collect{
                        _uploadResult.postValue(it)
                }
            }
        }
    }

    fun getUser(userId: String) {
        _user.value = Resource.Loading()
        userRepository.queryUser(userId) { result ->
            _user.value = result
        }
    }

    fun registerUser(user: Users) {
        _upsertResult.value = Resource.Loading()
        userRepository.upsertUser(user) {
            _upsertResult.value = it
        }
    }

    private val requestHealthAuthLiveData: MutableLiveData<Resource<String?>> = MutableLiveData()
    fun getRequestHealthAuthLiveData(): LiveData<Resource<String?>> = requestHealthAuthLiveData

    fun requestHealthAuth(data: Intent, mSettingController: SettingController) {
        requestHealthAuthLiveData.value = Resource.Loading()

        when (val result = healthAuthUseCase.execute(data, mSettingController)) {
            is Resource.Error -> {
                requestHealthAuthLiveData.value = result.message?.let { Resource.Error(it) }
            }
            is Resource.Success -> {
                requestHealthAuthLiveData.value = Resource.Success(result.data)
            }
            else -> {}
        }
    }
}