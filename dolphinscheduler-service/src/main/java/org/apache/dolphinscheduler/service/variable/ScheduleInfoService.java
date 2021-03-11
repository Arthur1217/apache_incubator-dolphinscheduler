package org.apache.dolphinscheduler.service.variable;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ScheduleType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.RuntimeVariable;
import org.apache.dolphinscheduler.dao.entity.ScheduleInfo;
import org.apache.dolphinscheduler.dao.mapper.RuntimeVariableMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * runtime variable
 *
 * @author ArthurZhang
 * @since 2021-02-05
 */
@Component
public class ScheduleInfoService {
    
    @Autowired
    private RuntimeVariableMapper runtimeVariableMapper;
    
    /**
     * save start info
     *
     * @param processInstanceId process instance id
     */
    public void saveStartInfo(int processInstanceId) {
        saveScheduleInfo(ScheduleType.START, processInstanceId, new Date());
    }
    
    /**
     * save timing info
     *
     * @param processInstanceId process instance id
     * @param triggerTime       trigger time
     */
    public void saveTimingInfo(int processInstanceId, Date triggerTime) {
        saveScheduleInfo(ScheduleType.TIMING, processInstanceId, triggerTime);
    }
    
    private void saveScheduleInfo(ScheduleType scheduleType, int processInstanceId, Date tiggerTime) {
        QueryWrapper<RuntimeVariable> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(RuntimeVariable::getProcessInstanceId, processInstanceId)
                .eq(RuntimeVariable::getTaskInstanceId, -1)
                .eq(RuntimeVariable::getVarName, Constants.SCHEDULE_INFO_VARIABLE);
        RuntimeVariable runtimeVariable = runtimeVariableMapper.selectOne(queryWrapper);
        if (null == runtimeVariable) {
            runtimeVariable = new RuntimeVariable();
            runtimeVariable.setProcessInstanceId(processInstanceId);
            runtimeVariable.setTaskInstanceId(-1);
            runtimeVariable.setVarName(Constants.SCHEDULE_INFO_VARIABLE);
            runtimeVariable.setVarValue(JSONUtils.toJson(new ScheduleInfo(scheduleType, tiggerTime)));
            runtimeVariableMapper.insert(runtimeVariable);
            return;
        }
        runtimeVariable.setVarValue(JSONUtils.toJson(new ScheduleInfo(scheduleType, tiggerTime)));
        runtimeVariableMapper.updateById(runtimeVariable);
    }
}
