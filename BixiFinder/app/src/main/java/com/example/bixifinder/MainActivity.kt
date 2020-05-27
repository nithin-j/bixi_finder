package com.example.bixifinder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.bixifinder.dbContext.AccountDetails
import com.example.bixifinder.model.BixiStationInfo
import com.example.bixifinder.model.BixiStationStatus
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.JsonObject
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
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
import kotlinx.android.synthetic.main.navigation_header.view.*
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Date
import java.text.SimpleDateFormat
import java.time.LocalDate

class MainActivity : AppCompatActivity(), PermissionsListener, OnMapReadyCallback,
    NavigationView.OnNavigationItemSelectedListener{

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

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        updateMembershipStatus()

        fab_dark.setOnClickListener {
            mapboxMap.setStyle(Style.TRAFFIC_NIGHT)
            fab_theme.collapse()
            Snackbar.make(it, "Dark Theme fo Map", Snackbar.LENGTH_SHORT)
                .show();
        }
        fab_light.setOnClickListener {
            mapboxMap.setStyle(Style.TRAFFIC_DAY)
            fab_theme.collapse()
            Snackbar.make(it, "Light Theme fo Map", Snackbar.LENGTH_SHORT)
                .show();
        }

        val stationInfoUrl = "https://api-core.bixi.com/gbfs/en/station_information.json"
        val stationStatusUrl = "https://api-core.bixi.com/gbfs/en/station_status.json"
        //val url = "https://api-core.bixi.com/gbfs/gbfs.json"
        AssyncTaskHandler().execute(stationInfoUrl,stationStatusUrl)

        fabRefresh.setOnClickListener {
            AssyncTaskHandler().execute(stationInfoUrl, stationStatusUrl)
            Snackbar.make(it, "Bixis Refreshed", Snackbar.LENGTH_SHORT)
                .show();
        }

        manageNavigationDrawer()

        navigation.bringToFront()
        navigation.setNavigationItemSelectedListener(this)




        fabMenu.setOnClickListener {
            drawer_layout.openDrawer(Gravity.LEFT)
        }

    }

    private fun updateMembershipStatus() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null){
            val userReference = FirebaseDatabase.getInstance().getReference(user?.uid.toString()).child("AccountDetails")

            val userListener = object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                @SuppressLint("SimpleDateFormat")
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onDataChange(p0: DataSnapshot) {
                    var userDetails = p0.getValue(AccountDetails::class.java)
                    var dateFormatter = SimpleDateFormat("yyyy-MM-dd")
                    var today = dateFormatter.format(Date.valueOf(LocalDate.now().toString()))//SimpleDateFormat("yyyy-MM-dd").parse(LocalDate.now().toString())
                    var validTo = dateFormatter.format(Date.valueOf(userDetails?.validUpTo)) //SimpleDateFormat("yyyy-MM-dd").parse(userDetails?.validUpTo.toString())

                    if (validTo < today)
                        userReference.child("membershipStatus").setValue("Invalid")
                    else
                        userReference.child("membershipStatus").setValue("Valid")
                }

            }

            userReference.addListenerForSingleValueEvent(userListener)

        }
    }

    private fun manageNavigationDrawer() {

        val user = FirebaseAuth.getInstance().currentUser
        val currentMenu = navigation.menu
        manageHeader(user)
        if(user != null){

            currentMenu.findItem(R.id.menu_update_account).isVisible = true
            currentMenu.findItem(R.id.menu_update_pass).isVisible = true
            currentMenu.findItem(R.id.menu_rate_bixi).isVisible = true
            currentMenu.findItem(R.id.menu_login).isVisible = false
            currentMenu.findItem(R.id.menu_logout).isVisible = true
        }
        else{

            currentMenu.findItem(R.id.menu_update_account).isVisible = false
            currentMenu.findItem(R.id.menu_update_pass).isVisible = false
            currentMenu.findItem(R.id.menu_rate_bixi).isVisible = false
            currentMenu.findItem(R.id.menu_login).isVisible = true
            currentMenu.findItem(R.id.menu_logout).isVisible = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun manageHeader(user: FirebaseUser?) {

        val currentHeader = navigation.getHeaderView(0)
        if (user != null){

            var userDetails : AccountDetails? = null
            val uid = user?.uid.toString()
            val userReference = FirebaseDatabase.getInstance().getReference(uid).child("AccountDetails")
            val userListener = object: ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userDetails = dataSnapshot.getValue(AccountDetails::class.java)
                    currentHeader.tv_user_full_name.text = userDetails?.name
                    if (userDetails?.membershipStatus?.toLowerCase().equals("valid"))
                        currentHeader.tv_pass_validity.text = "Bixi access is valid up to ${userDetails?.validUpTo}"
                    else
                        currentHeader.tv_pass_validity.text = "Bixi access expired on ${userDetails?.validUpTo}"
                }
            }
            userReference.addListenerForSingleValueEvent(userListener)
            currentHeader.tv_user_email.text = user.email
        }
        else{
            currentHeader.tv_user_full_name.text = "Guest User"
            currentHeader.tv_user_email.text = null
            currentHeader.tv_pass_validity.text = null
        }

    }


    @SuppressLint("StaticFieldLeak")
    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    inner class AssyncTaskHandler:AsyncTask<String,String,String>(){


        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg url: String?): String {

            var result:String
            val stationInfoConn = URL(url[0]).openConnection() as HttpURLConnection
            val stationStatusConn = URL(url[1]).openConnection() as HttpURLConnection
            try{
                stationInfoConn.connect()


                var tempResult = stationInfoConn.inputStream.use {
                    it.reader().use {
                            reader -> reader.readText()
                    }
                }

                result = tempResult

                stationStatusConn.connect()

                tempResult = stationStatusConn.inputStream.use {
                    it.reader().use {
                        reader -> reader.readText()
                    }
                }

                result = "$result || $tempResult"

            }
            finally {
                stationInfoConn.disconnect()
                stationStatusConn.disconnect()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            manageStationJSON(result)
        }

        private fun manageStationJSON(jsonString: String?){

            val jsonStringArray = jsonString?.split("||")?.toTypedArray()


            var jsonStationInfoObj = JSONObject(jsonStringArray?.get(0))
            jsonStationInfoObj = jsonStationInfoObj.getJSONObject("data")
            val jsonStationInfoArray = jsonStationInfoObj.getJSONArray("stations")

            var jsonStationStatusObj = JSONObject(jsonStringArray?.get(1))
            jsonStationStatusObj = jsonStationStatusObj.getJSONObject("data")
            val jsonStationStatusArray = jsonStationStatusObj.getJSONArray("stations")

            val listOfStations = ArrayList<BixiStationInfo>()
            var listOfBixis = ArrayList<BixiStationStatus>()
            var i = 0

            while (i<jsonStationInfoArray.length()){
                val jsonStationInfoObject = jsonStationInfoArray.getJSONObject(i)
                listOfStations.add(
                    BixiStationInfo(
                        jsonStationInfoObject.getInt("station_id"),
                        jsonStationInfoObject.getString("external_id"),
                        jsonStationInfoObject.getString("name"),
                        jsonStationInfoObject.getInt("short_name"),
                        jsonStationInfoObject.getDouble("lat"),
                        jsonStationInfoObject.getDouble("lon"),
                        jsonStationInfoObject.getInt("capacity"),
                        jsonStationInfoObject.getBoolean("electric_bike_surcharge_waiver"),
                        jsonStationInfoObject.getBoolean("eightd_has_key_dispenser")
                    )
                )

                val jsonStationStatusObject = jsonStationStatusArray.getJSONObject(i)
                listOfBixis.add(
                    BixiStationStatus(
                        jsonStationStatusObject.getInt("station_id"),
                        jsonStationStatusObject.getInt("num_bikes_available") - jsonStationStatusObject.getInt("num_ebikes_available"),
                        jsonStationStatusObject.getInt("num_ebikes_available"),
                        jsonStationStatusObject.getInt("num_docks_available")
                    )
                )

                i++
            }
            i = 0
            while (i<listOfStations.size){
                var markerTitle = "Name: ${listOfStations[i].name}\n" +
                        "Station ID: ${listOfBixis[i].station_id}\n" +
                        "Number of Bikes: ${listOfBixis[i].num_bikes_available}\n" +
                        "Number of E-Bikes: ${listOfBixis[i].num_ebikes_available}\n" +
                        "Number of Free Docks: ${listOfBixis[i].num_docks_available}"
                try {
                    mapboxMap.addMarker(
                        MarkerOptions().
                        position(LatLng(listOfStations[i].latitude,listOfStations[i].longitude)).
                        title(markerTitle)
                    )
                    i++
                }catch (exception: Exception){
                    Toast.makeText(this@MainActivity,"There was an unknown error\nPlease contact support",Toast.LENGTH_SHORT).show()
                }

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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val user = FirebaseAuth.getInstance().currentUser

        when (item.itemId){
            R.id.menu_update_account -> {
                val intent = Intent(this, ManageAccountActivity::class.java)
                intent.putExtra("layout","main")
                startActivity(intent)
            }
            R.id.menu_update_pass ->
                Toast.makeText(this, "Update pass will appear here", Toast.LENGTH_SHORT).show()
            R.id.menu_terms ->
                Toast.makeText(this, "Terms of use will appear here", Toast.LENGTH_SHORT).show()
            R.id.menu_learn ->
                Toast.makeText(this, "Learn more about bixi", Toast.LENGTH_SHORT).show()
            R.id.menu_rate_bixi ->
                Toast.makeText(this, "Rate our application here", Toast.LENGTH_SHORT).show()
            R.id.menu_user_ratings ->
                Toast.makeText(this, "Users can rate bixi finder here", Toast.LENGTH_SHORT).show()
            R.id.menu_login ->
                if( user?.uid == null){
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.putExtra("layout","main")
                    startActivity(intent)
                }
            R.id.menu_logout ->
                if( user?.uid != null){
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(this, MainActivityLoader::class.java)
                    intent.putExtra("layout","main")
                    startActivity(intent)
                }
        }
    drawer_layout.closeDrawer(Gravity.LEFT)

    return true
    }


}
