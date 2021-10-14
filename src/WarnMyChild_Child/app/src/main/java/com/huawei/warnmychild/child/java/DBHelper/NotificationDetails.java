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

import java.util.Date;

/**
 * Definition of ObjectType NotificationDetails.
 *
 * @since 2021-05-03
 */
@PrimaryKeys({Constant.NOTIFICATION_TABLE_PRIMARY_KEY})
public final class NotificationDetails extends CloudDBZoneObject {
    private String ID;

    private String ChildID;

    private String ParentID;

    private String Message;

    private Date DateTime;

    private Boolean isValid;

    public NotificationDetails() {
        super(NotificationDetails.class);
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getID() {
        return ID;
    }

    public void setChildID(String ChildID) {
        this.ChildID = ChildID;
    }

    public String getChildID() {
        return ChildID;
    }

    public void setParentID(String ParentID) {
        this.ParentID = ParentID;
    }

    public String getParentID() {
        return ParentID;
    }

    public void setMessage(String Message) {
        this.Message = Message;
    }

    public String getMessage() {
        return Message;
    }

    public void setDateTime(Date DateTime) {
        this.DateTime = DateTime;
    }

    public Date getDateTime() {
        return DateTime;
    }

    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }

    public Boolean getIsValid() {
        return isValid;
    }

}
