const API_BASE_URL = 'http://localhost:8080/api/v1';

/**
 * API服务
 */
export const api = {
  /**
   * 上传小说文件
   */
  async uploadNovel(file) {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch(`${API_BASE_URL}/upload`, {
      method: 'POST',
      body: formData
    });
    
    return response.json();
  },

  /**
   * 读取小说内容
   */
  async getNovelContent(fileId) {
    const response = await fetch(`${API_BASE_URL}/novel/${fileId}`);
    return response.json();
  },

  /**
   * 执行转换
   */
  async convertNovel(content) {
    const response = await fetch(`${API_BASE_URL}/convert`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ content })
    });
    
    return response.json();
  },

  /**
   * 质量评估
   */
  async evaluateQuality(content, script) {
    const response = await fetch(`${API_BASE_URL}/quality`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ content, script: JSON.stringify(script) })
    });
    
    return response.json();
  },

  /**
   * 创建修正
   */
  async createCorrection(correction) {
    const response = await fetch(`${API_BASE_URL}/corrections`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(correction)
    });
    
    return response.json();
  },

  /**
   * 获取修正列表
   */
  async getCorrections(scriptId) {
    const response = await fetch(`${API_BASE_URL}/corrections/${scriptId}`);
    return response.json();
  },

  /**
   * 应用修正
   */
  async applyCorrection(correctionId, script) {
    const response = await fetch(`${API_BASE_URL}/corrections/${correctionId}/apply`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ script })
    });
    
    return response.json();
  },

  /**
   * 导出剧本
   */
  async exportScript(scriptId) {
    const response = await fetch(`${API_BASE_URL}/export/${scriptId}`);
    return response.json();
  },

  /**
   * 健康检查
   */
  async healthCheck() {
    const response = await fetch(`${API_BASE_URL}/health`);
    return response.json();
  }
};
