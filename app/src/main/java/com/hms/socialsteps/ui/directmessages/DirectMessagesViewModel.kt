package com.hms.socialsteps.ui.directmessages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.core.util.UserPreference
import com.hms.socialsteps.data.model.LastMessage
import com.hms.socialsteps.domain.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DirectMessagesViewModel
    @Inject constructor(
        private val messageRepository: MessageRepository,
        private val userPreference: UserPreference
    ): ViewModel() {

    private val _lastMessages = MutableStateFlow<Resource<List<LastMessage>>>(Resource.Loading())
    val lastMessages get() = _lastMessages

        fun getLastMessages() = viewModelScope.launch {
            messageRepository.getLastMessages(userPreference.getStoredUser().uid).collect{
                _lastMessages.value = it
            }
        }
}