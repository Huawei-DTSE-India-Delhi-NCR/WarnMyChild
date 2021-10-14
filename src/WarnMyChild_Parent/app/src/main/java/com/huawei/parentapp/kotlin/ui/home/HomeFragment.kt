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
package com.huawei.parentapp.kotlin.ui.home

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.*
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.location.*
import com.huawei.hms.maps.*
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import com.huawei.parentapp.R
import com.huawei.parentapp.kotlin.ExceptionLogger
import com.huawei.parentapp.kotlin.LoginActivity
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.jvm.Throws

class HomeFragment : Fragment(), OnMapReadyCallback {
    private var mCloudDBZone: CloudDBZone? = null
    private var currentview: View? = null
    private var mRegister: ListenerHandler? = null
    private var sharedpreferences: SharedPreferences? = null
    private var startscanbtn: ImageView? = null
    private var cardview: CardView? = null
    private var parentview: LinearLayout? = null
    private var mMapView: MapView? = null
    private var huaweiMap: HuaweiMap? = null
    var mLocationCallback: LocationCallback? = null
    var mLocationRequest: LocationRequest? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentview = view
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "sdk >= 23 M")
            if (ActivityCompat.checkSelfPermission(activity as Context,
                            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(activity as Context,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val strings = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                ActivityCompat.requestPermissions(activity as Activity, strings, 1)
            }
        }
        AGConnectCloudDB.initialize(activity as Context)
        onLoadResources()
        addOnClickListener()
        establishconnection()
        setLocationInit()
        MapsInitializer.setApiKey(getString(R.string.API_KEY))
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(getString(R.string.map_view_bundle_key))
        }
        mMapView!!.onCreate(mapViewBundle)
        mMapView!!.getMapAsync(this)
        sharedpreferences = activity?.getSharedPreferences(getString(R.string.connection_shp_name), Context.MODE_PRIVATE)
    }

    private fun onLoadResources() {
        mMapView = currentview!!.findViewById(R.id.mapview)
        startscanbtn = currentview!!.findViewById<View>(R.id.btn_scan) as ImageView
        cardview = currentview!!.findViewById<View>(R.id.cardview) as CardView

        parentview = currentview!!.findViewById<View>(R.id.lay) as LinearLayout

        cardview!!.visibility = View.GONE
        parentview!!.visibility = View.GONE
        mMapView?.setVisibility(View.GONE)
    }

    private fun addOnClickListener() {
        startscanbtn!!.setOnClickListener { // QRCODE_SCAN_TYPE and DATAMATRIX_SCAN_TYPE are set for the barcode format, indicating that Scan Kit will support only QR Code and Data Matrix.
            val options = HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE, HmsScan.DATAMATRIX_SCAN_TYPE).create()
            ScanUtil.startScan(activity, REQUEST_CODE, options)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    fun setLocationInit() {
        //setlocationrequest
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        mSettingsClient = LocationServices.getSettingsClient(activity)
        mLocationRequest = LocationRequest()
        // Sets the interval for location update (unit: Millisecond)
        mLocationRequest!!.interval = INTERVAL_SECONDS.toLong()
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
                                        getString(R.string.location_result_lable) + location.longitude
                                                + "," + location.latitude + "," + location.accuracy)
                            }
                        }
                    }
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    if (locationAvailability != null) {
                        val flag = locationAvailability.isLocationAvailable
                        Log.d(TAG, getString(R.string.location_availablity_lable) + flag)
                    }
                }
            }
        }
    }

    fun updateNotificationFlag(notificationDetails: com.huawei.parentapp.java.NotificationDetails) {
        if (mCloudDBZone == null) {
            Log.d("TAG", "CloudDBZone is null, try re-open it")
            return
        }
        notificationDetails.isValid = false
        val upsertTask = mCloudDBZone!!.executeUpsert(notificationDetails)
        upsertTask.addOnSuccessListener { cloudDBZoneResult ->
            Log.d("TAG", "upsert_sucess_parent $cloudDBZoneResult records")
            Toast.makeText(activity, "upsert_sucess_notification $cloudDBZoneResult records", Toast.LENGTH_SHORT).show()
        }
        upsertTask.addOnFailureListener { e -> //  mUiCallBack.updateUiOnError("Insert book info failed");
            Log.d("TAG", "insert_failed " + e.localizedMessage + " records")
            Toast.makeText(activity, "insert_failed " + e.localizedMessage + " records", Toast.LENGTH_SHORT).show()
        }
    }

    fun queryParentDetails() {
        if (mCloudDBZone == null) {
            Log.d(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        val user = AGConnectAuth.getInstance().currentUser
        val query = CloudDBZoneQuery.where(com.huawei.parentapp.java.ParentInfo::class.java).equalTo("ParentID", user.uid)
        val queryTask = mCloudDBZone!!.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY)
        queryTask.addOnSuccessListener { snapshot ->
            val bookInfoCursor = snapshot.snapshotObjects
            val parentInfoslist: MutableList<com.huawei.parentapp.java.ParentInfo?> = ArrayList()
            try {
                while (bookInfoCursor.hasNext()) {
                    val parentInfo = bookInfoCursor.next()
                    parentInfoslist.add(parentInfo)
                }
                if (parentInfoslist.size > 0) {
                    Log.d(TAG, "parentInfoslistsize__: " + parentInfoslist.size)
                    cardview!!.visibility = View.GONE
                    parentview!!.visibility = View.GONE
                    mMapView!!.visibility = View.VISIBLE
                    val editor = sharedpreferences!!.edit()
                    editor.putBoolean("IsConnected", true)
                    editor.commit()
                } else {
                    Log.d(TAG, "parentInfoslist__else__: " + parentInfoslist.size)
                    cardview!!.visibility = View.VISIBLE
                    parentview!!.visibility = View.VISIBLE
                    mMapView!!.visibility = View.GONE
                }
            } catch (e: AGConnectCloudDBException) {
                Log.d(TAG, "processQueryResult: $e")
            }
            snapshot.release()
        }.addOnFailureListener { e ->
            Log.d(TAG, "CloudDBZone is null, try re-open it")
            showToastShort(e.message)
        }
    }

    fun addNotificationSubscription() {
        if (mCloudDBZone == null) {
            Log.d(TAG, "CloudDBZone is null, try re-open it")
            return
        }
        try {
            val user = AGConnectAuth.getInstance().currentUser
            val snapshotQuery = CloudDBZoneQuery.where(com.huawei.parentapp.java.NotificationDetails::class.java)
                    .equalTo("ParentID", user.uid).equalTo("isValid", true)
            mRegister = mCloudDBZone!!.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener)
        } catch (e: AGConnectCloudDBException) {
            Log.d(TAG, "subscribeSnapshot: $e")
        }
    }

    private val mSnapshotListener = OnSnapshotListener<com.huawei.parentapp.java.NotificationDetails> { cloudDBZoneSnapshot, e ->
        if (e != null) {
            Log.d(TAG, "onSnapshot: $e")
            return@OnSnapshotListener
        }
        val snapshotObjects = cloudDBZoneSnapshot.snapshotObjects
        val notificationlist: MutableList<com.huawei.parentapp.java.NotificationDetails> = ArrayList()
        try {
            if (snapshotObjects != null) {
                while (snapshotObjects.hasNext()) {
                    val notificationDetails = snapshotObjects.next()
                    notificationlist.add(notificationDetails)
                }
                if (notificationlist.size > 0) {
                    Log.d("onsnapsjotlistener_____", notificationlist[0].message)
                    shownotification(notificationlist[0].message)
                    updateNotificationFlag(notificationlist[0])
                }
            }
        } catch (snapshotException: Exception) {
            Log.d(TAG, "onSnapshot:(getObject) $snapshotException")
        } finally {
            cloudDBZoneSnapshot.release()
        }
    }

    private fun shownotification(message: String?) {
        val mNotificationManager: NotificationManager
        val mBuilder = NotificationCompat.Builder(activity as Context, "notify_001")
        val ii = Intent(activity, LoginActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(activity, 0, ii, 0)
        val bigText = NotificationCompat.BigTextStyle()
        bigText.bigText("Warning")
        bigText.setBigContentTitle("Message from Child App")
        bigText.setSummaryText(message)
        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round)
        mBuilder.setContentTitle("Warning")
        mBuilder.setContentText(message)
        mBuilder.priority = Notification.PRIORITY_MAX
        mBuilder.setStyle(bigText)
        mNotificationManager = activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

// === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "4546"
            val channel = NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH)
            mNotificationManager.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        mNotificationManager.notify(0, mBuilder.build())
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun showToastShort(content: String?) {
        Toast.makeText(activity, content, Toast.LENGTH_SHORT).show()
    }

    private fun showToastLong(content: String) {
        Toast.makeText(activity, content, Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Toast.makeText(activity, "Inside fragment on result", Toast.LENGTH_SHORT)
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        if (requestCode == REQUEST_CODE) {
            val obj: HmsScan = data.getParcelableExtra(ScanUtil.RESULT)
            if (obj != null) {
                // Display the decoding result.
                Toast.makeText(activity, obj.showResult, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Throws(JSONException::class)
    fun setScanResult(jsonObject: JSONObject) {
        upsertParentDetails(jsonObject)
    }

    fun establishconnection() {
        val mCloudDB = AGConnectCloudDB.getInstance()
        try {
            mCloudDB.createObjectType(com.huawei.parentapp.java.ObjectTypeInfoHelper.getObjectTypeInfo())
            val mConfig = CloudDBZoneConfig("test1",
                    CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                    CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC)
            mConfig.persistenceEnabled = true
            val openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true)
            openDBZoneTask.addOnSuccessListener { cloudDBZone ->
                Log.d("TAG", "open clouddbzone success")
                mCloudDBZone = cloudDBZone
                // Add subscription after opening cloudDBZone success
                addNotificationSubscription()
                Toast.makeText(activity, "openclouddbzonesuccess", Toast.LENGTH_SHORT).show()
                if (sharedpreferences!!.getBoolean("IsConnected", false)) {
                    cardview!!.visibility = View.GONE
                    parentview!!.visibility = View.GONE
                    mMapView!!.visibility = View.VISIBLE
                } else {
                    queryParentDetails()
                }
            }.addOnFailureListener { e ->
                Log.d("TAG", "open clouddbzone failed for $e")
                Toast.makeText(activity, "open clouddbzone failed for $e", Toast.LENGTH_SHORT).show()
            }
        } catch (e: AGConnectCloudDBException) {
            ExceptionLogger.printExceptionDetails("HomeFragment ", e)
        }
        catch(e: java.lang.Exception)
        {
            ExceptionLogger.printExceptionDetails("HomeFragmentKotlin__ ", e)
        }
    }

    @Throws(JSONException::class)
    fun upsertParentDetails(jsonObject: JSONObject) {
        if (mCloudDBZone == null) {
            Log.d("TAG", "CloudDBZone is null, try re-open it")
            return
        }
        val user = AGConnectAuth.getInstance().currentUser
        val parentInfo = com.huawei.parentapp.java.ParentInfo()
        parentInfo.parentID = user.uid
        parentInfo.parentName = user.displayName
        parentInfo.childIDs = jsonObject.getString("ChildID")
        parentInfo.emailID = user.email
        val upsertTask = mCloudDBZone!!.executeUpsert(parentInfo)
        upsertTask.addOnSuccessListener { cloudDBZoneResult ->
            Log.d("TAG", "upsert_sucess_parent " + " records")
            Toast.makeText(activity, "upsert_sucess_parent $cloudDBZoneResult records", Toast.LENGTH_SHORT).show()
            try {
                upsertChildDetails(jsonObject)
            } catch (e: JSONException) {
                ExceptionLogger.printExceptionDetails("HomeFragment ", e)
            }
        }
        upsertTask.addOnFailureListener { e -> //  mUiCallBack.updateUiOnError("Insert book info failed");
            Log.d("TAG", "insert_failed " + e.localizedMessage + " records")
            Toast.makeText(activity, "insert_failed " + e.localizedMessage + " records", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(JSONException::class)
    fun upsertChildDetails(jsonObject: JSONObject) {
        if (mCloudDBZone == null) {
            Log.d("TAG", "CloudDBZone is null, try re-open it")
            return
        }
        val user = AGConnectAuth.getInstance().currentUser
        val childInfo = com.huawei.parentapp.java.ChildInfo()
        childInfo.childID = jsonObject.getString("ChildID")
        childInfo.childName = jsonObject.getString("ChildName")
        childInfo.childEmail = jsonObject.getString("EmailID")
        childInfo.parentID = user.uid
        val upsertTask = mCloudDBZone!!.executeUpsert(childInfo)
        upsertTask.addOnSuccessListener { cloudDBZoneResult ->
            Log.d("TAG", "upsert_sucess_child " + " records")
            val editor = sharedpreferences!!.edit()
            editor.putBoolean("IsConnected", true)
            editor.commit()
            val editor1 = sharedpreferences!!.edit()
            try {
                editor1.putString("ChildID", jsonObject.getString("ChildID"))
            } catch (e: JSONException) {
                ExceptionLogger.printExceptionDetails("HomeFragment ", e)
            }
            editor1.commit()
            Navigation.findNavController(currentview!!).navigate(R.id.nav_geofence)
            Toast.makeText(activity, "Connected with Child app$cloudDBZoneResult records", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e -> //  mUiCallBack.updateUiOnError("Insert book info failed");
            Log.d("TAG", "insert_failed $e records")
            Toast.makeText(activity, "insert_failed $e records", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(huaweimap: HuaweiMap) {
        huaweiMap = huaweimap
        huaweiMap!!.isMyLocationEnabled = true // Enable the my-location overlay.
        huaweiMap!!.uiSettings.isMyLocationButtonEnabled = true // Enable the my-location icon.
        val task = mFusedLocationProviderClient!!.lastLocation // Define callback for success in obtaining the last known location.
                .addOnSuccessListener(OnSuccessListener { location ->
                    if (location == null) {
                        Toast.makeText(activity, "Location not available", Toast.LENGTH_SHORT).show()
                        return@OnSuccessListener
                    } else {
                        val update = CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude),
                                16.0f)
                        huaweiMap!!.moveCamera(update)
                        huaweiMap!!.addMarker(MarkerOptions()
                                .position(LatLng(location.latitude, location.longitude))
                                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("marker", 120, 120))))
                    }
                }) // Define callback for failure in obtaining the last known location.
                .addOnFailureListener {
                    // ...
                }
    }

    fun resizeMapIcons(iconName: String?, width: Int, height: Int): Bitmap {
        val imageBitmap = BitmapFactory.decodeResource(resources, resources.getIdentifier(iconName, "drawable", activity?.packageName))
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }

    companion object {
        private const val INTERVAL_SECONDS = 5000
        private const val REQUEST_CODE = 999
        const val TAG = "LocationUpdatesCallback"
    }
}




