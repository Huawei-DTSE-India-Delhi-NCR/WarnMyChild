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

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

/**
 * Definition of ObjectType GeofenceDetails.
 *
 * @since 2021-05-03
 */
@PrimaryKeys({Constant.GEOFENCE_TABLE_PRIMARY_KEY1,Constant.GEOFENCE_TABLE_PRIMARY_KEY2,Constant.GEOFENCE_TABLE_PRIMARY_KEY3,Constant.GEOFENCE_TABLE_PRIMARY_KEY4})
public final class GeofenceDetails extends CloudDBZoneObject {
    private String ChildID;

    private String Lat;

    private String Lon;

    private String Radius;

    private String GeofenceName;

    private String Assignedby;

    private Boolean Isvalid;

    public GeofenceDetails() {
        super(GeofenceDetails.class);
    }

    public void setChildID(String ChildID) {
        this.ChildID = ChildID;
    }

    public String getChildID() {
        return ChildID;
    }

    public void setLat(String Lat) {
        this.Lat = Lat;
    }

    public String getLat() {
        return Lat;
    }

    public void setLon(String Lon) {
        this.Lon = Lon;
    }

    public String getLon() {
        return Lon;
    }

    public void setRadius(String Radius) {
        this.Radius = Radius;
    }

    public String getRadius() {
        return Radius;
    }

    public void setGeofenceName(String GeofenceName) {
        this.GeofenceName = GeofenceName;
    }

    public String getGeofenceName() {
        return GeofenceName;
    }

    public void setAssignedby(String Assignedby) {
        this.Assignedby = Assignedby;
    }

    public String getAssignedby() {
        return Assignedby;
    }

    public void setIsvalid(Boolean Isvalid) {
        this.Isvalid = Isvalid;
    }

    public Boolean getIsvalid() {
        return Isvalid;
    }

}
