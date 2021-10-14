/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.parentapp.kotlin.ui.gallery

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.AGConnectCloudDB
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.location.*
import com.huawei.hms.maps.*
import com.huawei.hms.maps.HuaweiMap.OnMapClickListener
import com.huawei.hms.maps.HuaweiMap.OnMapLongClickListener
import com.huawei.hms.maps.model.*
import com.huawei.parentapp.R
import com.huawei.parentapp.java.ObjectTypeInfoHelper
import com.huawei.parentapp.kotlin.ExceptionLogger

class SetGeofenceFragment : Fragment(), OnMapReadyCallback, OnMapClickListener, OnMapLongClickListener {
    private var mMapView: MapView? = null
    private var huaweiMap: HuaweiMap? = null
    private var seekBar: SeekBar? = null
    private var mCircle: Circle? = null
    var mCloudDBZone: CloudDBZone? = null
    var mLocationCallback: LocationCallback? = null
    var mLocationRequest: LocationRequest? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    var currentlatlon: LatLng? = null
    var txtprogress: TextView? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_geofence, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        txtprogress = view.findViewById<View>(R.id.txt_progress) as TextView
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "sdk >= 23 M")
            if (ActivityCompat.checkSelfPermission(activity as Context,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity as Context,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val strings = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                ActivityCompat.requestPermissions(activity as Activity, strings, requestCode)
            }
        }
        setLocationInit()
        MapsInitializer.setApiKey(resources.getString(R.string.API_KEY))
        mMapView = view.findViewById(R.id.mapview_mapviewdemo)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey")
        }
        mMapView?.onCreate(mapViewBundle)
        mMapView?.getMapAsync(this)
        seekBar = view.findViewById<View>(R.id.seekBar) as SeekBar
        seekBar!!.max = maxValue
        seekBar!!.progress = progressValue
        txtprogress!!.text = defaultRadiusText
        currentradius = 200
        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                txtprogress!!.text = "Raduis:$progress"
                if (mCircle != null) {
                    mCircle!!.radius = progress.toDouble()
                    currentradius = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        val btn_submit = view.findViewById<View>(R.id.btn_submit) as Button
        btn_submit.setOnClickListener { upsertConfigurationDetails() }
    }

    fun setLocationInit() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        mSettingsClient = LocationServices.getSettingsClient(activity)
        mLocationRequest = LocationRequest()
        // Sets the interval for location update (unit: Millisecond)
        mLocationRequest!!.interval = intervalSeconds.toLong()
        // Sets the priority
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if (null == mLocationCallback) {
            mLocationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    if (locationResult != null) {
                        val locations = locationResult.locations
                        if (!locations.isEmpty()) {
                            for (location in locations) {
                                Log.d(TAG,
                                        "onLocationResult location[Longitude,Latitude,Accuracy]:" + location.longitude
                                                + "," + location.latitude + "," + location.accuracy)
                            }
                        }
                    }
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    if (locationAvailability != null) {
                        val flag = locationAvailability.isLocationAvailable
                        Log.d(TAG, "onLocationAvailability isLocationAvailable:$flag")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onMapClick(latLng: LatLng) {
        Log.d("onCLICKTRIGGERED", "")
        Toast.makeText(activity, latLng.toString(), Toast.LENGTH_SHORT).show()
        if (null == huaweiMap) {
            return
        }
        huaweiMap!!.clear()
        if (null != mCircle) {
            mCircle!!.remove()
            mCircle = null
        }
        mCircle = huaweiMap!!.addCircle(CircleOptions()
                .center(LatLng(latLng.latitude, latLng.longitude))
                .radius(defaultRadius.toDouble()).strokeWidth(1f)
                .fillColor(0x220000FF))
        huaweiMap!!.addMarker(MarkerOptions()
                .position(LatLng(latLng.latitude, latLng.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("marker", 120, 120))))
        seekBar!!.max = defaultMaxValue
        seekBar!!.progress = defaultProgress
        currentradius = 200
        txtprogress!!.text = defaultRadiusText
        currentlatlon = latLng
    }

    override fun onMapLongClick(latLng: LatLng) {}
    override fun onMapReady(huaweimap: HuaweiMap) {
        huaweiMap = huaweimap
        huaweiMap!!.isMyLocationEnabled = true // Enable the my-location overlay.
        huaweiMap!!.uiSettings.isMyLocationButtonEnabled = true // Enable the my-location icon.
        huaweiMap!!.setOnMapClickListener(this)
        val task = mFusedLocationProviderClient!!.lastLocation // Define callback for success in obtaining the last known location.
                .addOnSuccessListener(OnSuccessListener { location ->
                    if (location == null) {
                        Toast.makeText(activity, "Location not available", Toast.LENGTH_SHORT).show()
                        return@OnSuccessListener
                    } else {
                        val update = CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude),
                                defaultZoom)
                        huaweiMap!!.moveCamera(update)
                        if (null != mCircle) {
                            mCircle!!.remove()
                            mCircle = null
                        }
                        mCircle = huaweiMap!!.addCircle(CircleOptions()
                                .center(LatLng(location.latitude, location.longitude))
                                .radius(defaultRadius.toDouble()).strokeWidth(1f)
                                .fillColor(0x220000FF))
                        huaweiMap!!.addMarker(MarkerOptions()
                                .position(LatLng(location.latitude, location.longitude))
                                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("marker", 120, 120))))
                        seekBar!!.max = defaultMaxValue
                        seekBar!!.progress = defaultProgress
                        txtprogress!!.text = defaultRadiusText
                        currentlatlon = LatLng(location.latitude, location.longitude)
                    }
                }) // Define callback for failure in obtaining the last known location.
                .addOnFailureListener {
                    // ...
                }
        establishconnection()
    }

    fun resizeMapIcons(iconName: String?, width: Int, height: Int): Bitmap {
        val imageBitmap = BitmapFactory.decodeResource(resources, resources.getIdentifier(iconName, "drawable", activity?.packageName))
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }

    fun establishconnection() {
        AGConnectCloudDB.initialize(activity as Context)
        val mCloudDB = AGConnectCloudDB.getInstance()
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo())
            val mConfig = CloudDBZoneConfig("test1",
                    CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                    CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC)
            mConfig.persistenceEnabled = true
            val openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true)
            openDBZoneTask.addOnSuccessListener { cloudDBZone ->
                Log.d("TAG", "open clouddbzone success")
                mCloudDBZone = cloudDBZone
                // Add subscription after opening cloudDBZone success
                Toast.makeText(activity, "openclouddbzonesuccess", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e ->
                Log.d("TAG", "open clouddbzone failed for $e")
                Toast.makeText(activity, "open clouddbzone failed for $e", Toast.LENGTH_SHORT).show()
            }
        } catch (e: AGConnectCloudDBException) {
            ExceptionLogger.printExceptionDetails("SetGeofenceFragment ", e)
        }
    }

    fun upsertConfigurationDetails() {
        if (mCloudDBZone == null) {
            Log.d("TAG", "CloudDBZone is null, try re-open it")
            return
        }

        val progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Loading")
        progressDialog.show()


        val user = AGConnectAuth.getInstance().currentUser
        val sharedpreferences = activity?.getSharedPreferences("ConnectionInfo", Context.MODE_PRIVATE)
        val geofenceDetails = com.huawei.parentapp.java.GeofenceDetails()
        geofenceDetails.childID = sharedpreferences?.getString("ChildID", "")
        geofenceDetails.lat = currentlatlon!!.latitude.toString()
        geofenceDetails.lon = currentlatlon!!.longitude.toString()
        geofenceDetails.assignedby = user.uid
        geofenceDetails.isvalid = true
        geofenceDetails.radius = currentradius.toString() + ""
        geofenceDetails.geofenceName = "Park"
        val upsertTask = mCloudDBZone!!.executeUpsert(geofenceDetails)
        upsertTask.addOnSuccessListener { cloudDBZoneResult ->

            progressDialog.dismiss()
            Log.d("TAG", "upsert_sucess $cloudDBZoneResult records")
            Toast.makeText(activity, "upsert_sucess $cloudDBZoneResult records", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->

            progressDialog.dismiss()
            Log.d("TAG", "insert_failed $e records")
            Toast.makeText(activity, "insert_failed $e records", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private var currentradius = 100
        private const val defaultRadius = 200
        private const val defaultProgress = 200
        private const val defaultMaxValue = 1000
        private const val requestCode = 1
        private const val maxValue = 1000
        private const val progressValue = 200
        private const val intervalSeconds = 5000
        private const val defaultZoom = 16.0f
        private const val defaultRadiusText = "Raduis:200"
        const val TAG = "LocationUpdatesCallback"
    }
}