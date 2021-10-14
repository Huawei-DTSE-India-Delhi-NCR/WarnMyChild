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
package com.huawei.warnmychild.child.java;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
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
import com.huawei.hms.maps.model.Circle;
import com.huawei.hms.maps.model.CircleOptions;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.warnmychild.child.java.DBHelper.ChildInfo;
import com.huawei.warnmychild.child.java.DBHelper.CustomSharedPreference;
import com.huawei.warnmychild.child.java.DBHelper.GeofenceDetails;
import com.huawei.warnmychild.child.java.DBHelper.NotificationDetails;
import com.huawei.warnmychild.child.java.GeoFence.GeoFenceUpdate;
import com.huawei.warnmychild.child.java.GeoFence.GeoService;
import com.huawei.warnmychild.child.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.huawei.warnmychild.child.java.Constant.GEO_FENCE_LAT;
import static com.huawei.warnmychild.child.java.Constant.GEO_FENCE_LONG;
import static com.huawei.warnmychild.child.java.Constant.GEO_FENCE_NAME;
import static com.huawei.warnmychild.child.java.Constant.GEO_FENCE_REDIUS;
import static com.huawei.warnmychild.child.java.Constant.MAPVIEW_BUNDLE_KEY;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "TAG";
    private static final int REQUEST_CODE = 100;
    private static final int WIDTH_VALUE = 600;
    private static final int HEIGHT_VALUE = 600;
    public static final int UID_LENGTH = 5;

    public static final float DEFAULT_ZOOM = 16f;

    // Cloud db objects decleratation
    private static AGConnectCloudDB mCloudDB;
    private static CloudDBZoneConfig mConfig;
    private static CloudDBZone mCloudDBZone;
    private static ListenerHandler mRegister;
    private static AGConnectUser user;

    private CustomSharedPreference customSharedPreference;

    public GeofenceService geofenceService;
    boolean isBind = false;
    GeoService geoService;

    //location
    LocationCallback mLocationCallbacks;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingsClient;
    Double lan;
    Double lon;

    private Button tv_connect_server;
    private HuaweiMap hmap;
    private MapView mMapView;
    private CardView cv_connect;
    private Button logout_button;
    private Dialog dialog;
    private static Context context;
    private boolean isMapUpdated;

    private static final String[] RUNTIME_PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;
        customSharedPreference = new CustomSharedPreference();

        if (!hasPermissions(this, RUNTIME_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, RUNTIME_PERMISSIONS, REQUEST_CODE);
        }

        user = AGConnectAuth.getInstance().getCurrentUser();

        tv_connect_server = (Button) findViewById(R.id.tv_connect_server);
        tv_connect_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_connect_server.setText(R.string.connecting);
            }
        });

        cv_connect = (CardView) findViewById(R.id.cv_connect);
        mMapView = (MapView) findViewById(R.id.mapView);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        MapsInitializer.setApiKey(getString(R.string.API_KEY));
        mMapView.onCreate(mapViewBundle);

        if (customSharedPreference.getPrefsParentConnectionStatus(context) != null
                && customSharedPreference.getPrefsParentConnectionStatus(context).length() > UID_LENGTH
                && mCloudDBZone != null) {
            enableMapview();
        }
        initLocation();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        AGConnectAuth.getInstance().signOut();
        customSharedPreference.removeValue(context);
        mCloudDBZone = null;
        finish();
    }

    public void openQRCode(View v) {
        dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.qr_dialog);
        dialog.setCanceledOnTouchOutside(false);

        ImageView image = (ImageView) dialog.findViewById(R.id.iv_qr_code);

        String userDetail = getAccessJsonString();
        if (userDetail == null) {
            userDetail = getString(R.string.no_user_found);
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

    private String getAccessJsonString() {

        if (user != null && user.getUid().length() > UID_LENGTH) {
            return "{\"ChildID\":\"" + user.getUid() + "\"," +
                    "\"ChildName\":\"" + user.getDisplayName() + "\"," +
                    "\"EmailID\":\"" + user.getEmail() + "\"}";
        } else return null;
    }

    private Bitmap showQRCode(String qr_content) {

        int type = HmsScan.QRCODE_SCAN_TYPE;
        int width = WIDTH_VALUE;
        int height = HEIGHT_VALUE;

        HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator().setBitmapBackgroundColor(Color.TRANSPARENT).setBitmapColor(Color.BLACK).setBitmapMargin(3).create();

        try {
            Log.d(TAG, getString(R.string.qr_generated));
            return ScanUtil.buildBitmap(qr_content, type, width, height, options);
        } catch (WriterException e) {
            Log.d(getString(R.string.build_bitmap), e.getLocalizedMessage());
            return null;
        }

    }

    private void initLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(Constant.INTERVAL_SECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        isMapUpdated = false;
        if (null == mLocationCallbacks) {
            mLocationCallbacks = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    for (Location item : locationResult.getLocations()) {
                        if (hmap != null && !isMapUpdated) {
                            hmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(item.getLatitude(), item.getLongitude()), DEFAULT_ZOOM));
                            Toast.makeText(getApplicationContext(), "Location Updated.", Toast.LENGTH_SHORT).show();
                            isMapUpdated = true;
                        }
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    if (locationAvailability != null) {
                        boolean flag = locationAvailability.isLocationAvailable();
                        Toast.makeText(getApplicationContext(), getString(R.string.location_update_failed), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, getString(R.string.geo_fence_location_availablity) + flag);
                    }
                }
            };
        }
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallbacks, Looper.getMainLooper());
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(HuaweiMap map) {
        Log.d(TAG, getString(R.string.on_map_ready_lable));
        hmap = map;
        hmap.setMyLocationEnabled(true);
        hmap.setMapType(HuaweiMap.MAP_TYPE_NORMAL);
    }


    public void enableMapview() {

        cv_connect.setVisibility(View.GONE);
        this.mMapView.setVisibility(View.VISIBLE);
        this.mMapView.getMapAsync(this);
        Toast.makeText(getApplicationContext(), getString(R.string.map_updated_msg), Toast.LENGTH_SHORT).show();
    }

    private void addGeoFenceSubscription() {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db));
            return;
        }
        try {
            CloudDBZoneQuery<GeofenceDetails> snapshotQuery = CloudDBZoneQuery.where(GeofenceDetails.class)
                    .equalTo(getString(R.string.child_id_column_name), user.getUid());

            mRegister = mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mGeoFenceSnapshotListener);

            Log.d(TAG, getString(R.string.subscribe_snapshot_result));
        } catch (AGConnectCloudDBException e) {
            Log.d(TAG, getString(R.string.subscribe_snapshot_lable) + e);
        }
    }

    private OnSnapshotListener<ChildInfo> ChildInfoSnapshotListener = new OnSnapshotListener<ChildInfo>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<ChildInfo> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.d(TAG, getString(R.string.on_snapshot) + e);
                return;
            }
            CloudDBZoneObjectList<ChildInfo> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            List<ChildInfo> bookInfos = new ArrayList<>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        ChildInfo bookInfo = snapshotObjects.next();
                        bookInfos.add(bookInfo);

                        if (bookInfo.getChildID().equals(user.getUid())) {

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    if (dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                    }
                                    final Toast toast = Toast.makeText(context, R.string.connected_to_parent, Toast.LENGTH_SHORT);
                                    toast.show();
                                    customSharedPreference.saveConnectionStatus(bookInfo.getParentID(), context);
                                    enableMapview();
                                }
                            });

                        }
                    }
                }

            } catch (AGConnectCloudDBException snapshotException) {
                Log.d(TAG, getString(R.string.on_snapshot_get_object) + snapshotException);
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };


    private OnSnapshotListener<GeofenceDetails> mGeoFenceSnapshotListener = new OnSnapshotListener<GeofenceDetails>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<GeofenceDetails> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.d(TAG, getString(R.string.on_snapshot)+ e);
                return;
            }
            CloudDBZoneObjectList<GeofenceDetails> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            List<GeofenceDetails> geoFenceInfos = new ArrayList<>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        GeofenceDetails geoFenceInfo = snapshotObjects.next();
                        geoFenceInfos.add(geoFenceInfo);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = Toast.makeText(context, "Geo Fence Data Received.", Toast.LENGTH_SHORT);
                                toast.show();
                                if (geoFenceInfo.getChildID().equals(user.getUid())) {
                                    startGeoFence(geoFenceInfo);
                                }
                            }
                        });
                    }
                }

            } catch (AGConnectCloudDBException snapshotException) {
                Log.d(TAG, getString(R.string.on_snapshot_get_object) + snapshotException);
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };

    public void upsertChildInfos() {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db));
            return;
        }

        ChildInfo mNotificationDetails = new ChildInfo();

        if (user != null && user.getUid().length() > UID_LENGTH) {
            mNotificationDetails.setChildID(user.getUid());
        } else {
            mNotificationDetails.setChildID("No User Found");
        }

        mNotificationDetails.setChildEmail(getString(R.string.child_default_mail_id));
        mNotificationDetails.setChildName(getString(R.string.child_default_name));
        mNotificationDetails.setParentID(getString(R.string.child_default_parent_id));

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(mNotificationDetails);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.d(TAG, getString(R.string.upsert_success) + getString(R.string.records_lable));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, getString(R.string.insert_failed) + e + getString(R.string.records_lable));
            }
        });
    }


    public void upsertNotificationInfos(String msg) {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db));
            return;
        }

        NotificationDetails mNotificationDetails = new NotificationDetails();

        if (user != null && user.getUid().length() > UID_LENGTH) {
            mNotificationDetails.setChildID(user.getUid());
        } else {
            mNotificationDetails.setChildID(getString(R.string.no_user_found));
        }

        mNotificationDetails.setDateTime(Calendar.getInstance().getTime());
        mNotificationDetails.setID(user.getUid());
        mNotificationDetails.setIsValid(true);
        mNotificationDetails.setMessage(msg);
        mNotificationDetails.setParentID(customSharedPreference.getPrefsParentConnectionStatus(context));

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(mNotificationDetails);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.d(TAG, getString(R.string.notification_details_upsert_success) + cloudDBZoneResult +getString(R.string.records_lable));
                Toast.makeText(context, getString(R.string.notification_details_sent_success), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, getString(R.string.notification_details_insert_failed) + e + " records");
            }
        });
    }

    // Checking the all necessory permissions.
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * bind GeoService
     *
     * @param geoFenceInfo
     */
    private void startGeoFence(GeofenceDetails geoFenceInfo) {

        if (geoService != null) {
            geoService.updateGeoFence(geoFenceInfo.getLat(), geoFenceInfo.getLon(), geoFenceInfo.getRadius(), geoFenceInfo.getGeofenceName());
            return;
        }

        Log.d(TAG, getString(R.string.init_geofence_lable) + MainActivity.this);
        Intent intent = new Intent(MainActivity.this, GeoService.class);
        intent.putExtra(GEO_FENCE_LAT, geoFenceInfo.getLat());
        intent.putExtra(GEO_FENCE_LONG, geoFenceInfo.getLon());
        intent.putExtra(GEO_FENCE_REDIUS, geoFenceInfo.getRadius());
        intent.putExtra(GEO_FENCE_NAME, geoFenceInfo.getGeofenceName());
        bindService(intent, conn, BIND_AUTO_CREATE);
    }

    CircleOptions circleOptions;
    Circle circle;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {

            GeoService.MyBinder myBinder = (GeoService.MyBinder) binder;
            geoService = myBinder.getService();
            geoService.setOwner(new GeoFenceUpdate() {
                @Override
                public void update(String msg) {
                    Toast.makeText(getApplicationContext(), msg + "", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, getString(R.string.geo_updated_lable));
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void geoLocationDetails(LatLng location, float redius, boolean isUpdate) {
                    Log.d(TAG, getString(R.string.lat_lang_lable) + location.latitude + getString(R.string.radius_lable) + redius);
                    if (hmap != null) {
                        hmap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
                        if (!isUpdate) {
                            circleOptions = new CircleOptions()
                                    .center(location)
                                    .radius(redius)
                                    .fillColor(R.color.colorAccent_light)
                                    .strokeColor(getColor(R.color.colorPrimaryDark))
                                    .strokeWidth(3f);
                            circle = hmap.addCircle(circleOptions);
                        } else if(circle!= null){
                            // Circle will be updated when GeoFence will update.
                            circle.setCenter(location);
                        }
                    }
                }

                @Override
                public void getConversion(int conversion) {
                    //sendData(conversion);
                }
            });

            isBind = true;
            Log.d(TAG, getString(R.string.on_service_connected));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
            Log.d(TAG, getString(R.string.activity_a_service_disconnected));
        }
    };

    public static void sendData(int conversion) {
        switch (conversion) {
            case 1:// Enter the geofence
                Log.d(TAG, "Conversion : Enter the geofence");
                ((MainActivity) context).upsertNotificationInfos(context.getResources().getString(R.string.geofence_in));
                Toast.makeText(context, context.getResources().getString(R.string.geofence_in), Toast.LENGTH_SHORT).show();
                break;
            case 4:// Stay in geofence
                Log.d(TAG, "Conversion : Stay in geofence");
                ((MainActivity) context).upsertNotificationInfos(context.getResources().getString(R.string.geofence_stay));
                Toast.makeText(context, context.getResources().getString(R.string.geofence_stay), Toast.LENGTH_SHORT).show();
                break;
            case 2:// Go out of geofence
                Log.d(TAG, "Conversion : Go out of geofence");
                ((MainActivity) context).upsertNotificationInfos(context.getResources().getString(R.string.geofence_out));
                Toast.makeText(context, context.getResources().getString(R.string.geofence_out), Toast.LENGTH_SHORT).show();
                break;
        }
    }
}