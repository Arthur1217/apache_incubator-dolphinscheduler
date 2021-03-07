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
  <div class="home-main index-model">
    <m-dag v-if="!isLoading"></m-dag>
    <m-spin :is-spin="isLoading"></m-spin>
  </div>
</template>
<script>
import _ from 'lodash';
import mDag from './_source/dag.vue';
import mSpin from '@/module/components/spin/spin';
import disabledState from '@/module/mixin/disabledState';
import {mapActions, mapMutations, mapState} from 'vuex';

export default {
  name: 'form-flow',
  data() {
    return {
      // loading
      isLoading: true,
    }
  },
  computed: {
    ...mapState('dag', ['bizPropConfigParam']),
  },
  components: {mDag, mSpin},
  mixins: [disabledState],
  props: {},
  methods: {
    ...mapMutations('dag', ['resetParams', 'setIsDetails', 'setIsFormFlow']),
    ...mapActions('dag', ['getProcessList', 'getProjectList', 'getResourcesList', 'getProcessDetails', 'getResourcesListJar']),
    ...mapActions('security', ['getTenantList', 'getWorkerGroupsAll']),
    init() {
      this.isLoading = true;
      this.resetParams();
      this.setIsDetails(true);
      this.setIsFormFlow(true);
      Promise.all([
        // Node details
        this.getProcessDetails(this.$route.params.processId),
        // get process definition
        this.getProcessList(),
        // get project
        this.getProjectList(),
        // get resource
        this.getResourcesList(),
        // get jar
        this.getResourcesListJar(),
        this.getResourcesListJar('PYTHON'),
        // get worker group list
        this.getWorkerGroupsAll(),
        this.getTenantList()
      ]).then((data) => {
        this.isLoading = false
      }).catch(() => {
        this.isLoading = false
      });
    },
  },
  watch: {
    '$route': {
      deep: true,
      handler() {
        this.init();
      }
    }
  },
  created() {
    if (_.isEmpty(this.bizPropConfigParam)) {
      this.$router.push({name: 'projects-definition-list'});
    } else {
      this.init();
    }
  },
  mounted() {
  },
}
</script>
