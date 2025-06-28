<template>
  <div class="my-gallery-container">
    <header class="page-header">
      <h1 class="page-title">我的图鉴</h1>
      <router-link to="/my-encyclopedia/new" class="add-plant-button">
        + 申请收录新植物！🌟
      </router-link>
    </header>

    <div class="filter-controls">
      <button
        v-for="status in filterOptions"
        :key="status.value"
        @click="changeFilter(status.value)"
        :class="{ active: activeStatus === status.value }"
      >
        {{ status.text }}
      </button>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>正在加载您贡献的植物...</p>
    </div>

    <div v-else-if="error" class="error-state">
      <p>啊，加载失败了：{{ error }}</p>
    </div>

    <div v-else-if="plants.length === 0" class="empty-state">
      <h3>这里空空如也</h3>
      <p>您还没有创建任何植物条目，点击右上角按钮开始吧！</p>
    </div>

    <div v-else class="plant-grid">
      <PlantCard v-for="plant in plants" :key="plant.id" :plant="plant">
        <template #actions>
          <router-link
            :to="`/my-encyclopedia/edit/${plant.id}`"
            class="action-button edit"
          >
            编辑
          </router-link>
          <button @click="handleDelete(plant.id)" class="action-button delete">
            删除
          </button>
        </template>
      </PlantCard>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import apiClient from '@/services/apiClient';
import PlantCard from '@/components/PlantCard.vue';

const loading = ref(true);
const error = ref(null);
const plants = ref([]);

const activeStatus = ref(null);
const filterOptions = [
  { text: '全部', value: null },
  { text: '草稿', value: 'DRAFT' },
  { text: '审核中', value: 'PENDING_REVIEW' },
  { text: '已发布', value: 'PUBLISHED' },
  { text: '已驳回', value: 'REJECTED' },
];

const fetchMyPlants = async () => {
  loading.value = true;
  error.value = null;
  try {
    const params = {};
    if (activeStatus.value) {
      params.status = activeStatus.value;
    }
    const response = await apiClient.get('/my-gallery', { params });
    if (response.data.success && response.data.data.content) {
      plants.value = response.data.data.content;
    } else {
      plants.value = [];
    }
  } catch (err) {
    error.value = err.response?.data?.message || '获取数据失败';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const changeFilter = (status) => {
  activeStatus.value = status;
};

watch(activeStatus, fetchMyPlants);

onMounted(fetchMyPlants);

const handleDelete = async (plantId) => {
  if (confirm('确定要永久删除这个植物条目吗？')) {
    try {
      await apiClient.delete(`/my-gallery/${plantId}`);
      plants.value = plants.value.filter((p) => p.id !== plantId);
    } catch (err) {
      alert('删除失败: ' + (err.response?.data?.message || '未知错误'));
    }
  }
};
</script>

<style scoped>
.my-gallery-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 2rem;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}
.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2d3748;
}
.add-plant-button {
  background-color: #42b983;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 8px;
  text-decoration: none;
  font-weight: 600;
  transition: background-color 0.3s;
}
.add-plant-button:hover {
  background-color: #38a169;
}

.filter-controls {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
}
.filter-controls button {
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  border: 1px solid #cbd5e0;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-controls button.active {
  background-color: #2d3748;
  color: white;
  border-color: #2d3748;
}

.plant-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 2rem;
}

.loading-state,
.error-state,
.empty-state {
  text-align: center;
  padding: 4rem;
  color: #718096;
}
.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border-left-color: #42b983;
  animation: spin 1s ease infinite;
  margin: 0 auto 1rem;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.action-button {
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.875rem;
  font-weight: 500;
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}
.action-button.edit {
  background-color: #f0f4f8;
  color: #2d3748;
}
.action-button.edit:hover {
  background-color: #e2e8f0;
}
.action-button.delete {
  background-color: transparent;
  color: #e53e3e;
  border: 1px solid #e53e3e;
}
.action-button.delete:hover {
  background-color: #e53e3e;
  color: white;
}
</style>
