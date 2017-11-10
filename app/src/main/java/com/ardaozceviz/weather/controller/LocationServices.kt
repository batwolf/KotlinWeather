package com.ardaozceviz.weather.controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import com.ardaozceviz.weather.model.TAG_C_LOCATION
import com.ardaozceviz.weather.view.UserInterface
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.PermissionRequestErrorListener
import com.karumi.dexter.listener.single.PermissionListener


/**
 * Created by arda on 07/11/2017.
 */
class LocationServices(private val context: Context) {
    private val activity = context as Activity
    private val userInterface = UserInterface(context)
    private val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val enableLocationRequestCode = 123
    var deviceLocation: Location? = null

    /*
    * Refresh the weather location on location changed
    * */
    private var locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            Log.d(TAG_C_LOCATION, "locationListener onLocationChanged() is executed.")
            if (location != null) {
                val longitude = location.longitude.toString()
                val latitude = location.latitude.toString()
                Log.d(TAG_C_LOCATION, "longitude: $longitude, latitude: $latitude")
                Server(context).getWeatherForCurrentLocation(longitude, latitude)
            } else {
                Log.d(TAG_C_LOCATION, "location is null.")
                //userInterface.onError()
            }
            //Check if the location is not null
            //Remove the location listener as we don't need to fetch the weather again and again
            if (location?.latitude != null && location.latitude != 0.0 && location.longitude != 0.0) {
                deviceLocation = location
                locationManager.removeUpdates(this)
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            Log.d(TAG, "locationListener onStatusChanged() is executed.")
        }

        override fun onProviderEnabled(provider: String?) {
            Log.d(TAG, "locationListener onProviderEnabled() is executed.")
        }

        override fun onProviderDisabled(provider: String?) {
            Log.d(TAG, "locationListener onProviderDisabled() is executed.")
            userInterface.onError()
        }
    }

    fun locationPermission() {
        Log.d(TAG_C_LOCATION, "locationPermission() is executed.")

        Dexter.withActivity(activity)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        Log.d(TAG_C_LOCATION, "locationPermission() onPermissionGranted() is executed.")
                        checkLocationEnabledAndPrompt()
                    }

                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {
                        Log.d(TAG_C_LOCATION, "locationPermission() onPermissionRationaleShouldBeShown() is executed.")
                        token?.cancelPermissionRequest()
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        Log.d(TAG_C_LOCATION, "locationPermission() onPermissionDenied() is executed.")
                        userInterface.onError()
                    }
                })
                .withErrorListener(PermissionRequestErrorListener { e ->
                    Log.d(TAG_C_LOCATION, "locationPermission() PermissionRequestErrorListener: $e")
                }).check()


    }

    fun checkLocationEnabledAndPrompt() {
        Log.d(TAG_C_LOCATION, "checkLocationEnabledAndPrompt() is executed.")
        // Check if Location is enabled
        // NETWORK_PROVIDER determines location based on availability of cell tower and WiFi access points. Results are retrieved by means of a network lookup.
        val isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isLocationEnabled) {
            // Location is not enabled
            Log.d(TAG_C_LOCATION, "isLocationEnabled: $isLocationEnabled")
            Log.d(TAG_C_LOCATION, "checkLocationEnabledAndPrompt() AlertDialog show.")
            AlertDialog.Builder(context)
                    .setCancelable(false)
                    .setTitle("Location permission needed")
                    .setMessage("This app requires GPS to be enabled to get the weather information. Do you want to enable now?")
                    .setPositiveButton(android.R.string.ok, { dialog, _ ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        activity.startActivityForResult(intent, enableLocationRequestCode)
                        dialog.dismiss()
                    })
                    .setNegativeButton(android.R.string.cancel, { dialog, _ ->
                        Log.d(TAG_C_LOCATION, "checkLocationEnabledAndPrompt() AlertDialog cancel clicked.")
                        dialog.dismiss()
                        userInterface.onError()
                    })
                    .create()
                    .show()
        } else {
            // Location is enabled
            Log.d(TAG_C_LOCATION, "isLocationEnabled: $isLocationEnabled")
            requestLocationUpdates()
        }
    }


    /*
    * Start receiving the location updates
    * */
    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        Log.d(TAG, "requestLocationUpdates() is executed.")
        val provider = LocationManager.GPS_PROVIDER
        //Add the location listener and listen updates
        locationManager.requestLocationUpdates(provider, 0, 0.0f, locationListener)
        val location = locationManager.getLastKnownLocation(provider)
        Log.d(TAG, "requestLocationUpdates() location: $location.")
        locationListener.onLocationChanged(location)
    }

}