package com.hms.socialsteps.ui.post

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hms.socialsteps.core.util.Resource
import com.hms.socialsteps.data.model.Posts
import com.hms.socialsteps.domain.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PostViewModel
@Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    private val _upsertResult = MutableLiveData<Resource<Boolean>>()
    val upsertResult get() = _upsertResult

    fun upsertPost(post: Posts) {
        _upsertResult.value = Resource.Loading()
        postRepository.upsertPost(post) {
            _upsertResult.value = it
        }
    }

}