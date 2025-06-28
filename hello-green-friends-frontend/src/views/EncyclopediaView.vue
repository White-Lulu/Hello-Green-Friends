<template>
  <div class="encyclopedia-container">
    <header class="page-header">
      <h1 class="page-title">植物图鉴</h1>
      <p class="page-subtitle">探索校园里的绿色生命</p>
    </header>

    <div class="filter-bar">
      <div class="filter-group">
        <label for="area-filter">按区域筛选</label>
        <select id="area-filter" v-model="filters.areaName">
          <option value="">所有区域</option>
          <option v-for="area in allAreas" :key="area" :value="area">
            {{ area }}
          </option>
        </select>
      </div>
      <div class="filter-group">
        <label for="tag-filter">按标签筛选</label>
        <select id="tag-filter" v-model="filters.tagId">
          <option value="">所有标签</option>
          <option v-for="tag in allTags" :key="tag.id" :value="tag.id">
            {{ tag.name }}
          </option>
        </select>
      </div>
      <button @click="resetFilters" class="reset-button">重置筛选</button>
    </div>

    <div v-if="loading" class="state-container">
      <div class="spinner"></div>
      <p>正在努力加载植物...</p>
    </div>

    <div v-else-if="error" class="state-container error">
      <p>加载失败：{{ error }}</p>
    </div>

    <div v-else-if="plants.length === 0" class="state-container empty">
      <h3>未找到植物</h3>
      <p>尝试调整筛选条件或重置筛选。</p>
    </div>

    <div v-else class="plant-grid">
      <PlantCard v-for="plant in plants" :key="plant.id" :plant="plant" />
    </div>

    <BasePagination
      :current-page="pagination.currentPage"
      :total-pages="pagination.totalPages"
      @page-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import apiClient from '@/services/apiClient';
import PlantCard from '@/components/PlantCard.vue';
import BasePagination from '@/components/BasePagination.vue';

// 响应式状态定义
const plants = ref([]);
const loading = ref(true);
const error = ref(null);
const allAreas = ref([]);
const allTags = ref([]);

// 将筛选条件和分页信息聚合在一个响应式对象中
const params = reactive({
  areaName: '',
  tagId: '',
  page: 0, // API 的分页从 0 开始
  size: 12, // 每页显示12个
});

// 暴露给模板的筛选对象，用于 v-model
const filters = reactive({
  areaName: '',
  tagId: '',
});

const pagination = reactive({
  currentPage: 0,
  totalPages: 0,
  totalElements: 0,
});

/**
 * 核心数据获取函数
 */
const fetchPlants = async () => {
  loading.value = true;
  error.value = null;
  try {
    // 动态构建请求参数，只包含有值的字段
    const queryParams = { page: params.page, size: params.size };
    if (params.areaName) queryParams.areaName = params.areaName;
    if (params.tagId) queryParams.tagId = params.tagId;

    const response = await apiClient.get('/plants', { params: queryParams });

    if (response.data.success) {
      const responseData = response.data.data;
      plants.value = responseData.content;
      pagination.currentPage = responseData.currentPage;
      pagination.totalPages = responseData.totalPages;
      pagination.totalElements = responseData.totalElements;
    } else {
      throw new Error(response.data.message || '返回数据格式不正确');
    }
  } catch (err) {
    error.value = err.response?.data?.message || '获取植物数据失败';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

/**
 * 获取用于筛选的区域和标签列表
 */
const fetchFilterOptions = async () => {
  try {
    // 并行获取区域和标签
    const [areaResponse, tagResponse] = await Promise.all([
      apiClient.get('/locations/areas'),
      apiClient.get('/tags'),
    ]);
    if (areaResponse.data.success) {
      allAreas.value = areaResponse.data.data;
    }
    if (tagResponse.data.success) {
      allTags.value = tagResponse.data.data;
    }
  } catch (err) {
    console.error('获取筛选选项失败:', err);
  }
};

// 监听筛选条件的变化
watch(filters, () => {
  // 当筛选条件变化时，重置到第一页
  params.page = 0;
  params.areaName = filters.areaName;
  params.tagId = filters.tagId;
});

// 监听所有请求参数的变化，统一触发数据获取
watch(params, fetchPlants, { deep: true });

// 组件挂载后，初始化数据
onMounted(() => {
  fetchFilterOptions();
  fetchPlants(); // 首次加载
});

// 处理分页变化
const handlePageChange = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    params.page = newPage;
  }
};

// 重置筛选
const resetFilters = () => {
  filters.areaName = '';
  filters.tagId = '';
  // 相关的 watch 会自动触发数据重新获取
};
</script>

<style scoped>
.encyclopedia-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 2rem;
}

.page-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.page-title {
  font-size: 2.8rem;
  font-weight: 700;
  color: var(--text-dark);
}

.page-subtitle {
  font-size: 1.1rem;
  color: #6c757d;
  margin-top: 0.5rem;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 1.5rem;
  padding: 1.5rem;
  background-color: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  margin-bottom: 2.5rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
}

.filter-group label {
  font-size: 0.9rem;
  font-weight: 500;
  color: #495057;
  margin-bottom: 0.5rem;
}

.filter-group select {
  padding: 0.6rem 1rem;
  border-radius: 8px;
  border: 1px solid #ced4da;
  background-color: #f8f9fa;
  min-width: 200px;
  cursor: pointer;
}

.reset-button {
  align-self: flex-end;
  padding: 0.6rem 1.5rem;
  border-radius: 8px;
  border: 1px solid #6c757d;
  background-color: transparent;
  color: #6c757d;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.reset-button:hover {
  background-color: #6c757d;
  color: #fff;
}

.plant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
}

.state-container {
  text-align: center;
  padding: 4rem;
  color: #718096;
  background-color: #f8f9fa;
  border-radius: 12px;
}

.state-container.error {
  color: #d8000c;
  background-color: #ffd2d2;
}

.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border-left-color: var(--color-theme-green);
  animation: spin 1s ease infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
