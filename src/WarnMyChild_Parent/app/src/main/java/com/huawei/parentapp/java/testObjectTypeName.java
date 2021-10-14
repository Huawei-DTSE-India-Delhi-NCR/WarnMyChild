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
 * Definition of ObjectType testObjectTypeName.
 *
 * @since 2021-05-05
 */
@PrimaryKeys({Constants.TEST_TABLE_PRIMARY_KEY})
@Indexes({Constants.TEST_TABLE_INDEX1, Constants.TEST_TABLE_INDEX2})
public final class testObjectTypeName extends CloudDBZoneObject {
    private String field1;

    private Long field2;

    private Integer field3;

    public testObjectTypeName() {
        super(testObjectTypeName.class);
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public String getField1() {
        return field1;
    }

    public void setField2(Long field2) {
        this.field2 = field2;
    }

    public Long getField2() {
        return field2;
    }

    public void setField3(Integer field3) {
        this.field3 = field3;
    }

    public Integer getField3() {
        return field3;
    }



}
