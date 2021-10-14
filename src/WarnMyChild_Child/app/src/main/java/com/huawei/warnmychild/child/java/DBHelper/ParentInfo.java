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
 * Definition of ObjectType ParentInfo.
 *
 * @since 2021-05-03
 */
@PrimaryKeys({Constant.PARENT_TABLE_PRIMARY_KEY1})
public final class ParentInfo extends CloudDBZoneObject {
    private String ParentID;

    private String ParentName;

    private String EmailID;

    private String ChildIDs;

    public ParentInfo() {
        super(ParentInfo.class);
    }

    public void setParentID(String ParentID) {
        this.ParentID = ParentID;
    }

    public String getParentID() {
        return ParentID;
    }

    public void setParentName(String ParentName) {
        this.ParentName = ParentName;
    }

    public String getParentName() {
        return ParentName;
    }

    public void setEmailID(String EmailID) {
        this.EmailID = EmailID;
    }

    public String getEmailID() {
        return EmailID;
    }

    public void setChildIDs(String ChildIDs) {
        this.ChildIDs = ChildIDs;
    }

    public String getChildIDs() {
        return ChildIDs;
    }

}
