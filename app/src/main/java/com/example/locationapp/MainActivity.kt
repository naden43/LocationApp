package com.example.locationapp

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.protocol.HTTP

class MainActivity : AppCompatActivity() {

    val REQUEST_CODE = 500

    lateinit var longitidute:TextView
    lateinit var latiduite:TextView
    lateinit var tvLocation:TextView
    lateinit var btnSMS:Button
    lateinit var openMap:Button

    var positionText:String? = null

    lateinit var fusedLocationProviderClient:FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        longitidute = findViewById(R.id.longitiude)
        latiduite = findViewById(R.id.latidiude)
        tvLocation = findViewById(R.id.TextLocation)
        btnSMS = findViewById(R.id.smsBtn)
        openMap = findViewById(R.id.openMapBtn)
        tvLocation.isEnabled = false

        btnSMS.setOnClickListener {

            if(positionText!=null) {
                val phone = "01021207693"
                val uri = Uri.parse("smsto:$phone")
                val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                    putExtra("sms_body", positionText)
                }
                startActivity(intent)
                /*if (intent.resolveActivity(packageManager) != null) {
                } else {
                }*/
            }
            else{
                Toast.makeText(this, "No address found yet", Toast.LENGTH_SHORT).show()

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onStart() {
        super.onStart()

        if(checkPermission()){
            if(isLocationEnabled()){
                getLocation()
            }
            else
            {
                enableLocation()
            }
        }
        else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_CODE
            )
        }
    }

    fun checkPermission():Boolean{
        var status:Boolean = false
        if((ContextCompat.checkSelfPermission(this,ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED )
            || (ContextCompat.checkSelfPermission(this,ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED )){

            status = true
        }
        return status
    }

    fun isLocationEnabled():Boolean{
        var status = false
        var locationManager:LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            status = true
        }
        return status
    }

    fun enableLocation(){
        Toast.makeText(this , "open location" , Toast.LENGTH_LONG).show()
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }


    @SuppressLint("MissingPermission")
   @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun getLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationProviderClient.requestLocationUpdates(
            LocationRequest.Builder(0).apply {
                setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            }.build(),

            object :LocationCallback(){
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    val location = p0.lastLocation
                    longitidute.text = location?.longitude.toString()
                    latiduite.text = location?.latitude.toString()
                    convertToText(location!!)
                    //fusedLocationProviderClient.removeLocationUpdates(this)

                }
            },
            Looper.myLooper()
        )
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun convertToText(location: Location){

        val latitude = latiduite.text.toString().toDoubleOrNull()
        val longitude = longitidute.text.toString().toDoubleOrNull()
        val geoCoder:Geocoder = Geocoder(this)
        if(latitude!=null && longitude!=null) {
            geoCoder.getFromLocation(longitude, latitude, 1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        if (!addresses.isEmpty()) {
                            val address = addresses.get(0)
                            tvLocation.text = "${address.countryName}"
                            positionText = address.countryName

                        }
                    }
                }
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==REQUEST_CODE){
            if(grantResults.size >1 && grantResults.get(0) == PackageManager.PERMISSION_GRANTED){
                getLocation()
            }
        }

    }
}
