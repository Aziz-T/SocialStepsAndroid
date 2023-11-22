package com.hms.socialsteps.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hms.socialsteps.data.model.DirectionsRequest
import com.hms.socialsteps.data.model.DirectionsResponse
import com.hms.socialsteps.domain.repository.MapRepository
import com.hms.socialsteps.utils.MapUtils
import com.hms.socialsteps.utils.SDResource
import com.hms.socialsteps.utils.performGetOperation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel
@Inject constructor(
    private val mapRepository: MapRepository
): ViewModel() {

    private lateinit var _dResponse: LiveData<SDResource<DirectionsResponse>>
    val dResponse get() = _dResponse

    fun getDirections(type: String, directionsRequest: DirectionsRequest) = viewModelScope.launch{
        _dResponse = performGetOperation { mapRepository.getDirections(type,directionsRequest) }
    }

}
