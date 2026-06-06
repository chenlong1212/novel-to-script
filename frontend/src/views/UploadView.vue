<template>
  <div class="upload-view">
    <div class="card">
      <div class="card-header">
        <h5>上传小说</h5>
      </div>
      <div class="card-body">
        <div class="mb-3">
          <label for="novelFile" class="form-label">选择小说文件 (TXT格式)</label>
          <input 
            type="file" 
            class="form-control" 
            id="novelFile" 
            accept=".txt"
            @change="handleFileSelect"
          />
        </div>
        
        <div v-if="selectedFile" class="mb-3">
          <div class="alert alert-info">
            <strong>已选择文件：</strong> {{ selectedFile.name }}
          </div>
        </div>
        
        <div class="mb-3">
          <label for="novelContent" class="form-label">或者直接粘贴小说内容</label>
          <textarea 
            class="form-control" 
            id="novelContent" 
            rows="10"
            v-model="manualContent"
            placeholder="在这里粘贴小说内容..."
          ></textarea>
        </div>
        
        <button 
          class="btn btn-primary" 
          @click="handleUpload"
          :disabled="!canUpload"
        >
          {{ isLoading ? '处理中...' : '开始处理' }}
        </button>
        
        <div v-if="error" class="alert alert-danger mt-3">
          {{ error }}
        </div>
      </div>
    </div>
    
    <div v-if="isLoading" class="card mt-3">
      <div class="card-body">
        <div class="progress">
          <div 
            class="progress-bar progress-bar-striped progress-bar-animated" 
            role="progressbar" 
            :style="{ width: progress + '%' }"
          >
            {{ progress }}%
          </div>
        </div>
        <p class="mt-2 text-center">{{ statusMessage }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import { api } from '../services/api';

export default {
  name: 'UploadView',
  data() {
    return {
      selectedFile: null,
      manualContent: '',
      isLoading: false,
      progress: 0,
      statusMessage: '',
      error: ''
    };
  },
  computed: {
    canUpload() {
      return (this.selectedFile || this.manualContent.trim()) && !this.isLoading;
    }
  },
  methods: {
    handleFileSelect(event) {
      const file = event.target.files[0];
      if (file && file.type === 'text/plain') {
        this.selectedFile = file;
        this.error = '';
      } else {
        this.error = '请选择TXT格式的文件';
        this.selectedFile = null;
      }
    },
    
    async handleUpload() {
      this.error = '';
      this.isLoading = true;
      this.progress = 0;
      this.statusMessage = '准备上传...';
      
      try {
        if (this.manualContent.trim()) {
          // 直接使用手动输入的内容
          this.progress = 100;
          this.statusMessage = '内容已准备好';
          
          this.$emit('content-loaded', {
            content: this.manualContent,
            fileId: 'manual-' + Date.now()
          });
        } else if (this.selectedFile) {
          // 上传文件
          this.statusMessage = '正在上传文件...';
          this.progress = 30;
          
          const result = await api.uploadNovel(this.selectedFile);
          
          if (result.success) {
            this.progress = 60;
            this.statusMessage = '正在读取文件内容...';
            
            const contentResult = await api.getNovelContent(result.fileId);
            
            if (contentResult.success) {
              this.progress = 100;
              this.statusMessage = '完成';
              
              this.$emit('content-loaded', {
                content: contentResult.content,
                fileId: result.fileId
              });
            } else {
              throw new Error(contentResult.message);
            }
          } else {
            throw new Error(result.message);
          }
        }
      } catch (error) {
        this.error = error.message || '处理失败';
        this.progress = 0;
      } finally {
        setTimeout(() => {
          this.isLoading = false;
        }, 1000);
      }
    }
  }
};
</script>

<style scoped>
.upload-view {
  max-width: 800px;
  margin: 0 auto;
}

textarea {
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.alert {
  margin-top: 10px;
}
</style>
