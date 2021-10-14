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
package com.huawei.warnmychild.child.kotlin.DBHelper

object Constant {
    var uID: String? = null

    const val CHILDINFO_TABLE_PRIMARY_KEY_ID = "ChildID"
    const val GEOFENCE_TABLE_PRIMARY_KEY1 = "ChildID"
    const val GEOFENCE_TABLE_PRIMARY_KEY2 = "Lat"
    const val GEOFENCE_TABLE_PRIMARY_KEY3 = "Lon"
    const val GEOFENCE_TABLE_PRIMARY_KEY4 = "Assignedby"
    const val NOTIFICATION_TABLE_PRIMARY_KEY = "ID"
    const val TEST_TABLE_PRIMARY_KEY = "field1"
    const val TEST_TABLE_INDEX1 = "index1:field1,field2"
    const val TEST_TABLE_INDEX2 = "index2:field1,field2,field3"
}