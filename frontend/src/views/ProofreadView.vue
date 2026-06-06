<template>
  <div class="proofread-view">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5>人工校对</h5>
        <span class="badge bg-primary">{{ corrections.length }} 条修正</span>
      </div>
      <div class="card-body">
        <!-- 修正表单 -->
        <div class="correction-form mb-4">
          <h6>添加新修正</h6>
          <div class="row">
            <div class="col-md-3">
              <select v-model="newCorrection.type" class="form-select">
                <option value="CHARACTER">人物</option>
                <option value="DIALOGUE">对话</option>
                <option value="SCENE">场景</option>
                <option value="EMOTION">情绪</option>
                <option value="METADATA">元数据</option>
              </select>
            </div>
            <div class="col-md-3">
              <input v-model="newCorrection.targetId" type="text" class="form-control" placeholder="目标ID">
            </div>
            <div class="col-md-3">
              <input v-model="newCorrection.field" type="text" class="form-control" placeholder="字段">
            </div>
            <div class="col-md-3">
              <input v-model="newCorrection.correctedValue" type="text" class="form-control" placeholder="修正值">
            </div>
          </div>
          <div class="row mt-2">
            <div class="col-md-12">
              <input v-model="newCorrection.reason" type="text" class="form-control" placeholder="修正原因">
            </div>
          </div>
          <button class="btn btn-primary mt-2" @click="addCorrection">
            添加修正
          </button>
        </div>
        
        <!-- 修正列表 -->
        <div v-if="corrections.length > 0" class="corrections-list">
          <h6>修正列表</h6>
          <div class="list-group">
            <div 
              v-for="correction in corrections" 
              :key="correction.correctionId"
              class="list-group-item"
            >
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <span :class="'badge bg-' + getTypeColor(correction.type)">
                    {{ getTypeName(correction.type) }}
                  </span>
                  <strong class="ms-2">{{ correction.targetId }}</strong>
                  <span class="text-muted ms-2">{{ correction.field }}: {{ correction.correctedValue }}</span>
                </div>
                <div>
                  <span :class="'badge bg-' + getStatusColor(correction.status)">
                    {{ correction.status }}
                  </span>
                  <button 
                    v-if="correction.status === 'PENDING'"
                    class="btn btn-sm btn-success ms-2"
                    @click="applyCorrection(correction)"
                  >
                    应用
                  </button>
                  <button 
                    class="btn btn-sm btn-outline-danger ms-2"
                    @click="rejectCorrection(correction)"
                  >
                    拒绝
                  </button>
                </div>
              </div>
              <small v-if="correction.reason" class="text-muted">原因: {{ correction.reason }}</small>
            </div>
          </div>
        </div>
        
        <div v-else class="text-center text-muted">
          <p>暂无修正记录</p>
        </div>
        
        <!-- 操作按钮 -->
        <div class="mt-4 text-center">
          <button class="btn btn-success btn-lg me-2" @click="applyAllCorrections">
            应用所有修正
          </button>
          <button class="btn btn-outline-secondary" @click="finishProofreading">
            完成校对
          </button>
        </div>
      </div>
    </div>
    
    <!-- 剧本预览 -->
    <div class="card mt-3">
      <div class="card-header">
        <h5>剧本预览</h5>
      </div>
      <div class="card-body">
        <div class="script-preview">
          <pre>{{ formattedScript }}</pre>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ProofreadView',
  props: {
    script: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      corrections: [],
      newCorrection: {
        type: 'CHARACTER',
        targetId: '',
        field: '',
        correctedValue: '',
        reason: ''
      }
    };
  },
  computed: {
    formattedScript() {
      return JSON.stringify(this.script, null, 2);
    }
  },
  methods: {
    addCorrection() {
      if (!this.newCorrection.targetId || !this.newCorrection.field) {
        alert('请填写目标ID和字段');
        return;
      }
      
      const correction = {
        correctionId: 'corr_' + Date.now(),
        scriptId: this.script.meta?.title || 'unknown',
        type: this.newCorrection.type,
        targetId: this.newCorrection.targetId,
        field: this.newCorrection.field,
        originalValue: '',
        correctedValue: this.newCorrection.correctedValue,
        reason: this.newCorrection.reason,
        status: 'PENDING',
        createdAt: new Date().toISOString()
      };
      
      this.corrections.push(correction);
      
      // 重置表单
      this.newCorrection = {
        type: 'CHARACTER',
        targetId: '',
        field: '',
        correctedValue: '',
        reason: ''
      };
    },
    
    applyCorrection(correction) {
      correction.status = 'APPLIED';
      // 实际应该调用API应用修正
      alert(`已应用修正: ${correction.targetId} - ${correction.field}`);
    },
    
    rejectCorrection(correction) {
      const reason = prompt('请输入拒绝原因:');
      if (reason) {
        correction.status = 'REJECTED';
        correction.reason = reason;
      }
    },
    
    applyAllCorrections() {
      const pendingCorrections = this.corrections.filter(c => c.status === 'PENDING');
      if (pendingCorrections.length === 0) {
        alert('没有待应用的修正');
        return;
      }
      
      if (confirm(`确定要应用所有 ${pendingCorrections.length} 条修正吗?`)) {
        pendingCorrections.forEach(c => {
          c.status = 'APPLIED';
        });
        alert('所有修正已应用');
      }
    },
    
    finishProofreading() {
      const appliedCount = this.corrections.filter(c => c.status === 'APPLIED').length;
      alert(`校对完成！\n共 ${this.corrections.length} 条修正\n已应用 ${appliedCount} 条`);
    },
    
    getTypeName(type) {
      const names = {
        CHARACTER: '人物',
        DIALOGUE: '对话',
        SCENE: '场景',
        EMOTION: '情绪',
        METADATA: '元数据'
      };
      return names[type] || type;
    },
    
    getTypeColor(type) {
      const colors = {
        CHARACTER: 'primary',
        DIALOGUE: 'success',
        SCENE: 'info',
        EMOTION: 'warning',
        METADATA: 'secondary'
      };
      return colors[type] || 'secondary';
    },
    
    getStatusColor(status) {
      const colors = {
        PENDING: 'warning',
        APPLIED: 'success',
        REJECTED: 'danger'
      };
      return colors[status] || 'secondary';
    }
  }
};
</script>

<style scoped>
.proofread-view {
  max-width: 1000px;
  margin: 0 auto;
}

.correction-form {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.script-preview {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 15px;
  max-height: 500px;
  overflow-y: auto;
}

.script-preview pre {
  font-family: 'Courier New', monospace;
  font-size: 13px;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.btn-lg {
  padding: 12px 30px;
}
</style>
