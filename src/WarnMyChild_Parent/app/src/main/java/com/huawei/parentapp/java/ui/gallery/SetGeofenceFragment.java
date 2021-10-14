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
package com.huawei.parentapp.java.ui.gallery;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.MapsInitializer;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.Circle;
import com.huawei.hms.maps.model.CircleOptions;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.parentapp.java.ExceptionLogger;
import com.huawei.parentapp.R;
import com.huawei.parentapp.java.GeofenceDetails;
import com.huawei.parentapp.java.ObjectTypeInfoHelper;

import java.util.List;

public class SetGeofenceFragment extends Fragment implements OnMapReadyCallback, HuaweiMap.OnMapClickListener, HuaweiMap.OnMapLongClickListener {


    private MapView mMapView;
    private HuaweiMap huaweiMap;
    private SeekBar seekBar;
    private Circle mCircle =null;
    CloudDBZone mCloudDBZone;
    private static int currentradius = 100;
    private static final int defaultRadius = 200;
    private static final int defaultProgress = 200;
    private static final int defaultMaxValue = 1000;
    private static final int requestCode = 1;
    private static final int maxValue = 1000;
    private static final int progressValue = 200;
    private static final int intervalSeconds = 5000;
    private static final float defaultZoom = 16.0f;
    private static final String defaultRadiusText = "Raduis:200";

    public static final String TAG = "LocationUpdatesCallback";
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingsClient;


    LatLng currentlatlon;

    TextView txtprogress;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_geofence, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtprogress = (TextView)view.findViewById(R.id.txt_progress);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "sdk >= 23 M");
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(getActivity(), strings, requestCode);
            }
        }

        setLocationInit();

        MapsInitializer.setApiKey(getResources().getString(R.string.API_KEY));

        mMapView = view.findViewById(R.id.mapview_mapviewdemo);
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {

            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey");
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        seekBar = (SeekBar)view.findViewById(R.id.seekBar);

        seekBar.setMax(maxValue);
        seekBar.setProgress(progressValue);
        txtprogress.setText(defaultRadiusText);
        currentradius = 200;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


                txtprogress.setText("Raduis:"+progress);

                if(mCircle!=null)
                {
                    mCircle.setRadius(progress);
                    currentradius = progress;
                }

            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });

        Button btn_submit = (Button)view.findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                upsertConfigurationDetails();
            }
        });


    }


    public void setLocationInit()
    {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());
        mLocationRequest = new LocationRequest();
        // Sets the interval for location update (unit: Millisecond)
        mLocationRequest.setInterval(intervalSeconds);
        // Sets the priority
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (null == mLocationCallback) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null) {
                        List<Location> locations = locationResult.getLocations();
                        if (!locations.isEmpty()) {
                            for (Location location : locations) {
                                Log.d(TAG,
                                        "onLocationResult location[Longitude,Latitude,Accuracy]:" + location.getLongitude()
                                                + "," + location.getLatitude() + "," + location.getAccuracy());
                            }
                        }
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    if (locationAvailability != null) {
                        boolean flag = locationAvailability.isLocationAvailable();
                        Log.d(TAG, "onLocationAvailability isLocationAvailable:" + flag);
                    }
                }
            };
        }
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMapClick(LatLng latLng) {

        Log.d("onCLICKTRIGGERED","");
        Toast.makeText(getActivity(),latLng.toString(),Toast.LENGTH_SHORT).show();

        if (null == huaweiMap) {
            return;
        }
        huaweiMap.clear();



        if (null != mCircle) {
            mCircle.remove();
            mCircle = null;
        }

        mCircle = huaweiMap.addCircle(new CircleOptions()
                .center(new LatLng(latLng.latitude, latLng.longitude))
                .radius(defaultRadius).strokeWidth(1)
                .fillColor(0x220000FF));

        huaweiMap.addMarker(new MarkerOptions()
                .position(new LatLng(latLng.latitude, latLng.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("marker",120,120))));

        seekBar.setMax(defaultMaxValue);
        seekBar.setProgress(defaultProgress);
        currentradius = 200;
        txtprogress.setText(defaultRadiusText);
        currentlatlon = latLng;
    }

    @Override
    public void onMapLongClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(HuaweiMap huaweimap) {

        huaweiMap = huaweimap;
        huaweiMap.setMyLocationEnabled(true);// Enable the my-location overlay.
        huaweiMap.getUiSettings().setMyLocationButtonEnabled(true);// Enable the my-location icon.
        huaweiMap.setOnMapClickListener(this);
        Task<Location> task = mFusedLocationProviderClient.getLastLocation()
                // Define callback for success in obtaining the last known location.
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Toast.makeText(getActivity(),"Location not available",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else
                        {
                            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),
                                    defaultZoom);
                            huaweiMap.moveCamera(update);

                            if (null != mCircle) {
                                mCircle.remove();
                                mCircle = null;
                            }

                            mCircle = huaweiMap.addCircle(new CircleOptions()
                                    .center(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .radius(defaultRadius).strokeWidth(1)
                                    .fillColor(0x220000FF));

                            huaweiMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("marker",120,120))));


                            seekBar.setMax(defaultMaxValue);
                            seekBar.setProgress(defaultProgress);
                            txtprogress.setText(defaultRadiusText);
                            currentlatlon = new LatLng(location.getLatitude(), location.getLongitude());
                        }

                }
                })
                // Define callback for failure in obtaining the last known location.
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // ...
                    }
                });

        establishconnection();
    }

    public Bitmap resizeMapIcons(String iconName, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

    public void establishconnection()
    {
        AGConnectCloudDB.initialize(getActivity());

        AGConnectCloudDB mCloudDB = AGConnectCloudDB.getInstance();
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());


            CloudDBZoneConfig mConfig = new CloudDBZoneConfig("test1",
                    CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                    CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
            mConfig.setPersistenceEnabled(true);
            Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
            openDBZoneTask.addOnSuccessListener(new OnSuccessListener<CloudDBZone>() {
                @Override
                public void onSuccess(CloudDBZone cloudDBZone) {
                    Log.d("TAG", "open clouddbzone success");
                    mCloudDBZone = cloudDBZone;
                    // Add subscription after opening cloudDBZone success
                    Toast.makeText(getActivity(),"openclouddbzonesuccess",Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.d("TAG", "open clouddbzone failed for " + e);

                    Toast.makeText(getActivity(),"open clouddbzone failed for " + e,Toast.LENGTH_SHORT).show();
                }
            });





        } catch (AGConnectCloudDBException e) {
            ExceptionLogger.printExceptionDetails("SetGeofenceFragment ",e);
        }
    }

    public void upsertConfigurationDetails() {
        if (mCloudDBZone == null) {
            Log.d("TAG", "CloudDBZone is null, try re-open it");
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading");
        progressDialog.show();

        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();

        SharedPreferences sharedpreferences = getActivity().getSharedPreferences("ConnectionInfo", Context.MODE_PRIVATE);

        GeofenceDetails geofenceDetails = new GeofenceDetails();
        geofenceDetails.setChildID(sharedpreferences.getString("ChildID",""));
        geofenceDetails.setLat(String.valueOf(currentlatlon.latitude));
        geofenceDetails.setLon(String.valueOf(currentlatlon.longitude));
        geofenceDetails.setAssignedby(user.getUid());
        geofenceDetails.setIsvalid(true);
        geofenceDetails.setRadius(currentradius+"");
        geofenceDetails.setGeofenceName("Park");


        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(geofenceDetails);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {

                progressDialog.dismiss();

                Log.d("TAG", "upsert_sucess " + cloudDBZoneResult + " records");

                Toast.makeText(getActivity(),"upsert_sucess " + cloudDBZoneResult + " records",Toast.LENGTH_SHORT).show();


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {

                progressDialog.dismiss();

                Log.d("TAG", "insert_failed " + e + " records");

                Toast.makeText(getActivity(),"insert_failed " + e + " records",Toast.LENGTH_SHORT).show();


            }
        });
    }
}