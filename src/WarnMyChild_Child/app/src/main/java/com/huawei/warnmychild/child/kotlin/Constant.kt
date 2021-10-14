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
package com.huawei.warnmychild.child.kotlin

import com.huawei.hms.maps.model.LatLng

object Constant {
    val Origin = LatLng(20.361091, 86.321886)
    const val MAPVIEW_BUNDLE_KEY = "MapViewBundleKey"
    const val GEO_FENCE_LAT = "geo_lat"
    const val GEO_FENCE_LONG = "geo_long"
    const val GEO_FENCE_REDIUS = "geo_redius"
    const val GEO_FENCE_NAME = "geo_name"
    var REQUEST_CODE = 999
    var INTERVAL_SECONDS = 5000
    const val ERROR_CODE_LABLE = "errorcode: "
    const val CONVERSION_LABLE = "conversion: "
    const val GEOFENCE_ID_LABLE = "geofence id :"
    const val LOCATION_LABLE = "location is :"
    const val SUCCESSFULL_STATUS = "is successful :"
    const val NOT_UNIQUE_ID = "not unique ID!"
    const val ADD_GEOFENCE_FAILED = "addGeofence failed!"
    const val NO_GEOFENCE_DATA = "no GeoFence Data!"
    const val SLIDE_SHOW_FRAGMENT_LABLE = "This is slideshow fragment"
    const val BIG_TEXT = "Warning"
    const val BIG_CONTENT_TITLE = "Message from parent App"
    const val CONTENT_TITLE = "Warning"
    const val DEFAULT_CHANNEL_ID = "4546"
    const val REOPEN_CLOUD_DB = "CloudDBZone is null, try re-open it"
    const val NOTIFICATION_UPSERT_SUCCESS = "Notification details upsert_sucess "
    const val NOTIFICATION_INSERT_FAIL = "Notification details insert_failed "
    const val NO_USER_FOUND = "No User Found"
    const val RECORDS_LABLE = " records"
}