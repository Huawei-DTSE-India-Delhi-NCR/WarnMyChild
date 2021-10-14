/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.warnmychild.child.java.ui.slideshow;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.huawei.hms.ads.AdListener;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.hmsscankit.WriterException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.GeofenceService;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptor;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.Circle;
import com.huawei.hms.maps.model.CircleOptions;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.warnmychild.child.java.DBHelper.CustomSharedPreference;
import com.huawei.warnmychild.child.java.DBHelper.GeofenceDetails;
import com.huawei.warnmychild.child.java.GeoFence.GeoFenceUpdate;
import com.huawei.warnmychild.child.java.GeoFence.GeoService;
import com.huawei.warnmychild.child.java.HomeActivity;
import com.huawei.warnmychild.child.R;


import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.content.Context.BIND_AUTO_CREATE;
import static com.huawei.warnmychild.child.java.Constant.GEO_FENCE_LAT;
import static com.huawei.warnmychild.child.java.Constant.GEO_FENCE_LONG;
import static com.huawei.warnmychild.child.java.Constant.GEO_FENCE_NAME;
import static com.huawei.warnmychild.child.java.Constant.GEO_FENCE_REDIUS;
import static com.huawei.warnmychild.child.java.Constant.MAPVIEW_BUNDLE_KEY;
import static com.huawei.warnmychild.child.java.HomeActivity.upsertNotificationInfos;
import static com.huawei.warnmychild.child.java.HomeActivity.user;


public class SlideshowFragment extends Fragment implements OnMapReadyCallback, HuaweiMap.OnMyLocationClickListener, HuaweiMap.OnMyLocationButtonClickListener {

    private SlideshowViewModel slideshowViewModel;
    static SlideshowFragment slideshowFragment;
    private static final String TAG = "TAG";
    private static final int REQUEST_CODE = 100;

    private static CustomSharedPreference customSharedPreference;

    public GeofenceService geofenceService;
    boolean isBind = false;
    GeoService geoService;
    CircleOptions circleOptions;
    Circle circle;

    //Ads
    private BannerView defaultBannerView;
    private static final int REFRESH_TIME = 30;

    //location
    LocationCallback mLocationCallbacks;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingsClient;

    public static TextView tv_connect_server;
    private HuaweiMap hmap;
    private MapView mMapView;
    private LinearLayout cv_connect;
    private static ImageView buttonConnect;
    private CardView cardview;
    private Dialog dialog;
    private static Context context;
    private boolean isMapUpdated;
    View root;



    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        slideshowViewModel = ViewModelProviders.of(this).get(SlideshowViewModel.class);
        root = inflater.inflate(R.layout.qr_fragmentjava, container, false);
        loadDefaultBannerAd(root);
        buttonConnect = root.findViewById(R.id.btn_connect);
        cardview = root.findViewById(R.id.cardview);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 openQRCode(v);
            }
        });

        slideshowFragment = (SlideshowFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_slideshow);
        context = getActivity();
        customSharedPreference = new CustomSharedPreference();
        tv_connect_server = (TextView) root.findViewById(R.id.tv_connect_server);
        tv_connect_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getActivity() != null){
                    ((HomeActivity)getActivity()).setAGC_DB();
                    tv_connect_server.setText("Connecting...");
                }else {
                    Log.d("TAG","Activity null ");
                }
            }
        });

        cv_connect = (LinearLayout) root.findViewById(R.id.cv_connect);
        mMapView = (MapView) root.findViewById(R.id.mapView);

        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        // MapView
        MapsInitializer.setApiKey(getString(R.string.API_KEY));
        mMapView.onCreate(mapViewBundle);

        if (customSharedPreference.getPrefsParentConnectionStatus(context) != null
                && customSharedPreference.getPrefsParentConnectionStatus(context).length() > 5) {
            enableMapview(customSharedPreference.getPrefsParentConnectionStatus(context));
        }
        ((HomeActivity)getActivity()).setAGC_DB();
        initLocation();
        return root;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    private static String getAccessJsonString() {

        if (user != null && user.getUid().length() > 5) {
            return "{\"ChildID\":\"" + user.getUid() + "\"," +
                    "\"ChildName\":\"" + user.getDisplayName() + "\"," +
                    "\"EmailID\":\"" + user.getEmail() + "\"}";
        } else return null;
    }

    public void openQRCode(View v) {

        // custom dialog
        dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.qr_dialog);
        dialog.setCanceledOnTouchOutside(true);

        ImageView image = (ImageView) dialog.findViewById(R.id.iv_qr_code);

        String userDetail = getAccessJsonString();
        if (userDetail == null) {
            userDetail = "No user found";
        }
        Bitmap bitmapQRCode = showQRCode(userDetail);
        if (bitmapQRCode != null) {
            image.setImageBitmap(bitmapQRCode);
        }

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_ok);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        dialog.show();

    }

    private static Bitmap showQRCode(String qr_content) {

        int type = HmsScan.QRCODE_SCAN_TYPE;
        int width = 600;
        int height = 600;

        HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator().setBitmapBackgroundColor(Color.TRANSPARENT).setBitmapColor(Color.BLACK).setBitmapMargin(3).create();

        try {
            //If the HmsBuildBitmapOption object is not constructed, set options to null.
            Log.d(TAG, "qrBitmap Generated");
            return ScanUtil.buildBitmap(qr_content, type, width, height, options);
        } catch (WriterException e) {
            Log.d("buildBitmap", e.getLocalizedMessage());
            return null;
        }
    }

    private void initLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        isMapUpdated = false;
        if (null == mLocationCallbacks) {
            mLocationCallbacks = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    for (Location item : locationResult.getLocations()) {
                        if (hmap != null && !isMapUpdated) {

                            hmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(item.getLatitude(), item.getLongitude()), 16f));
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), android.R.drawable.presence_online);
                            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
                            hmap.addMarker(new MarkerOptions().icon(bitmapDescriptor).position(new LatLng(item.getLatitude(),item.getLongitude())));
                            if(!hmap.isMyLocationEnabled())
                            hmap.setMyLocationEnabled(true);
                            isMapUpdated = false;
                        }
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    if (locationAvailability != null) {
                        boolean flag = locationAvailability.isLocationAvailable();
                        Toast.makeText(getActivity(), "Location Updation failed!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "GeoFence onLocationAvailability isLocationAvailable:" + flag);
                    }
                }
            };
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallbacks, Looper.getMainLooper());
    }


   // @RequiresApi(api = Build.VERSION_CODES.M)
   @RequiresPermission(allOf = {ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE})
    @Override
    public void onMapReady(HuaweiMap map) {
        Log.d(TAG, "onMapReady: ");
        hmap = map;
        hmap.setMyLocationEnabled(true);
        hmap.getUiSettings().setMyLocationButtonEnabled(true);

    }

    public void enableMapview(String parentId) {

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        final Toast toast = Toast.makeText(context, "Connected to Parent.", Toast.LENGTH_SHORT);
        toast.show();
        customSharedPreference.saveConnectionStatus(parentId, context);
        cv_connect.setVisibility(View.GONE);
        cardview.setVisibility(View.GONE);
        this.mMapView.setVisibility(View.VISIBLE);
        //get map instance
        this.mMapView.getMapAsync(this);
        Toast.makeText(getActivity(), "Map Updated.", Toast.LENGTH_SHORT).show();
    }






    /**
     * bind GeoService
     *
     * @param geoFenceInfo
     */
    public void startGeoFence(GeofenceDetails geoFenceInfo) {

        if (geoService != null) {
            geoService.updateGeoFence(geoFenceInfo.getLat(), geoFenceInfo.getLon(), geoFenceInfo.getRadius(), geoFenceInfo.getGeofenceName());
            return;
        }

        Log.d(TAG, "initGeoFence: " + getActivity());
        Intent intent = new Intent(getActivity(), GeoService.class);
        intent.putExtra(GEO_FENCE_LAT, geoFenceInfo.getLat());
        intent.putExtra(GEO_FENCE_LONG, geoFenceInfo.getLon());
        intent.putExtra(GEO_FENCE_REDIUS, geoFenceInfo.getRadius());
        intent.putExtra(GEO_FENCE_NAME, geoFenceInfo.getGeofenceName());
        getActivity().bindService(intent, conn, BIND_AUTO_CREATE);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {

            GeoService.MyBinder myBinder = (GeoService.MyBinder) binder;
            geoService = myBinder.getService();
            geoService.setOwner(new GeoFenceUpdate() {
                @Override
                public void update(String msg) {
                    Toast.makeText(getActivity(), msg + "", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Geo Updated");
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void geoLocationDetails(LatLng location, float redius, boolean isUpdate) {
                    Log.d(TAG, "Lat,lng: " + location.latitude + " , Redius: " + redius);
                    if (hmap != null) {
                        Toast.makeText(getActivity(),"Geofence Data are "+String.valueOf(location.latitude)+" "+String.valueOf(location.longitude),Toast.LENGTH_SHORT).show();
                        hmap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16f));
                        if (!isUpdate) {
                            circleOptions = new CircleOptions()
                                    .center(location)
                                    .radius(redius)
                                    .fillColor(R.color.colorAccent_light)
                                    .strokeColor(getActivity().getColor(R.color.colorPrimaryDark))
                                    .strokeWidth(3f);
                            circle = hmap.addCircle(circleOptions);
                        } else if (circle != null) {
                            // Circle will be updated when GeoFence will update.
                            circle.setRadius(redius);
                            circle.setCenter(location);
                        }
                    }
                }

                @Override
                public void getConversion(int conversion) {
                }
            });

            isBind = true;
            Log.d(TAG, "onServiceConnected____");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
            Log.d(TAG, "ActivityA - onServiceDisconnected");
        }
    };

    public static void sendData(int conversion) {
        switch (conversion) {
            case 1:// Enter the geofence
                Log.d(TAG, "Conversion : Enter the geofence");
                upsertNotificationInfos(context.getResources().getString(R.string.geofence_in_child));
                Toast.makeText(context, context.getResources().getString(R.string.geofence_in), Toast.LENGTH_SHORT).show();
                break;
            case 4:// Stay in geofence
                Log.d(TAG, "Conversion : Stay in geofence");
                upsertNotificationInfos(context.getResources().getString(R.string.geofence_stay_child));
                Toast.makeText(context, context.getResources().getString(R.string.geofence_stay), Toast.LENGTH_SHORT).show();
                break;
            case 2:// Go out of geofence
                Log.d(TAG, "Conversion : Go out of geofence");
                upsertNotificationInfos(context.getResources().getString(R.string.geofence_out_child));
                Toast.makeText(context, context.getResources().getString(R.string.geofence_out), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void setConnection(boolean b) {
        if(!b){
            slideshowFragment.tv_connect_server.setText("Re-try to connect");
        }else {
            slideshowFragment.tv_connect_server.setText("Connected");

        }
    }

    /**
     * Load the default banner ad.
     * @param root
     */
    private void loadDefaultBannerAd(View root) {
        // Obtain BannerView based on the configuration in layout/activity_main.xml.
        defaultBannerView = root.findViewById(R.id.hw_banner_view);
        defaultBannerView.setAdListener(adListener);
        defaultBannerView.setBannerRefresh(REFRESH_TIME);

        AdParam adParam = new AdParam.Builder().build();
        defaultBannerView.loadAd(adParam);
    }

    /**
     * Ad listener.
     */
    private AdListener adListener = new AdListener()
    {
        @Override
        public void onAdLoaded() {
            // Called when an ad is loaded successfully.
        }

        @Override
        public void onAdFailed(int errorCode) {
            // Called when an ad fails to be loaded.
        }

        @Override
        public void onAdOpened() {
            // Called when an ad is opened.
        }

        @Override
        public void onAdClicked() {
            // Called when a user taps an ad.
        }

        @Override
        public void onAdLeave() {
            // Called when a user has left the app.
        }

        @Override
        public void onAdClosed() {
            // Called when an ad is closed.
        }
    };

    @Override
    public void onMyLocationClick(Location location) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return true;
    }
}