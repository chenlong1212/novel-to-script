<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-dialog">
      <div class="modal-header">
        <h5 class="modal-title">📖 剧本预览</h5>
        <button type="button" class="btn-close" @click="$emit('close')">×</button>
      </div>
      <div class="modal-body">
        <!-- 元信息 -->
        <div v-if="script.meta" class="info-section mb-3">
          <h6>📋 基本信息</h6>
          <p><strong>标题：</strong>{{ script.meta.title || '未命名' }}</p>
          <p><strong>作者：</strong>{{ script.meta.author || '未知' }}</p>
          <p><strong>版本：</strong>{{ script.meta.version || '1.0' }}</p>
        </div>

        <!-- 人物列表 -->
        <div v-if="script.characters && script.characters.length > 0" class="info-section mb-3">
          <h6>👥 人物列表</h6>
          <div class="character-list">
            <span 
              v-for="char in script.characters" 
              :key="char.id"
              class="badge bg-primary me-2 mb-2"
            >
              {{ char.name }}
              <span v-if="char.role" class="ms-1">({{ char.role }})</span>
            </span>
          </div>
        </div>

        <!-- 场景列表 -->
        <div v-if="script.scenes && script.scenes.length > 0" class="info-section">
          <h6>🎬 场景列表</h6>
          <div class="accordion" id="sceneAccordion">
            <div v-for="(scene, index) in script.scenes" :key="scene.id" class="accordion-item">
              <h2 class="accordion-header">
                <button 
                  class="accordion-button collapsed" 
                  type="button"
                  data-bs-toggle="collapse" 
                  :data-bs-target="'#scene-' + index"
                >
                  场景 {{ index + 1 }} - {{ scene.location?.name || '未知地点' }}
                </button>
              </h2>
              <div 
                :id="'scene-' + index" 
                class="accordion-collapse collapse"
                data-bs-parent="#sceneAccordion"
              >
                <div class="accordion-body">
                  <div v-if="scene.beats && scene.beats.length > 0" class="beats-container">
                    <div 
                      v-for="beat in scene.beats" 
                      :key="beat.id"
                      class="beat-item"
                      :class="getBeatTypeClass(beat.type)"
                    >
                      <span class="beat-type-badge">{{ getBeatTypeName(beat.type) }}</span>
                      <div v-if="beat.speaker" class="beat-speaker">
                        <strong>{{ beat.speaker }}</strong>
                        <span v-if="beat.emotion" class="emotion-tag ms-2">
                          ({{ beat.emotion }})
                        </span>
                      </div>
                      <div class="beat-content">{{ beat.content }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- JSON预览 -->
        <div class="mt-3">
          <button class="btn btn-outline-secondary btn-sm" @click="showJson = !showJson">
            {{ showJson ? '隐藏' : '显示' }} JSON 原始数据
          </button>
          <pre v-if="showJson" class="json-preview mt-2">{{ JSON.stringify(script, null, 2) }}</pre>
        </div>
      </div>
      <div class="modal-footer">
        <button class="btn btn-secondary" @click="$emit('close')">关闭</button>
        <button class="btn btn-primary" @click="copyToClipboard">复制剧本</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ScriptViewModal',
  props: {
    script: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      showJson: false
    };
  },
  methods: {
    getBeatTypeClass(type) {
      const classes = {
        'dialogue': 'beat-dialogue',
        'action': 'beat-action',
        'narration': 'beat-narration',
        'transition': 'beat-transition'
      };
      return classes[type] || '';
    },
    getBeatTypeName(type) {
      const names = {
        'dialogue': '💬 对话',
        'action': '🎬 动作',
        'narration': '📝 旁白',
        'transition': '🔄 转场'
      };
      return names[type] || type;
    },
    copyToClipboard() {
      const text = JSON.stringify(this.script, null, 2);
      navigator.clipboard.writeText(text).then(() => {
        alert('剧本已复制到剪贴板！');
      }).catch(() => {
        alert('复制失败，请手动复制');
      });
    }
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1050;
}

.modal-dialog {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 900px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #dee2e6;
  background-color: #f8f9fa;
  border-radius: 8px 8px 0 0;
}

.modal-title {
  margin: 0;
  font-size: 1.25rem;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.info-section h6 {
  border-bottom: 2px solid #007bff;
  padding-bottom: 8px;
  margin-bottom: 15px;
  color: #007bff;
}

.character-list {
  display: flex;
  flex-wrap: wrap;
}

.beat-item {
  padding: 12px;
  margin-bottom: 8px;
  border-left: 4px solid #6c757d;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.beat-dialogue {
  border-left-color: #007bff;
  background-color: #e7f1ff;
}

.beat-action {
  border-left-color: #28a745;
  background-color: #d4edda;
}

.beat-narration {
  border-left-color: #ffc107;
  background-color: #fff3cd;
}

.beat-transition {
  border-left-color: #6f42c1;
  background-color: #e2d5f1;
}

.beat-type-badge {
  display: inline-block;
  font-size: 12px;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 4px;
  margin-bottom: 4px;
  color: #6c757d;
  background-color: #e9ecef;
}

.beat-speaker {
  font-weight: bold;
  margin-bottom: 4px;
}

.emotion-tag {
  font-size: 12px;
  color: #6c757d;
  font-style: italic;
}

.beat-content {
  color: #333;
  line-height: 1.6;
}

.json-preview {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  padding: 15px;
  max-height: 300px;
  overflow-y: auto;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>
