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
package com.huawei.warnmychild.child.kotlin

import android.Manifest.permission
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.hmsscankit.WriterException
import com.huawei.hms.location.*
import com.huawei.hms.maps.*
import com.huawei.hms.maps.model.Circle
import com.huawei.hms.maps.model.CircleOptions
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.warnmychild.child.R
import com.huawei.warnmychild.child.kotlin.DBHelper.ChildInfo
import com.huawei.warnmychild.child.kotlin.DBHelper.CustomSharedPreference
import com.huawei.warnmychild.child.kotlin.DBHelper.GeofenceDetails
import com.huawei.warnmychild.child.kotlin.DBHelper.NotificationDetails
import com.huawei.warnmychild.child.kotlin.GeoFence.GeoFenceUpdate
import com.huawei.warnmychild.child.kotlin.GeoFence.GeoService
import com.huawei.warnmychild.child.kotlin.GeoFence.GeoService.MyBinder
import java.util.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var customSharedPreference: CustomSharedPreference? = null
    var geofenceService: GeofenceService? = null
    var isBind = false
    var geoService: GeoService? = null

    //location
    var mLocationCallbacks: LocationCallback? = null
    var mLocationRequest: LocationRequest? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    var lan: Double? = null
    var lon: Double? = null
    private var tv_connect_server: Button? = null
    private var hmap: HuaweiMap? = null
    private var mMapView: MapView? = null
    private var cv_connect: CardView? = null
    private val logout_button: Button? = null
    private var dialog: Dialog? = null
    private var isMapUpdated = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this@MainActivity
        customSharedPreference = CustomSharedPreference()
        if (!hasPermissions(this, *RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE)
        }
        user = AGConnectAuth.getInstance().currentUser
        tv_connect_server = findViewById<View>(R.id.tv_connect_server) as Button
        tv_connect_server!!.setOnClickListener { tv_connect_server!!.setText(R.string.connecting) }
        cv_connect = findViewById<View>(R.id.cv_connect) as CardView
        mMapView = findViewById<View>(R.id.mapView) as MapView
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constant.MAPVIEW_BUNDLE_KEY)
        }
        MapsInitializer.setApiKey(getString(R.string.API_KEY))
        mMapView!!.onCreate(mapViewBundle)
        if (customSharedPreference!!.getPrefsParentConnectionStatus(context) != null && customSharedPreference!!.getPrefsParentConnectionStatus(context)!!.length > UID_LENGTH && mCloudDBZone != null) {
            enableMapview()
        }
        initLocation()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        AGConnectAuth.getInstance().signOut()
        customSharedPreference!!.removeValue(context)
        mCloudDBZone = null
        finish()
    }

    fun openQRCode(v: View?) {
        dialog = Dialog(this@MainActivity)
        dialog!!.setContentView(R.layout.qr_dialog)
        dialog!!.setCanceledOnTouchOutside(false)
        val image = dialog!!.findViewById<View>(R.id.iv_qr_code) as ImageView
        var userDetail = accessJsonString
        if (userDetail == null) {
            userDetail = getString(R.string.no_user_found)
        }
        val bitmapQRCode = showQRCode(userDetail)
        if (bitmapQRCode != null) {
            image.setImageBitmap(bitmapQRCode)
        }
        val dialogButton = dialog!!.findViewById<View>(R.id.btn_ok) as Button
        dialogButton.setOnClickListener { }
        dialog!!.show()
    }

    private val accessJsonString: String?
        private get() = if (user != null && user!!.uid.length > UID_LENGTH) {
            "{\"ChildID\":\"" + user!!.uid + "\"," +
                    "\"ChildName\":\"" + user!!.displayName + "\"," +
                    "\"EmailID\":\"" + user!!.email + "\"}"
        } else null

    private fun showQRCode(qr_content: String): Bitmap? {
        val type = HmsScan.QRCODE_SCAN_TYPE
        val width = WIDTH_VALUE
        val height = HEIGHT_VALUE
        val options = HmsBuildBitmapOption.Creator().setBitmapBackgroundColor(Color.TRANSPARENT).setBitmapColor(Color.BLACK).setBitmapMargin(3).create()
        return try {
            Log.d(TAG, getString(R.string.qr_generated))
            ScanUtil.buildBitmap(qr_content, type, width, height, options)
        } catch (e: WriterException) {
            Log.d(getString(R.string.build_bitmap), e.localizedMessage)
            null
        }
    }

    private fun initLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = Constant.INTERVAL_SECONDS.toLong()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        isMapUpdated = false
        if (null == mLocationCallbacks) {
            mLocationCallbacks = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (item in locationResult.locations) {
                        if (hmap != null && !isMapUpdated) {
                            hmap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(item.latitude, item.longitude), DEFAULT_ZOOM))
                            Toast.makeText(applicationContext, "Location Updated.", Toast.LENGTH_SHORT).show()
                            isMapUpdated = true
                        }
                    }
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    if (locationAvailability != null) {
                        val flag = locationAvailability.isLocationAvailable
                        Toast.makeText(applicationContext, getString(R.string.location_update_failed), Toast.LENGTH_SHORT).show()
                        Log.d(TAG, getString(R.string.geo_fence_location_availablity) + flag)
                    }
                }
            }
        }
        mFusedLocationProviderClient?.requestLocationUpdates(mLocationRequest, mLocationCallbacks, Looper.getMainLooper())
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onMapReady(map: HuaweiMap) {
        Log.d(TAG, getString(R.string.on_map_ready_lable))
        hmap = map
        hmap!!.isMyLocationEnabled = true
        hmap!!.mapType = HuaweiMap.MAP_TYPE_NORMAL
    }

    fun enableMapview() {
        cv_connect!!.visibility = View.GONE
        mMapView!!.visibility = View.VISIBLE
        mMapView!!.getMapAsync(this)
        Toast.makeText(applicationContext, getString(R.string.map_updated_msg), Toast.LENGTH_SHORT).show()
    }

    private fun addGeoFenceSubscription() {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db))
            return
        }
        try {
            val snapshotQuery = CloudDBZoneQuery.where(GeofenceDetails::class.java)
                    .equalTo(getString(R.string.child_id_column_name), user!!.uid)
            mRegister = mCloudDBZone!!.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mGeoFenceSnapshotListener)
            Log.d(TAG, getString(R.string.subscribe_snapshot_result))
        } catch (e: AGConnectCloudDBException) {
            Log.d(TAG, getString(R.string.subscribe_snapshot_lable) + e)
        }
    }

    fun addChildInfoSubscription() {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db))
            return
        }
        try {
            val snapshotQuery = CloudDBZoneQuery.where(ChildInfo::class.java)
                    .equalTo(getString(R.string.child_id_column_name), user!!.uid)
            mRegister = mCloudDBZone!!.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, ChildInfoSnapshotListener)
            Log.d(TAG, getString(R.string.subscribe_snapshot_result))
        } catch (e: AGConnectCloudDBException) {
            Log.d(TAG, getString(R.string.subscribe_snapshot_lable) + e)
        }
    }

    private val ChildInfoSnapshotListener = OnSnapshotListener<ChildInfo> { cloudDBZoneSnapshot, e ->
        if (e != null) {
            Log.d(TAG, getString(R.string.on_snapshot) + e)
            return@OnSnapshotListener
        }
        val snapshotObjects = cloudDBZoneSnapshot.snapshotObjects
        val bookInfos: MutableList<ChildInfo> = ArrayList()
        try {
            if (snapshotObjects != null) {
                while (snapshotObjects.hasNext()) {
                    val bookInfo = snapshotObjects.next()
                    bookInfos.add(bookInfo)
                    if (bookInfo.childID == user!!.uid) {
                        runOnUiThread {
                            if (dialog != null && dialog!!.isShowing) {
                                dialog!!.dismiss()
                            }
                            val toast = Toast.makeText(context, R.string.connected_to_parent, Toast.LENGTH_SHORT)
                            toast.show()
                            customSharedPreference!!.saveConnectionStatus(bookInfo.parentID, context)
                            enableMapview()
                        }
                    }
                }
            }
        } catch (snapshotException: AGConnectCloudDBException) {
            Log.d(TAG, getString(R.string.on_snapshot_get_object) + snapshotException)
        } finally {
            cloudDBZoneSnapshot.release()
        }
    }
    private val mGeoFenceSnapshotListener = OnSnapshotListener<GeofenceDetails> { cloudDBZoneSnapshot, e ->
        if (e != null) {
            Log.d(TAG, getString(R.string.on_snapshot) + e)
            return@OnSnapshotListener
        }
        val snapshotObjects = cloudDBZoneSnapshot.snapshotObjects
        val geoFenceInfos: MutableList<GeofenceDetails> = ArrayList()
        try {
            if (snapshotObjects != null) {
                while (snapshotObjects.hasNext()) {
                    val geoFenceInfo = snapshotObjects.next()
                    geoFenceInfos.add(geoFenceInfo)
                    runOnUiThread {
                        val toast = Toast.makeText(context, "Geo Fence Data Received.", Toast.LENGTH_SHORT)
                        toast.show()
                        if (geoFenceInfo.childID == user!!.uid) {
                            startGeoFence(geoFenceInfo)
                        }
                    }
                }
            }
        } catch (snapshotException: AGConnectCloudDBException) {
            Log.d(TAG, getString(R.string.on_snapshot_get_object) + snapshotException)
        } finally {
            cloudDBZoneSnapshot.release()
        }
    }

    fun upsertChildInfos() {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db))
            return
        }
        val mNotificationDetails = ChildInfo()
        if (user != null && user!!.uid.length > UID_LENGTH) {
            mNotificationDetails.childID = user!!.uid
        } else {
            mNotificationDetails.childID = "No User Found"
        }
        mNotificationDetails.childEmail = getString(R.string.child_default_mail_id)
        mNotificationDetails.childName = getString(R.string.child_default_name)
        mNotificationDetails.parentID = getString(R.string.child_default_parent_id)
        val upsertTask = mCloudDBZone!!.executeUpsert(mNotificationDetails)
        upsertTask.addOnSuccessListener { Log.d(TAG, getString(R.string.upsert_success) + getString(R.string.records_lable)) }.addOnFailureListener { e -> Log.d(TAG, getString(R.string.insert_failed) + e + getString(R.string.records_lable)) }
    }

    fun upsertNotificationInfos(msg: String?) {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db))
            return
        }
        val mNotificationDetails = NotificationDetails()
        if (user != null && user!!.uid.length > UID_LENGTH) {
            mNotificationDetails.childID = user!!.uid
        } else {
            mNotificationDetails.childID = getString(R.string.no_user_found)
        }
        mNotificationDetails.dateTime = Calendar.getInstance().time
        mNotificationDetails.id = user!!.uid
        mNotificationDetails.isValid = true
        mNotificationDetails.message = msg
        mNotificationDetails.parentID = customSharedPreference!!.getPrefsParentConnectionStatus(context)
        val upsertTask = mCloudDBZone!!.executeUpsert(mNotificationDetails)
        upsertTask.addOnSuccessListener { cloudDBZoneResult ->
            Log.d(TAG, getString(R.string.notification_details_upsert_success) + cloudDBZoneResult + getString(R.string.records_lable))
            Toast.makeText(context, getString(R.string.notification_details_sent_success), Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e -> Log.d(TAG, getString(R.string.notification_details_insert_failed) + e + " records") }
    }

    /**
     * bind GeoService
     *
     * @param geoFenceInfo
     */
    private fun startGeoFence(geoFenceInfo: GeofenceDetails) {
        if (geoService != null) {
            geoService!!.updateGeoFence(geoFenceInfo.lat, geoFenceInfo.lon, geoFenceInfo.radius, geoFenceInfo.geofenceName)
            return
        }
        Log.d(TAG, getString(R.string.init_geofence_lable) + this@MainActivity)
        val intent = Intent(this@MainActivity, GeoService::class.java)
        intent.putExtra(Constant.GEO_FENCE_LAT, geoFenceInfo.lat)
        intent.putExtra(Constant.GEO_FENCE_LONG, geoFenceInfo.lon)
        intent.putExtra(Constant.GEO_FENCE_REDIUS, geoFenceInfo.radius)
        intent.putExtra(Constant.GEO_FENCE_NAME, geoFenceInfo.geofenceName)
        bindService(intent, conn, Context.BIND_AUTO_CREATE)
    }

    var circleOptions: CircleOptions? = null
    var circle: Circle? = null
    private val conn: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val myBinder = binder as MyBinder
            geoService = myBinder.service
            geoService!!.setOwner(object : GeoFenceUpdate {
                override fun update(msg: String) {
                    Toast.makeText(applicationContext, msg + "", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, getString(R.string.geo_updated_lable))
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                override fun geoLocationDetails(location: LatLng, redius: Float, isUpdate: Boolean) {
                    Log.d(TAG, getString(R.string.lat_lang_lable) + location.latitude + getString(R.string.radius_lable) + redius)
                    if (hmap != null) {
                        hmap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM))
                        if (!isUpdate) {
                            circleOptions = CircleOptions()
                                    .center(location)
                                    .radius(redius.toDouble())
                                    .fillColor(R.color.colorAccent_light)
                                    .strokeColor(getColor(R.color.colorPrimaryDark))
                                    .strokeWidth(3f)
                            circle = hmap!!.addCircle(circleOptions)
                        } else if (circle != null) {
                            // Circle will be updated when GeoFence will update.
                            circle!!.center = location
                        }
                    }
                }

                override fun getConversion(conversion: Int) {
                    //sendData(conversion);
                }
            })
            isBind = true
            Log.d(TAG, getString(R.string.on_service_connected))
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBind = false
            Log.d(TAG, getString(R.string.activity_a_service_disconnected))
        }
    }

    companion object {
        private const val TAG = "TAG"
        private const val REQUEST_CODE = 100
        private const val WIDTH_VALUE = 600
        private const val HEIGHT_VALUE = 600
        const val UID_LENGTH = 5
        const val DEFAULT_ZOOM = 16f

        // Cloud db objects decleratation
        private val mCloudDB: AGConnectCloudDB? = null
        private val mConfig: CloudDBZoneConfig? = null
        private var mCloudDBZone: CloudDBZone? = null
        private var mRegister: ListenerHandler? = null
        private var user: AGConnectUser? = null
        private var context: Context? = null
        private val RUNTIME_PERMISSIONS = arrayOf(permission.ACCESS_COARSE_LOCATION,
                permission.ACCESS_FINE_LOCATION, permission.INTERNET)

        // Checking the all necessory permissions.
        private fun hasPermissions(context: Context, vararg permissions: String): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
            }
            return true
        }

        fun sendData(conversion: Int) {
            when (conversion) {
                1 -> {
                    Log.d(TAG, "Conversion : Enter the geofence")
                    (context as MainActivity?)!!.upsertNotificationInfos(context!!.resources.getString(R.string.geofence_in))
                    Toast.makeText(context, context!!.resources.getString(R.string.geofence_in), Toast.LENGTH_SHORT).show()
                }
                4 -> {
                    Log.d(TAG, "Conversion : Stay in geofence")
                    (context as MainActivity?)!!.upsertNotificationInfos(context!!.resources.getString(R.string.geofence_stay))
                    Toast.makeText(context, context!!.resources.getString(R.string.geofence_stay), Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    Log.d(TAG, "Conversion : Go out of geofence")
                    (context as MainActivity?)!!.upsertNotificationInfos(context!!.resources.getString(R.string.geofence_out))
                    Toast.makeText(context, context!!.resources.getString(R.string.geofence_out), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}