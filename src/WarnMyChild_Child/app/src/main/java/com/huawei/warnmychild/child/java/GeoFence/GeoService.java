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
package com.huawei.warnmychild.child.java.GeoFence;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.Geofence;
import com.huawei.hms.location.GeofenceRequest;
import com.huawei.hms.location.GeofenceService;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.warnmychild.child.java.Constant;
import com.huawei.warnmychild.child.R;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;


public class GeoService extends Service {

    private String TAG = "TAG";
    public static final int UNIQUE_ID_LENGTH = 1000;
    public static final int ONE_BYTE_LENGTH = 1024;
    public static final int DEFAULT_TRIGGER_LENGTH = 5;
    public static final int DEFAULT_NOTIFICATION_ID = 114253;

    public GeofenceService geofenceService;
    //loc
    LocationCallback mLocationCallbacks;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingsClient;
    Double lan;
    Double lon;
    float redius;
    boolean isUpdate = false;
    private ArrayList<RequestList> requestList = new ArrayList<RequestList>();
    int trigGer = 7;
    private GeoFenceUpdate update;

    Data dataTemp;

    public class MyBinder extends Binder {
        public GeoService getService() {
            return GeoService.this;
        }
    }

    /**************************************************************************
     * Bound methods.
     *
     * Set the update, to be notified when the Geo fence update.
     *
     * @param update
     */
    public void setOwner(GeoFenceUpdate update) {
        this.update = update;
    }

    private MyBinder binder = new MyBinder();


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        geofenceService = new GeofenceService(this);
        lan = Double.valueOf(intent.getStringExtra(Constant.GEO_FENCE_LAT));
        lon = Double.valueOf(intent.getStringExtra(Constant.GEO_FENCE_LONG));
        redius = Float.valueOf(intent.getStringExtra(Constant.GEO_FENCE_REDIUS));
        createGeo();
        geoFence();
        return binder;
    }


    public void updateGeoFence(String lat, String lng, String updatedRedius, String name){
        removeAllFence();
        lan = Double.valueOf(lat);
        lon = Double.valueOf(lng);
        redius = Float.parseFloat(updatedRedius);
        isUpdate = true;
        createGeo();
    }

    public void createGeo() {
        dataTemp = new Data();
        dataTemp.longitude = lon;
        dataTemp.latitude = lan;
        dataTemp.radius = redius;


        try
        {
            int rand_int = DEFAULT_NOTIFICATION_ID;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                rand_int = ThreadLocalRandom.current().nextInt();
            }

            dataTemp.uniqueId = String.valueOf(rand_int);//genRandBytes(UNIQUE_ID_LENGTH);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            dataTemp.uniqueId = String.valueOf(DEFAULT_NOTIFICATION_ID);

        }


        dataTemp.conversions = Integer.parseInt(getString(R.string.default_conversion_value));
        dataTemp.validContinueTime = Long.parseLong(getString(R.string.default_valid_continue_time));
        dataTemp.dwellDelayTime = Integer.parseInt(getString(R.string.default_dwell_delay_time));
        dataTemp.notificationInterval = Integer.parseInt(getString(R.string.default_notification_interval));
        GeoFenceData.addGeofence(dataTemp);
        getGeoData();
    }

    public String genRandBytes(int len) {
        byte[] bytes = null;
        if (len > 0 && len < ONE_BYTE_LENGTH) {
            bytes = new byte[len];
            SecureRandom random = new SecureRandom();
            random.nextBytes(bytes);
            String string = new String(bytes, StandardCharsets.UTF_8);
            return string;
        }
        return "";
    }

    public void getGeoData() {
        ArrayList<Geofence> geofences = GeoFenceData.returnList();
        StringBuilder buf = new StringBuilder();
        String s = "";
        if (geofences.isEmpty()) {
            buf.append(getString(R.string.no_geofence_data));
        }
        for (int i = 0; i < geofences.size(); i++) {
            buf.append("Unique ID is " + geofences.get(i).getUniqueId() + "\n");
        }
        s = buf.toString();
        if (geofences.size() > 0) {
            requestGeoFenceWithNewIntent();
        }
    }

    PendingIntent pendingIntent;
    GeofenceRequest.Builder geofenceRequest;

    public void requestGeoFenceWithNewIntent() {
        if (GeoFenceData.returnList().isEmpty() == true) {
            Log.d(TAG, getString(R.string.no_new_request));
            return;
        }

        if (geofenceRequest == null) {
            geofenceRequest = new GeofenceRequest.Builder();
            geofenceRequest.createGeofenceList(GeoFenceData.returnList());
        }
        if (true) {
            geofenceRequest.setInitConversions(trigGer);
            Log.d(TAG, getString(R.string.trigger_lable) + trigGer);
        } else {
            geofenceRequest.setInitConversions(DEFAULT_TRIGGER_LENGTH);
            Log.d(TAG, getString(R.string.default_trigger_lable)+DEFAULT_TRIGGER_LENGTH);
        }

        if (pendingIntent == null) {
            pendingIntent = getPendingIntent();
            setList(pendingIntent, GeoFenceData.getRequestCode(), GeoFenceData.returnList());
        }
        try {
            geofenceService.createGeofenceList(geofenceRequest.build(), pendingIntent)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.i(TAG, getString(R.string.geofence_success_lable));
                                if (dataTemp != null) {
                                    update.update(getString(R.string.geofence_success_lable));
                                    update.geoLocationDetails(new LatLng(lan, lon), redius, isUpdate);
                                }
                            } else {
                                Log.d(TAG, getString(R.string.geofence_failed_lable) + task.getException());
                            }
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, getString(R.string.geofence_error_lable) + e);
        }
        GeoFenceData.createNewList();
    }



    public void setList(PendingIntent intent, int code, ArrayList<Geofence> geofences) {
        RequestList temp = new RequestList(intent, code, geofences);
        requestList.add(temp);
    }

    private void geoFence() {
        try {
            LocationSettingsRequest.Builder builders = new LocationSettingsRequest.Builder();
            builders.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builders.build();
            Task<LocationSettingsResponse> locationSettingsResponseTasks = mSettingsClient.checkLocationSettings(locationSettingsRequest);
            locationSettingsResponseTasks.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    Log.d(TAG, getString(R.string.location_setting_success));
                    mFusedLocationProviderClient
                            .requestLocationUpdates(mLocationRequest, mLocationCallbacks, Looper.getMainLooper())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, getString(R.string.geofence_success));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Log.d(TAG, getString(R.string.geofence_failure) + e);
                                }
                            });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.d(TAG, getString(R.string.location_setting_onfailure) + e);
                            int statusCodes = ((ApiException) e).getStatusCode();
                            switch (statusCodes) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, getString(R.string.geofence_exception) + e);
        }
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, GeoFenceBroadcastReceiver.class);
        intent.setAction(GeoFenceBroadcastReceiver.ACTION_PROCESS_LOCATION);
        Log.d(TAG, "new request");
        GeoFenceData.newRequest();
        return PendingIntent.getBroadcast(this, GeoFenceData.getRequestCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void removeAllFence() {
        GeoFenceData.createNewList();
        for (RequestList request :
                requestList) {
            geofenceService.deleteGeofenceList(request.intnet);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, getString(R.string.method_on_unbind));
        removeAllFence();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, getString(R.string.method_on_destroy));
        super.onDestroy();
    }
}
