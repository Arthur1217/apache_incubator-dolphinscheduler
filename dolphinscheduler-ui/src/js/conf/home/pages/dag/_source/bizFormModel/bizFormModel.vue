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
<template>
  <div class="biz-form-model-model" v-clickoutside="_handleClose">
    <div class="title-box">
      <span class="name">{{ $t('Biz prop config') }} - [{{isGlobal ? $t('Process') : $t('Task')}} : {{ name }}]</span>
      <span class="go-back">
        <a href="javascript:" @click="close"><em class="ansicon ans-icon-arrow-to-left"></em><em>{{ $t('Return') }}</em></a>
      </span>
    </div>
    <div class="content-box">
      <iframe :src="fullBizFormUrl" frameborder="0" style="width: 100%; height: 100%"></iframe>
    </div>
  </div>
</template>
<script>
import _ from 'lodash'
import clickoutside from '@/module/util/clickoutside'
import disabledState from '@/module/mixin/disabledState'
import localStore from '@/module/util/localStorage'

export default {
  name: 'biz-form-model',
  data() {
    return {
      // node name
      name: '',
      // business form url
      bizFormUrl: '',
    }
  },
  /**
   * Click on events that are not generated internally by the component
   */
  directives: {clickoutside},
  mixins: [disabledState],
  props: {
    isGlobal: {
      type: Boolean,
      default: false
    },
    id: Number,
    self: Object,
  },
  methods: {
    /**
     * Click external to close the current component
     */
    _handleClose() {
      // do nothing
    },
    /**
     * Close and destroy component and component internal events
     */
    close() {
      this.$emit('close', {
        fromThis: this
      })
    }
  },
  created() {
    if (this.isGlobal) {
      this.name = this.store.state.dag.name;
      this.bizFormUrl = this.store.state.dag.bizFormUrl;
    } else {
      // Backfill data
      let taskList = this.store.state.dag.tasks

      //fillback use cacheTasks
      let cacheTasks = this.store.state.dag.cacheTasks
      let o = {}
      if (cacheTasks[this.id]) {
        o = cacheTasks[this.id]
      } else {
        if (taskList.length) {
          taskList.forEach(v => {
            if (v.id === this.id) {
              o = v
            }
          })
        }
      }
      // Non-null objects represent backfill
      if (!_.isEmpty(o)) {
        this.name = o.name
        this.bizFormUrl = o.bizFormUrl
      }
    }
  },
  computed: {
    fullBizFormUrl() {
      let appRootUrl = this.store.state.dag.appRootUrl;
      if (!appRootUrl) {
        appRootUrl = '';
      }
      let fullBizFormUrl = appRootUrl + this.bizFormUrl;
      let concat;
      if (-1 !== fullBizFormUrl.indexOf('?')) {
        concat = '&';
      } else {
        concat = '?'
      }
      let resultUrl = fullBizFormUrl + `${concat}processDefinitionId=${this.router.history.current.params.processId}`;
      if (!this.isGlobal) {
        resultUrl += `&taskDefinitionId=${this.id}`;
      }
      let bizPropConfigParam = JSON.parse(localStore.getItem('bizPropConfigParam'));
      let scheduleType = bizPropConfigParam.type;
      if ('START' === scheduleType) {
        resultUrl += '&scheduleType=START';
      } else {
        let schedule = JSON.parse(bizPropConfigParam.param.apiParams.schedule);
        let startTime = schedule.startTime ? encodeURIComponent(schedule.startTime) : '';
        let endTime = schedule.endTime ? encodeURIComponent(schedule.endTime) : '';
        let crontab = schedule.crontab ? encodeURIComponent(schedule.crontab) : '';
        resultUrl += `&scheduleType=TIMING&startTime=${startTime}&endTime=${endTime}&crontab=${crontab}`;
      }
      return resultUrl;
    }
  },
  components: {}
}
</script>

<style lang="scss" rel="stylesheet/scss">
@import "./bizFormModel";

.ans-radio-disabled {
  .ans-radio-inner:after {
    background-color: #6F8391
  }
}
</style>
