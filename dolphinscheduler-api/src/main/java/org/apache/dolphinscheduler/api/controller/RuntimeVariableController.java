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

import io.swagger.annotations.*;
import org.apache.dolphinscheduler.api.exceptions.ApiException;
import org.apache.dolphinscheduler.api.service.RuntimeVariableService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Map;

import static org.apache.dolphinscheduler.api.enums.Status.*;


/**
 * runtime variable controller
 *
 * @author ArthurZhang
 * @since 2021-02-05
 */
@Api(tags = "RUNTIME_VARIABLE_TAG", position = 2)
@RestController
@RequestMapping("projects/{projectName}/runtime-variable")
public class RuntimeVariableController extends BaseController {
    
    private static final Logger logger = LoggerFactory.getLogger(RuntimeVariableController.class);
    
    @Autowired
    private RuntimeVariableService runtimeVariableService;
    
    
    /**
     * query global runtime variable list by process instance id
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @return runtime variable list
     */
    @ApiOperation(value = "queryGlobalRuntimeVariableListByProcessInstanceId", notes = "QUERY_GLOBAL_RUNTIME_VARIABLE_LIST_BY_PROCESS_INSTANCE_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int")
    })
    @GetMapping(value = "/list-global-by-process-instance-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_GLOBAL_RUNTIME_VARIABLE_LIST_BY_PROCESS_INSTANCE_ID)
    public Result queryGlobalRuntimeVariableListByProcessInstanceId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                    @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                                    @RequestParam(value = "processInstanceId") int processInstanceId) {
        logger.info("query global runtime variable list, login user:{}, project name:{}, process instance id:{}",
                loginUser.getUserName(), projectName, processInstanceId);
        Map<String, Object> result = runtimeVariableService.queryGlobalRuntimeVariableListByProcessInstanceId(loginUser, projectName, processInstanceId);
        return returnDataList(result);
    }
    
    /**
     * get global runtime variable by process instance id and var name
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @param varName           variable name
     * @return runtime variable
     */
    @ApiOperation(value = "getGlobalRuntimeVariableByProcessInstanceIdAndVarName", notes = "GET_GLOBAL_RUNTIME_VARIABLE_BY_PROCESS_INSTANCE_ID_AND_VAR_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int"),
            @ApiImplicitParam(name = "varName", value = "VARIABLE_NAME", required = true, dataType = "String")
    })
    @GetMapping(value = "/get-global-by-process-instance-id-and-var-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_GLOBAL_RUNTIME_VARIABLE_BY_PROCESS_INSTANCE_ID_AND_VAR_NAME)
    public Result getGlobalRuntimeVariableByProcessInstanceIdAndVarName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                        @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                                        @RequestParam(value = "processInstanceId") int processInstanceId,
                                                                        @RequestParam(value = "varName") String varName) {
        logger.info("get global runtime variable, login user:{}, project name:{}, process instance id:{}, variable name:{}",
                loginUser.getUserName(), projectName, processInstanceId, varName);
        Map<String, Object> result = runtimeVariableService.getGlobalRuntimeVariableByProcessInstanceIdAndVarName(loginUser, projectName, processInstanceId, varName);
        return returnDataList(result);
    }
    
    /**
     * set global runtime variable
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @param varName           variable name
     * @param varValue          variable value
     * @return returns an error if it failed
     */
    @ApiOperation(value = "setGlobalRuntimeVariable", notes = "SET_GLOBAL_RUNTIME_VARIABLE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int"),
            @ApiImplicitParam(name = "varName", value = "VARIABLE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "varValue", value = "VARIABLE_VALUE", required = true, dataType = "String")
    })
    @GetMapping(value = "/set-global")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SET_GLOBAL_RUNTIME_VARIABLE)
    public Result setGlobalRuntimeVariable(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                           @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                           @RequestParam(value = "processInstanceId") int processInstanceId,
                                           @RequestParam(value = "varName") String varName,
                                           @RequestParam(value = "varValue") String varValue) {
        logger.info("set global runtime variable, login user:{}, project name:{}, process instance id:{}, variable name:{}, variable value:{}",
                loginUser.getUserName(), projectName, processInstanceId, varName, varValue);
        Map<String, Object> result = runtimeVariableService.setGlobalRuntimeVariable(loginUser, projectName, processInstanceId, varName, varValue);
        return returnDataList(result);
    }
    
    /**
     * query local runtime variable list by process instance id and task instance id
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @param taskInstanceId    task instance id
     * @return runtime variable list
     */
    @ApiOperation(value = "queryLocalRuntimeVariableListByProcessInstanceIdAndTaskInstanceId", notes = "QUERY_LOCAL_RUNTIME_VARIABLE_LIST_BY_PROCESS_INSTANCE_ID_AND_TASK_INSTANCE_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int"),
            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_INSTANCE_ID", required = true, dataType = "Int")
    })
    @GetMapping(value = "/list-global-by-process-instance-id-and-task-instance-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(QUERY_LOCAL_RUNTIME_VARIABLE_LIST_BY_PROCESS_INSTANCE_ID_AND_TASK_INSTANCE_ID)
    public Result queryLocalRuntimeVariableListByProcessInstanceIdAndTaskInstanceId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                                    @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                                                    @RequestParam(value = "processInstanceId") int processInstanceId,
                                                                                    @RequestParam(value = "taskInstanceId") int taskInstanceId) {
        logger.info("query local runtime variable list, login user:{}, project name:{}, process instance id:{}, task instance id:{}",
                loginUser.getUserName(), projectName, processInstanceId, taskInstanceId);
        Map<String, Object> result = runtimeVariableService.queryLocalRuntimeVariableListByProcessInstanceIdAndTaskInstanceId(loginUser, projectName, processInstanceId, taskInstanceId);
        return returnDataList(result);
    }
    
    /**
     * get local runtime variable by process instance id and task instance id and var name
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @param taskInstanceId    task instance id
     * @param varName           variable name
     * @return runtime variable
     */
    @ApiOperation(value = "getLocalRuntimeVariableByProcessInstanceIdAndTaskInstanceIdAndVarName", notes = "GET_LOCAL_RUNTIME_VARIABLE_BY_PROCESS_INSTANCE_ID_AND_TASK_INSTANCE_ID_AND_VAR_NAME_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int"),
            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_INSTANCE_ID", required = true, dataType = "Int"),
            @ApiImplicitParam(name = "varName", value = "VARIABLE_NAME", required = true, dataType = "String")
    })
    @GetMapping(value = "/get-local-by-process-instance-id-and-task-instance-id-and-var-name")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_LOCAL_RUNTIME_VARIABLE_BY_PROCESS_INSTANCE_ID_AND_TASK_INSTANCE_ID_AND_VAR_NAME)
    public Result getLocalRuntimeVariableByProcessInstanceIdAndTaskInstanceIdAndVarName(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                                                        @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                                                        @RequestParam(value = "processInstanceId") int processInstanceId,
                                                                                        @RequestParam(value = "taskInstanceId") int taskInstanceId,
                                                                                        @RequestParam(value = "varName") String varName) {
        logger.info("query local runtime variable, login user:{}, project name:{}, process instance id:{}, task instance id:{}, variable name:{}",
                loginUser.getUserName(), projectName, processInstanceId, taskInstanceId, varName);
        Map<String, Object> result = runtimeVariableService.getLocalRuntimeVariableByProcessInstanceIdAndTaskInstanceIdAndVarName(loginUser, projectName, processInstanceId, taskInstanceId, varName);
        return returnDataList(result);
    }
    
    /**
     * set local runtime variable
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @param taskInstanceId    task instance id
     * @param varName           variable name
     * @param varValue          variable value
     * @return returns an error if it failed
     */
    @ApiOperation(value = "setLocalRuntimeVariable", notes = "SET_LOCAL_RUNTIME_VARIABLE_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int"),
            @ApiImplicitParam(name = "taskInstanceId", value = "TASK_INSTANCE_ID", required = true, dataType = "Int"),
            @ApiImplicitParam(name = "varName", value = "VARIABLE_NAME", required = true, dataType = "String"),
            @ApiImplicitParam(name = "varValue", value = "VARIABLE_VALUE", required = true, dataType = "String")
    })
    @GetMapping(value = "/set-local")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(SET_LOCAL_RUNTIME_VARIABLE)
    public Result setLocalRuntimeVariable(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                          @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                          @RequestParam(value = "processInstanceId") int processInstanceId,
                                          @RequestParam(value = "taskInstanceId") int taskInstanceId,
                                          @RequestParam(value = "varName") String varName,
                                          @RequestParam(value = "varValue") String varValue) {
        logger.info("set local runtime variable, login user:{}, project name:{}, process instance id:{}, task instance id:{}, variable name:{}, variable value:{}",
                loginUser.getUserName(), projectName, processInstanceId, taskInstanceId, varName, varValue);
        Map<String, Object> result = runtimeVariableService.setLocalRuntimeVariable(loginUser, projectName, processInstanceId, taskInstanceId, varName, varValue);
        return returnDataList(result);
    }
    
    /**
     * get schedule info variable by process instance id
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @return runtime variable
     */
    @ApiOperation(value = "getScheduleInfoVariableByProcessInstanceId", notes = "GET_SCHEDULE_INFO_VARIABLE_BY_PROCESS_INSTANCE_ID_NOTES")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "PROCESS_INSTANCE_ID", required = true, dataType = "Int"),
            @ApiImplicitParam(name = "varName", value = "VARIABLE_NAME", required = true, dataType = "String")
    })
    @GetMapping(value = "/get-schedule-info-by-process-instance-id")
    @ResponseStatus(HttpStatus.OK)
    @ApiException(GET_SCHEDULE_INFO_VARIABLE_BY_PROCESS_INSTANCE_ID)
    public Result getScheduleInfoVariableByProcessInstanceId(@ApiIgnore @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                                             @ApiParam(name = "projectName", value = "PROJECT_NAME", required = true) @PathVariable String projectName,
                                                             @RequestParam(value = "processInstanceId") int processInstanceId) {
        logger.info("get global runtime variable, login user:{}, project name:{}, process instance id:{}",
                loginUser.getUserName(), projectName, processInstanceId);
        Map<String, Object> result = runtimeVariableService.getScheduleInfoVariableByProcessInstanceId(loginUser, projectName, processInstanceId);
        return returnDataList(result);
    }
}
