<template>
  <div class="search-container">
    <header class="search-header">
      <h1 class="page-title">探索植物世界</h1>
      <p class="page-subtitle">输入植物的名称进行搜索</p>
      <form @submit.prevent="handleSearchSubmit" class="search-form">
        <input
          type="text"
          v-model="inputQuery"
          placeholder="例如：银杏、樱花、常春藤"
          class="search-input"
        />
        <button type="submit" class="search-button">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            viewBox="0 0 24 24"
            fill="currentColor"
          >
            <path
              d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"
            />
          </svg>
          <span>搜索</span>
        </button>
      </form>
    </header>

    <main class="results-main">
      <div v-if="!hasSearched" class="state-container initial">
        <p>请输入关键词开始您的探索之旅 !</p>
      </div>

      <div v-else-if="loading" class="state-container">
        <div class="spinner"></div>
        <p>正在努力搜索中...</p>
      </div>

      <div v-else-if="error" class="state-container error">
        <p>搜索出错：{{ error }}</p>
      </div>

      <div v-else-if="results.length === 0" class="state-container empty">
        <h3>未能找到任何结果</h3>
        <p>
          对于"<strong>{{ activeQuery }}</strong
          >"，我们没有找到匹配的植物，请尝试其他关键词。
        </p>
      </div>

      <div v-else>
        <h2 class="results-title">
          关于 "<strong>{{ activeQuery }}</strong
          >" 的搜索结果
        </h2>
        <div class="plant-grid">
          <PlantCard v-for="plant in results" :key="plant.id" :plant="plant" />
        </div>
        <BasePagination
          :current-page="pagination.currentPage"
          :total-pages="pagination.totalPages"
          @page-change="handlePageChange"
        />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import apiClient from '@/services/apiClient';
import PlantCard from '@/components/PlantCard.vue';
import BasePagination from '@/components/BasePagination.vue';

const route = useRoute();
const router = useRouter();

// 响应式状态
const inputQuery = ref('');
const activeQuery = ref('');
const results = ref([]);
const loading = ref(false);
const error = ref(null);
const hasSearched = ref(false);

const pagination = reactive({
  currentPage: 0,
  totalPages: 0,
});

/**
 * 执行搜索的异步函数
 */
const performSearch = async (query, page = 0) => {
  if (!query || query.trim() === '') {
    results.value = [];
    hasSearched.value = true;
    return;
  }

  loading.value = true;
  error.value = null;
  hasSearched.value = true;
  activeQuery.value = query;

  try {
    const response = await apiClient.get('/plants/search', {
      params: { query, page, size: 12 },
    });

    if (response.data.success) {
      const data = response.data.data;
      results.value = data.content;
      pagination.currentPage = data.currentPage;
      pagination.totalPages = data.totalPages;
    } else {
      throw new Error(response.data.message || '返回数据格式不正确');
    }
  } catch (err) {
    error.value = err.response?.data?.message || '搜索植物时发生错误';
    console.error(err);
    results.value = []; // 出错时清空结果
  } finally {
    loading.value = false;
  }
};

/**
 * 处理表单提交
 */
const handleSearchSubmit = () => {
  // 当用户点击搜索按钮时，更新路由来触发搜索
  router.push({ name: 'Search', query: { q: inputQuery.value, page: 0 } });
};

/**
 * 处理分页变化
 */
const handlePageChange = (newPage) => {
  router.push({
    name: 'Search',
    query: { ...route.query, page: newPage },
  });
};

// 监听路由的变化
watch(
  () => route.query,
  (newQuery) => {
    const query = newQuery.q || '';
    const page = parseInt(newQuery.page, 10) || 0;

    // 只有当URL中存在查询参数'q'时，才执行搜索
    if (query) {
      inputQuery.value = query; // 同步输入框内容
      performSearch(query, page);
    } else {
      // 如果URL中没有查询参数，则重置页面到初始状态
      hasSearched.value = false;
      results.value = [];
      inputQuery.value = '';
    }
  },
  { immediate: true, deep: true } //确保组件加载时立即执行一次
);
</script>

<style scoped>
.search-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 2rem;
}

.search-header {
  text-align: center;
  margin-bottom: 3rem;
  padding: 2rem;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
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
  margin-bottom: 2rem;
}

.search-form {
  display: flex;
  max-width: 600px;
  margin: 0 auto;
  border-radius: 50px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.search-input {
  flex-grow: 1;
  border: none;
  padding: 1rem 1.5rem;
  font-size: 1.1rem;
  outline: none;
}

.search-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  border: none;
  background-color: var(--color-theme-green);
  color: white;
  padding: 0 1.8rem;
  cursor: pointer;
  transition: background-color 0.3s;
  font-size: 1.1rem;
  font-weight: 600;
}

.search-button:hover {
  background-color: #45a049;
}

.search-button svg {
  width: 20px;
  height: 20px;
}

.results-main {
  margin-top: 2rem;
}

.results-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 2rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e0e0e0;
}

.plant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.state-container {
  text-align: center;
  padding: 4rem;
  color: #718096;
}

.state-container.initial {
  font-size: 1.2rem;
}

.state-container.error {
  color: #d8000c;
  background-color: #ffd2d2;
  border-radius: 8px;
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
