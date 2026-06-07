<template>
  <div class="app-container">
    <!-- Header -->
    <header class="app-header">
        <div class="header-content">
            <h1>
                <span class="icon">📖</span>
                AI小说转剧本工具
            </h1>
            <span class="version-badge">v1.4</span>
        </div>
    </header>

    <!-- Main Content -->
    <main class="main-content">
      <!-- Left: Upload Section -->
      <section class="upload-section">
        <div class="card">
          <h2 class="card-title">
            <span class="title-icon">📁</span>
            上传小说文件
          </h2>

          <!-- Upload Area -->
          <div
            class="upload-area"
            :class="{ 'drag-over': isDragOver }"
            @dragover.prevent="handleDragOver"
            @dragleave.prevent="handleDragLeave"
            @drop.prevent="handleDrop"
            @click="triggerFileSelect"
          >
            <div class="upload-visual">
              <div class="upload-icon">
                <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
                  <polyline points="7 10 12 15 17 10"/>
                  <line x1="12" y1="15" x2="12" y2="3"/>
                </svg>
              </div>
              <p class="upload-text">拖拽文件到这里，或点击选择</p>
              <p class="upload-hint">仅支持 .txt 格式文件</p>
            </div>
            <input
              ref="fileInput"
              type="file"
              accept=".txt"
              @change="handleFileSelect"
              style="display: none"
            />
          </div>

          <!-- Selected File -->
          <div v-if="selectedFile" class="selected-file">
            <div class="file-info">
              <span class="file-icon">📄</span>
              <span class="file-name">{{ selectedFile.name }}</span>
              <span class="file-size">{{ formatFileSize(selectedFile.size) }}</span>
            </div>
            <button class="btn-clear" @click="clearFile">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <line x1="18" y1="6" x2="6" y2="18"/>
                <line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          </div>

          <!-- Action Buttons -->
          <div class="button-group">
            <button
              class="btn-primary"
              :disabled="!selectedFile || isLoading"
              @click="handleConvert"
            >
              <span v-if="!isLoading">✨ 开始转换</span>
              <span v-else class="loading-text">
                <span class="spinner-dot"></span>
                <span class="spinner-dot"></span>
                <span class="spinner-dot"></span>
                转换中
              </span>
            </button>
            <button
              class="btn-outline"
              :disabled="!yamlContent"
              @click="handleDownload"
            >
              📥 下载 YAML
            </button>
          </div>

          <!-- Quick Tips -->
          <div class="tips-box">
            <div class="tips-header">💡 快速提示</div>
            <ul class="tips-list">
              <li>推荐使用 UTF-8 编码的 txt 文件</li>
              <li>确保小说内容排版清晰，对话完整</li>
              <li>转换过程不会上传或保存你的小说</li>
            </ul>
          </div>
        </div>
      </section>

      <!-- Right: Results Section -->
      <section class="result-section">
        <div class="card results-card">
          <!-- Header -->
          <div class="results-header">
            <h2 class="card-title">
              <span class="title-icon">📝</span>
              转换结果
            </h2>
            
            <!-- Stats -->
            <div v-if="script" class="stats-row">
              <div class="stat-item">
                <span class="stat-value">{{ script.meta?.total_scenes || 0 }}</span>
                <span class="stat-label">场景</span>
              </div>
              <div class="stat-item">
                <span class="stat-value">{{ script.characters?.length || 0 }}</span>
                <span class="stat-label">人物</span>
              </div>
            </div>
          </div>

          <!-- Tabs -->
          <div v-if="script" class="tabs-container">
            <div class="tabs">
              <button
                v-for="tab in tabs"
                :key="tab.id"
                class="tab-button"
                :class="{ active: activeTab === tab.id }"
                @click="activeTab = tab.id"
              >
                <span class="tab-icon">{{ tab.icon }}</span>
                {{ tab.label }}
              </button>
            </div>

            <!-- Tab Content -->
            <div class="tab-content">
              <!-- Overview -->
              <div v-if="activeTab === 'overview'" class="overview-content">
                <div class="meta-box">
                  <div class="meta-row">
                    <span class="meta-label">标题</span>
                    <span class="meta-value">{{ script.meta?.title || '未命名' }}</span>
                  </div>
                  <div class="meta-row">
                    <span class="meta-label">来源</span>
                    <span class="meta-value">{{ originalFileName }}</span>
                  </div>
                  <div class="meta-row">
                    <span class="meta-label">转换时间</span>
                    <span class="meta-value">{{ formatDate() }}</span>
                  </div>
                </div>

                <!-- Characters -->
                <div v-if="script.characters && script.characters.length > 0" class="section-box">
                  <h3 class="section-title">👥 出场人物</h3>
                  <div class="characters-grid">
                    <div
                      v-for="(char, index) in script.characters"
                      :key="char.id"
                      class="character-card"
                    >
                      <div class="char-avatar">
                        <span>{{ (char.name || '?')[0] }}</span>
                      </div>
                      <div class="char-info">
                        <div class="char-name">{{ char.name }}</div>
                        <div v-if="char.role" class="char-role">
                          {{ getRoleLabel(char.role) }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Scenes -->
              <div v-if="activeTab === 'scenes'" class="scenes-content">
                <div
                  v-for="(scene, index) in script.scenes"
                  :key="scene.id"
                  class="scene-card"
                >
                  <div class="scene-header">
                    <div class="scene-number">
                      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                        <rect x="3" y="3" width="18" height="18" rx="2"/>
                        <circle cx="8.5" cy="8.5" r="1.5"/>
                        <polyline points="21 15 16 10 5 21"/>
                      </svg>
                    </div>
                    <div class="scene-title">
                      场景 {{ scene.number }}
                      <span v-if="scene.location?.name" class="scene-location">
                        {{ scene.location.name }}
                      </span>
                    </div>
                  </div>

                  <div class="beats-list">
                    <div
                      v-for="beat in scene.beats"
                      :key="beat.id"
                      class="beat-item"
                      :class="beat.type"
                    >
                      <span class="beat-tag">{{ getBeatLabel(beat.type) }}</span>
                      <span v-if="beat.speaker" class="beat-speaker">
                        {{ beat.speaker }}：
                      </span>
                      <span class="beat-content">{{ beat.content }}</span>
                      <span v-if="beat.emotion && beat.emotion !== 'neutral'" class="beat-emotion">
                        ({{ getEmotionLabel(beat.emotion) }})
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              <!-- YAML -->
              <div v-if="activeTab === 'yaml'" class="yaml-content">
                <div class="yaml-header">
                  <span>YAML 源代码</span>
                  <button class="btn-copy-yaml" @click="copyYaml">
                    📋 复制
                  </button>
                </div>
                <textarea
                  :value="yamlContent"
                  readonly
                  class="yaml-textarea"
                ></textarea>
              </div>
            </div>
          </div>

          <!-- Loading -->
          <div v-else-if="isLoading" class="loading-container">
            <div class="loading-spinner"></div>
            <p class="loading-text">正在分析小说内容...</p>
            <p class="loading-subtext">请稍候，这可能需要几秒钟</p>
          </div>

          <!-- Error -->
          <div v-else-if="hasError" class="error-container">
            <div class="error-icon">⚠️</div>
            <p class="error-text">{{ errorMessage }}</p>
            <button class="btn-retry" @click="resetState">重新尝试</button>
          </div>

          <!-- Empty -->
          <div v-else class="empty-container">
            <div class="empty-icon">
              <svg width="96" height="96" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
                <polyline points="10 9 9 9 8 9"/>
              </svg>
            </div>
            <p class="empty-title">还没有转换结果</p>
            <p class="empty-subtitle">上传小说文件，点击开始转换</p>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
export default {
  name: "App",
  data() {
    return {
      activeTab: "overview",
      selectedFile: null,
      script: null,
      yamlContent: "",
      originalFileName: "",
      isLoading: false,
      isDragOver: false,
      hasError: false,
      errorMessage: "",
      tabs: [
        { id: "overview", label: "概览", icon: "📊" },
        { id: "scenes", label: "场景 & 对话", icon: "🎬" },
        { id: "yaml", label: "YAML 源码", icon: "📝" }
      ]
    };
  },
  methods: {
    handleDragOver() {
      this.isDragOver = true;
    },
    handleDragLeave() {
      this.isDragOver = false;
    },
    handleDrop(event) {
      this.isDragOver = false;
      const files = event.dataTransfer.files;
      if (files.length > 0 && files[0].name.endsWith(".txt")) {
        this.selectedFile = files[0];
      }
    },
    handleFileSelect(event) {
      const file = event.target.files[0];
      if (file) {
        this.selectedFile = file;
      }
    },
    triggerFileSelect() {
      this.$refs.fileInput.click();
    },
    clearFile() {
      this.selectedFile = null;
      this.$refs.fileInput.value = "";
      this.resetState();
    },
    async handleConvert() {
      if (!this.selectedFile) return;

      this.isLoading = true;
      this.hasError = false;
      this.errorMessage = "";
      this.script = null;
      this.yamlContent = "";
      this.originalFileName = this.selectedFile.name;
      this.activeTab = "overview";

      const formData = new FormData();
      formData.append("file", this.selectedFile);

      try {
        const response = await fetch("http://localhost:8080/api/v1/convert", {
          method: "POST",
          body: formData
        });

        const result = await response.json();

        if (!result.success) {
          throw new Error(result.error || "转换失败");
        }

        this.script = result.script;
        this.yamlContent = result.yaml;
      } catch (error) {
        console.error("转换失败:", error);
        this.hasError = true;
        this.errorMessage = error.message || "网络错误，请确保后端服务已启动";
      } finally {
        this.isLoading = false;
      }
    },
    handleDownload() {
      if (!this.yamlContent) return;

      let fileName = "script_converted.yaml";
      if (this.originalFileName) {
        const baseName = this.originalFileName.replace(".txt", "");
        fileName = baseName + "_converted.yaml";
      }

      const blob = new Blob([this.yamlContent], {
        type: "application/x-yaml; charset=utf-8"
      });
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    },
    copyYaml() {
      if (!this.yamlContent) return;
      navigator.clipboard.writeText(this.yamlContent).then(() => {
        alert("YAML 已复制到剪贴板！");
      });
    },
    resetState() {
      this.script = null;
      this.yamlContent = "";
      this.hasError = false;
      this.errorMessage = "";
    },
    formatFileSize(bytes) {
      if (bytes < 1024) return bytes + " B";
      if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(2) + " KB";
      return (bytes / (1024 * 1024)).toFixed(2) + " MB";
    },
    formatDate() {
      const now = new Date();
      return now.toLocaleString("zh-CN");
    },
    getRoleLabel(role) {
      const labels = {
        protagonist: "主角",
        antagonist: "反派",
        supporting: "配角",
        minor: "次要角色",
        cameo: "客串"
      };
      return labels[role] || role;
    },
    getBeatLabel(type) {
      const labels = {
        dialogue: "对话",
        action: "动作",
        narration: "旁白",
        transition: "转场"
      };
      return labels[type] || type;
    },
    getEmotionLabel(emotion) {
      const labels = {
        happy: "开心",
        angry: "生气",
        sad: "悲伤",
        surprised: "惊讶",
        curious: "好奇",
        mysterious: "神秘",
        neutral: "平静"
      };
      return labels[emotion] || emotion;
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

:root {
  --primary-color: #6366f1;
  --primary-dark: #4f46e5;
  --secondary-color: #8b5cf6;
  --accent-color: #10b981;
  --bg-gradient-start: #6366f1;
  --bg-gradient-end: #8b5cf6;
  --text-primary: #1f2937;
  --text-secondary: #6b7280;
  --bg-card: #ffffff;
  --border-color: #e5e7eb;
  --shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  --radius-sm: 6px;
  --radius-md: 10px;
  --radius-lg: 16px;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  background: linear-gradient(135deg, var(--bg-gradient-start) 0%, var(--bg-gradient-end) 100%);
  min-height: 100vh;
  padding: 24px;
  color: var(--text-primary);
}

.app-container {
  max-width: 1400px;
  margin: 0 auto;
}

/* Header */
.app-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.header-content h1 {
  font-size: 2.25rem;
  font-weight: 700;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-content h1 .icon {
  font-size: 2.5rem;
}

.version-badge {
  background: rgba(255, 255, 255, 0.25);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 600;
  color: white;
  backdrop-filter: blur(10px);
}

/* Main Layout */
.main-content {
  display: grid;
  grid-template-columns: 400px 1fr;
  gap: 24px;
  align-items: start;
}

@media (max-width: 1024px) {
  .main-content {
    grid-template-columns: 1fr;
  }
}

/* Card */
.card {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-xl);
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 1.75rem;
}

/* Upload Section */
.upload-area {
  border: 2px dashed var(--border-color);
  border-radius: var(--radius-md);
  padding: 48px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #f9fafb;
}

.upload-area:hover {
  border-color: var(--primary-color);
  background: #f0f4ff;
}

.upload-area.drag-over {
  border-color: var(--primary-color);
  background: #eef2ff;
  transform: scale(1.01);
}

.upload-icon {
  color: var(--primary-color);
  margin-bottom: 16px;
}

.upload-text {
  font-size: 1rem;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.upload-hint {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* Selected File */
.selected-file {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: var(--radius-md);
  margin-top: 20px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  font-size: 1.5rem;
}

.file-name {
  font-weight: 600;
  color: var(--text-primary);
}

.file-size {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.btn-clear {
  background: none;
  border: none;
  cursor: pointer;
  color: #ef4444;
  padding: 6px;
  border-radius: var(--radius-sm);
  transition: all 0.2s;
}

.btn-clear:hover {
  background: #fef2f2;
}

/* Buttons */
.button-group {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.btn-primary,
.btn-outline {
  flex: 1;
  padding: 14px 20px;
  border-radius: var(--radius-md);
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
  color: white;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-outline {
  background: transparent;
  border: 2px solid var(--primary-color);
  color: var(--primary-color);
}

.btn-outline:hover:not(:disabled) {
  background: #f0f4ff;
}

.btn-outline:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* Loading dots */
.loading-text {
  display: flex;
  align-items: center;
  gap: 4px;
}

.spinner-dot {
  width: 8px;
  height: 8px;
  background: white;
  border-radius: 50%;
  animation: dot-bounce 1.4s infinite ease-in-out both;
}

.spinner-dot:nth-child(1) { animation-delay: -0.32s; }
.spinner-dot:nth-child(2) { animation-delay: -0.16s; }

@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.6; }
  40% { transform: scale(1); opacity: 1; }
}

/* Tips */
.tips-box {
  margin-top: 28px;
  background: #fefce8;
  border: 1px solid #fef08a;
  border-radius: var(--radius-md);
  padding: 18px;
}

.tips-header {
  font-weight: 600;
  color: #92400e;
  margin-bottom: 12px;
}

.tips-list {
  padding-left: 20px;
  font-size: 0.875rem;
  color: #78350f;
}

.tips-list li {
  margin-bottom: 6px;
}

/* Results Section */
.results-card {
  min-height: 600px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 20px;
}

.stats-row {
  display: flex;
  gap: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--primary-color);
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* Tabs */
.tabs-container {
  margin-top: 20px;
}

.tabs {
  display: flex;
  gap: 8px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 24px;
  padding-bottom: 8px;
}

.tab-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: 0.95rem;
  font-weight: 500;
  border: none;
  background: none;
  color: var(--text-secondary);
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: all 0.2s;
}

.tab-button:hover {
  background: #f3f4f6;
  color: var(--text-primary);
}

.tab-button.active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.1));
  color: var(--primary-color);
  font-weight: 600;
}

.tab-icon {
  font-size: 1.1rem;
}

.tab-content {
  min-height: 400px;
}

/* Overview */
.meta-box {
  background: #f9fafb;
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 24px;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--border-color);
}

.meta-row:last-child {
  border-bottom: none;
}

.meta-label {
  font-weight: 600;
  color: var(--text-secondary);
}

.meta-value {
  color: var(--text-primary);
}

.section-box {
  margin-bottom: 24px;
}

.section-title {
  font-size: 1.125rem;
  font-weight: 700;
  margin-bottom: 16px;
  color: var(--text-primary);
}

.characters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 14px;
}

.character-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: linear-gradient(135deg, #f9fafb, #f3f4f6);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  transition: all 0.2s;
}

.character-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-md);
}

.char-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.25rem;
}

.char-name {
  font-weight: 600;
  font-size: 1rem;
}

.char-role {
  font-size: 0.8rem;
  color: var(--text-secondary);
}

/* Scenes */
.scene-card {
  margin-bottom: 20px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.scene-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.1));
}

.scene-number {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.scene-title {
  font-weight: 600;
  font-size: 1rem;
  color: var(--text-primary);
}

.scene-location {
  margin-left: 8px;
  font-weight: 400;
  color: var(--text-secondary);
}

.beats-list {
  padding: 10px 18px;
}

.beat-item {
  padding: 10px 0;
  border-bottom: 1px dashed var(--border-color);
  line-height: 1.7;
}

.beat-item:last-child {
  border-bottom: none;
}

.beat-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 0.75rem;
  font-weight: 600;
  border-radius: 10px;
  margin-right: 10px;
}

.beat-item.dialogue .beat-tag {
  background: #dbeafe;
  color: #1d4ed8;
}

.beat-item.action .beat-tag {
  background: #d1fae5;
  color: #065f46;
}

.beat-item.narration .beat-tag {
  background: #fef3c7;
  color: #92400e;
}

.beat-item.transition .beat-tag {
  background: #f3e8ff;
  color: #7e22ce;
}

.beat-speaker {
  font-weight: 600;
  margin-right: 4px;
}

.beat-content {
  color: var(--text-primary);
}

.beat-emotion {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

/* YAML */
.yaml-content {
  display: flex;
  flex-direction: column;
  height: 520px;
}

.yaml-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}

.btn-copy-yaml {
  padding: 6px 12px;
  font-size: 0.875rem;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: all 0.2s;
}

.btn-copy-yaml:hover {
  background: var(--primary-dark);
}

.yaml-textarea {
  flex: 1;
  resize: none;
  padding: 16px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  font-family: "Courier New", Courier, monospace;
  font-size: 0.9rem;
  line-height: 1.6;
  background: #f9fafb;
  color: #1f2937;
}

/* States */
.loading-container,
.error-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
}

.loading-spinner {
  width: 60px;
  height: 60px;
  border: 4px solid #e5e7eb;
  border-top: 4px solid var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 24px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-text {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 6px;
}

.loading-subtext {
  font-size: 0.875rem;
  color: var(--text-secondary);
}

.error-icon {
  font-size: 4rem;
  margin-bottom: 20px;
}

.error-text {
  color: #dc2626;
  font-size: 1rem;
  margin-bottom: 20px;
}

.btn-retry {
  padding: 10px 28px;
  font-size: 1rem;
  font-weight: 600;
  background: var(--primary-color);
  color: white;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}

.btn-retry:hover {
  background: var(--primary-dark);
}

.empty-icon {
  color: #d1d5db;
  margin-bottom: 20px;
}

.empty-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
}

.empty-subtitle {
  font-size: 0.95rem;
  color: #9ca3af;
}
</style>
