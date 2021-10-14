/*
    Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.huawei.warnmychild.child.kotlin.GeoFence

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.huawei.hms.common.ApiException
import com.huawei.hms.location.*
import com.huawei.hms.maps.model.LatLng
import com.huawei.warnmychild.child.R
import com.huawei.warnmychild.child.kotlin.Constant
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class GeoService : Service() {
    private val TAG = "TAG"
    var geofenceService: GeofenceService? = null
    val DEFAULT_NOTIFICATION_ID = 114253

    //loc
    var mLocationCallbacks: LocationCallback? = null
    var mLocationRequest: LocationRequest? = null
    private val mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val mSettingsClient: SettingsClient? = null
    var lan: Double? = null
    var lon: Double? = null
    var redius = 0f
    var isUpdate = false
    private val requestList = ArrayList<RequestList>()
    var trigGer = 7
    private var update: GeoFenceUpdate? = null
    var dataTemp: Data? = null

    inner class MyBinder : Binder() {
        val service: GeoService
            get() = this@GeoService
    }

    /**************************************************************************
     * Bound methods.
     *
     * Set the update, to be notified when the Geo fence update.
     *
     * @param update
     */
    fun setOwner(update: GeoFenceUpdate?) {
        this.update = update
    }

    private val binder = MyBinder()
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind: ")
        geofenceService = GeofenceService(this)
        lan = java.lang.Double.valueOf(intent.getStringExtra(Constant.GEO_FENCE_LAT))
        lon = java.lang.Double.valueOf(intent.getStringExtra(Constant.GEO_FENCE_LONG))
        redius = java.lang.Float.valueOf(intent.getStringExtra(Constant.GEO_FENCE_REDIUS))
        createGeo()
        geoFence()
        return binder
    }

    fun updateGeoFence(lat: String?, lng: String?, updatedRedius: String?, name: String?) {
        removeAllFence()
        lan = java.lang.Double.valueOf(lat!!)
        lon = java.lang.Double.valueOf(lng!!)
        redius = updatedRedius!!.toFloat()
        isUpdate = true
        createGeo()
    }

    fun createGeo() {
        dataTemp = Data()
        dataTemp!!.longitude = lon!!
        dataTemp!!.latitude = lan!!
        dataTemp!!.radius = redius


        try {
            var rand_int = DEFAULT_NOTIFICATION_ID
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                rand_int = ThreadLocalRandom.current().nextInt()
            }

            dataTemp!!.uniqueId = rand_int.toString()
        }
        catch (e : Exception)
        {
            dataTemp!!.uniqueId = DEFAULT_NOTIFICATION_ID.toString()
            e.printStackTrace()
        }

       // dataTemp!!.uniqueId = "456645446";//genRandBytes(UNIQUE_ID_LENGTH)
        dataTemp!!.conversions = getString(R.string.default_conversion_value).toInt()
        dataTemp!!.validContinueTime = getString(R.string.default_valid_continue_time).toLong()
        dataTemp!!.dwellDelayTime = getString(R.string.default_dwell_delay_time).toInt()
        dataTemp!!.notificationInterval = getString(R.string.default_notification_interval).toInt()
        GeoFenceData.addGeofence(dataTemp!!)
        geoData
    }

    fun genRandBytes(len: Int): String {
        var bytes: ByteArray? = null
        if (len > 0 && len < ONE_BYTE_LENGTH) {
            bytes = ByteArray(len)
            val random = SecureRandom()
            random.nextBytes(bytes)
            return String(bytes, StandardCharsets.UTF_8)
        }
        return ""
    }

    val geoData: Unit
        get() {
            val geofences = GeoFenceData.returnList()
            val buf = StringBuilder()
            var s = ""
            if (geofences!!.isEmpty()) {
                buf.append(getString(R.string.no_geofence_data))
            }
            for (i in geofences.indices) {
                buf.append("""
    Unique ID is ${geofences[i]!!.uniqueId}
    
    """.trimIndent())
            }
            s = buf.toString()
            if (geofences.size > 0) {
                requestGeoFenceWithNewIntent()
            }
        }

    private var pendingIntent: PendingIntent? = null
    var geofenceRequest: GeofenceRequest.Builder? = null
    fun requestGeoFenceWithNewIntent() {
        if (GeoFenceData.returnList().isEmpty() == true) {
            Log.d(TAG, getString(R.string.no_new_request))
            return
        }
        if (geofenceRequest == null) {
            geofenceRequest = GeofenceRequest.Builder()
            geofenceRequest!!.createGeofenceList(GeoFenceData.returnList())
        }
        if (true) {
            geofenceRequest!!.setInitConversions(trigGer)
            Log.d(TAG, getString(R.string.trigger_lable) + trigGer)
        } else {
            geofenceRequest!!.setInitConversions(DEFAULT_TRIGGER_LENGTH)
            Log.d(TAG, getString(R.string.default_trigger_lable) + DEFAULT_TRIGGER_LENGTH)
        }
        if (pendingIntent == null) {
            pendingIntent = getPendingIntent()
            setList(pendingIntent, GeoFenceData.requestCode, GeoFenceData.returnList())
        }
        try {
            geofenceService!!.createGeofenceList(geofenceRequest!!.build(), pendingIntent)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i(TAG, getString(R.string.geofence_success_lable))
                            if (dataTemp != null) {
                                update!!.update(getString(R.string.geofence_success_lable))
                                update!!.geoLocationDetails(LatLng(lan!!, lon!!), redius, isUpdate)
                            }
                        } else {
                            Log.d(TAG, getString(R.string.geofence_failed_lable) + task.exception)
                        }
                    }
        } catch (e: Exception) {
            Log.d(TAG, getString(R.string.geofence_error_lable) + e)
        }
        GeoFenceData.createNewList()
    }

    fun setList(intent: PendingIntent?, code: Int, geofences: ArrayList<Geofence?>?) {
        val temp = RequestList(intent, code, geofences)
        requestList.add(temp)
    }

    private fun geoFence() {
        try {
            val builders = LocationSettingsRequest.Builder()
            builders.addLocationRequest(mLocationRequest)
            val locationSettingsRequest = builders.build()
            val locationSettingsResponseTasks = mSettingsClient!!.checkLocationSettings(locationSettingsRequest)
            locationSettingsResponseTasks.addOnSuccessListener {
                Log.d(TAG, getString(R.string.location_setting_success))
                mFusedLocationProviderClient
                        ?.requestLocationUpdates(mLocationRequest, mLocationCallbacks, Looper.getMainLooper())
                        ?.addOnSuccessListener { Log.d(TAG, getString(R.string.geofence_success)) }
                        ?.addOnFailureListener { e -> Log.d(TAG, getString(R.string.geofence_failure) + e) }
            }
                    .addOnFailureListener { e ->
                        Log.d(TAG, getString(R.string.location_setting_onfailure) + e)
                        val statusCodes = (e as ApiException).statusCode
                        when (statusCodes) {
                            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            }
                            else -> {
                            }
                        }
                    }
        } catch (e: Exception) {
            Log.d(TAG, getString(R.string.geofence_exception) + e)
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, GeoFenceBroadcastReceiver::class.java)
        intent.action = GeoFenceBroadcastReceiver.Companion.ACTION_PROCESS_LOCATION
        Log.d(TAG, "new request")
        GeoFenceData.newRequest()
        return PendingIntent.getBroadcast(this, GeoFenceData.requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun removeAllFence() {
        GeoFenceData.createNewList()
        for (request in requestList) {
            geofenceService!!.deleteGeofenceList(request.intnet)
        }
    }

    override fun onUnbind(intent: Intent): Boolean {
        Log.d(TAG, getString(R.string.method_on_unbind))
        removeAllFence()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        Log.d(TAG, getString(R.string.method_on_destroy))
        super.onDestroy()
    }

    companion object {
        const val UNIQUE_ID_LENGTH = 1000
        const val ONE_BYTE_LENGTH = 1024
        const val DEFAULT_TRIGGER_LENGTH = 5
    }



}


