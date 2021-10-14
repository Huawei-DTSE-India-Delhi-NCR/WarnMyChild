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
import com.huawei.hms.maps.model.LatLng;
public class Constant {
    public static final LatLng Origin = new LatLng(20.361091, 86.321886);
    public static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";

    public static final String GEO_FENCE_LAT = "geo_lat";
    public static final String GEO_FENCE_LONG = "geo_long";
    public static final String GEO_FENCE_REDIUS = "geo_redius";
    public static final String GEO_FENCE_NAME = "geo_name";
    public static int REQUEST_CODE = 999;
    public static int INTERVAL_SECONDS = 5000;

    public static final String ERROR_CODE_LABLE = "errorcode: ";
    public static final String CONVERSION_LABLE = "conversion: ";
    public static final String GEOFENCE_ID_LABLE = "geofence id :";
    public static final String LOCATION_LABLE = "location is :";
    public static final String SUCCESSFULL_STATUS = "is successful :";

    public static final String NOT_UNIQUE_ID = "not unique ID!";
    public static final String ADD_GEOFENCE_FAILED = "addGeofence failed!";
    public static final String NO_GEOFENCE_DATA = "no GeoFence Data!";

    public static final String SLIDE_SHOW_FRAGMENT_LABLE = "This is slideshow fragment";

    public static final String BIG_TEXT = "Warning";
    public static final String BIG_CONTENT_TITLE = "Message from parent App";
    public static final String CONTENT_TITLE = "Warning";
    public static final String DEFAULT_CHANNEL_ID = "4546";
    public static final String REOPEN_CLOUD_DB = "CloudDBZone is null, try re-open it";
    public static final String NOTIFICATION_UPSERT_SUCCESS = "Notification details upsert_sucess ";
    public static final String NOTIFICATION_INSERT_FAIL = "Notification details insert_failed ";

    public static final String NO_USER_FOUND = "No User Found";
    public static final String RECORDS_LABLE = " records";


}
