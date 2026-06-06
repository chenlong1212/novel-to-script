<template>
  <div class="app-container">
    <!-- 头部 -->
    <header class="app-header">
      <h1>📖 AI小说转剧本工具</h1>
      <span class="version-tag">v1.2</span>
    </header>

    <!-- 主要内容区 -->
    <main class="main-content">
      <!-- 左侧：上传区 -->
      <div class="upload-section">
        <div class="card">
          <h2>📄 上传小说</h2>
          
          <!-- 拖拽区域 -->
          <div 
            class="upload-area" 
            :class="{ 'drag-over': isDragOver }"
            @dragover.prevent="handleDragOver"
            @dragleave.prevent="handleDragLeave"
            @drop.prevent="handleDrop"
            @click="triggerFileSelect"
          >
            <div class="upload-icon">📂</div>
            <p>拖拽文件到这里，或点击选择</p>
            <p class="file-hint">支持 .txt 格式</p>
            <input 
              ref="fileInput"
              type="file" 
              accept=".txt" 
              @change="handleFileSelect"
              style="display: none;"
            >
          </div>

          <!-- 已选择的文件 -->
          <div v-if="selectedFile" class="selected-file">
            <div class="file-info">
              <span class="file-name">{{ selectedFile.name }}</span>
              <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
            </div>
            <button class="btn-remove" @click="removeFile">✕</button>
          </div>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <button 
              class="btn-convert" 
              :disabled="!selectedFile || isLoading"
              @click="convertToYaml"
            >
              {{ isLoading ? '⏳ 转换中...' : '✨ 转换为剧本' }}
            </button>
            <button 
              class="btn-download"
              :disabled="!yamlContent"
              @click="downloadYaml"
            >
              📥 下载YAML
            </button>
          </div>
        </div>
      </div>

      <!-- 右侧：结果区 -->
      <div class="result-section">
        <div class="card">
          <h2>📝 转换结果 (YAML)</h2>
          
          <!-- 加载状态 -->
          <div v-if="isLoading" class="loading-state">
            <div class="spinner"></div>
            <p>正在分析小说内容，请稍候...</p>
          </div>

          <!-- 错误状态 -->
          <div v-else-if="hasError" class="error-state">
            <p>❌ 转换失败：{{ errorMessage }}</p>
            <button class="btn-retry" @click="reset">重试</button>
          </div>

          <!-- YAML内容展示 -->
          <div v-else-if="yamlContent" class="yaml-display">
            <textarea 
              v-model="yamlContent" 
              readonly
              class="yaml-textarea"
              spellcheck="false"
            ></textarea>
          </div>

          <!-- 空状态 -->
          <div v-else class="empty-state">
            <div class="empty-icon">📝</div>
            <p>上传小说后点击转换，结果将在这里显示</p>
          </div>
        </div>
      </div>
    </main>

    <!-- 页脚 -->
    <footer class="app-footer">
      <p>使用符合规范的 YAML Schema 格式输出</p>
    </footer>
  </div>
</template>

<script>
export default {
  name: 'App',
  data() {
    return {
      selectedFile: null,
      yamlContent: '',
      isLoading: false,
      isDragOver: false,
      hasError: false,
      errorMessage: '',
      originalFileName: ''
    };
  },
  methods: {
    triggerFileSelect() {
      this.$refs.fileInput.click();
    },
    handleDragOver() {
      this.isDragOver = true;
    },
    handleDragLeave() {
      this.isDragOver = false;
    },
    handleDrop(event) {
      this.isDragOver = false;
      const files = event.dataTransfer.files;
      if (files.length > 0 && files[0].name.endsWith('.txt')) {
        this.selectedFile = files[0];
      }
    },
    handleFileSelect(event) {
      const file = event.target.files[0];
      if (file) {
        this.selectedFile = file;
      }
    },
    removeFile() {
      this.selectedFile = null;
      this.yamlContent = '';
      this.hasError = false;
      this.errorMessage = '';
      if (this.$refs.fileInput) {
        this.$refs.fileInput.value = '';
      }
    },
    async convertToYaml() {
      if (!this.selectedFile) {
        return;
      }
      
      this.isLoading = true;
      this.hasError = false;
      this.errorMessage = '';
      this.yamlContent = '';
      this.originalFileName = this.selectedFile.name;
      
      const formData = new FormData();
      formData.append('file', this.selectedFile);
      
      try {
        const response = await fetch('http://localhost:8080/api/v1/convert/yaml', {
          method: 'POST',
          body: formData
        });
        
        if (!response.ok) {
          const errorText = await response.text();
          throw new Error(errorText || '转换失败');
        }
        
        const yaml = await response.text();
        this.yamlContent = yaml;
        
        console.log('转换成功');
        
      } catch (error) {
        console.error('转换失败:', error);
        this.hasError = true;
        this.errorMessage = error.message || '网络错误';
      } finally {
        this.isLoading = false;
      }
    },
    downloadYaml() {
      if (!this.yamlContent) {
        return;
      }
      
      // 生成文件名
      let fileName = 'script_converted.yaml';
      if (this.originalFileName) {
        const baseName = this.originalFileName.replace('.txt', '');
        fileName = baseName + '_converted.yaml';
      }
      
      // 创建下载
      const blob = new Blob([this.yamlContent], { type: 'application/x-yaml;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    },
    reset() {
      this.removeFile();
    },
    formatFileSize(bytes) {
      if (bytes < 1024) return bytes + ' B';
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + ' KB';
      return (bytes / (1024 * 1024)).toFixed(2) + ' MB';
    }
  }
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
  padding: 20px;
}

.app-container {
  max-width: 1400px;
  margin: 0 auto;
}

.app-header {
  text-align: center;
  margin-bottom: 30px;
  color: white;
}

.app-header h1 {
  font-size: 2.5rem;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
}

.version-tag {
  display: inline-block;
  background: rgba(255, 255, 255, 0.2);
  padding: 5px 15px;
  border-radius: 20px;
  font-size: 0.9rem;
}

.main-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.card h2 {
  font-size: 1.5rem;
  color: #333;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 3px solid #667eea;
  display: inline-block;
}

.upload-area {
  border: 3px dashed #ddd;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #f9f9f9;
}

.upload-area:hover,
.upload-area.drag-over {
  border-color: #667eea;
  background: #f0f4ff;
}

.upload-area.drag-over {
  transform: scale(1.02);
}

.upload-icon {
  font-size: 4rem;
  margin-bottom: 15px;
}

.upload-area p {
  color: #666;
  font-size: 1.1rem;
  margin-bottom: 5px;
}

.file-hint {
  font-size: 0.9rem;
  color: #999;
}

.selected-file {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #e8eeff;
  border-radius: 8px;
  margin-top: 20px;
}

.file-info {
  display: flex;
  gap: 15px;
  align-items: center;
}

.file-name {
  font-weight: bold;
  color: #333;
}

.file-size {
  color: #666;
  font-size: 0.9rem;
}

.btn-remove {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #ff6b6b;
  transition: 0.3s;
}

.btn-remove:hover {
  transform: scale(1.2);
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 25px;
}

.btn-convert,
.btn-download {
  flex: 1;
  padding: 14px;
  font-size: 1rem;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-convert {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-convert:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

.btn-convert:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-download {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.btn-download:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(17, 153, 142, 0.4);
}

.btn-download:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.yaml-display {
  margin-top: 10px;
}

.yaml-textarea {
  width: 100%;
  min-height: 500px;
  padding: 15px;
  font-family: 'Courier New', Courier, monospace;
  font-size: 0.95rem;
  line-height: 1.6;
  border: 1px solid #ddd;
  border-radius: 8px;
  resize: vertical;
  background: #fafafa;
  color: #333;
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 4rem;
  margin-bottom: 15px;
  opacity: 0.5;
}

.error-state p {
  color: #ff6b6b;
  font-size: 1.1rem;
  margin-bottom: 20px;
}

.btn-retry {
  padding: 10px 30px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
}

.app-footer {
  text-align: center;
  margin-top: 30px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 0.9rem;
}

@media (max-width: 968px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}
</style>
