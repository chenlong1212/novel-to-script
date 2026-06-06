<template>
  <div class="app-container">
    <!-- 顶部标题 -->
    <header class="app-header">
      <h1>📖 小说转剧本工具</h1>
      <span class="version-tag">v1.1</span>
    </header>

    <!-- 主要内容 -->
    <main class="main-content">
      <!-- 左侧：上传区 -->
      <div class="upload-section">
        <div class="card">
          <h2>1. 上传小说</h2>
          
          <div class="upload-area" 
               :class="{ 'drag-over': isDragOver }"
               @dragover.prevent="handleDragOver"
               @dragleave.prevent="isDragOver = false"
               @drop.prevent="handleDrop"
               @click="$refs.fileInput.click"
          >
            <div class="upload-icon">📄</div>
            <p>拖拽文件到这里或点击上传</p>
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

          <!-- 小说预览 -->
          <div v-if="novelContent" class="preview-area">
            <h3>小说预览</h3>
            <div class="novel-text">{{ novelContent }}</div>
          </div>

          <!-- 转换按钮 -->
          <button 
            class="btn-convert"
            :disabled="!selectedFile || isConverting"
            @click="startConversion"
          >
            {{ isConverting ? '🔄 转换中...' : '✨ 开始转换' }}
          </button>
        </div>
      </div>

      <!-- 右侧：结果区 -->
      <div class="result-section">
        <div class="card">
          <h2>2. 转换结果</h2>

          <!-- 状态提示 -->
          <div v-if="isConverting" class="status-info converting">
            <div class="spinner"></div>
            <p>{{ statusMessage }}</p>
          </div>

          <div v-else-if="hasError" class="status-info error">
            <p>❌ 转换失败：{{ errorMessage }}</p>
            <button class="btn-retry" @click="reset">重试</button>
          </div>

          <!-- 转换成功结果 -->
          <div v-else-if="script" class="script-output">
            <!-- 剧本信息 -->
            <div class="script-info">
              <div class="info-item">
                <span class="label">标题</span>
                <span class="value">{{ script.meta?.title || '未命名' }}</span>
              </div>
              <div class="info-item">
                <span class="label">人物</span>
                <span class="value">{{ script.characters?.length || 0 }} 个</span>
              </div>
              <div class="info-item">
                <span class="label">场景</span>
                <span class="value">{{ script.scenes?.length || 0 }} 个</span>
              </div>
            </div>

            <!-- 人物列表 -->
            <div v-if="script.characters && script.characters.length > 0" class="section">
              <h3>👥 人物列表</h3>
              <div class="character-list">
                <span 
                  v-for="char in script.characters" 
                  :key="char.id"
                  class="character-badge"
                >
                  {{ char.name }}
                </span>
              </div>
            </div>

            <!-- 剧本内容 -->
            <div v-if="script.scenes && script.scenes.length > 0" class="section">
              <h3>🎬 剧本内容</h3>
              <div 
                v-for="scene in script.scenes" 
                :key="scene.id"
                class="scene-block"
              >
                <div class="scene-header">
                  场景 {{ scene.number }} - {{ scene.location?.name || '未知地点' }}
                </div>
                <div class="scene-content">
                  <div 
                    v-for="beat in scene.beats" 
                    :key="beat.id"
                    class="beat-line"
                    :class="beat.type"
                  >
                    <span class="beat-tag">[{{ getBeatTypeName(beat.type) }}]</span>
                    <span v-if="beat.speaker" class="speaker">{{ beat.speaker }}：</span>
                    <span class="content">{{ beat.content }}</span>
                    <span v-if="beat.emotion" class="emotion">({{ beat.emotion }})</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="actions">
              <button class="btn-copy" @click="copyScript">📋 复制剧本</button>
              <button class="btn-reset" @click="reset">🔄 重新转换</button>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-else class="empty-state">
            <div class="empty-icon">📝</div>
            <p>请上传小说开始转换</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import { api } from './services/api';

export default {
  name: 'App',
  data() {
    return {
      selectedFile: null,
      novelContent: '',
      script: null,
      isConverting: false,
      isDragOver: false,
      hasError: false,
      errorMessage: '',
      statusMessage: ''
    };
  },
  methods: {
    handleDragOver() {
      this.isDragOver = true;
    },
    handleDrop(e) {
      this.isDragOver = false;
      const file = e.dataTransfer.files[0];
      if (file && file.name.endsWith('.txt')) {
        this.loadFile(file);
      }
    },
    handleFileSelect(e) {
      const file = e.target.files[0];
      if (file) {
        this.loadFile(file);
      }
    },
    loadFile(file) {
      this.selectedFile = file;
      const reader = new FileReader();
      reader.onload = (e) => {
        this.novelContent = e.target.result;
      };
      reader.readAsText(file, 'UTF-8');
    },
    removeFile() {
      this.selectedFile = null;
      this.novelContent = '';
      this.$refs.fileInput.value = '';
    },
    async startConversion() {
      if (!this.novelContent) return;

      this.isConverting = true;
      this.hasError = false;
      this.errorMessage = '';
      this.script = null;
      this.statusMessage = '正在分析小说...';

      try {
        // 直接调用后端转换
        const result = await api.convertNovel(this.novelContent);
        
        if (result.success) {
          this.script = result.script;
          this.statusMessage = '转换完成！';
        } else {
          throw new Error(result.message || '转换失败');
        }
      } catch (error) {
        this.hasError = true;
        this.errorMessage = error.message || '网络错误';
        console.error('转换失败:', error);
      } finally {
        this.isConverting = false;
      }
    },
    getBeatTypeName(type) {
      const names = {
        'dialogue': '对话',
        'action': '动作',
        'narration': '旁白',
        'transition': '转场'
      };
      return names[type] || type;
    },
    copyScript() {
      let text = this.formatScriptAsText(this.script);
      navigator.clipboard.writeText(text).then(() => {
        alert('已复制到剪贴板！');
      }).catch(() => {
        alert('复制失败，请手动复制');
      });
    },
    formatScriptAsText(script) {
      let output = '';
      
      // 标题
      output += `剧本标题：${script.meta?.title || '未命名'}\n`;
      output += '='.repeat(50) + '\n\n';

      // 人物
      if (script.characters && script.characters.length > 0) {
        output += '人物表：\n';
        script.characters.forEach(char => {
          output += `  - ${char.name}`;
          if (char.role) output += ` (${char.role})`;
          output += '\n';
        });
        output += '\n';
      }

      // 场景
      if (script.scenes && script.scenes.length > 0) {
        script.scenes.forEach(scene => {
          output += `【场景 ${scene.number}】 ${scene.location?.name || '未知地点'}\n`;
          output += '-'.repeat(50) + '\n';
          
          if (scene.beats && scene.beats.length > 0) {
            scene.beats.forEach(beat => {
              output += `[${this.getBeatTypeName(beat.type)}] `;
              if (beat.speaker) {
                output += `${beat.speaker}：`;
              }
              output += beat.content;
              if (beat.emotion) {
                output += ` (${beat.emotion})`;
              }
              output += '\n';
            });
          }
          output += '\n';
        });
      }

      return output;
    },
    reset() {
      this.script = null;
      this.hasError = false;
      this.errorMessage = '';
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
  font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif;
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
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
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

/* 上传区 */
.upload-area {
  border: 3px dashed #ddd;
  border-radius: 12px;
  padding: 60px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #f9f9f9;
}

.upload-area:hover {
  border-color: #667eea;
  background: #f0f4ff;
}

.upload-area.drag-over {
  border-color: #667eea;
  background: #e8eeff;
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

.preview-area {
  margin-top: 20px;
}

.preview-area h3 {
  font-size: 1rem;
  color: #333;
  margin-bottom: 10px;
}

.novel-text {
  max-height: 300px;
  overflow-y: auto;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  line-height: 1.8;
  white-space: pre-wrap;
  color: #333;
}

.btn-convert {
  width: 100%;
  margin-top: 25px;
  padding: 18px;
  font-size: 1.2rem;
  font-weight: bold;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-convert:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
}

.btn-convert:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 结果区 */
.status-info {
  text-align: center;
  padding: 40px;
}

.status-info.converting {
  color: #667eea;
}

.status-info.error {
  color: #ff6b6b;
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

.btn-retry {
  margin-top: 20px;
  padding: 10px 30px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #999;
}

.empty-icon {
  font-size: 5rem;
  margin-bottom: 20px;
}

.script-info {
  display: flex;
  gap: 30px;
  margin-bottom: 25px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 10px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-item .label {
  font-size: 0.85rem;
  color: #666;
}

.info-item .value {
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
}

.section {
  margin-bottom: 25px;
}

.section h3 {
  font-size: 1.1rem;
  color: #333;
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.character-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.character-badge {
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-size: 0.95rem;
  font-weight: 500;
}

.scene-block {
  border: 2px solid #e0e0e0;
  border-radius: 10px;
  margin-bottom: 15px;
  overflow: hidden;
}

.scene-header {
  padding: 12px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
  font-size: 1.05rem;
}

.scene-content {
  padding: 15px 20px;
}

.beat-line {
  padding: 10px 0;
  line-height: 1.6;
  border-bottom: 1px dashed #eee;
}

.beat-line:last-child {
  border-bottom: none;
}

.beat-tag {
  font-weight: bold;
  color: #667eea;
  margin-right: 8px;
  font-size: 0.85rem;
}

.speaker {
  font-weight: bold;
  color: #333;
}

.beat-line.dialogue {
  background: #f0f4ff;
  margin: 0 -20px;
  padding: 10px 20px;
}

.beat-line.action {
  background: #f0fff4;
  margin: 0 -20px;
  padding: 10px 20px;
}

.beat-line.narration {
  background: #fff8e6;
  margin: 0 -20px;
  padding: 10px 20px;
}

.emotion {
  color: #999;
  font-size: 0.85rem;
  margin-left: 8px;
}

.actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.btn-copy, .btn-reset {
  flex: 1;
  padding: 14px;
  font-size: 1rem;
  font-weight: bold;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.3s;
}

.btn-copy {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.btn-copy:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(17, 153, 142, 0.4);
}

.btn-reset {
  background: #f0f0f0;
  color: #333;
}

.btn-reset:hover {
  background: #e0e0e0;
}

/* 响应式 */
@media (max-width: 968px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}
</style>
