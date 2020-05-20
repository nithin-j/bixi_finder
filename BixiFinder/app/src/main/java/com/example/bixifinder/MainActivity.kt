package com.example.bixifinder

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bixifinder.model.BixiStationInfo
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity(), PermissionsListener, OnMapReadyCallback {

    private var permissionsManager: PermissionsManager = PermissionsManager(this)
    private lateinit var mapboxMap: MapboxMap



    //to set the map boundries
    private val BOUND_CORNER_NW = LatLng(45.86352447, -73.65357972)
    private val BOUND_CORNER_SE = LatLng(45.24738588, -73.47045137)
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

        val url = "https://api-core.bixi.com/gbfs/en/station_information.json"
        AssyncTaskHandler().execute(url)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fab_dark.setOnClickListener {
            mapboxMap.setStyle(Style.TRAFFIC_NIGHT)
        }
        fab_light.setOnClickListener {
            mapboxMap.setStyle(Style.TRAFFIC_DAY)
        }

    }

    inner class AssyncTaskHandler:AsyncTask<String,String,String>(){


        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg url: String?): String {
            val result:String
            val connection = URL(url[0]).openConnection() as HttpURLConnection
            try{
               connection.connect()
                result = connection.inputStream.use {
                    it.reader().use {
                        reader -> reader.readText()
                    }
                }
            }
            finally {
                connection.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            manageStationJSON(result)
        }

        private fun manageStationJSON(jsonString: String?){

            var jsonObject = JSONObject(jsonString)
            jsonObject = jsonObject.getJSONObject("data")
            val jsonStationArray = jsonObject.getJSONArray("stations")
            val listOfStations = ArrayList<BixiStationInfo>()
            var i = 0

            while (i<jsonStationArray.length()){
                val jsonStationObject = jsonStationArray.getJSONObject(i)
                listOfStations.add(
                    BixiStationInfo(
                        jsonStationObject.getInt("station_id"),
                        jsonStationObject.getString("external_id"),
                        jsonStationObject.getString("name"),
                        jsonStationObject.getInt("short_name"),
                        jsonStationObject.getDouble("lat"),
                        jsonStationObject.getDouble("lon"),
                        jsonStationObject.getInt("capacity"),
                        jsonStationObject.getBoolean("electric_bike_surcharge_waiver"),
                        jsonStationObject.getBoolean("eightd_has_key_dispenser")
                    )
                )

                i++
            }
            i = 0
            while (i<listOfStations.size){
                mapboxMap.addMarker(
                    MarkerOptions().
                    position(LatLng(listOfStations[i].latitude,listOfStations[i].longitude)).
                    title(listOfStations[i].name).
                    icon(IconFactory.getInstance(this@MainActivity).fromResource(R.drawable.pin))
                )
                i++
            }

        }

    }


    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap
        mapboxMap.setStyle(Style.TRAFFIC_DAY){
            mapboxMap.setLatLngBoundsForCameraTarget(RESTRICTED_BOUNDS_AREA)
            mapboxMap.setMinZoomPreference(8.0)
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
