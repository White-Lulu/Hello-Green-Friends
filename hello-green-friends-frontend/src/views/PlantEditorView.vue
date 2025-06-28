<template>
  <div class="editor-container">
    <h2>{{ isEditMode ? '编辑植物图鉴' : '创建新的植物图鉴' }}</h2>
    <form @submit.prevent="handleSubmit('PENDING_REVIEW')" class="editor-form">
      <div class="form-section">
        <h3>基本信息</h3>
        <div class="form-group">
          <label for="name">名称</label>
          <input id="name" v-model="plant.name" type="text" required />
        </div>
        <div class="form-group">
          <label for="scientificName">学名</label>
          <input
            id="scientificName"
            v-model="plant.scientificName"
            type="text"
          />
        </div>
        <div class="form-group">
          <label for="description">描述</label>
          <textarea
            id="description"
            v-model="plant.description"
            rows="5"
          ></textarea>
        </div>
        <div class="form-group">
          <label for="floweringPeriod">花期</label>
          <input
            id="floweringPeriod"
            v-model="plant.floweringPeriod"
            type="text"
          />
        </div>
      </div>

      <div class="form-section">
        <h3>主图</h3>
        <div class="form-group">
          <input id="mainImage" type="file" @change="handleImageUpload" />
          <div v-if="imagePreviewUrl" class="image-preview">
            <img :src="imagePreviewUrl" alt="Image Preview" />
          </div>
          <div
            v-else-if="isEditMode && plant.mainImageUrl"
            class="image-preview"
          >
            <img :src="plant.mainImageUrl" alt="Current Image" />
          </div>
        </div>
      </div>

      <div class="form-section">
        <h3>位置信息</h3>
        <div v-if="plant.locations.length === 0" class="location-placeholder">
          当前没有位置信息，请点击下方按钮添加。
        </div>

        <div
          v-for="(location, index) in plant.locations"
          :key="index"
          class="location-entry"
        >
          <div class="location-header">
            <h4>位置 {{ index + 1 }}</h4>
            <button
              type="button"
              @click="removeLocation(index)"
              class="btn-remove-location"
              title="删除该位置"
            >
              &times;
            </button>
          </div>
          <div class="location-form-grid">
            <div class="form-group">
              <label :for="'area-' + index">区域</label>
              <select
                :id="'area-' + index"
                v-model="location.areaName"
                required
              >
                <option disabled value="">请选择区域</option>
                <option
                  v-for="area in availableAreas"
                  :key="area"
                  :value="area"
                >
                  {{ area }}
                </option>
              </select>
            </div>
            <div class="form-group">
              <label :for="'specificName-' + index">具体描述</label>
              <input
                :id="'specificName-' + index"
                v-model="location.specificName"
                type="text"
                placeholder="例如：图书馆东侧"
                required
              />
            </div>
            <div class="form-group">
              <label :for="'coordX-' + index">地图 X 坐标</label>
              <input
                :id="'coordX-' + index"
                v-model.number="location.mapCoordX"
                type="number"
                step="any"
                placeholder="例如: 850.5"
                required
              />
            </div>
            <div class="form-group">
              <label :for="'coordY-' + index">地图 Y 坐标</label>
              <input
                :id="'coordY-' + index"
                v-model.number="location.mapCoordY"
                type="number"
                step="any"
                placeholder="例如: 432.0"
                required
              />
            </div>
          </div>
        </div>

        <button type="button" @click="addLocation" class="btn-add-location">
          + 添加新位置
        </button>
      </div>
      <div class="form-section">
        <h3>选择标签 (可多选)</h3>
        <div v-if="allTags.length > 0" class="checkbox-group">
          <div v-for="tag in allTags" :key="tag.id" class="checkbox-item">
            <input
              type="checkbox"
              :id="'tag-' + tag.id"
              :value="tag.id"
              v-model="plant.tagIds"
            />
            <label :for="'tag-' + tag.id">{{ tag.name }}</label>
          </div>
        </div>
      </div>

      <div v-if="error" class="error-message">{{ error }}</div>

      <div class="form-actions">
        <button type="button" @click="saveDraft" :disabled="isLoading">
          {{ isLoading ? '保存中...' : '存为草稿' }}
        </button>
        <button type="submit" :disabled="isLoading">
          {{ isLoading ? '提交中...' : '提交审核' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/services/apiClient';

const route = useRoute();
const router = useRouter();
const plant = ref({
  name: '',
  scientificName: '',
  description: '',
  floweringPeriod: '',
  status: 'DRAFT',
  tagIds: [],
  locations: [], 
});

const mainImage = ref(null);
const imagePreviewUrl = ref(null);
const allTags = ref([]);
const availableAreas = ref([]);
const isLoading = ref(false);
const error = ref('');

const plantId = computed(() => route.params.id);
const isEditMode = computed(() => !!plantId.value);

const addLocation = () => {
  plant.value.locations.push({
    areaName: '',
    specificName: '',
    mapCoordX: null,
    mapCoordY: null,
  });
};

const removeLocation = (index) => {
  plant.value.locations.splice(index, 1);
};

// 获取预设的区域列表
const fetchAvailableAreas = async () => {
  try {
    const response = await apiClient.get('/locations/areas');
    if (response.data.success) {
      availableAreas.value = response.data.data;
    }
  } catch (err) {
    console.error('Failed to fetch areas:', err);
    error.value = '无法加载区域列表，请检查后端服务。';
  }
};

/**
 * 从后端异步获取所有可用的标签列表。
 */
const fetchTags = async () => {
  try {
    // 使用 apiClient 向后端 /api/tags 发送 GET 请求
    const response = await apiClient.get('/tags');

    // 检查后端返回的自定义响应体中的 success 字段
    if (response.data.success) {
      // 如果成功，将返回的数据（标签数组）赋值给 allTags ref
      allTags.value = response.data.data;
    } else {
      // 如果后端明确返回失败，则抛出其提供的错误信息
      throw new Error(
        response.data.message || 'Failed to fetch tags from server.'
      );
    }
  } catch (err) {
    // 捕获网络错误或上面抛出的错误
    console.error('Failed to fetch tags:', err);
    // 更新 error ref，以便在UI上向用户显示错误信息
    error.value = '无法加载标签列表，请稍后重试。';
  }
};
/*
 * 处理用户选择主图文件的事件。
 * @param {Event} event - 文件输入框的 change 事件对象。
 */
const handleImageUpload = (event) => {
  const file = event.target.files[0];
  // 确保用户确实选择了一个文件
  if (file) {
    mainImage.value = file;
    imagePreviewUrl.value = URL.createObjectURL(file);
  }
};

onMounted(async () => {
  // 页面加载时，并行获取标签和区域列表
  await Promise.all([fetchTags(), fetchAvailableAreas()]);

  if (isEditMode.value) {
    // 编辑模式下，获取植物的现有数据
    isLoading.value = true;
    try {
      const response = await apiClient.get(`/my-gallery/${plantId.value}`);
      if (response.data.success) {
        const data = response.data.data;
        // 填充表单
        plant.value.name = data.name;
        plant.value.scientificName = data.scientificName;
        plant.value.description = data.description;
        plant.value.floweringPeriod = data.floweringPeriod;
        plant.value.mainImageUrl = data.mainImageUrl; // 用于显示旧图片
        plant.value.tagIds = data.tags.map((tag) => tag.id);

        // 将后端的 LocationDTO 数组映射为前端的 locations 数组
        plant.value.locations = data.locations.map((loc) => ({
          areaName: loc.areaName,
          specificName: loc.specificName,
          mapCoordX: loc.mapCoordX,
          mapCoordY: loc.mapCoordY,
        }));
      }
    } catch (err) {
      error.value = '加载植物数据失败。';
    } finally {
      isLoading.value = false;
    }
  }
});

const handleSubmit = async (status = 'PENDING_REVIEW') => {
  isLoading.value = true;
  error.value = '';

  // 校验至少有一个位置信息
  if (plant.value.locations.length === 0) {
    error.value = '请至少添加一个位置信息。';
    isLoading.value = false;
    return;
  }

  const formData = new FormData();

  // 更新状态并准备提交的数据
  plant.value.status = status;
  formData.append(
    'plant',
    new Blob([JSON.stringify(plant.value)], { type: 'application/json' })
  );

  if (mainImage.value) {
    formData.append('mainImage', mainImage.value);
  }

  try {
    let response;
    const url = isEditMode.value
      ? `/my-gallery/${plantId.value}`
      : '/my-gallery';
    const method = isEditMode.value ? 'put' : 'post';

    response = await apiClient[method](url, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

    if (response.data.success) {
      router.push('/my-encyclopedia');
    } else {
      throw new Error(response.data.message);
    }
  } catch (err) {
    error.value = err.response?.data?.message || '操作失败，请重试。';
  } finally {
    isLoading.value = false;
  }
};

const saveDraft = () => {
  handleSubmit('DRAFT');
};
</script>

<style scoped>
@import '@/assets/base.css';

.editor-container {
  max-width: 900px;
  margin: 3rem auto;
  padding: 2rem 3rem;
  background-color: rgba(255, 255, 255, 0.8); 
  border-radius: 12px;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.18);
}

h2 {
  color: var(--color-heading);
  text-align: center;
  margin-bottom: 2.5rem;
  font-weight: 600;
}

.form-section {
  border-top: 1px solid var(--color-border);
  padding-top: 2rem;
  margin-top: 2rem;
}

.form-section h3 {
  color: var(--color-heading);
  font-size: 1.25rem;
  margin-bottom: 1.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid var(--color-border-hover);
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: var(--color-text);
  font-size: 0.95rem;
}

input[type='text'],
input[type='number'],
textarea,
select {
  width: 90%;
  padding: 0.8rem 1rem;
  border: 1px solid var(--color-border);
  border-radius: 8px;
  background-color: #fff;
  font-size: 1rem;
  color: var(--color-text);
  transition: border-color 0.3s, box-shadow 0.3s;
}

input[type='text']:focus,
input[type='number']:focus,
textarea:focus,
select:focus {
  outline: none;
  border-color: var(--color-theme-green); /* 自定义一个主题绿色 */
  box-shadow: 0 0 0 3px rgba(76, 175, 80, 0.2);
}

textarea {
  resize: vertical;
  min-height: 120px;
}

.location-entry {
  background-color: rgba(240, 245, 240, 0.7);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  position: relative;
  transition: box-shadow 0.3s;
}
.location-entry:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.location-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}
.location-header h4 {
  margin: 0;
  color: var(--color-heading);
}

.location-form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem 1.5rem;
}

.btn-remove-location {
  background: none;
  border: none;
  font-size: 1.8rem;
  font-weight: bold;
  color: #aaa;
  cursor: pointer;
  line-height: 1;
  transition: color 0.3s;
}
.btn-remove-location:hover {
  color: #e53935;
}

.btn-add-location {
  display: block;
  width: 100%;
  padding: 0.8rem;
  margin-top: 1rem;
  background-color: transparent;
  color: var(--color-theme-green);
  border: 2px dashed var(--color-theme-green);
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  transition: background-color 0.3s, color 0.3s;
}
.btn-add-location:hover {
  background-color: var(--color-theme-green);
  color: #fff;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
}
.checkbox-item {
  display: flex;
  align-items: center;
  background-color: #f8f9fa;
  border: 1px solid var(--color-border);
  border-radius: 20px;
  padding: 0.5rem 1rem;
  cursor: pointer;
  transition: all 0.3s;
}
.checkbox-item:hover {
  border-color: var(--color-theme-green);
  background-color: #eff7ef;
}
.checkbox-item input[type='checkbox'] {
  display: none;
}
.checkbox-item label {
  margin: 0;
  font-weight: 500;
  position: relative;
  padding-left: 24px;
}

.checkbox-item label::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  border: 2px solid #ccc;
  border-radius: 4px;
  transition: all 0.3s;
}
.checkbox-item input[type='checkbox']:checked + label::before {
  background-color: var(--color-theme-green);
  border-color: var(--color-theme-green);
}
.checkbox-item input[type='checkbox']:checked + label::after {
  content: '✔';
  position: absolute;
  left: 3px;
  top: 50%;
  transform: translateY(-50%);
  color: white;
  font-size: 12px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2.5rem;
  border-top: 1px solid var(--color-border);
  padding-top: 2rem;
}

.form-actions button {
  padding: 0.8rem 2rem;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
  transition: all 0.3s;
}
.form-actions button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

button[type='submit'] {
  background-color: var(--color-theme-green);
  color: white;
  box-shadow: 0 4px 14px 0 rgba(76, 175, 80, 0.39);
}
button[type='submit']:hover:not(:disabled) {
  background-color: #45a049;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px 0 rgba(76, 175, 80, 0.45);
}

.form-actions button[type='button'] {
  background-color: #f0f0f0;
  color: #333;
}
.form-actions button[type='button']:hover:not(:disabled) {
  background-color: #e0e0e0;
}

/* 错误消息 */
.error-message {
  color: #d8000c;
  background-color: #ffd2d2;
  padding: 1rem;
  border-radius: 8px;
  text-align: center;
  margin-top: 1.5rem;
}
</style>
