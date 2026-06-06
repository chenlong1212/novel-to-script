<template>
  <div id="app">
    <nav class="navbar navbar-expand-lg navbar-dark bg-primary">
      <div class="container">
        <a class="navbar-brand" href="#">
          <h4>AI 小说转剧本工具</h4>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav ms-auto">
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentView === 'upload' }" @click="currentView = 'upload'">
                上传小说
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentView === 'convert' }" @click="currentView = 'convert'" :disabled="!hasContent">
                转换
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentView === 'quality' }" @click="currentView = 'quality'" :disabled="!hasScript">
                质量评估
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" :class="{ active: currentView === 'proofread' }" @click="currentView = 'proofread'" :disabled="!hasScript">
                校对
              </a>
            </li>
          </ul>
        </div>
      </div>
    </nav>

    <div class="container mt-4">
      <!-- 上传视图 -->
      <UploadView v-if="currentView === 'upload'" @content-loaded="handleContentLoaded" />
      
      <!-- 转换视图 -->
      <ConvertView v-if="currentView === 'convert'" :content="novelContent" @conversion-complete="handleConversionComplete" />
      
      <!-- 质量评估视图 -->
      <QualityView v-if="currentView === 'quality'" :script="script" :original-content="novelContent" />
      
      <!-- 校对视图 -->
      <ProofreadView v-if="currentView === 'proofread'" :script="script" />
    </div>

    <!-- 状态栏 -->
    <footer class="footer mt-5">
      <div class="container text-center">
        <p class="text-muted">AI 小说转剧本工具 v1.0.0</p>
      </div>
    </footer>
  </div>
</template>

<script>
import UploadView from './views/UploadView.vue';
import ConvertView from './views/ConvertView.vue';
import QualityView from './views/QualityView.vue';
import ProofreadView from './views/ProofreadView.vue';

export default {
  name: 'App',
  components: {
    UploadView,
    ConvertView,
    QualityView,
    ProofreadView
  },
  data() {
    return {
      currentView: 'upload',
      novelContent: '',
      script: null,
      fileId: null
    };
  },
  computed: {
    hasContent() {
      return this.novelContent && this.novelContent.length > 0;
    },
    hasScript() {
      return this.script !== null;
    }
  },
  methods: {
    handleContentLoaded({ content, fileId }) {
      this.novelContent = content;
      this.fileId = fileId;
      this.currentView = 'convert';
    },
    handleConversionComplete(script) {
      this.script = script;
      this.currentView = 'quality';
    }
  }
};
</script>

<style>
body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f5f5f5;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

#app {
  flex: 1;
}

.container {
  max-width: 1200px;
}

.footer {
  margin-top: auto;
  padding: 20px 0;
  background-color: #f8f9fa;
  border-top: 1px solid #e9ecef;
}

.card {
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  border: none;
  margin-bottom: 20px;
}

.card-header {
  background-color: #007bff;
  color: white;
  font-weight: bold;
}

.btn-primary {
  background-color: #007bff;
  border-color: #007bff;
}

.btn-primary:hover {
  background-color: #0056b3;
  border-color: #0056b3;
}

.progress {
  height: 25px;
}

.progress-bar {
  font-size: 14px;
  line-height: 25px;
}
</style>
