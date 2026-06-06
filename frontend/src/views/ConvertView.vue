<template>
  <div class="convert-view">
    <div class="card">
      <div class="card-header d-flex justify-content-between align-items-center">
        <h5>小说转换</h5>
        <span class="badge bg-info">{{ content.length }} 字符</span>
      </div>
      <div class="card-body">
        <div class="mb-3">
          <label class="form-label">小说预览</label>
          <div class="novel-preview">
            {{ content }}
          </div>
        </div>
        
        <button 
          class="btn btn-primary btn-lg" 
          @click="startConversion"
          :disabled="isConverting"
        >
          {{ isConverting ? '转换中...' : '开始转换' }}
        </button>
        
        <div v-if="isConverting" class="mt-3">
          <div class="progress">
            <div 
              class="progress-bar progress-bar-striped progress-bar-animated bg-success" 
              role="progressbar" 
              :style="{ width: progress + '%' }"
            >
              {{ progress }}%
            </div>
          </div>
          <p class="mt-2 text-center text-muted">{{ statusMessage }}</p>
        </div>
        
        <div v-if="error" class="alert alert-danger mt-3">
          {{ error }}
        </div>
      </div>
    </div>
    
    <!-- 转换结果预览 -->
    <div v-if="script" class="card mt-3">
      <div class="card-header bg-success text-white">
        <h5>转换完成！</h5>
      </div>
      <div class="card-body">
        <div class="row">
          <div class="col-md-6">
            <h6>剧本信息</h6>
            <p><strong>标题：</strong>{{ script.meta?.title }}</p>
            <p><strong>作者：</strong>{{ script.meta?.author }}</p>
            <p><strong>人物数：</strong>{{ script.characters?.length || 0 }}</p>
            <p><strong>场景数：</strong>{{ script.scenes?.length || 0 }}</p>
          </div>
          <div class="col-md-6">
            <h6>操作</h6>
            <button class="btn btn-success me-2" @click="viewScript">
              查看剧本
            </button>
            <button class="btn btn-outline-primary" @click="proceedToQuality">
              质量评估
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { api } from '../services/api';

export default {
  name: 'ConvertView',
  props: {
    content: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      isConverting: false,
      progress: 0,
      statusMessage: '',
      error: '',
      script: null
    };
  },
  methods: {
    async startConversion() {
      this.error = '';
      this.isConverting = true;
      this.progress = 0;
      this.statusMessage = '开始分析小说...';
      
      try {
        this.progress = 20;
        this.statusMessage = '识别角色和对话...';
        
        this.progress = 50;
        this.statusMessage = '分割场景...';
        
        this.progress = 70;
        this.statusMessage = '标注情绪...';
        
        this.progress = 90;
        this.statusMessage = '生成剧本...';
        
        const result = await api.convertNovel(this.content);
        
        if (result.success) {
          this.progress = 100;
          this.statusMessage = '转换完成！';
          this.script = result.script;
        } else {
          throw new Error(result.message);
        }
      } catch (error) {
        this.error = error.message || '转换失败';
        this.progress = 0;
      } finally {
        setTimeout(() => {
          this.isConverting = false;
        }, 1500);
      }
    },
    
    viewScript() {
      // 可以打开一个新窗口或模态框显示完整的剧本
      console.log('查看剧本', this.script);
      alert('剧本查看功能开发中...\n\n角色数：' + (this.script.characters?.length || 0) + 
            '\n场景数：' + (this.script.scenes?.length || 0));
    },
    
    proceedToQuality() {
      this.$emit('conversion-complete', this.script);
    }
  }
};
</script>

<style scoped>
.convert-view {
  max-width: 900px;
  margin: 0 auto;
}

.novel-preview {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 15px;
  max-height: 400px;
  overflow-y: auto;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  white-space: pre-wrap;
}

.btn-lg {
  padding: 12px 30px;
  font-size: 18px;
}
</style>
