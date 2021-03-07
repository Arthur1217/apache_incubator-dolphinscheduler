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

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.ReleaseState;
import org.apache.dolphinscheduler.common.process.Property;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * process template
 *
 * @author ArthurZhang
 * @since 2021-02-05
 */
@TableName("t_ds_process_template")
public class ProcessTemplate {
    
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private int id;
    /**
     * name
     */
    private String name;
    /**
     * business type id
     */
    private Integer bizTypeId;
    /**
     * business form url
     */
    private String bizFormUrl;
    /**
     * project id
     */
    private int projectId;
    /**
     * process user id
     */
    private int userId;
    /**
     * version
     */
    private int version;
    /**
     * release state : online/offline
     */
    private ReleaseState releaseState;
    /**
     * process template is valid: yes/no
     */
    private Flag flag;
    /**
     * create time
     */
    private Date createTime;
    /**
     * update time
     */
    private Date updateTime;
    /**
     * modify user name
     */
    private String modifyBy;
    /**
     * description
     */
    private String description;
    /**
     * locations array for web
     */
    private String locations;
    /**
     * connects array for web
     */
    private String connects;
    /**
     * template json string
     */
    private String processTemplateJson;
    /**
     * user defined parameters
     */
    private String globalParams;
    /**
     * resource ids
     */
    private String resourceIds;
    /**
     * tenant id
     */
    private int tenantId;
    
    
    /**
     * user name
     */
    @TableField(exist = false)
    private String userName;
    /**
     * project name
     */
    @TableField(exist = false)
    private String projectName;
    /**
     * user defined parameter list
     */
    @TableField(exist = false)
    private List<Property> globalParamList;
    /**
     * user define parameter map
     */
    @TableField(exist = false)
    private Map<String, String> globalParamMap;
    
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getBizTypeId() {
        return bizTypeId;
    }
    
    public void setBizTypeId(Integer bizTypeId) {
        this.bizTypeId = bizTypeId;
    }
    
    public String getBizFormUrl() {
        return bizFormUrl;
    }
    
    public void setBizFormUrl(String bizFormUrl) {
        this.bizFormUrl = bizFormUrl;
    }
    
    public int getProjectId() {
        return projectId;
    }
    
    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public int getVersion() {
        return version;
    }
    
    public void setVersion(int version) {
        this.version = version;
    }
    
    public ReleaseState getReleaseState() {
        return releaseState;
    }
    
    public void setReleaseState(ReleaseState releaseState) {
        this.releaseState = releaseState;
    }
    
    public Flag getFlag() {
        return flag;
    }
    
    public void setFlag(Flag flag) {
        this.flag = flag;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    
    public String getModifyBy() {
        return modifyBy;
    }
    
    public void setModifyBy(String modifyBy) {
        this.modifyBy = modifyBy;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getLocations() {
        return locations;
    }
    
    public void setLocations(String locations) {
        this.locations = locations;
    }
    
    public String getConnects() {
        return connects;
    }
    
    public void setConnects(String connects) {
        this.connects = connects;
    }
    
    public String getProcessTemplateJson() {
        return processTemplateJson;
    }
    
    public void setProcessTemplateJson(String processTemplateJson) {
        this.processTemplateJson = processTemplateJson;
    }
    
    public String getGlobalParams() {
        return globalParams;
    }
    
    public void setGlobalParams(String globalParams) {
        this.globalParamList = JSON.parseArray(globalParams, Property.class);
        this.globalParams = globalParams;
    }
    
    public String getResourceIds() {
        return resourceIds;
    }
    
    public void setResourceIds(String resourceIds) {
        this.resourceIds = resourceIds;
    }
    
    public int getTenantId() {
        return tenantId;
    }
    
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public List<Property> getGlobalParamList() {
        return globalParamList;
    }
    
    public void setGlobalParamList(List<Property> globalParamList) {
        this.globalParams = JSON.toJSONString(globalParamList);
        this.globalParamList = globalParamList;
    }
    
    public Map<String, String> getGlobalParamMap() {
        List<Property> propList;
        if (globalParamMap == null && StringUtils.isNotEmpty(globalParams)) {
            propList = JSON.parseArray(globalParams, Property.class);
            globalParamMap = propList.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
        }
        return globalParamMap;
    }
    
    public void setGlobalParamMap(Map<String, String> globalParamMap) {
        this.globalParamMap = globalParamMap;
    }
    
    
    @Override
    public String toString() {
        return "ProcessDefinition{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", bizTypeId=" + bizTypeId +
                ", bizFormUrl='" + bizFormUrl + '\'' +
                ", projectId=" + projectId +
                ", userId=" + userId +
                ", version=" + version +
                ", releaseState=" + releaseState +
                ", flag=" + flag +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", modifyBy='" + modifyBy + '\'' +
                ", description='" + description + '\'' +
                ", locations='" + locations + '\'' +
                ", connects='" + connects + '\'' +
                ", processTemplateJson='" + processTemplateJson + '\'' +
                ", globalParams='" + globalParams + '\'' +
                ", resourceIds='" + resourceIds + '\'' +
                ", tenantId=" + tenantId +
                ", userName='" + userName + '\'' +
                ", projectName='" + projectName + '\'' +
                ", globalParamList=" + globalParamList +
                ", globalParamMap=" + globalParamMap +
                '}';
    }
    
}
