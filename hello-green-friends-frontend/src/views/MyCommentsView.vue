<template>
  <div class="my-comments-container">
    <h1 class="page-title">我的评论</h1>

    <div v-if="loading" class="state-container">
      <div class="spinner"></div>
      <p>正在加载您的精彩评论...</p>
    </div>

    <div v-else-if="error" class="state-container error">
      <p>抱歉，加载评论失败了：{{ error }}</p>
    </div>

    <div v-else-if="comments.length === 0" class="state-container">
      <p>期待您为绿色朋友们的精彩留言~</p>
      <router-link to="/encyclopedia" class="back-link"
        >去逛逛植物图鉴</router-link
      >
    </div>

    <div v-else class="comments-list">
      <div
        v-for="(comment, index) in comments"
        :key="comment.id"
        class="comment-card"
      >
        <div class="comment-content">
          <p :class="{ 'deleted-text': !comment.user }">
            "{{ comment.content }}"
          </p>
          <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
        </div>
        <div class="plant-info">
          <span>评论于：</span>
          <router-link :to="`/plant/${comment.plant.id}`" class="plant-link">
            <img
              :src="comment.plant.mainImageUrl || '/images/placeholder.png'"
              alt="plant image"
              class="plant-thumbnail"
            />
            <span>{{ comment.plant.name }}</span>
          </router-link>
        </div>
        <div class="actions">
          <button
            @click="confirmDelete(comment.id, index)"
            :disabled="!comment.user || deletingId === comment.id"
            class="delete-button"
          >
            <span v-if="deletingId === comment.id">删除中...</span>
            <span v-else>删除</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import apiClient from '@/services/apiClient';

const comments = ref([]);
const loading = ref(true);
const error = ref(null);
const deletingId = ref(null);

const fetchMyComments = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await apiClient.get('/my-comments');
    if (response.data.success) {
      comments.value = response.data.data.content;
    } else {
      throw new Error(response.data.message);
    }
  } catch (err) {
    console.error('获取我的评论失败:', err);
    error.value = err.response?.data?.message || err.message;
  } finally {
    loading.value = false;
  }
};

const confirmDelete = (commentId, index) => {
  if (window.confirm('您确定要永久删除这条评论吗？')) {
    deleteComment(commentId, index);
  }
};

const deleteComment = async (commentId, index) => {
  deletingId.value = commentId;
  try {
    await apiClient.delete(`/comments/${commentId}`);
    comments.value[index].content = '此评论已被作者删除';
    comments.value[index].user = null;
  } catch (err) {
    alert('删除失败：' + (err.response?.data?.message || err.message));
  } finally {
    deletingId.value = null;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleString('zh-CN');
};

onMounted(fetchMyComments);
</script>

<style scoped>
.my-comments-container {
  max-width: 900px;
  margin: 0 auto;
  margin-top: 2rem;
  margin-bottom: 2rem;
  padding: 2rem;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}
.page-title {
  font-size: 2rem;
  font-weight: 700;
  margin-bottom: 2rem;
  text-align: center;
}
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.comment-card {
  display: grid;
  grid-template-columns: 1fr auto;
  grid-template-rows: auto auto;
  gap: 0.5rem 1rem;
  padding: 1.5rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  border-left: 4px solid var(--color-theme-green);
  align-items: center;
}
.comment-content {
  grid-column: 1 / 3;
}
.comment-content p {
  font-size: 1.1rem;
  line-height: 1.6;
  margin-bottom: 0.5rem;
}
.comment-content .deleted-text {
  color: #999;
  font-style: italic;
}
.comment-date {
  font-size: 0.8rem;
  color: #6c757d;
}
.plant-info {
  grid-column: 1 / 2;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #495057;
}
.plant-link {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  text-decoration: none;
  font-weight: 600;
  color: var(--color-theme-green);
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  transition: background-color 0.2s;
}
.plant-link:hover {
  background-color: #e9f5e9;
}
.plant-thumbnail {
  width: 32px;
  height: 32px;
  object-fit: cover;
  border-radius: 50%;
}
.actions {
  grid-column: 2 / 3;
  justify-self: end;
}
.delete-button {
  background-color: #fbecec;
  color: #c82333;
  border: 1px solid #f5c6cb;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
}
.delete-button:hover:not(:disabled) {
  background-color: #c82333;
  color: #fff;
}
.delete-button:disabled {
  background-color: #e9ecef;
  color: #6c757d;
  border-color: #ced4da;
  cursor: not-allowed;
}

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
.back-link {
  margin-top: 1rem;
  display: inline-block;
  color: var(--color-theme-green);
  text-decoration: none;
  font-weight: 600;
}
</style>
