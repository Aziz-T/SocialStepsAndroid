package com.hms.socialsteps.ui.notification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.Notifications
import com.hms.socialsteps.data.model.Users
import com.hms.socialsteps.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Notification
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
): ViewModel() {
    private val _notificationListLiveData: MutableLiveData<Resource<ArrayList<Notifications>>> = MutableLiveData()
    val notificationListLiveData: LiveData<Resource<ArrayList<Notifications>>> get() = _notificationListLiveData


    fun getNotificationList() {
        _notificationListLiveData.value = Resource.Loading()
        notificationRepository.queryNotifications {
            _notificationListLiveData.value = it
        }
    }

}