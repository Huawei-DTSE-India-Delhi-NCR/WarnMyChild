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
package com.huawei.warnmychild.child.java.DBHelper;
public class Constant {


    public static String UID = null;

    public static String getUID() {
        return UID;
    }

    public static void setUID(String access_Token) {
        UID = access_Token;
    }

    public static final String CHILDINFO_TABLE_PRIMARY_KEY_ID = "ChildID";
    public static final String GEOFENCE_TABLE_PRIMARY_KEY1 = "ChildID";
    public static final String GEOFENCE_TABLE_PRIMARY_KEY2 = "Lat";
    public static final String GEOFENCE_TABLE_PRIMARY_KEY3 = "Lon";
    public static final String GEOFENCE_TABLE_PRIMARY_KEY4 = "Assignedby";
    public static final String NOTIFICATION_TABLE_PRIMARY_KEY = "ID";
    public static final String TEST_TABLE_PRIMARY_KEY = "field1";
    public static final String TEST_TABLE_INDEX1 = "index1:field1,field2";
    public static final String TEST_TABLE_INDEX2 = "index2:field1,field2,field3";
    public static final String PARENT_TABLE_PRIMARY_KEY1 = "ParentID";

}
