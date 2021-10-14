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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.warnmychild.child.java.DBHelper.ChildInfo;
import com.huawei.warnmychild.child.java.DBHelper.CustomSharedPreference;
import com.huawei.warnmychild.child.R;
import com.huawei.warnmychild.child.java.DBHelper.GeofenceDetails;
import com.huawei.warnmychild.child.java.DBHelper.NotificationDetails;
import com.huawei.warnmychild.child.java.DBHelper.ObjectTypeInfoHelper;
import com.huawei.warnmychild.child.java.ui.slideshow.SlideshowFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private static final String TAG = "TAG";
    public static final int UID_LENGTH = 5;
    public static final int DEFAULT_NOTIFICATION_ID = 114253;
    private static SlideshowFragment slideshowFragment;
    private TextView tv_user;

    private static AGConnectCloudDB mCloudDB;
    private static CloudDBZoneConfig mConfig;
    private static CloudDBZone mCloudDBZone;
    public static AGConnectUser user;
    private static CustomSharedPreference customSharedPreference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homejava);
        Toolbar toolbar = findViewById(R.id.toolbarjava);
        setSupportActionBar(toolbar);

        AGConnectCloudDB.initialize(HomeActivity.this);
        mCloudDB = AGConnectCloudDB.getInstance();


        DrawerLayout drawer = findViewById(R.id.drawer_layoutjava);
        NavigationView navigationView = findViewById(R.id.nav_viewjava);
        View headerView = navigationView.getHeaderView(0);
        tv_user = (TextView) headerView.findViewById(R.id.tv_userjava);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_slideshowjava)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragmentjava);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        customSharedPreference = new CustomSharedPreference();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Fragment navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if (fragment != null) {
            try {
                slideshowFragment = (SlideshowFragment) fragment;
                Log.d(getString(R.string.home_activity_tag), getString(R.string.fragment_instance_success));
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.issue_in_scanning), Toast.LENGTH_SHORT).show();
                ExceptionLogger exceptionLogger = ExceptionLogger.getInstance();
                exceptionLogger.printExceptionDetails(HomeActivity.this.getLocalClassName(),e);
            }
        } else {
            Toast.makeText(this, getString(R.string.null_fragment), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Fragment navHostFragment = getSupportFragmentManager().getPrimaryNavigationFragment();
        Fragment fragment = navHostFragment.getChildFragmentManager().getFragments().get(0);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        if (requestCode == Constant.REQUEST_CODE) {
            Object obj = data.getParcelableExtra(ScanUtil.RESULT);
            HmsScan hmsScan = (HmsScan) obj;
            if (obj != null) {
                if (!TextUtils.isEmpty(((HmsScan) obj).getOriginalValue())) {
                    Toast.makeText(this, hmsScan.getOriginalValue(), Toast.LENGTH_SHORT).show();


                    if (fragment != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(hmsScan.getOriginalValue());
                            slideshowFragment = (SlideshowFragment) fragment;
                        } catch (JSONException e) {
                            Toast.makeText(this, getString(R.string.issue_in_scanning), Toast.LENGTH_SHORT).show();
                            ExceptionLogger exceptionLogger = ExceptionLogger.getInstance();
                            exceptionLogger.printExceptionDetails(HomeActivity.this.getLocalClassName(),e);
                        }
                    } else {
                        Toast.makeText(this, getString(R.string.fragment_null), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getString(R.string.scan_result_not_found), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        AGConnectAuth.getInstance().signOut();
        customSharedPreference.removeValue(getApplicationContext());
        mCloudDBZone = null;
        Intent loginscreen = new Intent(this, LoginActivity.class);
        loginscreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginscreen);
        this.finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragmentjava);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void setAGC_DB() {
        user = AGConnectAuth.getInstance().getCurrentUser();

        if(user != null){
            tv_user.setText(user.getDisplayName());
        }
        // AGConnect Cloud DB...

       mCloudDB = AGConnectCloudDB.getInstance();
        if(mCloudDB != null)
        {
            try {

                mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
            } catch (Exception e) {

                Toast.makeText(this,"Clouddb connection issue",Toast.LENGTH_LONG).show();
                ExceptionLogger exceptionLogger = ExceptionLogger.getInstance();
                exceptionLogger.printExceptionDetails(HomeActivity.this.getLocalClassName(),e);
            }
        }


        mConfig = new CloudDBZoneConfig(getString(R.string.test_cloud_db_name),
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(new OnSuccessListener<CloudDBZone>() {
            @Override
            public void onSuccess(CloudDBZone cloudDBZone) {
                Log.d(TAG, getString(R.string.open_cloud_db_success));
                mCloudDBZone = cloudDBZone;

                if (mCloudDBZone == null) {
                    slideshowFragment.setConnection(false);
                    return;
                } else {
                    slideshowFragment.setConnection(true);
                    // Add subscription after opening cloudDBZone success
                    addChildInfoSubscription();
                    addGeoFenceSubscription();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, getString(R.string.open_cloud_db_failed_for) + e);
            }
        });
    }

    public void addChildInfoSubscription() {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db));
            return;
        }
        try {
            CloudDBZoneQuery<ChildInfo> snapshotQuery = CloudDBZoneQuery.where(ChildInfo.class)
                    .equalTo(getString(R.string.child_id_column_name), user.getUid());

            mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, ChildInfoSnapshotListener);

            Log.d(TAG, getString(R.string.subscribe_snapshot_result));
        } catch (AGConnectCloudDBException e) {
            Log.d(TAG, getString(R.string.subscribe_snapshot_lable) + e);
        }
    }


    private void addGeoFenceSubscription() {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.try_reopen_cloud_db));
            return;
        }
        try {
            CloudDBZoneQuery<GeofenceDetails> snapshotQuery = CloudDBZoneQuery.where(GeofenceDetails.class)
                    .equalTo(getString(R.string.child_id_column_name), user.getUid());


            mCloudDBZone.subscribeSnapshot(snapshotQuery,
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

                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (bookInfo.getChildID().equals(user.getUid())) {
                                    Log.d(TAG, getString(R.string.on_snapshot_ui_1));
                                    slideshowFragment.enableMapview(bookInfo.getParentID());
                                    Log.d(TAG, getString(R.string.on_snapshot_ui_2));
                                }
                            }
                        });
                        break;
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
                Log.d(TAG, getString(R.string.on_snapshot) + e);
                return;
            }
            CloudDBZoneObjectList<GeofenceDetails> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            try {
                if (snapshotObjects != null) {
                    if (snapshotObjects.size() > 0) {


                        List<GeofenceDetails> notificationDetailslist = new ArrayList<>();
                        try {
                            while (snapshotObjects.hasNext()) {
                                GeofenceDetails NotificationDetails = snapshotObjects.next();
                                notificationDetailslist.add(NotificationDetails);
                            }
                        } catch (Exception e1) {
                            e.printStackTrace();
                        }


                        GeofenceDetails geoFenceInfo = snapshotObjects.get(snapshotObjects.size() - 1);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                final Toast toast = Toast.makeText(getApplicationContext(), "Geo Fence Data Received.", Toast.LENGTH_SHORT);
                                toast.show();
                                if (geoFenceInfo.getChildID().equals(user.getUid())) {
                                    slideshowFragment.startGeoFence(geoFenceInfo);
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

    public static void upsertNotificationInfos(String msg) {
        if (mCloudDBZone == null) {
            Log.d(TAG, Constant.REOPEN_CLOUD_DB);
            return;
        }

        NotificationDetails mNotificationDetails = new NotificationDetails();

        if (user != null && user.getUid().length() > UID_LENGTH) {
            mNotificationDetails.setChildID(user.getUid());
        } else {
            mNotificationDetails.setChildID(Constant.NO_USER_FOUND);
        }


        mNotificationDetails.setDateTime(Calendar.getInstance().getTime());
        int rand_int = DEFAULT_NOTIFICATION_ID;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            rand_int = ThreadLocalRandom.current().nextInt();
        }
        mNotificationDetails.setID(String.valueOf(rand_int));
        mNotificationDetails.setIsValid(true);
        mNotificationDetails.setMessage(msg);
        mNotificationDetails.setParentID(customSharedPreference.getPrefsParentConnectionStatus(slideshowFragment.getContext()));

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(mNotificationDetails);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.d(TAG, Constant.NOTIFICATION_UPSERT_SUCCESS + cloudDBZoneResult + " records");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, Constant.NOTIFICATION_INSERT_FAIL + e + Constant.RECORDS_LABLE);
            }
        });
    }


}