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
package com.huawei.warnmychild.child.kotlin.GeoFence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huawei.hms.location.Geofence
import com.huawei.hms.location.GeofenceData
import com.huawei.warnmychild.child.kotlin.Constant
import com.huawei.warnmychild.child.R
import com.huawei.warnmychild.child.kotlin.ui.slideshow.SlideshowFragment
import java.util.*

/**
 * location broadcast receiver
 *
 * @author xxx888888
 * @since 2020-5-11
 */
class GeoFenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent != null) {
            val action = intent.action
            val sb = StringBuilder()
            val next = "\n"
            if (ACTION_PROCESS_LOCATION == action) {
                val geofenceData = GeofenceData.getDataFromIntent(intent)
                if (geofenceData != null) {
                    val errorCode = geofenceData.errorCode
                    val conversion = geofenceData.conversion
                    val list = geofenceData.convertingGeofenceList as ArrayList<Geofence>
                    val myLocation = geofenceData.convertingLocation
                    val status = geofenceData.isSuccess
                    sb.append(Constant.ERROR_CODE_LABLE + errorCode + next)
                    sb.append(Constant.CONVERSION_LABLE + conversion + next)
                    if (list != null) {
                        for (i in list.indices) {
                            sb.append(Constant.GEOFENCE_ID_LABLE + list[i].uniqueId + next)
                        }
                    }
                    if (myLocation != null) {
                        sb.append(Constant.LOCATION_LABLE + myLocation.longitude + " " + myLocation.latitude + next)
                    }
                    sb.append(Constant.SUCCESSFULL_STATUS + status)

                    // Send conversion data to main activity.
                    SlideshowFragment.Companion.sendData(conversion)
                    when (conversion) {
                        1 -> NotificatioHelper(context, context.resources.getString(R.string.geofence),
                                context.resources.getString(R.string.geofence_in))
                        4 -> NotificatioHelper(context, context.resources.getString(R.string.geofence),
                                context.resources.getString(R.string.geofence_stay))
                        2 -> NotificatioHelper(context, context.resources.getString(R.string.geofence),
                                context.resources.getString(R.string.geofence_out))
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_PROCESS_LOCATION = "com.huawei.hmssample.geofence.GeoFenceBroadcastReceiver.ACTION_PROCESS_LOCATION"
        private const val TAG = "TAG"
    }
}