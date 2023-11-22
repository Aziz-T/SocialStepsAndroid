package com.hms.socialsteps.data.model

import com.google.gson.annotations.SerializedName

data class DirectionsResponse (@SerializedName("routes") val routes: List<Routes>,
                               @SerializedName("returnCode") val returnCode: String,
                               @SerializedName("returnDesc") val returnDesc: String)
data class Routes (@SerializedName("paths") val paths: List<Paths>,
                   @SerializedName("bounds") val bounds: Bounds)

data class Paths (@SerializedName("duration") val duration: Double,
                  @SerializedName("durationText") val durationText: String,
                  @SerializedName("durationInTraffic") val durationInTraffic: Double,
                  @SerializedName("distance") val distance: Double,
                  @SerializedName("startLocation") val startLocation: LatLngData,
                  @SerializedName("startAddress") val startAddress: String,
                  @SerializedName("distanceText") val distanceText: String,
                  @SerializedName("steps") val steps: List<Steps>,
                  @SerializedName("endLocation") val endLocation: LatLngData,
                  @SerializedName("endAddress") val endAddress: String)

data class Bounds (@SerializedName("southwest") val southwest: LatLngData,
                   @SerializedName("northeast") val northeast: LatLngData)

data class Steps (@SerializedName("duration") val duration: Double,
                  @SerializedName("orientation") val orientation: Double,
                  @SerializedName("durationText") val durationText: String,
                  @SerializedName("distance") val distance: Double,
                  @SerializedName("startLocation") val startLocation: LatLngData,
                  @SerializedName("instruction") val instruction: String,
                  @SerializedName("action") val action: String,
                  @SerializedName("distanceText") val distanceText: String,
                  @SerializedName("endLocation") val endLocation: LatLngData,
                  @SerializedName("polyline") val polyline: List<LatLngData>,
                  @SerializedName("roadName") val roadName: String)