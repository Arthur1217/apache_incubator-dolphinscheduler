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
  import i18n from '@/module/i18n'
  import mDag from './_source/dag.vue'
  import { mapActions, mapMutations } from 'vuex'
  import mSpin from '@/module/components/spin/spin'
  import Affirm from './_source/jumpAffirm'
  import disabledState from '@/module/mixin/disabledState'

  export default {
    name: 'create-index',
    data () {
      return {
        // loading
        isLoading: true
      }
    },
    computed: {
      templateId() {
        return this.$route.query.processTemplateId;
      }
    },
    // mixins
    mixins: [disabledState],
    methods: {
      ...mapMutations('dag', ['resetParams', 'setTemplateId', 'setName', 'setDesc']),
      ...mapActions('dag', ['getProcessList', 'getTemplateDetails', 'getProjectList', 'getResourcesList','getResourcesListJar','getResourcesListJar']),
      ...mapActions('security', ['getTenantList','getWorkerGroupsAll']),
      _getTemplateDetailsOrNot() {
        if (this.templateId) {
          this.getTemplateDetails(this.templateId).then(res => {
            this.setTemplateId(res.id);
            this.setName('');
            this.setDesc('');
          });
        }
      },
      _showWarn() {
        this.$modal.dialog({
          closable: false,
          showMask: true,
          escClose: true,
          className: 'v-modal-custom',
          transitionName: 'opacityp',
          title: `<span style="color: red; font-weight: bold;">${i18n.$t('Warning')}</span>`,
          content: `${i18n.$t('Definition with template editing warning')}`,
          ok: {
            text: `${i18n.$t('Known')}`
          },
          cancel: {
            show: false
          }
        });
      },
      /**
       * init
       */
      init () {
        this.isLoading = true
        // Initialization parameters
        this.resetParams()
        // Promise Get node needs data
        Promise.all([
          // get process definition list
          this.getProcessList(),
          // get process template detail or not
          this._getTemplateDetailsOrNot(),
          // get project
          this.getProjectList(),
          // get jar
          this.getResourcesListJar(),
          this.getResourcesListJar('PYTHON'),
          // get resource
          this.getResourcesList(),
          // get worker group list
          this.getWorkerGroupsAll(),
          this.getTenantList()
        ]).then((data) => {
          this.isLoading = false
          // Whether to pop up the box?
          Affirm.init(this.$root)
          this.$nextTick(() => {
            if (this.templateId) {
              this._showWarn();
            }
          });
        }).catch(() => {
          this.isLoading = false
        })
      }
    },
    watch: {
      '$route': {
        deep: true,
        handler () {
          this.init()
        }
      }
    },
    created () {
      this.init()
    },
    mounted () {
    },
    components: { mDag, mSpin }
  }
</script>
