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
package com.huawei.parentapp.java.ui.slideshow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.parentapp.java.ExceptionLogger;
import com.huawei.parentapp.java.NotificationAdapter;
import com.huawei.parentapp.R;
import com.huawei.parentapp.java.NotificationDetails;
import com.huawei.parentapp.java.ObjectTypeInfoHelper;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class NotificationFragment extends Fragment {

    private View currentview;
    private RecyclerView recyclerView;
    private TextView txt_norecords;
    private CloudDBZone mCloudDBZone;
    private NotificationAdapter notificationAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_notification, container, false);
        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        currentview = view;
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerview);
        //recyclerView.setItemViewCacheSize();
        txt_norecords = (TextView) view.findViewById(R.id.text_norecordsfound);
        txt_norecords.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        establishconnection();

    }

    public void establishconnection()
    {
        AGConnectCloudDB mCloudDB = AGConnectCloudDB.getInstance();
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
            CloudDBZoneConfig mConfig = new CloudDBZoneConfig(getString(R.string.CLOUD_DB_NAME),
                    CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                    CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
            mConfig.setPersistenceEnabled(true);
            Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
            openDBZoneTask.addOnSuccessListener(new OnSuccessListener<CloudDBZone>() {
                @Override
                public void onSuccess(CloudDBZone cloudDBZone) {
                    Log.d(getString(R.string.NOTIFICATION_FRAGMENT_TAG), getString(R.string.OPEN_CLOUD_DB_SUCCESS_MSG));
                    mCloudDBZone = cloudDBZone;

                    Log.d(getString(R.string.NOTIFICATION_TAG),getString(R.string.OPEN_DB_SUCCESS));

                    queryNotificationDetails();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.d(getString(R.string.NOTIFICATION_TAG), getString(R.string.DB_OPEN_FAILED_REASON) + e);

                    Toast.makeText(getActivity(),getString(R.string.DB_OPEN_FAILED_REASON) + e,Toast.LENGTH_SHORT).show();

                }
            });

        } catch (AGConnectCloudDBException e) {
            ExceptionLogger.printExceptionDetails(getString(R.string.FRAGMENT_NAME_TAG),e);
        }
    }


    public void queryNotificationDetails()
    {
        if (mCloudDBZone == null) {
            Log.d(TAG, getString(R.string.TRY_REOPEN_MSG));
            return;
        }

        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();

        CloudDBZoneQuery<NotificationDetails> query = CloudDBZoneQuery.where(NotificationDetails.class).
                equalTo(getString(R.string.PARENT_ID_COLUMN_NAME),user.getUid()).orderByAsc(getString(R.string.DATE_TIME_COLUMN_NAME));

        Task<CloudDBZoneSnapshot<NotificationDetails>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<NotificationDetails>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<NotificationDetails> snapshot) {

                CloudDBZoneObjectList<NotificationDetails> bookInfoCursor = snapshot.getSnapshotObjects();
                List<NotificationDetails> notificationDetailslist = new ArrayList<>();
                try {
                    while (bookInfoCursor.hasNext()) {
                        NotificationDetails NotificationDetails = bookInfoCursor.next();
                        notificationDetailslist.add(NotificationDetails);
                    }

                    if(notificationDetailslist.size() > 0)
                    {
                        recyclerView.setVisibility(View.VISIBLE);
                        txt_norecords.setVisibility(View.GONE);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                        recyclerView.setLayoutManager(layoutManager);
                        notificationAdapter = new NotificationAdapter(getActivity(),notificationDetailslist);
                        recyclerView.setAdapter(notificationAdapter);

                    }
                    else
                    {
                        recyclerView.setVisibility(View.GONE);
                        txt_norecords.setVisibility(View.VISIBLE);
                    }


                } catch (AGConnectCloudDBException e) {
                    Log.d(TAG, getString(R.string.QUERY_RESULT_LABLE) + e);
                }
                snapshot.release();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, getString(R.string.TRY_REOPEN_MSG));
            }
        });
    }



}