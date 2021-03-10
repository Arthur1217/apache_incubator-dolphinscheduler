/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dolphinscheduler.dao.entity;

import org.apache.dolphinscheduler.common.enums.ScheduleType;

import java.util.Date;


/**
 * schedule info
 *
 * @author ArthurZhang
 * @since 2021-02-05
 */
public class ScheduleInfo {
    
    /**
     * schedule type
     */
    private ScheduleType scheduleType;
    /**
     * trigger time
     */
    private Date triggerTime;
    
    
    public ScheduleInfo(ScheduleType scheduleType, Date triggerTime) {
        this.scheduleType = scheduleType;
        this.triggerTime = triggerTime;
    }
    
    
    public ScheduleType getScheduleType() {
        return scheduleType;
    }
    
    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }
    
    public Date getTriggerTime() {
        return triggerTime;
    }
    
    public void setTriggerTime(Date triggerTime) {
        this.triggerTime = triggerTime;
    }
    
}
