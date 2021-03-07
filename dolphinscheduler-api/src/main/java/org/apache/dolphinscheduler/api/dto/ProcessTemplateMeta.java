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
package org.apache.dolphinscheduler.api.dto;

/**
 * process template meta
 *
 * @author ArthurZhang
 * @since 2021-02-05
 */
public class ProcessTemplateMeta {
    
    /**
     * project name
     */
    private String projectName;
    /**
     * process template name
     */
    private String processTemplateName;
    /**
     * process template json
     */
    private String processTemplateJson;
    /**
     * process template desc
     */
    private String processTemplateDescription;
    /**
     * process template locations
     */
    private String processTemplateLocations;
    /**
     * process template connects
     */
    private String processTemplateConnects;
    /**
     * business type id
     */
    private int bizTypeId;
    /**
     * business form url
     */
    private String bizFormUrl;
    
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getProcessTemplateName() {
        return processTemplateName;
    }
    
    public void setProcessTemplateName(String processTemplateName) {
        this.processTemplateName = processTemplateName;
    }
    
    public String getProcessTemplateJson() {
        return processTemplateJson;
    }
    
    public void setProcessTemplateJson(String processTemplateJson) {
        this.processTemplateJson = processTemplateJson;
    }
    
    public String getProcessTemplateDescription() {
        return processTemplateDescription;
    }
    
    public void setProcessTemplateDescription(String processTemplateDescription) {
        this.processTemplateDescription = processTemplateDescription;
    }
    
    public String getProcessTemplateLocations() {
        return processTemplateLocations;
    }
    
    public void setProcessTemplateLocations(String processTemplateLocations) {
        this.processTemplateLocations = processTemplateLocations;
    }
    
    public String getProcessTemplateConnects() {
        return processTemplateConnects;
    }
    
    public void setProcessTemplateConnects(String processTemplateConnects) {
        this.processTemplateConnects = processTemplateConnects;
    }
    
    public int getBizTypeId() {
        return bizTypeId;
    }
    
    public void setBizTypeId(int bizTypeId) {
        this.bizTypeId = bizTypeId;
    }
    
    public String getBizFormUrl() {
        return bizFormUrl;
    }
    
    public void setBizFormUrl(String bizFormUrl) {
        this.bizFormUrl = bizFormUrl;
    }
}
