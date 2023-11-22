package com.hms.socialsteps.ui.messagingscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.DirectMessage
import com.hms.socialsteps.data.model.MessageItem
import com.hms.socialsteps.domain.repository.MessageRepository
import com.hms.socialsteps.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessagingScreenViewModel
@Inject constructor(
    private val messageRepository: MessageRepository,
    private val notificationRepository: NotificationRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    private val _messages = MutableStateFlow<Resource<DirectMessage>>(Resource.Loading())
    val messages get() = _messages

    private val _sendMessageResult = MutableStateFlow<Resource<Any?>>(Resource.Loading())
    val sendMessageResult get() = _sendMessageResult


    fun getMessages(receiverId: String) = viewModelScope.launch {
        messageRepository.getMessagesSnapshot(
            userPreference.getStoredUser().uid,
            receiverId
        ).collect {
            _messages.value = it
        }
    }

    fun sendMessage(message: MessageItem) = viewModelScope.launch{
        messageRepository.sendMessage(message).collect{
            if (it is Resource.Success)
                notificationRepository.pushNotification(
                    userPreference.getStoredUser(),message.receiverId,message)

        }
    }

    fun getUserId(): String = userPreference.getStoredUser().uid
}