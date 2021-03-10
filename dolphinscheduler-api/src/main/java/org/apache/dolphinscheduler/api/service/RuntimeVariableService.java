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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.RuntimeVariable;
import org.apache.dolphinscheduler.dao.entity.RuntimeVariableData;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ProjectMapper;
import org.apache.dolphinscheduler.dao.mapper.RuntimeVariableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * runtime variable service
 *
 * @author ArthurZhang
 * @since 2021-02-05
 */
@Service
public class RuntimeVariableService extends BaseService {
    
    /**
     * 运行时变量ID
     */
    private final static String RUNTIME_VARIABLE_ID = "RUNTIME_VARIABLE_ID";
    
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private RuntimeVariableMapper runtimeVariableMapper;
    
    /**
     * query global runtime variable list by process instance id
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @return runtime variable list
     */
    public Map<String, Object> queryGlobalRuntimeVariableListByProcessInstanceId(User loginUser, String projectName, int processInstanceId) {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        QueryWrapper<RuntimeVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RuntimeVariable::getProcessInstanceId, processInstanceId);
        List<RuntimeVariableData> list = runtimeVariableMapper.selectList(queryWrapper)
                .stream().map(e -> new RuntimeVariableData(e.getVarName(), e.getVarValue())).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.DATA_LIST, list);
        putMsg(result, Status.SUCCESS);
        return result;
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
    public Map<String, Object> getGlobalRuntimeVariableByProcessInstanceIdAndVarName(User loginUser, String projectName, int processInstanceId, String varName) {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        QueryWrapper<RuntimeVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RuntimeVariable::getProcessInstanceId, processInstanceId)
                .eq(RuntimeVariable::getTaskInstanceId, -1)
                .eq(RuntimeVariable::getVarName, varName);
        RuntimeVariable runtimeVariable = runtimeVariableMapper.selectOne(queryWrapper);
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.DATA_LIST, null == runtimeVariable ? null : new RuntimeVariableData(runtimeVariable.getVarName(), runtimeVariable.getVarValue()));
        putMsg(result, Status.SUCCESS);
        return result;
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
    public Map<String, Object> setGlobalRuntimeVariable(User loginUser, String projectName, int processInstanceId, String varName, String varValue) {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        QueryWrapper<RuntimeVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RuntimeVariable::getProcessInstanceId, processInstanceId)
                .eq(RuntimeVariable::getTaskInstanceId, -1)
                .eq(RuntimeVariable::getVarName, varName);
        RuntimeVariable runtimeVariable = runtimeVariableMapper.selectOne(queryWrapper);
        if (null != runtimeVariable) {
            runtimeVariable.setVarValue(varValue);
            runtimeVariableMapper.updateById(runtimeVariable);
        } else {
            runtimeVariable = new RuntimeVariable();
            runtimeVariable.setProcessInstanceId(processInstanceId);
            runtimeVariable.setTaskInstanceId(-1);
            runtimeVariable.setVarName(varName);
            runtimeVariable.setVarValue(varValue);
            runtimeVariableMapper.insert(runtimeVariable);
        }
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);
        result.put(RUNTIME_VARIABLE_ID, runtimeVariable.getId());
        return result;
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
    public Map<String, Object> queryLocalRuntimeVariableListByProcessInstanceIdAndTaskInstanceId(User loginUser, String projectName, int processInstanceId, int taskInstanceId) {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        QueryWrapper<RuntimeVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RuntimeVariable::getProcessInstanceId, processInstanceId)
                .eq(RuntimeVariable::getTaskInstanceId, taskInstanceId);
        List<RuntimeVariableData> list = runtimeVariableMapper.selectList(queryWrapper)
                .stream().map(e -> new RuntimeVariableData(e.getVarName(), e.getVarValue())).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.DATA_LIST, list);
        putMsg(result, Status.SUCCESS);
        return result;
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
    public Map<String, Object> getLocalRuntimeVariableByProcessInstanceIdAndTaskInstanceIdAndVarName(User loginUser, String projectName, int processInstanceId, int taskInstanceId, String varName) {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        QueryWrapper<RuntimeVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RuntimeVariable::getProcessInstanceId, processInstanceId)
                .eq(RuntimeVariable::getTaskInstanceId, taskInstanceId)
                .eq(RuntimeVariable::getVarName, varName);
        RuntimeVariable runtimeVariable = runtimeVariableMapper.selectOne(queryWrapper);
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.DATA_LIST, null == runtimeVariable ? null : new RuntimeVariableData(runtimeVariable.getVarName(), runtimeVariable.getVarValue()));
        putMsg(result, Status.SUCCESS);
        return result;
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
    public Map<String, Object> setLocalRuntimeVariable(User loginUser, String projectName, int processInstanceId, int taskInstanceId, String varName, String varValue) {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        QueryWrapper<RuntimeVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RuntimeVariable::getProcessInstanceId, processInstanceId)
                .eq(RuntimeVariable::getTaskInstanceId, taskInstanceId)
                .eq(RuntimeVariable::getVarName, varName);
        RuntimeVariable runtimeVariable = runtimeVariableMapper.selectOne(queryWrapper);
        if (null != runtimeVariable) {
            runtimeVariable.setVarValue(varValue);
            runtimeVariableMapper.updateById(runtimeVariable);
        } else {
            runtimeVariable = new RuntimeVariable();
            runtimeVariable.setProcessInstanceId(processInstanceId);
            runtimeVariable.setTaskInstanceId(taskInstanceId);
            runtimeVariable.setVarName(varName);
            runtimeVariable.setVarValue(varValue);
            runtimeVariableMapper.insert(runtimeVariable);
        }
        Map<String, Object> result = new HashMap<>(5);
        putMsg(result, Status.SUCCESS);
        result.put(RUNTIME_VARIABLE_ID, runtimeVariable.getId());
        return result;
    }
    
    /**
     * get schedule info variable by process instance id
     *
     * @param loginUser         login user
     * @param projectName       project name
     * @param processInstanceId process instance id
     * @return runtime variable
     */
    public Map<String, Object> getScheduleInfoVariableByProcessInstanceId(User loginUser, String projectName, int processInstanceId) {
        Project project = projectMapper.queryByName(projectName);
        Map<String, Object> checkResult = projectService.checkProjectAndAuth(loginUser, project, projectName);
        Status resultStatus = (Status) checkResult.get(Constants.STATUS);
        if (resultStatus != Status.SUCCESS) {
            return checkResult;
        }
        QueryWrapper<RuntimeVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RuntimeVariable::getProcessInstanceId, processInstanceId)
                .eq(RuntimeVariable::getTaskInstanceId, -1)
                .eq(RuntimeVariable::getVarName, Constants.SCHEDULE_INFO_VARIABLE);
        RuntimeVariable runtimeVariable = runtimeVariableMapper.selectOne(queryWrapper);
        Map<String, Object> result = new HashMap<>(5);
        result.put(Constants.DATA_LIST, null == runtimeVariable ? null : new RuntimeVariableData(runtimeVariable.getVarName(), runtimeVariable.getVarValue()));
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
