@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package systems.brn.gpsgame1

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import systems.brn.gpsgame1.databinding.ActivityMapsBinding


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private val sharedPrefFile = "MyConfig"
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val interval: Long = 2000
    private val fastestinterval: Long = 1000
    private lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mymarker: Marker
    private lateinit var mycircle: Circle
    private val requestpermissionslocation = 10
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var firsttime = true
    private lateinit var sharedPreferences: SharedPreferences
    private var client = OkHttpClient.Builder().connectionSpecs(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS)).build()
    private fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
                true
            }else{
                // Show the permission request
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestpermissionslocation)
                false
            }
        } else {
            true
        }
    }


    private fun loginerror(){
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun startLocationUpdates() {

        // Create the location request to start receiving updates

        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = interval
        mLocationRequest.fastestInterval = fastestinterval

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(mLocationRequest, mLocationCallback,
            Looper.myLooper())
    }


    private fun center(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mLastLocation.latitude, mLastLocation.longitude), 18.0f))
    }

    fun onLocationChanged(location: Location) {
        // New location has now been determined

        mLastLocation = location
        try {
            mymarker.remove()
        }
        catch (e: Exception){ }
        try {
            mycircle.remove()
        }
        catch (e: Exception){ }
        try {
            mymarker =  mMap.addMarker(MarkerOptions().position(LatLng(mLastLocation.latitude, mLastLocation.longitude)).title("My Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.smiley)))
            if (firsttime){
                center()
                firsttime = false
            }
            mycircle = mMap.addCircle(CircleOptions().center(LatLng(mLastLocation.latitude, mLastLocation.longitude)).radius(mLastLocation.accuracy.toDouble()).strokeColor(R.color.BLUE).fillColor(R.color.LIGHTBLUE))
        }
        catch (e: SecurityException){
            e.printStackTrace()
        }
        // You can now create a LatLng Object for use with maps
    }

        private val mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                // do work here
                onLocationChanged(locationResult.lastLocation)
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestpermissionslocation) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have to add startlocationUpdate() method later instead of Toast
                Toast.makeText(this,"Permission granted",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun dologin(){
        val configured = sharedPreferences.getInt("configured", 0)
        if (configured == 0){
            loginerror()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        dologin()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mLocationRequest = LocationRequest.create().apply {
            interval = interval
            fastestInterval = fastestinterval
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime = 100
        }
        checkPermissionForLocation(this)
        startLocationUpdates()
    }

}