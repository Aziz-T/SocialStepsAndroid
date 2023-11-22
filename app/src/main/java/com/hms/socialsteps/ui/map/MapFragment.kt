package com.hms.socialsteps.ui.map

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.hms.socialsteps.R
import com.hms.socialsteps.data.model.DirectionsRequest
import com.hms.socialsteps.data.model.LatLngData
import com.hms.socialsteps.data.model.Paths
import com.hms.socialsteps.databinding.FragmentMapBinding
import com.hms.socialsteps.ui.base.BaseFragment
import com.hms.socialsteps.utils.MapUtils
import com.hms.socialsteps.utils.MapUtils.Companion.MAPVIEW_BUNDLE_KEY
import com.hms.socialsteps.utils.SDResource
import com.hms.socialsteps.utils.binding.viewBinding
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MapFragment(): BaseFragment(R.layout.fragment_map), OnMapReadyCallback {
    val TAG = "MapFragment"

    private val binding by viewBinding(FragmentMapBinding::bind)
    private val viewModel: MapViewModel by viewModels()
    private lateinit var mapView: MapView
    private var hMap: HuaweiMap? = null
    private lateinit var directionRequest : DirectionsRequest
    private var originMarker: Marker? = null
    private var desMarker: Marker? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = binding.mapView
        initializeMap(savedInstanceState)
    }

    private fun initializeMap(savedInstanceState: Bundle?) {
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY)
        }
        with(mapView){
            onCreate(mapViewBundle)
            getMapAsync(this@MapFragment)
        }
    }

    override fun onMapReady(map: HuaweiMap?) {
        if (map != null) {
            hMap = map
        }
        Timber.tag(TAG).d( "onMapReady: map loaded")
        getDirection()
    }

    private fun getDirection(){
        val origin = LatLngData(40.226541, 28.830128)
        val destination = LatLngData(40.226787, 28.832212)
        directionRequest = DirectionsRequest(origin,destination)
        // Let’s request for walking type:
        viewModel.getDirections(MapUtils.DirectionType.WALKING.type,directionRequest)
        viewModel.dResponse.observe(viewLifecycleOwner){
            when(it.status){
                SDResource.Status.LOADING -> {
                    Timber.tag(TAG).d( "onMapReady: Loading")
                }
                SDResource.Status.SUCCESS -> {
                    Timber.tag(TAG).d( "onMapReady: success")
                    clearMarkers()
                    addPolyLines(it.data?.routes!![0].paths[0])
                    addMarkers(origin,destination)
                }
                SDResource.Status.ERROR -> {
                    Timber.tag(TAG).e( "onMapReady: Error! ${it.message}")
                }
            }
        }
    }

    private fun clearMarkers(){
        originMarker?.remove()
        desMarker?.remove()
    }

    private fun addMarkers(originData: LatLngData, desData: LatLngData) {
        val oData = LatLng(originData.lat,originData.lng)
        val dData = LatLng(desData.lat,desData.lng)
        val originOptions = MarkerOptions()
            .position(oData)
        val desOptions = MarkerOptions()
            .position(dData)
        originMarker = hMap?.addMarker(originOptions)
        desMarker = hMap?.addMarker(desOptions)

        //After adding markers, set camera to origin position
        moveCamera(oData)
    }

    private fun moveCamera(position: LatLng){
        hMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(position, MapUtils.ZOOM_VALUE))
    }

    private fun addPolyLines(path : Paths){
        val options = PolylineOptions()
        options.add(LatLng(path.startLocation.lat, path.startLocation.lng))
        path.steps.forEach{
            it.polyline.forEach {it1->
                options.add(LatLng(it1.lat, it1.lng))
            }
        }
        options.add(LatLng(path.endLocation.lat, path.endLocation.lng))
        options.color(Color.BLUE)
        options.width(10f)
        hMap!!.addPolyline(options)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle: Bundle? = outState.getBundle(MAPVIEW_BUNDLE_KEY)
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle)
        }

        mapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}