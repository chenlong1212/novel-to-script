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
            <button class="btn btn-success me-2" @click="showScriptModal = true">
              查看剧本
            </button>
            <button class="btn btn-outline-primary" @click="proceedToQuality">
              质量评估
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 剧本查看模态框 -->
    <ScriptViewModal 
      v-if="showScriptModal" 
      :script="script" 
      @close="showScriptModal = false"
    />
  </div>
</template>

<script>
import { api } from '../services/api';
import ScriptViewModal from './ScriptViewModal.vue';

export default {
  name: 'ConvertView',
  components: {
    ScriptViewModal
  },
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
      script: null,
      showScriptModal: false
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
        
        // 为了演示，我们模拟一个剧本（实际应该调用API）
        this.script = this.generateMockScript();
        this.progress = 100;
        this.statusMessage = '转换完成！';
        
      } catch (error) {
        this.error = error.message || '转换失败';
        this.progress = 0;
      } finally {
        setTimeout(() => {
          this.isConverting = false;
        }, 1500);
      }
    },
    
    generateMockScript() {
      return {
        schemaVersion: '1.0',
        meta: {
          title: '咖啡厅的秘密',
          author: '测试作者',
          version: '1.0'
        },
        characters: [
          { id: 'char_1', name: '王小明', role: '主角', description: '男主角' },
          { id: 'char_2', name: '李雪', role: '主角', description: '女主角' },
          { id: 'char_3', name: '风衣男人', role: '配角', description: '神秘人物' },
          { id: 'char_4', name: '神秘女人', role: '配角', description: '神秘人物' },
          { id: 'char_5', name: '服务员', role: '配角', description: '咖啡厅服务员' }
        ],
        scenes: [
          {
            id: 'scene_1',
            number: 1,
            location: { name: '咖啡厅', locationType: 'public' },
            time: { timeOfDay: 'afternoon' },
            description: '午后的咖啡厅，阳光明媚',
            beats: [
              { id: 'beat_1', type: 'narration', content: '午后的阳光透过玻璃窗洒进来，给整个咖啡厅镀上一层温暖的金色。王小明推开咖啡厅的门，门上的风铃发出清脆的响声。' },
              { id: 'beat_2', type: 'dialogue', speaker: '服务员', emotion: 'happy', content: '欢迎光临！请问几位？' },
              { id: 'beat_3', type: 'dialogue', speaker: '王小明', emotion: 'neutral', content: '一位，谢谢。' },
              { id: 'beat_4', type: 'action', content: '王小明环顾四周，目光在靠窗的位置停下。一个熟悉的身影坐在那里，正低头看着手机。' },
              { id: 'beat_5', type: 'dialogue', speaker: '王小明', emotion: 'surprised', content: '李雪？' },
              { id: 'beat_6', type: 'action', content: '女孩抬起头，眼中闪过惊喜。' },
              { id: 'beat_7', type: 'dialogue', speaker: '李雪', emotion: 'happy', content: '小明！真的是你！好久不见！' },
              { id: 'beat_8', type: 'action', content: '李雪站起身，两人热情地拥抱。王小明在她对面坐下，服务员很快送来了菜单。' }
            ]
          },
          {
            id: 'scene_2',
            number: 2,
            location: { name: '咖啡厅角落', locationType: 'public' },
            time: { timeOfDay: 'afternoon' },
            description: '咖啡厅的角落位置',
            beats: [
              { id: 'beat_9', type: 'action', content: '就在这时，咖啡厅的门再次被推开。一个穿着黑色风衣的男人走了进来，神色匆匆。他在角落的位置坐下，不停地看手表。' },
              { id: 'beat_10', type: 'dialogue', speaker: '李雪', emotion: 'curious', content: '那个人看起来很奇怪。' },
              { id: 'beat_11', type: 'action', content: '王小明也注意到了那个男人。风衣男人从包里拿出一个黑色的文件袋，小心翼翼地放在桌上。' },
              { id: 'beat_12', type: 'action', content: '几分钟后，另一个女人走了进来，径直走到风衣男人的桌前。' },
              { id: 'beat_13', type: 'dialogue', speaker: '神秘女人', emotion: 'serious', content: '东西带来了吗？' },
              { id: 'beat_14', type: 'dialogue', speaker: '风衣男人', emotion: 'serious', content: '带来了，钱呢？' },
              { id: 'beat_15', type: 'action', content: '女人从包里拿出一个信封，推到男人面前。男人打开信封看了看，满意地点点头，然后把文件袋推给女人。' }
            ]
          }
        ]
      };
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
