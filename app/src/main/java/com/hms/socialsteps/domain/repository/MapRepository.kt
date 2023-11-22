package com.hms.socialsteps.domain.repository

import android.util.Log
import com.hms.socialsteps.data.DirectionApis
import com.hms.socialsteps.data.model.DirectionsRequest
import com.hms.socialsteps.data.model.DirectionsResponse
import com.hms.socialsteps.utils.BaseDataSource
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class MapRepository
@Inject constructor(
    private val directionApis: DirectionApis
): BaseDataSource(){

    suspend fun getDirections(type: String, directionRequest: DirectionsRequest) =
        getResult { directionApis.getDirectionsWithType(type, directionRequest) }
}