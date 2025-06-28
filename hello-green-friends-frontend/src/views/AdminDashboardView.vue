<template>
  <div class="admin-dashboard-container">
    <h1 class="page-title">管理员控制台</h1>

    <nav class="admin-tabs">
      <button
        :class="{ active: currentTab === 'pendingPlants' }"
        @click="currentTab = 'pendingPlants'"
      >
        待审核植物
      </button>
    </nav>

    <div class="tab-content">
      <div v-if="currentTab === 'pendingPlants'">
        <h2 class="section-title">待审核植物列表</h2>

        <div v-if="loadingPlants" class="state-container">
          <div class="spinner"></div>
          <p>正在加载待审核植物...</p>
        </div>

        <div v-else-if="plantError" class="state-container error">
          <p>加载失败：{{ plantError }}</p>
        </div>

        <div v-else-if="pendingPlants.length > 0" class="plant-list">
          <div
            v-for="plant in pendingPlants"
            :key="plant.id"
            class="plant-card"
          >
            <img
              :src="plant.mainImageUrl || '/images/placeholder.png'"
              :alt="plant.name"
              class="plant-image"
              @error="onImageError"
            />
            <div class="plant-info">
              <h3>{{ plant.name }} ({{ plant.scientificName }})</h3>
              <p>
                提交者: {{ plant.creator ? plant.creator.nickname : '未知' }}
              </p>
              <p>提交时间: {{ new Date(plant.createdAt).toLocaleString() }}</p>
              <div class="plant-actions">
                <button
                  class="approve-button"
                  @click="reviewPlant(plant.id, 'PUBLISHED')"
                  :disabled="isReviewing === plant.id"
                >
                  {{ isReviewing === plant.id ? '处理中...' : '通过' }}
                </button>
                <button
                  class="reject-button"
                  @click="openRejectModal(plant.id)"
                  :disabled="isReviewing === plant.id"
                >
                  {{ isReviewing === plant.id ? '处理中...' : '拒绝' }}
                </button>
              </div>
            </div>
          </div>
          <div class="pagination">
            <button
              @click="changePlantPage(currentPage - 1)"
              :disabled="currentPage === 0"
            >
              上一页
            </button>
            <span>第 {{ currentPage + 1 }} / {{ totalPlantPages }} 页</span>
            <button
              @click="changePlantPage(currentPage + 1)"
              :disabled="currentPage === totalPlantPages - 1"
            >
              下一页
            </button>
          </div>
        </div>

        <div v-else class="empty-list">
          <p>暂无待审核植物。</p>
        </div>
      </div>

      <div
        v-if="showRejectModal"
        class="modal-overlay"
        @click.self="closeRejectModal"
      >
        <div class="modal-content">
          <h3>拒绝植物图鉴</h3>
          <p>请输入拒绝理由：</p>
          <textarea
            v-model="rejectNotes"
            rows="4"
            placeholder="例如：图片不清晰，信息不准确等。"
          ></textarea>
          <div class="modal-actions">
            <button class="cancel-button" @click="closeRejectModal">
              取消
            </button>
            <button
              class="confirm-button"
              @click="confirmRejectPlant"
              :disabled="isReviewing !== null"
            >
              {{ isReviewing !== null ? '提交中...' : '确认拒绝' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import apiClient from '@/services/apiClient';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router'; // 引入 useRouter

const authStore = useAuthStore();
const router = useRouter(); // 初始化 router

const currentTab = ref('pendingPlants'); // 当前激活的选项卡
const pendingPlants = ref([]);
const loadingPlants = ref(true);
const plantError = ref(null);
const isReviewing = ref(null); // 存储正在审核的植物ID，防止重复点击

// 分页相关
const currentPage = ref(0);
const totalPlantPages = ref(0);
const totalPlantElements = ref(0);
const pageSize = 10; // 每页显示数量

// 拒绝模态框相关
const showRejectModal = ref(false);
const plantIdToReject = ref(null);
const rejectNotes = ref('');

// 检查用户权限，如果不是 ADMIN 则重定向
const checkAdminRole = () => {
  if (
    !authStore.isAuthenticated ||
    (authStore.currentUser && authStore.currentUser.role !== 'ADMIN')
  ) {
    alert('您没有权限访问此页面！');
    router.push('/'); // 重定向到首页
  }
};

// 获取待审核植物列表
const fetchPendingPlants = async () => {
  loadingPlants.value = true;
  plantError.value = null;
  try {
    const response = await apiClient.get('/admin/plants/pending', {
      params: {
        page: currentPage.value,
        size: pageSize,
      },
    });
    if (response.data.success) {
      pendingPlants.value = response.data.data.content;
      totalPlantPages.value = response.data.data.totalPages;
      totalPlantElements.value = response.data.data.totalElements;
    } else {
      throw new Error(response.data.message || '获取待审核植物失败');
    }
  } catch (err) {
    console.error('获取待审核植物失败:', err);
    plantError.value = err.response?.data?.message || '加载待审核植物列表失败';
  } finally {
    loadingPlants.value = false;
  }
};

// 审核植物 (通过或拒绝)
const reviewPlant = async (plantId, status) => {
  isReviewing.value = plantId; // 设置正在审核的植物ID
  try {
    const payload = {
      status: status, // PUBLISHED 或 REJECTED
      reviewNotes: status === 'REJECTED' ? rejectNotes.value : null,
    };
    const response = await apiClient.put(
      `/admin/plants/${plantId}/status`,
      payload
    );
    if (response.data.success) {
      alert(
        `植物 ${plantId} 已成功${status === 'PUBLISHED' ? '通过' : '拒绝'}！`
      );
      // 成功后从列表中移除该植物，并重新加载列表或更新本地数据
      pendingPlants.value = pendingPlants.value.filter(
        (plant) => plant.id !== plantId
      );
      // 如果当前页没有植物了，且不是第一页，则回到上一页
      if (pendingPlants.value.length === 0 && currentPage.value > 0) {
        currentPage.value--;
        fetchPendingPlants();
      } else {
        fetchPendingPlants(); // 重新获取当前页数据以更新状态
      }
      closeRejectModal(); // 关闭模态框
    } else {
      throw new Error(response.data.message || `处理植物 ${plantId} 失败`);
    }
  } catch (err) {
    console.error(`处理植物 ${plantId} 失败:`, err);
    alert(
      `处理植物 ${plantId} 失败: ` +
        (err.response?.data?.message || err.message)
    );
  } finally {
    isReviewing.value = null; // 重置审核状态
  }
};

// 打开拒绝模态框
const openRejectModal = (plantId) => {
  plantIdToReject.value = plantId;
  rejectNotes.value = ''; // 清空理由
  showRejectModal.value = true;
};

// 关闭拒绝模态框
const closeRejectModal = () => {
  showRejectModal.value = false;
  plantIdToReject.value = null;
  rejectNotes.value = '';
};

// 确认拒绝植物
const confirmRejectPlant = () => {
  if (!rejectNotes.value.trim()) {
    alert('请输入拒绝理由！');
    return;
  }
  reviewPlant(plantIdToReject.value, 'REJECTED');
};

// 改变植物列表页码
const changePlantPage = (newPage) => {
  if (newPage >= 0 && newPage < totalPlantPages.value) {
    currentPage.value = newPage;
    fetchPendingPlants();
  }
};

// 图片加载失败时显示占位图
const onImageError = (event) => {
  event.target.src = '/images/placeholder.png';
};

onMounted(() => {
  checkAdminRole(); // 页面加载时检查权限
  if (
    authStore.isAuthenticated &&
    authStore.currentUser &&
    authStore.currentUser.role === 'ADMIN'
  ) {
    fetchPendingPlants();
  }
});
</script>

<style scoped>
.admin-dashboard-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.page-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: var(--text-dark);
  margin-bottom: 2rem;
  text-align: center;
}

.admin-tabs {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
  border-bottom: 2px solid #e0e0e0;
}

.admin-tabs button {
  background-color: #f0f0f0;
  border: none;
  padding: 1rem 2rem;
  cursor: pointer;
  font-size: 1.1rem;
  font-weight: 600;
  color: #555;
  border-radius: 8px 8px 0 0;
  transition: all 0.3s ease;
}

.admin-tabs button:hover {
  background-color: #e0e0e0;
}

.admin-tabs button.active {
  background-color: var(--color-theme-green);
  color: white;
  border-bottom: 2px solid var(--color-theme-green);
  transform: translateY(2px); /* 视觉效果 */
}

.tab-content {
  background-color: #fff;
  padding: 2rem;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.section-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  color: var(--text-dark);
  text-align: center;
}

.plant-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.plant-card {
  border: 1px solid #eee;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
}

.plant-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.plant-info {
  padding: 1rem;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.plant-info h3 {
  font-size: 1.2rem;
  margin-top: 0;
  margin-bottom: 0.5rem;
  color: var(--text-dark);
}

.plant-info p {
  font-size: 0.9rem;
  color: #6c757d;
  margin-bottom: 0.25rem;
}

.plant-actions {
  margin-top: 1rem;
  display: flex;
  gap: 0.75rem;
}

.approve-button,
.reject-button {
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.3s ease;
  flex-grow: 1;
}

.approve-button {
  background-color: var(--color-theme-green);
  color: white;
}

.approve-button:hover {
  background-color: #43a047;
}

.approve-button:disabled {
  background-color: #9e9e9e;
  cursor: not-allowed;
}

.reject-button {
  background-color: #f44336; /* Red */
  color: white;
}

.reject-button:hover {
  background-color: #d32f2f;
}

.reject-button:disabled {
  background-color: #9e9e9e;
  cursor: not-allowed;
}

.empty-list {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
  background-color: #f8f9fa;
  border-radius: 8px;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 2rem;
  gap: 1rem;
}

.pagination button {
  padding: 0.6rem 1.2rem;
  background-color: var(--color-theme-green);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.pagination button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.pagination span {
  font-size: 1.1rem;
  color: var(--text-dark);
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  width: 90%;
  max-width: 500px;
  text-align: center;
}

.modal-content h3 {
  margin-top: 0;
  font-size: 1.5rem;
  color: var(--text-dark);
}

.modal-content p {
  margin-bottom: 1rem;
  color: #495057;
}

.modal-content textarea {
  width: calc(100% - 20px);
  padding: 10px;
  margin-bottom: 1rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
}

.modal-actions button {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.modal-actions .cancel-button {
  background-color: #6c757d;
  color: white;
}

.modal-actions .confirm-button {
  background-color: #f44336;
  color: white;
}

/* 加载和错误状态的通用样式 */
.state-container {
  text-align: center;
  padding: 4rem;
  color: #718096;
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
