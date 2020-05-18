package com.example.bixifinder

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.RenderNode
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.os.PersistableBundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PermissionsListener, OnMapReadyCallback {

    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapboxMap: MapboxMap

    //to set the map boundries
    private val BOUND_CORNER_NW = LatLng(45.5505849, -73.60223174)
    private val BOUND_CORNER_SE = LatLng(45.5505849, -73.60223174)
    private val RESTRICTED_BOUNDS_AREA = LatLngBounds.Builder()
        .include(BOUND_CORNER_NW)
        .include(BOUND_CORNER_SE)
        .build()

    private val points:List<List<Point>> = mutableListOf()
    private val outerPoints:List<Point> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext,getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fab_dark.setOnClickListener {
            mapboxMap.setStyle(Style.TRAFFIC_NIGHT)
        }
        fab_light.setOnClickListener {
            mapboxMap.setStyle(Style.TRAFFIC_DAY)
        }

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.TRAFFIC_DAY){
            mapboxMap.setLatLngBoundsForCameraTarget(RESTRICTED_BOUNDS_AREA)
            mapboxMap.setMinZoomPreference(5.0)
            showBoundArea(it)
            showCrossHair()
            enableLocationComponent(it)
        }

    }

    private fun showBoundArea(loadMapStyle: Style) {
        outerPoints?.toMutableList()?.add(Point.fromLngLat(RESTRICTED_BOUNDS_AREA.northWest.longitude,
            RESTRICTED_BOUNDS_AREA.northWest.latitude))
        outerPoints?.toMutableList()?.add(Point.fromLngLat(RESTRICTED_BOUNDS_AREA.northEast.longitude,
            RESTRICTED_BOUNDS_AREA.northEast.latitude))
        outerPoints?.toMutableList()?.add(Point.fromLngLat(RESTRICTED_BOUNDS_AREA.southEast.longitude,
            RESTRICTED_BOUNDS_AREA.southEast.latitude))
        outerPoints?.toMutableList()?.add(Point.fromLngLat(RESTRICTED_BOUNDS_AREA.southWest.longitude,
            RESTRICTED_BOUNDS_AREA.southWest.latitude))
        outerPoints?.toMutableList()?.add(Point.fromLngLat(RESTRICTED_BOUNDS_AREA.northWest.longitude,
            RESTRICTED_BOUNDS_AREA.northWest.latitude))

        points?.toMutableList()?.add(outerPoints)

        loadMapStyle.addSource(GeoJsonSource("source-id",
            Polygon.fromLngLats(points)))

        loadMapStyle.addLayer(FillLayer("layer-id", "source-id"))
    }

    private fun showCrossHair() {
        var crossHair = View(this)
        crossHair.layoutParams = FrameLayout.LayoutParams(5,5,Gravity.CENTER)
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        if (PermissionsManager.areLocationPermissionsGranted(this)){
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.accent))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(this,
                loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()
            mapboxMap.locationComponent.apply {
                activateLocationComponent(locationComponentActivationOptions)

                isLocationComponentEnabled = true

                cameraMode = CameraMode.TRACKING
                renderMode = RenderMode.COMPASS
            }
        }
        else{
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted){
            enableLocationComponent(mapboxMap.style!!)
        }else{
            Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
            finish()
        }
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

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


}
