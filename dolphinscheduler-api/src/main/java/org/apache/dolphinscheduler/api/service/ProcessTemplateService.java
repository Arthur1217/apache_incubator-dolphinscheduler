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

package org.apache.dolphinscheduler.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.dolphinscheduler.api.dto.ProcessTemplateMeta;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.CheckUtils;
import org.apache.dolphinscheduler.api.utils.FileUtils;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.exportprocess.ProcessAddTaskParam;
import org.apache.dolphinscheduler.api.utils.exportprocess.TaskNodeParamFactory;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.*;
import org.apache.dolphinscheduler.common.graph.DAG;
import org.apache.dolphinscheduler.common.model.TaskNode;
import org.apache.dolphinscheduler.common.process.Property;
import org.apache.dolphinscheduler.common.process.ResourceInfo;
import org.apache.dolphinscheduler.common.task.AbstractParameters;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.common.utils.TaskParametersUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessTemplate;
import org.apache.dolphinscheduler.dao.entity.ProcessTemplateData;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProcessTemplateMapper;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.service.permission.PermissionCheck;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * process template service
 *
 * @author ArthurZhang
 * @since 2021-02-05
 */
@Service
public class ProcessTemplateService extends BaseDAGService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessTemplateService.class);
    
    private static final String PROCESSTEMPLATEID = "processTemplateId";
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ProcessTemplateMapper processTemplateMapper;
    
    @Autowired
    private ProcessService processService;
    
    /**
     * create process template
     *
     * @param loginUser           login user
     * @param projectName         project name
     * @param name                process template name
     * @param bizTypeId           business type id
     * @param bizFormUrl          business form url
     * @param desc                description
     * @param locations           locations for nodes
     * @param connects            connects for nodes
     * @param processTemplateJson process template json
     * @return create result code
     * @throws JsonProcessingException JsonProcessingException
     */
    public Map<String, Object> createProcessTemplate(User loginUser,
                                                     String projectName,
                                                     String name,
                                                     Integer bizTypeId,
                                                     String bizFormUrl,
                                                     String desc,
                                                     String locations,
                                                     String connects,
                                                     String processTemplateJson) throws JsonProcessingException {
        
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);
        // check project auth
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        
        ProcessTemplate processTemplate = new ProcessTemplate();
        Date now = new Date();
        
        ProcessTemplateData processTemplateData = JSONUtils.parseObject(processTemplateJson, ProcessTemplateData.class);
        Map<String, Object> checkProcessJson = checkProcessNodeList(processTemplateData, processTemplateJson);
        if (checkProcessJson.get(Constants.STATUS) != Status.SUCCESS) {
            return checkProcessJson;
        }
        
        processTemplate.setName(name);
        processTemplate.setBizTypeId(bizTypeId);
        processTemplate.setBizFormUrl(bizFormUrl);
        processTemplate.setProjectId(project.getId());
        processTemplate.setUserId(loginUser.getId());
        processTemplate.setTenantId(processTemplateData.getTenantId());
        processTemplate.setReleaseState(ReleaseState.OFFLINE);
        processTemplate.setFlag(Flag.YES);
        processTemplate.setCreateTime(now);
        processTemplate.setUpdateTime(now);
        processTemplate.setModifyBy(loginUser.getUserName());
        processTemplate.setDescription(desc);
        processTemplate.setLocations(locations);
        processTemplate.setConnects(connects);
        processTemplate.setProcessTemplateJson(processTemplateJson);
        
        // custom global params
        List<Property> globalParamsList = processTemplateData.getGlobalParams();
        if (CollectionUtils.isNotEmpty(globalParamsList)) {
            Set<Property> globalParamsSet = new HashSet<>(globalParamsList);
            globalParamsList = new ArrayList<>(globalParamsSet);
            processTemplate.setGlobalParamList(globalParamsList);
        }
        
        processTemplate.setResourceIds(getResourceIds(processTemplateData));
        
        processTemplateMapper.insert(processTemplate);
        putMsg(result, Status.SUCCESS);
        result.put(PROCESSTEMPLATEID, processTemplate.getId());
        return result;
    }
    
    /**
     * get resource ids
     *
     * @param processTemplateData process template data
     * @return resource ids
     */
    private String getResourceIds(ProcessTemplateData processTemplateData) {
        List<TaskNode> tasks = processTemplateData.getTasks();
        Set<Integer> resourceIds = new HashSet<>();
        for (TaskNode taskNode : tasks) {
            String taskParameter = taskNode.getParams();
            AbstractParameters params = TaskParametersUtils.getParameters(taskNode.getType(), taskParameter);
            if (CollectionUtils.isNotEmpty(params.getResourceFilesList())) {
                Set<Integer> tempSet = params.getResourceFilesList().stream().filter(t -> t.getId() != 0).map(ResourceInfo::getId).collect(Collectors.toSet());
                resourceIds.addAll(tempSet);
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i : resourceIds) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(i);
        }
        return sb.toString();
    }
    
    /**
     * query process template list
     *
     * @param loginUser   login user
     * @param projectName project name
     * @return template list
     */
    public Map<String, Object> queryProcessTemplateList(User loginUser, String projectName) {
        
        HashMap<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);
        
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        
        List<ProcessTemplate> resourceList = processTemplateMapper.queryAllTemplateList(project.getId());
        result.put(Constants.DATA_LIST, resourceList);
        putMsg(result, Status.SUCCESS);
        
        return result;
    }
    
    /**
     * query process template list paging
     *
     * @param loginUser   login user
     * @param projectName project name
     * @param searchVal   search value
     * @param pageNo      page number
     * @param pageSize    page size
     * @param userId      user id
     * @return process template page
     */
    public Map<String, Object> queryProcessTemplateListPaging(User loginUser, String projectName, String searchVal, Integer pageNo, Integer pageSize, Integer userId) {
        
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);
        
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        
        Page<ProcessTemplate> page = new Page(pageNo, pageSize);
        IPage<ProcessTemplate> processTemplateIPage = processTemplateMapper.queryTemplateListPaging(
                page, searchVal, userId, project.getId(), isAdmin(loginUser));
        
        PageInfo pageInfo = new PageInfo<ProcessTemplateData>(pageNo, pageSize);
        pageInfo.setTotalCount((int) processTemplateIPage.getTotal());
        pageInfo.setLists(processTemplateIPage.getRecords());
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);
        
        return result;
    }
    
    /**
     * query datail of process template
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processTemplateId process template id
     * @return process template detail
     */
    public Map<String, Object> queryProcessTemplateById(User loginUser, String projectName, Integer processTemplateId) {
        
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);
        
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        
        ProcessTemplate processTemplate = processTemplateMapper.selectById(processTemplateId);
        if (processTemplate == null) {
            putMsg(result, Status.PROCESS_TEMPLATE_NOT_EXIST, processTemplateId);
        } else {
            result.put(Constants.DATA_LIST, processTemplate);
            putMsg(result, Status.SUCCESS);
        }
        return result;
    }
    
    /**
     * copy process template
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processTemplateId process template id
     * @return copy result code
     */
    public Map<String, Object> copyProcessTemplate(User loginUser, String projectName, Integer processTemplateId) throws JsonProcessingException {
        
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);
        
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        
        ProcessTemplate processTemplate = processTemplateMapper.selectById(processTemplateId);
        if (processTemplate == null) {
            putMsg(result, Status.PROCESS_TEMPLATE_NOT_EXIST, processTemplateId);
            return result;
        } else {
            return createProcessTemplate(
                    loginUser,
                    projectName,
                    processTemplate.getName() + "_copy_" + System.currentTimeMillis(),
                    processTemplate.getBizTypeId(),
                    processTemplate.getBizFormUrl(),
                    processTemplate.getDescription(),
                    processTemplate.getLocations(),
                    processTemplate.getConnects(),
                    processTemplate.getProcessTemplateJson());
        }
    }
    
    /**
     * update process template
     *
     * @param loginUser           login user
     * @param projectName         project name
     * @param id                  process template id
     * @param name                process template name
     * @param bizTypeId           business type id
     * @param bizFormUrl          business form url
     * @param desc                description
     * @param locations           locations for nodes
     * @param connects            connects for nodes
     * @param processTemplateJson process template json
     * @return update result code
     */
    public Map<String, Object> updateProcessTemplate(User loginUser, String projectName, int id, String name,
                                                     Integer bizTypeId, String bizFormUrl, String desc,
                                                     String locations, String connects, String processTemplateJson) {
        Map<String, Object> result = new HashMap<>(5);
        
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        
        ProcessTemplateData processTemplateData = JSONUtils.parseObject(processTemplateJson, ProcessTemplateData.class);
        Map<String, Object> checkProcessJson = checkProcessNodeList(processTemplateData, processTemplateJson);
        if ((checkProcessJson.get(Constants.STATUS) != Status.SUCCESS)) {
            return checkProcessJson;
        }
        ProcessTemplate processTemplate = processService.findProcessTemplateById(id);
        // check process template exists
        if (processTemplate == null) {
            putMsg(result, Status.PROCESS_TEMPLATE_NOT_EXIST, id);
            return result;
        }
        
        if (processTemplate.getReleaseState() == ReleaseState.ONLINE) {
            // online can not permit edit
            putMsg(result, Status.PROCESS_TEMPLATE_NOT_ALLOWED_EDIT, processTemplate.getName());
            return result;
        }
        
        if (!name.equals(processTemplate.getName())) {
            // check whether the new process template name exist
            ProcessTemplate template = processTemplateMapper.verifyByTemplateName(project.getId(), name);
            if (template != null) {
                putMsg(result, Status.VERIFY_PROCESS_TEMPLATE_NAME_UNIQUE_ERROR, name);
                return result;
            }
        }
        
        Date now = new Date();
        
        processTemplate.setId(id);
        processTemplate.setName(name);
        processTemplate.setBizTypeId(bizTypeId);
        processTemplate.setBizFormUrl(bizFormUrl);
        processTemplate.setProjectId(project.getId());
        processTemplate.setTenantId(processTemplateData.getTenantId());
        processTemplate.setReleaseState(ReleaseState.OFFLINE);
        processTemplate.setFlag(Flag.YES);
        processTemplate.setUpdateTime(now);
        processTemplate.setModifyBy(loginUser.getUserName());
        processTemplate.setDescription(desc);
        processTemplate.setLocations(locations);
        processTemplate.setConnects(connects);
        processTemplate.setProcessTemplateJson(processTemplateJson);
        
        //custom global params
        List<Property> globalParamsList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processTemplateData.getGlobalParams())) {
            Set<Property> userDefParamsSet = new HashSet<>(processTemplateData.getGlobalParams());
            globalParamsList = new ArrayList<>(userDefParamsSet);
        }
        processTemplate.setGlobalParamList(globalParamsList);
        
        processTemplate.setResourceIds(getResourceIds(processTemplateData));
        
        if (processTemplateMapper.updateById(processTemplate) > 0) {
            putMsg(result, Status.SUCCESS);
            
        } else {
            putMsg(result, Status.UPDATE_PROCESS_TEMPLATE_ERROR);
        }
        return result;
    }
    
    /**
     * verify process template name unique
     *
     * @param loginUser   login user
     * @param projectName project name
     * @param name        name
     * @return true if process template name not exists, otherwise false
     */
    public Map<String, Object> verifyProcessTemplateName(User loginUser, String projectName, String name) {
        
        Map<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);
        
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        ProcessTemplate processTemplate = processTemplateMapper.verifyByTemplateName(project.getId(), name);
        if (processTemplate == null) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.VERIFY_PROCESS_TEMPLATE_NAME_UNIQUE_ERROR, name);
        }
        return result;
    }
    
    /**
     * delete process template by id
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processTemplateId process template id
     * @return delete result code
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> deleteProcessTemplateById(User loginUser, String projectName, Integer processTemplateId) {
        
        Map<String, Object> result = new HashMap<>(5);
        Project project = projectMapper.queryByName(projectName);
        
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        
        ProcessTemplate processTemplate = processTemplateMapper.selectById(processTemplateId);
        
        if (processTemplate == null) {
            putMsg(result, Status.PROCESS_TEMPLATE_NOT_EXIST, processTemplateId);
            return result;
        }
        
        // Determine if the login user is the owner of the process template
        if (loginUser.getId() != processTemplate.getUserId() && loginUser.getUserType() != UserType.ADMIN_USER) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        
        int delete = processTemplateMapper.deleteById(processTemplateId);
        
        if (delete > 0) {
            putMsg(result, Status.SUCCESS);
        } else {
            putMsg(result, Status.DELETE_PROCESS_TEMPLATE_BY_ID_ERROR);
        }
        return result;
    }
    
    /**
     * release process template: online / offline
     *
     * @param loginUser    login user
     * @param projectName  project name
     * @param id           process template id
     * @param releaseState release state
     * @return release result code
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> releaseProcessTemplate(User loginUser, String projectName, int id, int releaseState) {
        HashMap<String, Object> result = new HashMap<>();
        Project project = projectMapper.queryByName(projectName);
        
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultEnum = (Status) checkResult.get(Constants.STATUS);
        if (resultEnum != Status.SUCCESS) {
            return checkResult;
        }
        
        ReleaseState state = ReleaseState.getEnum(releaseState);
        
        // check state
        if (null == state) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "releaseState");
            return result;
        }
        
        ProcessTemplate processTemplate = processTemplateMapper.selectById(id);
        
        switch (state) {
            case ONLINE:
                // To check resources whether they are already cancel authorized or deleted
                String resourceIds = processTemplate.getResourceIds();
                if (StringUtils.isNotBlank(resourceIds)) {
                    Integer[] resourceIdArray = Arrays.stream(resourceIds.split(",")).map(Integer::parseInt).toArray(Integer[]::new);
                    PermissionCheck<Integer> permissionCheck = new PermissionCheck(AuthorizationType.RESOURCE_FILE_ID, processService, resourceIdArray, loginUser.getId(), logger);
                    try {
                        permissionCheck.checkPermission();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        putMsg(result, Status.RESOURCE_NOT_EXIST_OR_NO_PERMISSION, "releaseState");
                        return result;
                    }
                }
                
                processTemplate.setReleaseState(state);
                processTemplateMapper.updateById(processTemplate);
                break;
            case OFFLINE:
                processTemplate.setReleaseState(state);
                processTemplateMapper.updateById(processTemplate);
                break;
            default:
                putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, "releaseState");
                return result;
        }
        
        putMsg(result, Status.SUCCESS);
        return result;
    }
    
    /**
     * batch export process template by ids
     *
     * @param loginUser          login user
     * @param projectName        project name
     * @param processTemplateIds process template ids
     * @param response           http response
     */
    public void batchExportProcessTemplateByIds(User loginUser, String projectName, String processTemplateIds, HttpServletResponse response) {
        
        if (StringUtils.isEmpty(processTemplateIds)) {
            return;
        }
        
        //export project info
        Project project = projectMapper.queryByName(projectName);
        
        //check user access for project
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        
        if (resultStatus != Status.SUCCESS) {
            return;
        }
        
        List<ProcessTemplateMeta> processTemplateList =
                getProcessTemplateList(processTemplateIds);
        
        if (CollectionUtils.isNotEmpty(processTemplateList)) {
            downloadProcessTemplateFile(response, processTemplateList);
        }
    }
    
    /**
     * get process template list by ids
     *
     * @param processTemplateIds process template ids
     * @return process template meta list
     */
    private List<ProcessTemplateMeta> getProcessTemplateList(String processTemplateIds) {
        List<ProcessTemplateMeta> processTemplateList = new ArrayList<>();
        String[] processTemplateIdArray = processTemplateIds.split(",");
        for (String strProcessTemplateId : processTemplateIdArray) {
            //get workflow info
            int processTemplateId = Integer.parseInt(strProcessTemplateId);
            ProcessTemplate processTemplate = processTemplateMapper.queryByTemplateId(processTemplateId);
            if (null != processTemplate) {
                processTemplateList.add(exportProcessTemplateMetaData(processTemplateId, processTemplate));
            }
        }
        
        return processTemplateList;
    }
    
    /**
     * download the process template file
     *
     * @param response            http response
     * @param processTemplateList process template list
     */
    private void downloadProcessTemplateFile(HttpServletResponse response, List<ProcessTemplateMeta> processTemplateList) {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        BufferedOutputStream buff = null;
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
            buff = new BufferedOutputStream(out);
            buff.write(JSON.toJSONString(processTemplateList).getBytes(StandardCharsets.UTF_8));
            buff.flush();
            buff.close();
        } catch (IOException e) {
            logger.warn("export process fail", e);
        } finally {
            if (null != buff) {
                try {
                    buff.close();
                } catch (Exception e) {
                    logger.warn("export process buffer not close", e);
                }
            }
            if (null != out) {
                try {
                    out.close();
                } catch (Exception e) {
                    logger.warn("export process output stream not close", e);
                }
            }
        }
    }
    
    /**
     * get export process metadata string
     *
     * @param processTemplateId process template id
     * @param processTemplate   process template
     * @return export process metadata string
     */
    public String exportProcessTemplateMetaDataStr(Integer processTemplateId, ProcessTemplate processTemplate) {
        //create workflow json file
        return JSONUtils.toJsonString(exportProcessTemplateMetaData(processTemplateId, processTemplate));
    }
    
    /**
     * get export process metadata string
     *
     * @param processTemplateId process template id
     * @param processTemplate   process template
     * @return export process metadata string
     */
    public ProcessTemplateMeta exportProcessTemplateMetaData(Integer processTemplateId, ProcessTemplate processTemplate) {
        //correct task param which has data source or dependent param
        String correctProcessTemplateJson = addExportTaskNodeSpecialParam(processTemplate.getProcessTemplateJson());
        processTemplate.setProcessTemplateJson(correctProcessTemplateJson);
        
        //export process metadata
        ProcessTemplateMeta exportProcessTemplateMeta = new ProcessTemplateMeta();
        exportProcessTemplateMeta.setProjectName(processTemplate.getProjectName());
        exportProcessTemplateMeta.setProcessTemplateName(processTemplate.getName());
        exportProcessTemplateMeta.setProcessTemplateJson(processTemplate.getProcessTemplateJson());
        exportProcessTemplateMeta.setProcessTemplateLocations(processTemplate.getLocations());
        exportProcessTemplateMeta.setProcessTemplateConnects(processTemplate.getConnects());
        
        //create workflow json file
        return exportProcessTemplateMeta;
    }
    
    /**
     * correct task param which has datasource or dependent
     *
     * @param processTemplateJson process template json
     * @return correct process template json
     */
    public String addExportTaskNodeSpecialParam(String processTemplateJson) {
        JSONObject jsonObject = JSONUtils.parseObject(processTemplateJson);
        JSONArray jsonArray = (JSONArray) jsonObject.get("tasks");
        
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject taskNode = jsonArray.getJSONObject(i);
            if (StringUtils.isNotEmpty(taskNode.getString("type"))) {
                String taskType = taskNode.getString("type");
                
                ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);
                if (null != addTaskParam) {
                    addTaskParam.addExportSpecialParam(taskNode);
                }
            }
        }
        jsonObject.put("tasks", jsonArray);
        return jsonObject.toString();
    }
    
    /**
     * check task if has sub process
     *
     * @param taskType task type
     * @return if task has sub process return true else false
     */
    private boolean checkTaskHasSubProcess(String taskType) {
        return taskType.equals(TaskType.SUB_PROCESS.name());
    }
    
    /**
     * import process template
     *
     * @param loginUser          login user
     * @param file               process metadata json file
     * @param currentProjectName current project name
     * @return import process template
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importProcessTemplate(User loginUser, MultipartFile file, String currentProjectName) {
        Map<String, Object> result = new HashMap<>(5);
        String processTemplateMetaJson = FileUtils.file2String(file);
        List<ProcessTemplateMeta> processTemplateMetaList = JSON.parseArray(processTemplateMetaJson, ProcessTemplateMeta.class);
        
        //check file content
        if (CollectionUtils.isEmpty(processTemplateMetaList)) {
            putMsg(result, Status.DATA_IS_NULL, "fileContent");
            return result;
        }
        
        for (ProcessTemplateMeta processTemplateMeta : processTemplateMetaList) {
            
            if (!checkAndImportProcessTemplate(loginUser, currentProjectName, result, processTemplateMeta)) {
                return result;
            }
        }
        
        return result;
    }
    
    /**
     * check and import process template
     *
     * @param loginUser           login user
     * @param currentProjectName  current project name
     * @param result              import process
     * @param processTemplateMeta process template meta
     * @return if import successfully return true else false
     */
    private boolean checkAndImportProcessTemplate(User loginUser, String currentProjectName, Map<String, Object> result, ProcessTemplateMeta processTemplateMeta) {
        
        if (!checkImportanceParams(processTemplateMeta, result)) {
            return false;
        }
        
        // deal with process name
        String processTemplateName = processTemplateMeta.getProcessTemplateName();
        // use currentProjectName to query
        Project targetProject = projectMapper.queryByName(currentProjectName);
        if (null != targetProject) {
            processTemplateName = recursionProcessTemplateName(targetProject.getId(),
                    processTemplateName, 1);
        }
        
        // unique check
        Map<String, Object> checkResult = verifyProcessTemplateName(loginUser, currentProjectName, processTemplateName);
        Status status = (Status) checkResult.get(Constants.STATUS);
        if (Status.SUCCESS.equals(status)) {
            putMsg(result, Status.SUCCESS);
        } else {
            result.putAll(checkResult);
            return false;
        }
        
        // get create process result
        Map<String, Object> createProcessTemplateResult =
                getCreateProcessTemplateResult(loginUser,
                        currentProjectName,
                        result,
                        processTemplateMeta,
                        processTemplateName,
                        addImportTaskNodeParam(loginUser, processTemplateMeta.getProcessTemplateJson(), targetProject));
        
        return null != createProcessTemplateResult;
        
    }
    
    /**
     * get create process template result
     *
     * @param loginUser           login user
     * @param currentProjectName  current project name
     * @param result              create result code
     * @param processTemplateMeta process template meta
     * @param processTemplateName process template name
     * @param importProcessParam  import process param
     * @return result of process template creating result
     */
    private Map<String, Object> getCreateProcessTemplateResult(User loginUser,
                                                               String currentProjectName,
                                                               Map<String, Object> result,
                                                               ProcessTemplateMeta processTemplateMeta,
                                                               String processTemplateName,
                                                               String importProcessParam) {
        Map<String, Object> createProcessTemplateResult = null;
        try {
            createProcessTemplateResult = createProcessTemplate(loginUser
                    , currentProjectName,
                    processTemplateName + "_import_" + System.currentTimeMillis(),
                    processTemplateMeta.getBizTypeId(),
                    processTemplateMeta.getBizFormUrl(),
                    processTemplateMeta.getProcessTemplateDescription(),
                    processTemplateMeta.getProcessTemplateLocations(),
                    processTemplateMeta.getProcessTemplateConnects(),
                    importProcessParam);
            putMsg(result, Status.SUCCESS);
        } catch (JsonProcessingException e) {
            logger.error("import process template meta json data: {}", e.getMessage(), e);
            putMsg(result, Status.IMPORT_PROCESS_TEMPLATE_ERROR);
        }
        
        return createProcessTemplateResult;
    }
    
    /**
     * check importance params
     *
     * @param processTemplateMeta process template meta
     * @param result              import result code
     * @return check result code
     */
    private boolean checkImportanceParams(ProcessTemplateMeta processTemplateMeta, Map<String, Object> result) {
        if (StringUtils.isEmpty(processTemplateMeta.getProjectName())) {
            putMsg(result, Status.DATA_IS_NULL, "projectName");
            return false;
        }
        if (StringUtils.isEmpty(processTemplateMeta.getProcessTemplateName())) {
            putMsg(result, Status.DATA_IS_NULL, "processTemplateName");
            return false;
        }
        if (StringUtils.isEmpty(processTemplateMeta.getProcessTemplateJson())) {
            putMsg(result, Status.DATA_IS_NULL, "processTemplateJson");
            return false;
        }
        
        return true;
    }
    
    /**
     * import process add special task param
     *
     * @param loginUser           login user
     * @param processTemplateJson process template json
     * @param targetProject       target project
     * @return import process param
     */
    private String addImportTaskNodeParam(User loginUser, String processTemplateJson, Project targetProject) {
        JSONObject jsonObject = JSONUtils.parseObject(processTemplateJson);
        JSONArray jsonArray = (JSONArray) jsonObject.get("tasks");
        //add sql and dependent param
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject taskNode = jsonArray.getJSONObject(i);
            String taskType = taskNode.getString("type");
            ProcessAddTaskParam addTaskParam = TaskNodeParamFactory.getByTaskType(taskType);
            if (null != addTaskParam) {
                addTaskParam.addImportSpecialParam(taskNode);
            }
        }
        
        //recursive sub-process parameter correction map key for old process id value for new process id
        Map<Integer, Integer> subProcessTemplateIdMap = new HashMap<>(20);
        
        List<Object> subProcessList = jsonArray.stream()
                .filter(elem -> checkTaskHasSubProcess(JSONUtils.parseObject(elem.toString()).getString("type")))
                .collect(Collectors.toList());
        
        if (CollectionUtils.isNotEmpty(subProcessList)) {
            importSubProcessTemplate(loginUser, targetProject, jsonArray, subProcessTemplateIdMap);
        }
        
        jsonObject.put("tasks", jsonArray);
        return jsonObject.toString();
    }
    
    /**
     * check import process template has sub process template
     * recursion create sub process template
     *
     * @param loginUser               login user
     * @param targetProject           target project
     * @param jsonArray               process task array
     * @param subProcessTemplateIdMap correct sub process template id map
     */
    public void importSubProcessTemplate(User loginUser, Project targetProject, JSONArray jsonArray, Map<Integer, Integer> subProcessTemplateIdMap) {
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject taskNode = jsonArray.getJSONObject(i);
            String taskType = taskNode.getString("type");
            
            if (checkTaskHasSubProcess(taskType)) {
                //get sub process info
                JSONObject subParams = JSONUtils.parseObject(taskNode.getString("params"));
                Integer subProcessTemplateId = subParams.getInteger(PROCESSTEMPLATEID);
                ProcessTemplate subProcessTemplate = processTemplateMapper.queryByTemplateId(subProcessTemplateId);
                //check is sub process exist in db
                if (null != subProcessTemplate) {
                    String subProcessTemplateJson = subProcessTemplate.getProcessTemplateJson();
                    //check current project has sub process
                    ProcessTemplate currentProjectSubProcess = processTemplateMapper.queryByTemplateName(targetProject.getId(), subProcessTemplate.getName());
                    
                    if (null == currentProjectSubProcess) {
                        JSONArray subJsonArray = (JSONArray) JSONUtils.parseObject(subProcessTemplate.getProcessTemplateJson()).get("tasks");
                        
                        List<Object> subProcessList = subJsonArray.stream()
                                .filter(item -> checkTaskHasSubProcess(JSONUtils.parseObject(item.toString()).getString("type")))
                                .collect(Collectors.toList());
                        
                        if (CollectionUtils.isNotEmpty(subProcessList)) {
                            importSubProcessTemplate(loginUser, targetProject, subJsonArray, subProcessTemplateIdMap);
                            //sub process processTemplateId correct
                            if (!subProcessTemplateIdMap.isEmpty()) {
                                
                                for (Map.Entry<Integer, Integer> entry : subProcessTemplateIdMap.entrySet()) {
                                    String oldSubProcessId = "\"processTemplateId\":" + entry.getKey();
                                    String newSubProcessId = "\"processTemplateId\":" + entry.getValue();
                                    subProcessTemplateJson = subProcessTemplateJson.replaceAll(oldSubProcessId, newSubProcessId);
                                }
                                
                                subProcessTemplateIdMap.clear();
                            }
                        }
                        
                        //if sub-process recursion
                        Date now = new Date();
                        //create sub process in target project
                        ProcessTemplate processTemplate = new ProcessTemplate();
                        processTemplate.setName(subProcessTemplate.getName());
                        processTemplate.setProjectId(targetProject.getId());
                        processTemplate.setUserId(loginUser.getId());
                        processTemplate.setTenantId(subProcessTemplate.getTenantId());
                        processTemplate.setVersion(subProcessTemplate.getVersion());
                        processTemplate.setReleaseState(subProcessTemplate.getReleaseState());
                        processTemplate.setFlag(subProcessTemplate.getFlag());
                        processTemplate.setCreateTime(now);
                        processTemplate.setUpdateTime(now);
                        processTemplate.setModifyBy(subProcessTemplate.getUserName());
                        processTemplate.setProcessTemplateJson(subProcessTemplateJson);
                        processTemplate.setDescription(subProcessTemplate.getDescription());
                        processTemplate.setGlobalParams(subProcessTemplate.getGlobalParams());
                        processTemplate.setLocations(subProcessTemplate.getLocations());
                        processTemplate.setConnects(subProcessTemplate.getConnects());
                        processTemplate.setResourceIds(subProcessTemplate.getResourceIds());
                        processTemplate.setBizTypeId(subProcessTemplate.getBizTypeId());
                        
                        processTemplateMapper.insert(processTemplate);
                        
                        logger.info("create sub process, project: {}, process name: {}", targetProject.getName(), processTemplate.getName());
                        
                        //modify task node
                        ProcessTemplate newSubProcessTemplate = processTemplateMapper.queryByTemplateName(processTemplate.getProjectId(), processTemplate.getName());
                        
                        if (null != newSubProcessTemplate) {
                            subProcessTemplateIdMap.put(subProcessTemplateId, newSubProcessTemplate.getId());
                            subParams.put(PROCESSTEMPLATEID, newSubProcessTemplate.getId());
                            taskNode.put("params", subParams);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * check the process template node meets the specifications
     *
     * @param processTemplateData process template data
     * @param processTemplateJson process template json
     * @return check result code
     */
    public Map<String, Object> checkProcessNodeList(ProcessTemplateData processTemplateData, String processTemplateJson) {
        
        Map<String, Object> result = new HashMap<>(5);
        try {
            if (processTemplateData == null) {
                logger.error("process template data is null");
                putMsg(result, Status.DATA_IS_NOT_VALID, processTemplateJson);
                return result;
            }
            
            // Check whether the task node is normal
            List<TaskNode> taskNodes = processTemplateData.getTasks();
            
            if (taskNodes == null) {
                logger.error("process node info is empty");
                putMsg(result, Status.DATA_IS_NULL, processTemplateJson);
                return result;
            }
            
            // check has cycle
            if (graphHasCycle(taskNodes)) {
                logger.error("process DAG has cycle");
                putMsg(result, Status.PROCESS_NODE_HAS_CYCLE);
                return result;
            }
            
            // check whether the process template json is normal
            for (TaskNode taskNode : taskNodes) {
                if (!CheckUtils.checkTaskNodeParameters(taskNode.getParams(), taskNode.getType())) {
                    logger.error("task node {} parameter invalid", taskNode.getName());
                    putMsg(result, Status.PROCESS_NODE_S_PARAMETER_INVALID, taskNode.getName());
                    return result;
                }
                
                // check extra params
                CheckUtils.checkOtherParams(taskNode.getExtras());
            }
            putMsg(result, Status.SUCCESS);
        } catch (Exception e) {
            result.put(Constants.STATUS, Status.REQUEST_PARAMS_NOT_VALID_ERROR);
            result.put(Constants.MSG, e.getMessage());
        }
        return result;
    }
    
    /**
     * get task node details based on process template
     *
     * @param templateId template id
     * @return task node list
     * @throws Exception exception
     */
    public Map<String, Object> getTaskNodeListByTemplateId(Integer templateId) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        ProcessTemplate processTemplate = processTemplateMapper.selectById(templateId);
        if (processTemplate == null) {
            logger.info("process template not exists");
            putMsg(result, Status.PROCESS_TEMPLATE_NOT_EXIST, templateId);
            return result;
        }
        
        String processTemplateJson = processTemplate.getProcessTemplateJson();
        
        ProcessTemplateData processTemplateData = JSONUtils.parseObject(processTemplateJson, ProcessTemplateData.class);
        
        // process template data check
        if (null == processTemplateData) {
            logger.error("process template data is null");
            putMsg(result, Status.DATA_IS_NOT_VALID, processTemplateJson);
            return result;
        }
        
        List<TaskNode> taskNodeList = (processTemplateData.getTasks() == null) ? new ArrayList<>() : processTemplateData.getTasks();
        
        result.put(Constants.DATA_LIST, taskNodeList);
        putMsg(result, Status.SUCCESS);
        
        return result;
        
    }
    
    /**
     * get task node details based on process template
     *
     * @param templateIdList template id list
     * @return task node list
     * @throws Exception exception
     */
    public Map<String, Object> getTaskNodeListByTemplateIdList(String templateIdList) throws Exception {
        Map<String, Object> result = new HashMap<>();
        
        Map<Integer, List<TaskNode>> taskNodeMap = new HashMap<>();
        String[] idList = templateIdList.split(",");
        List<Integer> idIntList = new ArrayList<>();
        for (String templateId : idList) {
            idIntList.add(Integer.parseInt(templateId));
        }
        Integer[] idArray = idIntList.toArray(new Integer[idIntList.size()]);
        List<ProcessTemplate> processTemplateList = processTemplateMapper.queryTemplateListByIdList(idArray);
        if (CollectionUtils.isEmpty(processTemplateList)) {
            logger.info("process template not exists");
            putMsg(result, Status.PROCESS_TEMPLATE_NOT_EXIST, templateIdList);
            return result;
        }
        
        for (ProcessTemplate processTemplate : processTemplateList) {
            String processTemplateJson = processTemplate.getProcessTemplateJson();
            ProcessTemplateData processTemplateData = JSONUtils.parseObject(processTemplateJson, ProcessTemplateData.class);
            List<TaskNode> taskNodeList = (processTemplateData.getTasks() == null) ? new ArrayList<>() : processTemplateData.getTasks();
            taskNodeMap.put(processTemplate.getId(), taskNodeList);
        }
        
        result.put(Constants.DATA_LIST, taskNodeMap);
        putMsg(result, Status.SUCCESS);
        
        return result;
        
    }
    
    /**
     * query process template all by project id
     *
     * @param projectId project id
     * @return process templates in the project
     */
    public Map<String, Object> queryProcessTemplateAllByProjectId(Integer projectId) {
        
        HashMap<String, Object> result = new HashMap<>(5);
        
        List<ProcessTemplate> resourceList = processTemplateMapper.queryAllTemplateList(projectId);
        result.put(Constants.DATA_LIST, resourceList);
        putMsg(result, Status.SUCCESS);
        
        return result;
    }
    
    /**
     * whether the graph has a ring
     *
     * @param taskNodeResponseList task node response list
     * @return if graph has cycle flag
     */
    private boolean graphHasCycle(List<TaskNode> taskNodeResponseList) {
        DAG<String, TaskNode, String> graph = new DAG<>();
        
        // Fill the vertices
        for (TaskNode taskNodeResponse : taskNodeResponseList) {
            graph.addNode(taskNodeResponse.getName(), taskNodeResponse);
        }
        
        // Fill edge relations
        for (TaskNode taskNodeResponse : taskNodeResponseList) {
            taskNodeResponse.getPreTasks();
            List<String> preTasks = JSONUtils.toList(taskNodeResponse.getPreTasks(), String.class);
            if (CollectionUtils.isNotEmpty(preTasks)) {
                for (String preTask : preTasks) {
                    if (!graph.addEdge(preTask, taskNodeResponse.getName())) {
                        return true;
                    }
                }
            }
        }
        
        return graph.hasCycle();
    }
    
    /**
     * @param projectId           project id
     * @param processTemplateName process template name
     * @param num                 num
     * @return recursive process template name
     */
    private String recursionProcessTemplateName(Integer projectId, String processTemplateName, int num) {
        ProcessTemplate processTemplate = processTemplateMapper.queryByTemplateName(projectId, processTemplateName);
        if (processTemplate != null) {
            if (num > 1) {
                String str = processTemplateName.substring(0, processTemplateName.length() - 3);
                processTemplateName = str + "(" + num + ")";
            } else {
                processTemplateName = processTemplate.getName() + "(" + num + ")";
            }
        } else {
            return processTemplateName;
        }
        return recursionProcessTemplateName(projectId, processTemplateName, num + 1);
    }
    
}

