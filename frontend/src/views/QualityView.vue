<template>
  <div class="quality-view">
    <div class="card">
      <div class="card-header">
        <h5>质量评估报告</h5>
      </div>
      <div class="card-body">
        <div v-if="!qualityReport" class="text-center">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
          </div>
          <p class="mt-2">正在生成质量报告...</p>
        </div>
        
        <div v-else>
          <!-- 综合评分 -->
          <div class="quality-score text-center mb-4">
            <div class="score-circle" :class="scoreClass">
              <span class="score-value">{{ Math.round(qualityReport.overallScore * 100) }}</span>
              <span class="score-label">/ 100</span>
            </div>
            <h4 class="mt-3">{{ qualityReport.grade }}</h4>
            <p class="text-muted">综合评分</p>
          </div>
          
          <!-- 各项评分 -->
          <div class="row mb-4">
            <div class="col-md-6">
              <div class="metric-card">
                <h6>人物准确率</h6>
                <div class="progress">
                  <div class="progress-bar bg-primary" :style="{ width: qualityReport.characterAccuracy * 100 + '%' }">
                    {{ Math.round(qualityReport.characterAccuracy * 100) }}%
                  </div>
                </div>
              </div>
            </div>
            <div class="col-md-6">
              <div class="metric-card">
                <h6>对话覆盖率</h6>
                <div class="progress">
                  <div class="progress-bar bg-success" :style="{ width: qualityReport.dialogueCoverage * 100 + '%' }">
                    {{ Math.round(qualityReport.dialogueCoverage * 100) }}%
                  </div>
                </div>
              </div>
            </div>
            <div class="col-md-6">
              <div class="metric-card">
                <h6>场景完整性</h6>
                <div class="progress">
                  <div class="progress-bar bg-info" :style="{ width: qualityReport.sceneCompleteness * 100 + '%' }">
                    {{ Math.round(qualityReport.sceneCompleteness * 100) }}%
                  </div>
                </div>
              </div>
            </div>
            <div class="col-md-6">
              <div class="metric-card">
                <h6>情绪准确率</h6>
                <div class="progress">
                  <div class="progress-bar bg-warning" :style="{ width: qualityReport.emotionAccuracy * 100 + '%' }">
                    {{ Math.round(qualityReport.emotionAccuracy * 100) }}%
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 问题列表 -->
          <div v-if="qualityReport.issues && qualityReport.issues.length > 0" class="issues-section mb-4">
            <h6>发现的问题 ({{ qualityReport.issues.length }})</h6>
            <div class="list-group">
              <div 
                v-for="(issue, index) in qualityReport.issues" 
                :key="index"
                class="list-group-item list-group-item-action flex-column align-items-start"
              >
                <div class="d-flex w-100 justify-content-between">
                  <h6 class="mb-1">
                    <span :class="'badge bg-' + getSeverityColor(issue.severity)">
                      {{ issue.severity }}
                    </span>
                    {{ getTypeName(issue.type) }}
                  </h6>
                </div>
                <p class="mb-1">{{ issue.detail }}</p>
              </div>
            </div>
          </div>
          
          <!-- 改进建议 -->
          <div v-if="qualityReport.suggestions && qualityReport.suggestions.length > 0" class="suggestions-section mb-4">
            <h6>改进建议</h6>
            <ul class="list-group">
              <li v-for="(suggestion, index) in qualityReport.suggestions" :key="index" class="list-group-item">
                {{ suggestion }}
              </li>
            </ul>
          </div>
          
          <!-- 操作按钮 -->
          <div class="text-center">
            <button class="btn btn-success btn-lg me-2" @click="proceedToProofread">
              人工校对
            </button>
            <button class="btn btn-outline-secondary" @click="exportScript">
              导出剧本
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
  name: 'QualityView',
  props: {
    script: {
      type: Object,
      required: true
    },
    originalContent: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      qualityReport: null
    };
  },
  computed: {
    scoreClass() {
      if (!this.qualityReport) return '';
      const score = this.qualityReport.overallScore;
      if (score >= 0.8) return 'score-excellent';
      if (score >= 0.6) return 'score-good';
      if (score >= 0.4) return 'score-fair';
      return 'score-poor';
    }
  },
  mounted() {
    this.generateReport();
  },
  methods: {
    async generateReport() {
      try {
        // 模拟生成报告（实际应该调用API）
        this.qualityReport = {
          overallScore: 0.85,
          grade: '良好',
          characterAccuracy: 0.9,
          dialogueCoverage: 0.88,
          sceneCompleteness: 0.82,
          emotionAccuracy: 0.85,
          formatValidity: 0.95,
          issues: [
            { type: 'character', severity: 'medium', detail: '部分人物描述不够详细' },
            { type: 'dialogue', severity: 'low', detail: '少量对话缺少说话人' }
          ],
          suggestions: [
            '建议检查人物识别规则',
            '建议核对对话说话人识别'
          ]
        };
      } catch (error) {
        console.error('生成报告失败', error);
      }
    },
    
    getSeverityColor(severity) {
      const colors = {
        high: 'danger',
        medium: 'warning',
        low: 'info'
      };
      return colors[severity] || 'secondary';
    },
    
    getTypeName(type) {
      const names = {
        character: '人物',
        dialogue: '对话',
        scene: '场景',
        emotion: '情绪',
        format: '格式'
      };
      return names[type] || type;
    },
    
    proceedToProofread() {
      this.$emit('change-view', 'proofread');
    },
    
    exportScript() {
      alert('导出功能开发中...\n剧本已准备好，可以复制使用。');
    }
  }
};
</script>

<style scoped>
.quality-view {
  max-width: 900px;
  margin: 0 auto;
}

.quality-score {
  padding: 30px;
}

.score-circle {
  display: inline-block;
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
}

.score-value {
  font-size: 48px;
  font-weight: bold;
}

.score-label {
  font-size: 18px;
  opacity: 0.9;
}

.metric-card {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 15px;
}

.metric-card h6 {
  margin-bottom: 10px;
  font-weight: 600;
}

.issues-section,
.suggestions-section {
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.btn-lg {
  padding: 12px 30px;
}
</style>
