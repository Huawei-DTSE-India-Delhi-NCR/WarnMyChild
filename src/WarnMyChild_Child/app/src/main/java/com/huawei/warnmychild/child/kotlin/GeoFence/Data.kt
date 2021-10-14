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
package com.huawei.warnmychild.child.kotlin.GeoFence

import java.io.Serializable

class Data : Serializable {
    var latitude = 0.0
    var longitude = 0.0
    var radius = 0f
    var uniqueId: String? = null
    var conversions = 0
    var validContinueTime: Long = 0
    var dwellDelayTime = 0
    var notificationInterval = 0
}