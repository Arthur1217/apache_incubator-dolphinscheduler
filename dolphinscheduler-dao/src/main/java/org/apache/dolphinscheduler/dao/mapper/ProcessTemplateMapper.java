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
package org.apache.dolphinscheduler.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.dolphinscheduler.dao.entity.ProcessTemplate;
import org.apache.dolphinscheduler.dao.entity.TemplateGroupByUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * process template mapper interface
 *
 * @author ArthurZhang
 * @since 2021-02-05
 */
public interface ProcessTemplateMapper extends BaseMapper<ProcessTemplate> {
    
    /**
     * verify process template by name
     *
     * @param projectId projectId
     * @param name      name
     * @return process template
     */
    ProcessTemplate verifyByTemplateName(@Param("projectId") int projectId,
                                         @Param("processTemplateName") String name);
    
    /**
     * query process template by name
     *
     * @param projectId projectId
     * @param name      name
     * @return process template
     */
    ProcessTemplate queryByTemplateName(@Param("projectId") int projectId,
                                        @Param("processTemplateName") String name);
    
    /**
     * query process template by id
     *
     * @param processTemplateId processTemplateId
     * @return process template
     */
    ProcessTemplate queryByTemplateId(@Param("processTemplateId") int processTemplateId);
    
    /**
     * process template page
     *
     * @param page      page
     * @param searchVal searchVal
     * @param userId    userId
     * @param projectId projectId
     * @param isAdmin   isAdmin
     * @return process template IPage
     */
    IPage<ProcessTemplate> queryTemplateListPaging(IPage<ProcessTemplate> page,
                                                   @Param("searchVal") String searchVal,
                                                   @Param("userId") int userId,
                                                   @Param("projectId") int projectId,
                                                   @Param("isAdmin") boolean isAdmin);
    
    /**
     * query all process template list
     *
     * @param projectId projectId
     * @return process template list
     */
    List<ProcessTemplate> queryAllTemplateList(@Param("projectId") int projectId);
    
    /**
     * query process template by ids
     *
     * @param ids ids
     * @return process template list
     */
    List<ProcessTemplate> queryTemplateListByIdList(@Param("ids") Integer[] ids);
    
    /**
     * query process template by tenant
     *
     * @param tenantId tenantId
     * @return process template list
     */
    List<ProcessTemplate> queryTemplateListByTenant(@Param("tenantId") int tenantId);
    
    /**
     * count process template group by user
     *
     * @param userId     userId
     * @param projectIds projectIds
     * @param isAdmin    isAdmin
     * @return process template list
     */
    List<TemplateGroupByUser> countTemplateGroupByUser(
            @Param("userId") Integer userId,
            @Param("projectIds") Integer[] projectIds,
            @Param("isAdmin") boolean isAdmin);
    
}
