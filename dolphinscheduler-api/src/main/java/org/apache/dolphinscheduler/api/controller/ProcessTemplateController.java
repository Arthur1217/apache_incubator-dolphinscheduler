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
package org.apache.dolphinscheduler.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.annotations.*;
import io.swagger.models.auth.In;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.ProcessTemplateService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.ParameterUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.*;


/**
 * process template controller
 *
 * @author ArthurZhang
 * @since 2021-02-05
 */
@Api(tags = "PROCESS_TEMPLATE_TAG", position = 2)
@RestController
@RequestMapping("projects/{projectName}/template")
public class ProcessTemplateController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProcessTemplateController.class);
    
    @Autowired
    private ProcessTemplateService processTemplateService;
    
    /**
     * create process template
     *
     * @param loginUser   login user
     * @param projectName project name
     * @param name        process template name
     * @param bizTypeId   business type id
     * @param bizFormUrl  business form url
     * @param description description
     * @param locations   locations for nodes
     * @param connects    connects for nodes
     * @param json        process template json
     * @return create result code
     */
    @ApiOperation(value = "save", notes = "CREATE_PROCESS_TEMPLATE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_TEMPLATE_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "bizTypeId", value = "PROCESS_TEMPLATE_BIZ_TYPE_ID", required = false, dataType = "Int"),
            @ApiImplicitParam(name = "bizFormUrl", value = "PROCESS_TEMPLATE_BIZ_FORM_URL", required = false, type = "String"),
            @ApiImplicitParam(name = "description", value = "PROCESS_TEMPLATE_DESC", required = false, type = "String"),
            @ApiImplicitParam(name = "processTemplateJson", value = "PROCESS_TEMPLATE_JSON", required = true, type = "String"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_TEMPLATE_LOCATIONS", required = true, type = "String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_TEMPLATE_CONNECTS", required = true, type = "String")
    })
    @PostMapping(value = "/save")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiException(CREATE_PROCESS_TEMPLATE)
    public Result createProcessTemplate(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                        @RequestParam(value = "name", required = true) String name,
                                        @RequestParam(value = "bizTypeId", required = false) Integer bizTypeId,
                                        @RequestParam(value = "bizFormUrl", required = false) String bizFormUrl,
                                        @RequestParam(value = "description", required = false) String description,
                                        @RequestParam(value = "locations", required = true) String locations,
                                        @RequestParam(value = "connects", required = true) String connects,
                                        @RequestParam(value = "processTemplateJson", required = true) String json) throws JsonProcessingException {
        
        logger.info("login user {}, create  process template, project name: {}, process template name: {}, " +
                "bizTypeId: {}, bizFormUrl: {}, desc: {}, locations:{}, connects:{}, process_template_json: {}" +
                loginUser.getUserName(), projectName, name, bizTypeId, bizFormUrl, description, locations, connects, json);
        Map<String, Object> result = processTemplateService.createProcessTemplate(loginUser, projectName, name, bizTypeId, bizFormUrl,
                description, locations, connects, json);
        return returnDataList(result);
    }
    
    /**
     * copy process template
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processTemplateId process template id
     * @return copy result code
     */
    @ApiOperation(value = "copyProcessTemplate", notes = "COPY_PROCESS_TEMPLATE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processTemplateId", value = "PROCESS_TEMPLATE_ID", required = true, dataType = "Int", example = "100")
    })
    @PostMapping(value = "/copy")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(COPY_PROCESS_TEMPLATE_ERROR)
    public Result copyProcessTemplate(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                      @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                      @RequestParam(value = "processTemplateId", required = true) int processTemplateId) throws JsonProcessingException {
        logger.info("copy process template, login user:{}, project name:{}, process template id:{}",
                loginUser.getUserName(), projectName, processTemplateId);
        Map<String, Object> result = processTemplateService.copyProcessTemplate(loginUser, projectName, processTemplateId);
        return returnDataList(result);
    }
    
    /**
     * verify process template name unique
     *
     * @param loginUser   login user
     * @param projectName project name
     * @param name        name
     * @return true if process template name not exists, otherwise false
     */
    @ApiOperation(value = "verify-name", notes = "VERIFY_PROCESS_TEMPLATE_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_TEMPLATE_NAME", required = true, type = "String")
    })
    @GetMapping(value = "/verify-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(VERIFY_PROCESS_TEMPLATE_NAME_UNIQUE_ERROR)
    public Result verifyProcessTemplateName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                            @RequestParam(value = "name", required = true) String name) {
        logger.info("verify process template name unique, user:{}, project name:{}, process template name:{}",
                loginUser.getUserName(), projectName, name);
        Map<String, Object> result = processTemplateService.verifyProcessTemplateName(loginUser, projectName, name);
        return returnDataList(result);
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
     * @param description         description
     * @param locations           locations for nodes
     * @param connects            connects for nodes
     * @param processTemplateJson process template json
     * @return update result code
     */
    @ApiOperation(value = "updateProcessTemplate", notes = "UPDATE_PROCESS_TEMPLATE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "PROCESS_TEMPLATE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "name", value = "PROCESS_TEMPLATE_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "bizTypeId", value = "PROCESS_TEMPLATE_BIZ_TYPE_ID", required = false, dataType = "Int"),
            @ApiImplicitParam(name = "bizFormUrl", value = "PROCESS_TEMPLATE_BIZ_FORM_URL", required = false, type = "String"),
            @ApiImplicitParam(name = "description", value = "PROCESS_TEMPLATE_DESC", required = false, type = "String"),
            @ApiImplicitParam(name = "locations", value = "PROCESS_TEMPLATE_LOCATIONS", required = true, type = "String"),
            @ApiImplicitParam(name = "connects", value = "PROCESS_TEMPLATE_CONNECTS", required = true, type = "String"),
            @ApiImplicitParam(name = "processTemplateJson", value = "PROCESS_TEMPLATE_JSON", required = true, type = "String")
    })
    @PostMapping(value = "/update")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(UPDATE_PROCESS_TEMPLATE_ERROR)
    public Result updateProcessTemplate(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                        @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                        @RequestParam(value = "id", required = true) int id,
                                        @RequestParam(value = "name", required = true) String name,
                                        @RequestParam(value = "bizTypeId", required = false) Integer bizTypeId,
                                        @RequestParam(value = "bizFormUrl", required = false) String bizFormUrl,
                                        @RequestParam(value = "description", required = false) String description,
                                        @RequestParam(value = "locations", required = false) String locations,
                                        @RequestParam(value = "connects", required = false) String connects,
                                        @RequestParam(value = "processTemplateJson", required = true) String processTemplateJson) {
        logger.info("login user {}, update process template, project name: {}, process template name: {}, " +
                        "bizTypeId: {}, bizFormUrl: {}, desc: {}, locations:{}, connects:{}, process_template_json: {}",
                loginUser.getUserName(), projectName, name, bizTypeId, bizFormUrl, description, locations, connects, processTemplateJson);
        Map<String, Object> result = processTemplateService.updateProcessTemplate(loginUser, projectName, id, name,
                bizTypeId, bizFormUrl, description, locations, connects, processTemplateJson);
        return returnDataList(result);
    }
    
    /**
     * release process template
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processTemplateId process template id
     * @param releaseState      release state
     * @return release result code
     */
    @ApiOperation(value = "releaseProcessTemplate", notes = "RELEASE_PROCESS_TEMPLATE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "PROCESS_TEMPLATE_NAME", required = true, type = "String"),
            @ApiImplicitParam(name = "processTemplateId", value = "PROCESS_TEMPLATE_ID", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "releaseState", value = "PROCESS_TEMPLATE_CONNECTS", required = true, dataType = "Int", example = "100"),
    })
    @PostMapping(value = "/release")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(RELEASE_PROCESS_DEFINITION_ERROR)
    public Result releaseProcessTemplate(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                         @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                         @RequestParam(value = "processTemplateId", required = true) int processTemplateId,
                                         @RequestParam(value = "releaseState", required = true) int releaseState) {
        
        logger.info("login user {}, release process template, project name: {}, release state: {}",
                loginUser.getUserName(), projectName, releaseState);
        Map<String, Object> result = processTemplateService.releaseProcessTemplate(loginUser, projectName, processTemplateId, releaseState);
        return returnDataList(result);
    }
    
    /**
     * query detail of process template
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processTemplateId process template id
     * @return process template detail
     */
    @ApiOperation(value = "queryProcessTemplateById", notes = "QUERY_PROCESS_TEMPLATE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processTemplateId", value = "PROCESS_TEMPLATE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/select-by-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_DETAIL_OF_PROCESS_TEMPLATE_ERROR)
    public Result queryProcessTemplateById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                           @RequestParam("processTemplateId") Integer processTemplateId) {
        logger.info("query detail of process template, login user:{}, project name:{}, process template id:{}",
                loginUser.getUserName(), projectName, processTemplateId);
        Map<String, Object> result = processTemplateService.queryProcessTemplateById(loginUser, projectName, processTemplateId);
        return returnDataList(result);
    }
    
    /**
     * query process template list
     *
     * @param loginUser   login user
     * @param projectName project name
     * @return process template list
     */
    @ApiOperation(value = "queryProcessTemplateList", notes = "QUERY_PROCESS_TEMPLATE_LIST_NOTES")
    @GetMapping(value = "/list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_TEMPLATE_LIST)
    public Result queryProcessTemplateList(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName) {
        logger.info("query process template list, login user:{}, project name:{}",
                loginUser.getUserName(), projectName);
        Map<String, Object> result = processTemplateService.queryProcessTemplateList(loginUser, projectName);
        return returnDataList(result);
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
    @ApiOperation(value = "queryProcessTemplateListPaging", notes = "QUERY_PROCESS_TEMPLATE_LIST_PAGING_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNo", value = "PAGE_NO", required = true, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "searchVal", value = "SEARCH_VAL", required = false, type = "String"),
            @ApiImplicitParam(name = "userId", value = "USER_ID", required = false, dataType = "Int", example = "100"),
            @ApiImplicitParam(name = "pageSize", value = "PAGE_SIZE", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "/list-paging")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_TEMPLATE_LIST_PAGING_ERROR)
    public Result queryProcessTemplateListPaging(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                 @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                 @RequestParam("pageNo") Integer pageNo,
                                                 @RequestParam(value = "searchVal", required = false) String searchVal,
                                                 @RequestParam(value = "userId", required = false, defaultValue = "0") Integer userId,
                                                 @RequestParam("pageSize") Integer pageSize) {
        logger.info("query process template list paging, login user:{}, project name:{}", loginUser.getUserName(), projectName);
        Map<String, Object> result = checkPageParams(pageNo, pageSize);
        if (result.get(Constants.STATUS) != Status.SUCCESS) {
            return returnDataListPaging(result);
        }
        searchVal = ParameterUtils.handleEscapes(searchVal);
        result = processTemplateService.queryProcessTemplateListPaging(loginUser, projectName, searchVal, pageNo, pageSize, userId);
        return returnDataListPaging(result);
    }
    
    /**
     * get tasks list by process template id
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processTemplateId process template id
     * @return task list
     */
    @ApiOperation(value = "getNodeListByTemplateId", notes = "GET_NODE_LIST_BY_TEMPLATE_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processTemplateId", value = "PROCESS_TEMPLATE_ID", required = true, dataType = "Int", example = "100")
    })
    @GetMapping(value = "get-task-list-by-template-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_TEMPLATE_ID_ERROR)
    public Result getNodeListByTemplateId(
            @ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam("processTemplateId") Integer processTemplateId) throws Exception {
        logger.info("query task node name list by templateId, login user:{}, project name:{}, id : {}",
                loginUser.getUserName(), projectName, processTemplateId);
        Map<String, Object> result = processTemplateService.getTaskNodeListByTemplateId(processTemplateId);
        return returnDataList(result);
    }
    
    /**
     * get tasks list by process template id
     *
     * @param loginUser             login user
     * @param projectName           project name
     * @param processTemplateIdList process template id list
     * @return node list data
     */
    @ApiOperation(value = "getNodeListByTemplateIdList", notes = "GET_NODE_LIST_BY_TEMPLATE_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processTemplateIdList", value = "PROCESS_TEMPLATE_ID_LIST", required = true, type = "String")
    })
    @GetMapping(value = "/get-task-list-by-template-id-list")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_TASKS_LIST_BY_PROCESS_TEMPLATE_ID_LIST_ERROR)
    public Result getNodeListByTemplateIdList(
            @ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
            @RequestParam("processTemplateIdList") String processTemplateIdList) throws Exception {
        
        logger.info("query task node name list by templateId list, login user:{}, project name:{}, id list: {}",
                loginUser.getUserName(), projectName, processTemplateIdList);
        Map<String, Object> result = processTemplateService.getTaskNodeListByTemplateIdList(processTemplateIdList);
        return returnDataList(result);
    }
    
    /**
     * delete process template by id
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processTemplateId process template id
     * @return delete result code
     */
    @ApiOperation(value = "deleteProcessTemplateById", notes = "DELETE_PROCESS_TEMPLATE_BY_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processTemplateId", value = "PROCESS_TEMPLATE_ID", dataType = "Int", example = "100")
    })
    @GetMapping(value = "/delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(DELETE_PROCESS_TEMPLATE_BY_ID_ERROR)
    public Result deleteProcessTemplateById(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                            @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                            @RequestParam("processTemplateId") Integer processTemplateId) {
        logger.info("delete process template by id, login user:{}, project name:{}, process template id:{}",
                loginUser.getUserName(), projectName, processTemplateId);
        Map<String, Object> result = processTemplateService.deleteProcessTemplateById(loginUser, projectName, processTemplateId);
        return returnDataList(result);
    }
    
    /**
     * batch delete process template by ids
     *
     * @param loginUser          login user
     * @param projectName        project name
     * @param processTemplateIds process template id list
     * @return delete result code
     */
    @ApiOperation(value = "batchDeleteProcessTemplateByIds", notes = "BATCH_DELETE_PROCESS_TEMPLATE_BY_IDS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processTemplateIds", value = "PROCESS_TEMPLATE_IDS", type = "String")
    })
    @GetMapping(value = "/batch-delete")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(BATCH_DELETE_PROCESS_TEMPLATE_BY_IDS_ERROR)
    public Result batchDeleteProcessTemplateByIds(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                  @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                  @RequestParam("processTemplateIds") String processTemplateIds) {
        logger.info("delete process template by ids, login user:{}, project name:{}, process template ids:{}",
                loginUser.getUserName(), projectName, processTemplateIds);
        
        Map<String, Object> result = new HashMap<>(5);
        List<String> deleteFailedIdList = new ArrayList<>();
        if (StringUtils.isNotEmpty(processTemplateIds)) {
            String[] processTemplateIdArray = processTemplateIds.split(",");
            
            for (String strProcessTemplateId : processTemplateIdArray) {
                int processTemplateId = Integer.parseInt(strProcessTemplateId);
                try {
                    Map<String, Object> deleteResult = processTemplateService.deleteProcessTemplateById(loginUser, projectName, processTemplateId);
                    if (!Status.SUCCESS.equals(deleteResult.get(Constants.STATUS))) {
                        deleteFailedIdList.add(strProcessTemplateId);
                        logger.error((String) deleteResult.get(Constants.MSG));
                    }
                } catch (Exception e) {
                    deleteFailedIdList.add(strProcessTemplateId);
                }
            }
        }
        
        if (!deleteFailedIdList.isEmpty()) {
            putMsg(result, Status.BATCH_DELETE_PROCESS_TEMPLATE_BY_IDS_ERROR, String.join(",", deleteFailedIdList));
        } else {
            putMsg(result, Status.SUCCESS);
        }
        
        return returnDataList(result);
    }
    
    /**
     * batch export process template by ids
     *
     * @param loginUser          login user
     * @param projectName        project name
     * @param processTemplateIds process template ids
     * @param response           response
     */
    @ApiOperation(value = "batchExportProcessTemplateByIds", notes = "BATCH_EXPORT_PROCESS_TEMPLATE_BY_IDS_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processTemplateIds", value = "PROCESS_TEMPLATE_ID", required = true, dataType = "String")
    })
    @GetMapping(value = "/export")
    @ResponseBody
    public void batchExportProcessTemplateByIds(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                @RequestParam("processTemplateIds") String processTemplateIds,
                                                HttpServletResponse response) {
        try {
            logger.info("batch export process template by ids, login user:{}, project name:{}, process template ids:{}",
                    loginUser.getUserName(), projectName, processTemplateIds);
            processTemplateService.batchExportProcessTemplateByIds(loginUser, projectName, processTemplateIds, response);
        } catch (Exception e) {
            logger.error(Status.BATCH_EXPORT_PROCESS_TEMPLATE_BY_IDS_ERROR.getMsg(), e);
        }
    }
    
    /**
     * query process template all by project id
     *
     * @param loginUser login user
     * @param projectId project id
     * @return process template list
     */
    @ApiOperation(value = "queryProcessTemplateAllByProjectId", notes = "QUERY_PROCESS_TEMPLATE_All_BY_PROJECT_ID_NOTES")
    @GetMapping(value = "/queryProcessTemplateAllByProjectId")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_PROCESS_TEMPLATE_LIST)
    public Result queryProcessTemplateAllByProjectId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                     @RequestParam("projectId") Integer projectId) {
        logger.info("query process template list, login user:{}, project id:{}",
                loginUser.getUserName(), projectId);
        Map<String, Object> result = processTemplateService.queryProcessTemplateAllByProjectId(projectId);
        return returnDataList(result);
    }
    
}
