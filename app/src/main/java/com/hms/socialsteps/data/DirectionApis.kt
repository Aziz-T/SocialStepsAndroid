package com.hms.socialsteps.data

import com.hms.socialsteps.data.model.DirectionsRequest
import com.hms.socialsteps.data.model.DirectionsResponse
import dagger.Binds
import dagger.Provides
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path


interface DirectionApis {
    @POST("routeService/{type}")
    suspend fun getDirectionsWithType(
        @Path(value = "type",encoded = true) type : String,
        @Body directionRequest: DirectionsRequest
    ): Response<DirectionsResponse>
}