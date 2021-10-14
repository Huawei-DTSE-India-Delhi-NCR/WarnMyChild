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
package com.huawei.parentapp.java.ui.home;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

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
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.MarkerOptions;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.huawei.parentapp.java.ChildInfo;
import com.huawei.parentapp.java.ExceptionLogger;
import com.huawei.parentapp.java.LoginActivity;
import com.huawei.parentapp.R;
import com.huawei.parentapp.java.NotificationDetails;
import com.huawei.parentapp.java.ObjectTypeInfoHelper;
import com.huawei.parentapp.java.ParentInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private static final int INTERVAL_SECONDS = 5000;
    private static final int REQUEST_CODE = 999;
    private CloudDBZone mCloudDBZone;
    private View currentview;
    private ListenerHandler mRegister;
    private SharedPreferences sharedpreferences;
    private ImageView startscanbtn;
    private CardView cardview;
    private LinearLayout parentview;

    private MapView mMapView;
    private HuaweiMap huaweiMap;

    public static final String TAG = "LocationUpdatesCallback";
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private SettingsClient mSettingsClient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentview= view;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "sdk >= 23 M");
            if (ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(getActivity(), strings, 1);
            }
        }

        AGConnectCloudDB.initialize(getActivity());
        onLoadResources();
        addOnClickListener();
        establishconnection();
        setLocationInit();

        MapsInitializer.setApiKey(getString(R.string.API_KEY));
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(getString(R.string.map_view_bundle_key));
        }
        mMapView.onCreate(mapViewBundle);
        mMapView.getMapAsync(this);

        sharedpreferences = getActivity().getSharedPreferences(getString(R.string.connection_shp_name), Context.MODE_PRIVATE);

    }

    private void onLoadResources()
    {
        mMapView = currentview.findViewById(R.id.mapview);
        startscanbtn = (ImageView)currentview.findViewById(R.id.btn_scan);
        cardview = (CardView) currentview.findViewById(R.id.cardview);
        parentview = (LinearLayout) currentview.findViewById(R.id.lay);

        cardview.setVisibility(View.GONE);
        parentview.setVisibility(View.GONE);
        mMapView.setVisibility(View.GONE);
    }

    private void addOnClickListener()
    {

        startscanbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // QRCODE_SCAN_TYPE and DATAMATRIX_SCAN_TYPE are set for the barcode format, indicating that Scan Kit will support only QR Code and Data Matrix.
                HmsScanAnalyzerOptions options = new HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE , HmsScan.DATAMATRIX_SCAN_TYPE).create();
                ScanUtil.startScan(getActivity(), REQUEST_CODE, options);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();


    }



    public void setLocationInit()
    {
        //setlocationrequest
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mSettingsClient = LocationServices.getSettingsClient(getActivity());
        mLocationRequest = new LocationRequest();
        // Sets the interval for location update (unit: Millisecond)
        mLocationRequest.setInterval(INTERVAL_SECONDS);
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
                                        getString(R.string.location_result_lable) + location.getLongitude()
                                                + "," + location.getLatitude() + "," + location.getAccuracy());
                            }
                        }
                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    if (locationAvailability != null) {
                        boolean flag = locationAvailability.isLocationAvailable();
                        Log.d(TAG, getString(R.string.location_availablity_lable) + flag);
                    }
                }
            };
        }
    }

    public void updateNotificationFlag(NotificationDetails notificationDetails)
    {
        if (mCloudDBZone == null) {
            Log.d("TAG", "CloudDBZone is null, try re-open it");
            return;
        }

        notificationDetails.setIsValid(false);

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(notificationDetails);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.d("TAG", "upsert_sucess_parent " + cloudDBZoneResult + " records");
                Toast.makeText(getActivity(), "upsert_sucess_notification " + cloudDBZoneResult + " records", Toast.LENGTH_SHORT).show();
            }
        });
        upsertTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                //  mUiCallBack.updateUiOnError("Insert book info failed");
                Log.d("TAG", "insert_failed " + e.getLocalizedMessage() + " records");
                Toast.makeText(getActivity(), "insert_failed " + e.getLocalizedMessage() + " records", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void queryParentDetails()
    {
        if (mCloudDBZone == null) {
            Log.d(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Loading");
        progressDialog.show();

        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();

        CloudDBZoneQuery<ParentInfo> query = CloudDBZoneQuery.where(ParentInfo.class).equalTo("ParentID",user.getUid());

        Task<CloudDBZoneSnapshot<ParentInfo>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<ParentInfo>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<ParentInfo> snapshot) {

                progressDialog.dismiss();

                CloudDBZoneObjectList<ParentInfo> bookInfoCursor = snapshot.getSnapshotObjects();
                List<ParentInfo> parentInfoslist = new ArrayList<>();
                try {
                    while (bookInfoCursor.hasNext()) {
                        ParentInfo parentInfo = bookInfoCursor.next();
                        parentInfoslist.add(parentInfo);
                    }
                    if(parentInfoslist.size() > 0)
                    {
                        Log.d(TAG, "parentInfoslistsize__: " + parentInfoslist.size());
                        cardview.setVisibility(View.GONE);
                        parentview.setVisibility(View.GONE);
                        mMapView.setVisibility(View.VISIBLE);

                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean("IsConnected", true);
                        editor.commit();

                    }
                    else
                    {
                        Log.d(TAG, "parentInfoslist__else__: " + parentInfoslist.size());
                        cardview.setVisibility(View.VISIBLE);
                        parentview.setVisibility(View.VISIBLE);
                        mMapView.setVisibility(View.GONE);
                    }


                } catch (AGConnectCloudDBException e) {
                    Log.d(TAG, "processQueryResult: " + e);
                }
                snapshot.release();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {

                progressDialog.dismiss();
                Log.d(TAG, "CloudDBZone is null, try re-open it");
                showToastShort(e.getMessage());

            }
        });
    }

    public void addNotificationSubscription() {
        if (mCloudDBZone == null) {
            Log.d(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        try {
            AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();

            CloudDBZoneQuery<NotificationDetails> snapshotQuery = CloudDBZoneQuery.where(NotificationDetails.class)
                    .equalTo("ParentID", user.getUid()).equalTo("isValid",true);
            mRegister = mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener);
        } catch (AGConnectCloudDBException e) {
            Log.d(TAG, "subscribeSnapshot: " + e);
        }
    }


    private OnSnapshotListener<NotificationDetails> mSnapshotListener = new OnSnapshotListener<NotificationDetails>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<NotificationDetails> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.d(TAG, "onSnapshot: " + e);
                return;
            }
            CloudDBZoneObjectList<NotificationDetails> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            List<NotificationDetails> notificationlist = new ArrayList<NotificationDetails>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        NotificationDetails notificationDetails = snapshotObjects.next();
                        notificationlist.add(notificationDetails);
                    }

                    if(notificationlist.size()>0)
                    {
                        Log.d("onsnapsjotlistener_____",notificationlist.get(0).getMessage());
                        shownotification(notificationlist.get(0).getMessage());
                        updateNotificationFlag(notificationlist.get(0));
                    }

                }
            } catch (Exception snapshotException) {
                Log.d(TAG, "onSnapshot:(getObject) " + snapshotException);
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };


    private void shownotification(String message)
    {
        NotificationManager mNotificationManager;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getActivity(), "notify_001");
        Intent ii = new Intent(getActivity(), LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("Warning");
        bigText.setBigContentTitle("Message from Child App");
        bigText.setSummaryText(message);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Warning");
        mBuilder.setContentText(message);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        mNotificationManager =
                (NotificationManager)getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

// === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "4546";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();


    }



    private void showToastShort(String content)
    {
        Toast.makeText(getActivity(),content,Toast.LENGTH_SHORT).show();
    }

    private void showToastLong(String content)
    {
        Toast.makeText(getActivity(),content,Toast.LENGTH_LONG).show();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(getActivity(),"Inside fragment on result",Toast.LENGTH_SHORT);

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                // Display the decoding result.
                Toast.makeText(getActivity(),obj.showResult,Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setScanResult(JSONObject jsonObject) throws JSONException {
        upsertParentDetails(jsonObject);
    }

    public void establishconnection()
    {

        AGConnectCloudDB.initialize(getActivity());
        AGConnectCloudDB mCloudDB = AGConnectCloudDB.getInstance();
        if(mCloudDB != null) {
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
                        addNotificationSubscription();
                        Toast.makeText(getActivity(), "openclouddbzonesuccess", Toast.LENGTH_SHORT).show();

                        if (sharedpreferences.getBoolean("IsConnected", false)) {
                            cardview.setVisibility(View.GONE);
                            parentview.setVisibility(View.GONE);
                            mMapView.setVisibility(View.VISIBLE);
                        } else {
                            queryParentDetails();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.d("TAG", "open clouddbzone failed for " + e);

                        Toast.makeText(getActivity(), "open clouddbzone failed for " + e, Toast.LENGTH_SHORT).show();

                    }
                });

            } catch (AGConnectCloudDBException e) {
                ExceptionLogger.printExceptionDetails("HomeFragment ", e);
            } catch (Exception e) {
                ExceptionLogger.printExceptionDetails("HomeFragmentJava__ ", e);
                e.printStackTrace();
            }
        }
    }

    public void upsertParentDetails(JSONObject jsonObject) throws JSONException {
        if (mCloudDBZone == null) {
            Log.d("TAG", "CloudDBZone is null, try re-open it");
            return;
        }

        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();

        ParentInfo parentInfo = new ParentInfo();
        parentInfo.setParentID(user.getUid());
        parentInfo.setParentName(user.getDisplayName());
        parentInfo.setChildIDs(jsonObject.getString("ChildID"));
        parentInfo.setEmailID(user.getEmail());

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(parentInfo);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.d("TAG", "upsert_sucess_parent " + " records");

                Toast.makeText(getActivity(), "upsert_sucess_parent " + cloudDBZoneResult + " records", Toast.LENGTH_SHORT).show();

                try {
                    upsertChildDetails(jsonObject);
                } catch (JSONException e) {
                    ExceptionLogger.printExceptionDetails("HomeFragment ",e);
                }


            }
        });
        upsertTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                //  mUiCallBack.updateUiOnError("Insert book info failed");
                Log.d("TAG", "insert_failed " + e.getLocalizedMessage() + " records");

                Toast.makeText(getActivity(), "insert_failed " + e.getLocalizedMessage() + " records", Toast.LENGTH_SHORT).show();


            }
        });
    }

    public void upsertChildDetails(JSONObject jsonObject) throws JSONException {
        if (mCloudDBZone == null) {
            Log.d("TAG", "CloudDBZone is null, try re-open it");
            return;
        }

        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();

        ChildInfo childInfo = new ChildInfo();
        childInfo.setChildID(jsonObject.getString("ChildID"));
        childInfo.setChildName(jsonObject.getString("ChildName"));
        childInfo.setChildEmail(jsonObject.getString("EmailID"));
        childInfo.setParentID(user.getUid());

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(childInfo);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.d("TAG", "upsert_sucess_child "  + " records");


                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean("IsConnected", true);
                editor.commit();

                SharedPreferences.Editor editor1 = sharedpreferences.edit();
                try {
                    editor1.putString("ChildID", jsonObject.getString("ChildID"));
                } catch (JSONException e) {
                    ExceptionLogger.printExceptionDetails("HomeFragment ",e);
                }
                editor1.commit();



                Navigation.findNavController(currentview).navigate(R.id.nav_geofence);

                Toast.makeText(getActivity(),"Connected with Child app" + cloudDBZoneResult + " records",Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                //  mUiCallBack.updateUiOnError("Insert book info failed");
                Log.d("TAG", "insert_failed " + e + " records");

                Toast.makeText(getActivity(),"insert_failed " + e + " records",Toast.LENGTH_SHORT).show();


            }
        });
    }

    @Override
    public void onMapReady(HuaweiMap huaweimap) {

        huaweiMap = huaweimap;
        huaweiMap.setMyLocationEnabled(true);// Enable the my-location overlay.
        huaweiMap.getUiSettings().setMyLocationButtonEnabled(true);// Enable the my-location icon.

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
                                    16.0f);
                            huaweiMap.moveCamera(update);

                            huaweiMap.addMarker(new MarkerOptions()
                                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons("marker",120,120))));

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

    }

    public Bitmap resizeMapIcons(String iconName,int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(),getResources().getIdentifier(iconName, "drawable", getActivity().getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
}




