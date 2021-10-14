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

import android.util.Log
import com.huawei.hms.location.Geofence
import com.huawei.warnmychild.child.kotlin.Constant
import java.util.*

object GeoFenceData {
    var requestCode = 0
        private set
    var geofences = ArrayList<Geofence?>()
    var geoBuild = Geofence.Builder()
    const val TAG = "GeoFenceActivity"
    fun addGeofence(data: Data) {
        if (checkStyle(geofences, data.uniqueId) == false) {
            Log.d(TAG, Constant.NOT_UNIQUE_ID)
            Log.d(TAG, Constant.ADD_GEOFENCE_FAILED)
            return
        }
        geoBuild.setRoundArea(data.latitude, data.longitude, data.radius)
        geoBuild.setUniqueId(data.uniqueId)
        geoBuild.setConversions(data.conversions)
        geoBuild.setValidContinueTime(data.validContinueTime)
        geoBuild.setDwellDelayTime(data.dwellDelayTime)
        geoBuild.setNotificationInterval(data.notificationInterval)
        geofences.add(geoBuild.build())
    }

    fun createNewList() {
        geofences = ArrayList()
    }

    fun checkStyle(geofences: ArrayList<Geofence?>, ID: String?): Boolean {
        for (i in geofences.indices) {
            if (geofences[i]!!.uniqueId == ID) {
                return false
            }
        }
        return true
    }

    fun returnList(): ArrayList<Geofence?> {
        return geofences
    }

    fun show() {
        if (geofences.isEmpty()) {
            Log.d(TAG, Constant.NO_GEOFENCE_DATA)
        }
    }

    fun newRequest() {
        requestCode++
    }

}