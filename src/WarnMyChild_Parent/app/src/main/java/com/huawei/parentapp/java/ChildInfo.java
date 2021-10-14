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
package com.huawei.parentapp.java;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.Text;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.NotNull;
import com.huawei.agconnect.cloud.database.annotations.Indexes;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

import java.util.Date;

/**
 * Definition of ObjectType ChildInfo.
 *
 * @since 2021-05-05
 */
@PrimaryKeys({Constants.CHILD_TABLE_PRIMARY_KEY})
public final class ChildInfo extends CloudDBZoneObject {
    private String ChildID;

    private String ChildName;

    private String ChildEmail;

    private String ParentID;

    public ChildInfo() {
        super(ChildInfo.class);
    }

    public void setChildID(String ChildID) {
        this.ChildID = ChildID;
    }

    public String getChildID() {
        return ChildID;
    }

    public void setChildName(String ChildName) {
        this.ChildName = ChildName;
    }

    public String getChildName() {
        return ChildName;
    }

    public void setChildEmail(String ChildEmail) {
        this.ChildEmail = ChildEmail;
    }

    public String getChildEmail() {
        return ChildEmail;
    }

    public void setParentID(String ParentID) {
        this.ParentID = ParentID;
    }

    public String getParentID() {
        return ParentID;
    }

}
