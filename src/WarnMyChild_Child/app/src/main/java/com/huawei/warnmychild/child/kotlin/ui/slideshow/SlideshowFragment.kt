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
package com.huawei.warnmychild.child.kotlin.ui.slideshow

import android.Manifest.permission
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.banner.BannerView
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.hmsscankit.WriterException
import com.huawei.hms.location.*
import com.huawei.hms.maps.*
import com.huawei.hms.maps.HuaweiMap.OnMyLocationButtonClickListener
import com.huawei.hms.maps.HuaweiMap.OnMyLocationClickListener
import com.huawei.hms.maps.model.*
import com.huawei.hms.ml.scan.HmsBuildBitmapOption
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.warnmychild.child.kotlin.Constant
import com.huawei.warnmychild.child.kotlin.DBHelper.CustomSharedPreference
import com.huawei.warnmychild.child.kotlin.GeoFence.GeoFenceUpdate
import com.huawei.warnmychild.child.kotlin.GeoFence.GeoService
import com.huawei.warnmychild.child.kotlin.GeoFence.GeoService.MyBinder
import com.huawei.warnmychild.child.kotlin.HomeActivity
import com.huawei.warnmychild.child.R
import com.huawei.warnmychild.child.kotlin.DBHelper.GeofenceDetails

class SlideshowFragment : Fragment(), OnMapReadyCallback, OnMyLocationClickListener, OnMyLocationButtonClickListener {
    private var slideshowViewModel: SlideshowViewModel? = null
    var geofenceService: GeofenceService? = null
    var isBind = false
    var geoService: GeoService? = null
    var circleOptions: CircleOptions? = null
    var circle: Circle? = null

    //Ads
    private var defaultBannerView: BannerView? = null

    //location
    var mLocationCallbacks: LocationCallback? = null
    var mLocationRequest: LocationRequest? = null
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var hmap: HuaweiMap? = null
    private var mMapView: MapView? = null
    private var cv_connect: LinearLayout? = null
    private var cardview: CardView? = null
    private var dialog: Dialog? = null
    private var isMapUpdated = false
    var root: View? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        slideshowViewModel = ViewModelProviders.of(this).get(SlideshowViewModel::class.java)
        root = inflater.inflate(R.layout.qr_fragment, container, false)
        loadDefaultBannerAd(root)
        buttonConnect = root?.findViewById(R.id.btn_connect)
        cardview = root?.findViewById(R.id.cardview)
        buttonConnect?.setOnClickListener(View.OnClickListener { v -> openQRCode(v) })
        slideshowFragment = activity?.supportFragmentManager?.findFragmentById(R.id.nav_slideshow) as SlideshowFragment?
        Companion.context = activity
        customSharedPreference = CustomSharedPreference()
        tv_connect_server = root?.findViewById<View>(R.id.tv_connect_server) as TextView
        tv_connect_server!!.setOnClickListener {
            if (activity != null) {
                (activity as HomeActivity?)!!.setAGC_DB()
                tv_connect_server!!.text = "Connecting..."
            } else {
                Log.d("TAG", "Activity null ")
            }
        }
        cv_connect = root?.findViewById<View>(R.id.cv_connect) as LinearLayout
        mMapView = root?.findViewById<View>(R.id.mapView) as MapView
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(Constant.MAPVIEW_BUNDLE_KEY)
        }
        // MapView
        MapsInitializer.setApiKey(getString(R.string.API_KEY))
        mMapView!!.onCreate(mapViewBundle)
        if (customSharedPreference!!.getPrefsParentConnectionStatus(Companion.context) != null
                && customSharedPreference!!.getPrefsParentConnectionStatus(Companion.context)!!.length > 5) {
            enableMapview(customSharedPreference!!.getPrefsParentConnectionStatus(Companion.context))
        }
        (activity as HomeActivity?)!!.setAGC_DB()
        initLocation()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun openQRCode(v: View?) {

        // custom dialog
        dialog = Dialog(activity as Context)
        dialog!!.setContentView(R.layout.qr_dialog)
        dialog!!.setCanceledOnTouchOutside(false)
        val image = dialog!!.findViewById<View>(R.id.iv_qr_code) as ImageView
        var userDetail = accessJsonString
        if (userDetail == null) {
            userDetail = "No user found"
        }
        val bitmapQRCode = showQRCode(userDetail)
        if (bitmapQRCode != null) {
            image.setImageBitmap(bitmapQRCode)
        }
        val dialogButton = dialog!!.findViewById<View>(R.id.btn_ok) as Button
        dialogButton.setOnClickListener { }
        dialog!!.show()
    }

    private fun initLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity)
        mSettingsClient = LocationServices.getSettingsClient(activity)
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 10000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        isMapUpdated = false
        if (null == mLocationCallbacks) {
            mLocationCallbacks = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    for (item in locationResult.locations) {
                        if (hmap != null && !isMapUpdated) {
                            hmap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(item.latitude, item.longitude), 16f))
                            val bitmap = BitmapFactory.decodeResource(resources, android.R.drawable.presence_online)
                            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                            hmap!!.addMarker(MarkerOptions().icon(bitmapDescriptor).position(LatLng(item.latitude, item.longitude)))
                            if (!hmap!!.isMyLocationEnabled) hmap!!.isMyLocationEnabled = true
                            isMapUpdated = false
                        }
                    }
                }

                override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                    if (locationAvailability != null) {
                        val flag = locationAvailability.isLocationAvailable
                        Toast.makeText(activity, "Location Updation failed!", Toast.LENGTH_SHORT).show()
                        Log.d(TAG, "GeoFence onLocationAvailability isLocationAvailable:$flag")
                    }
                }
            }
        }
        mFusedLocationProviderClient?.requestLocationUpdates(mLocationRequest, mLocationCallbacks, Looper.getMainLooper())



    }

    // @RequiresApi(api = Build.VERSION_CODES.M)
    @RequiresPermission(allOf = [permission.ACCESS_FINE_LOCATION, permission.ACCESS_WIFI_STATE])
    override fun onMapReady(map: HuaweiMap) {
        Log.d(TAG, "onMapReady: ")
        hmap = map
        hmap!!.isMyLocationEnabled = true
        hmap!!.uiSettings.isMyLocationButtonEnabled = true
    }

    fun enableMapview(parentId: String?) {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
        }
        val toast = Toast.makeText(Companion.context, "Connected to Parent.", Toast.LENGTH_SHORT)
        toast.show()
        customSharedPreference!!.saveConnectionStatus(parentId, Companion.context)
        cv_connect!!.visibility = View.GONE
        cardview!!.visibility = View.GONE
        mMapView!!.visibility = View.VISIBLE
        //get map instance
        mMapView!!.getMapAsync(this)
        Toast.makeText(activity, "Map Updated.", Toast.LENGTH_SHORT).show()
    }

    /**
     * bind GeoService
     *
     * @param geoFenceInfo
     */
    fun startGeoFence(geoFenceInfo: GeofenceDetails) {
        if (geoService != null) {
            geoService!!.updateGeoFence(geoFenceInfo.lat, geoFenceInfo.lon, geoFenceInfo.radius, geoFenceInfo.geofenceName)
            return
        }
        Log.d(TAG, "initGeoFence: $activity")
        val intent = Intent(activity, GeoService::class.java)
        intent.putExtra(Constant.GEO_FENCE_LAT, geoFenceInfo.lat)
        intent.putExtra(Constant.GEO_FENCE_LONG, geoFenceInfo.lon)
        intent.putExtra(Constant.GEO_FENCE_REDIUS, geoFenceInfo.radius)
        intent.putExtra(Constant.GEO_FENCE_NAME, geoFenceInfo.geofenceName)
        activity?.bindService(intent, conn, Context.BIND_AUTO_CREATE)
    }

    private val conn: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            val myBinder = binder as MyBinder
            geoService = myBinder.service
            geoService!!.setOwner(object : GeoFenceUpdate {
                override fun update(msg: String) {
                    Toast.makeText(activity, msg + "", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Geo Updated")
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                override fun geoLocationDetails(location: LatLng, redius: Float, isUpdate: Boolean) {
                    Log.d(TAG, "Lat,lng: " + location.latitude + " , Redius: " + redius)
                    if (hmap != null) {
                        Toast.makeText(activity, "Geofence Data are " + location.latitude.toString() + " " + location.longitude.toString(), Toast.LENGTH_SHORT).show()
                        hmap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f))
                        if (!isUpdate) {
                            circleOptions = CircleOptions()
                                    .center(location)
                                    .radius(redius.toDouble())
                                    .fillColor(R.color.colorAccent_light)
                                    .strokeColor(activity!!.getColor(R.color.colorPrimaryDark))
                                    .strokeWidth(3f)
                            circle = hmap!!.addCircle(circleOptions)
                        } else if (circle != null) {
                            // Circle will be updated when GeoFence will update.
                            circle!!.radius = redius.toDouble()
                            circle!!.center = location
                        }
                    }
                }

                override fun getConversion(conversion: Int) {}
            })
            isBind = true
            Log.d(TAG, "  onServiceConnected")
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isBind = false
            Log.d(TAG, "ActivityA - onServiceDisconnected")
        }
    }

    fun setConnection(b: Boolean) {
        if (!b) {
            tv_connect_server!!.text = "Re-try to connect"
        } else {
            tv_connect_server!!.text = "Connected"
        }
    }

    /**
     * Load the default banner ad.
     * @param root
     */
    private fun loadDefaultBannerAd(root: View?) {
        // Obtain BannerView based on the configuration in layout/activity_main.xml.
        defaultBannerView = root!!.findViewById(R.id.hw_banner_view)
        defaultBannerView?.setAdListener(adListener)
        defaultBannerView?.setBannerRefresh(REFRESH_TIME.toLong())
        val adParam = AdParam.Builder().build()
        defaultBannerView?.loadAd(adParam)
    }

    /**
     * Ad listener.
     */
    private val adListener: AdListener = object : AdListener() {
        override fun onAdLoaded() {
            // Called when an ad is loaded successfully.
        }

        override fun onAdFailed(errorCode: Int) {
            // Called when an ad fails to be loaded.
        }

        override fun onAdOpened() {
            // Called when an ad is opened.
        }

        override fun onAdClicked() {
            // Called when a user taps an ad.
        }

        override fun onAdLeave() {
            // Called when a user has left the app.
        }

        override fun onAdClosed() {
            // Called when an ad is closed.
        }
    }

    override fun onMyLocationClick(location: Location) {}
    override fun onMyLocationButtonClick(): Boolean {
        return true
    }

    companion object {
        var slideshowFragment: SlideshowFragment? = null
        private const val TAG = "TAG"
        private const val REQUEST_CODE = 100
        private var customSharedPreference: CustomSharedPreference? = null
        private const val REFRESH_TIME = 30
        var tv_connect_server: TextView? = null
        private var buttonConnect: ImageView? = null
        private var context: Context? = null
        private val accessJsonString: String?
            private get() = if (HomeActivity.Companion.user != null && HomeActivity.Companion.user!!.uid.length > 5) {
                "{\"ChildID\":\"" + HomeActivity.Companion.user?.uid + "\"," +
                        "\"ChildName\":\"" + HomeActivity.Companion.user?.displayName + "\"," +
                        "\"EmailID\":\"" + HomeActivity.Companion.user?.email + "\"}"
            } else null

        private fun showQRCode(qr_content: String): Bitmap? {
            val type = HmsScan.QRCODE_SCAN_TYPE
            val width = 600
            val height = 600
            val options = HmsBuildBitmapOption.Creator().setBitmapBackgroundColor(Color.TRANSPARENT).setBitmapColor(Color.BLACK).setBitmapMargin(3).create()
            return try {
                //If the HmsBuildBitmapOption object is not constructed, set options to null.
                Log.d(TAG, "qrBitmap Generated")
                ScanUtil.buildBitmap(qr_content, type, width, height, options)
            } catch (e: WriterException) {
                Log.d("buildBitmap", e.localizedMessage)
                null
            }
        }

        fun sendData(conversion: Int) {
            when (conversion) {
                1 -> {
                    Log.d(TAG, "Conversion : Enter the geofence")
                    HomeActivity.Companion.upsertNotificationInfos(context!!.resources.getString(R.string.geofence_in_child))
                    Toast.makeText(context, context!!.resources.getString(R.string.geofence_in), Toast.LENGTH_SHORT).show()
                }
                4 -> {
                    Log.d(TAG, "Conversion : Stay in geofence")
                    HomeActivity.Companion.upsertNotificationInfos(context!!.resources.getString(R.string.geofence_stay_child))
                    Toast.makeText(context, context!!.resources.getString(R.string.geofence_stay), Toast.LENGTH_SHORT).show()
                }
                2 -> {
                    Log.d(TAG, "Conversion : Go out of geofence")
                    HomeActivity.Companion.upsertNotificationInfos(context!!.resources.getString(R.string.geofence_out_child))
                    Toast.makeText(context, context!!.resources.getString(R.string.geofence_out), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}