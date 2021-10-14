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

package com.huawei.warnmychild.child.java.GeoFence;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import com.huawei.hms.location.Geofence;
import com.huawei.hms.location.GeofenceData;
import com.huawei.warnmychild.child.java.Constant;
import com.huawei.warnmychild.child.R;
import com.huawei.warnmychild.child.java.ui.slideshow.SlideshowFragment;

import java.util.ArrayList;

/**
 * location broadcast receiver
 *
 * @author xxx888888
 * @since 2020-5-11
 */
public class GeoFenceBroadcastReceiver extends BroadcastReceiver {
    public static final String ACTION_PROCESS_LOCATION = "com.huawei.hmssample.geofence.GeoFenceBroadcastReceiver.ACTION_PROCESS_LOCATION";
    private static final String TAG = "TAG";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            StringBuilder sb = new StringBuilder();
            String next = "\n";
            if (ACTION_PROCESS_LOCATION.equals(action)) {
                GeofenceData geofenceData = GeofenceData.getDataFromIntent(intent);
                if (geofenceData != null) {
                    int errorCode = geofenceData.getErrorCode();
                    int conversion = geofenceData.getConversion();
                    ArrayList<Geofence> list = (ArrayList<Geofence>) geofenceData.getConvertingGeofenceList();
                    Location myLocation = geofenceData.getConvertingLocation();
                    boolean status = geofenceData.isSuccess();
                    sb.append(Constant.ERROR_CODE_LABLE + errorCode + next);
                    sb.append(Constant.CONVERSION_LABLE + conversion + next);
                    if (list != null) {
                        for (int i = 0; i < list.size(); i++) {
                            sb.append(Constant.GEOFENCE_ID_LABLE + list.get(i).getUniqueId() + next);
                        }
                    }
                    if (myLocation != null) {
                        sb.append(Constant.LOCATION_LABLE + myLocation.getLongitude() + " " + myLocation.getLatitude() + next);
                    }
                    sb.append(Constant.SUCCESSFULL_STATUS + status);

                    // Send conversion data to main activity.
                    SlideshowFragment.sendData(conversion);
                    switch (conversion){
                        case 1:// Enter the geofence
                            new NotificatioHelper(context,context.getResources().getString(R.string.geofence),
                                    context.getResources().getString(R.string.geofence_in));
                            break;
                        case 4:// Stay in geofence
                            new NotificatioHelper(context,context.getResources().getString(R.string.geofence),
                                    context.getResources().getString(R.string.geofence_stay));
                            break;
                        case 2:// Go out of geofence
                            new NotificatioHelper(context,context.getResources().getString(R.string.geofence),
                                    context.getResources().getString(R.string.geofence_out));
                            break;

                    }
                }
            }
        }
    }
}
