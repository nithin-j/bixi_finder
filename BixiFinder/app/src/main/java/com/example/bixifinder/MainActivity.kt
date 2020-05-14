package com.example.bixifinder

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.RenderNode
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.os.PersistableBundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), PermissionsListener, OnMapReadyCallback {

    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapboxMap: MapboxMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(applicationContext,getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fab_dark.setOnClickListener {
            mapboxMap.setStyle(Style.DARK)
        }
        fab_light.setOnClickListener {
            mapboxMap.setStyle(Style.MAPBOX_STREETS)
        }

    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.MAPBOX_STREETS){
            enableLocationComponent(it)
        }



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
