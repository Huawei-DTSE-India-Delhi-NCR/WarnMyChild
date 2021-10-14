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
package com.huawei.warnmychild.child.kotlin.GeoFence

import android.app.PendingIntent
import com.huawei.hms.location.Geofence
import java.util.*

class RequestList(var intnet: PendingIntent?, var requestCode: Int, var geofences: ArrayList<Geofence?>?) {
    fun show(): String {
        val buf = StringBuilder()
        var s = ""
        for (i in geofences!!.indices) {
            buf.append("""PendingIntent: $requestCode UniqueID: ${geofences!![i]!!.uniqueId}
""")
        }
        s = buf.toString()
        return s
    }

    fun checkID(): Boolean {
        val list = GeoFenceData.returnList()
        for (j in list!!.indices) {
            val s = list[j]!!.uniqueId
            for (i in geofences!!.indices) {
                if (s == geofences!![i]!!.uniqueId) {
                    return true
                    //id already exist
                }
            }
        }
        return false
    }

    fun removeID(str: Array<String>) {
        for (i in str.indices) {
            val s = str[i]
            for (j in geofences!!.indices.reversed()) {
                if (s == geofences!![j]!!.uniqueId) {
                    geofences!!.removeAt(j)
                }
            }
        }
    }

}