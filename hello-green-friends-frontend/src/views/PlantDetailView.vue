<template>
  <div class="plant-detail-container">
    <div v-if="loading" class="state-container">
      <div class="spinner"></div>
      <p>正在为您呈现植物的奥秘...</p>
    </div>

    <div v-else-if="error" class="state-container error">
      <p>抱歉，加载植物详情失败了：{{ error }}</p>
      <router-link to="/encyclopedia" class="back-link">返回图鉴</router-link>
    </div>

    <article v-else-if="plant" class="plant-content">
      <div class="main-content-grid">
        <div class="image-section">
          <img
            :src="plant.mainImageUrl || '/images/placeholder.png'"
            :alt="plant.name"
            class="main-image"
            @error="onImageError"
          />
        </div>
        <div class="info-section">
          <h1 class="plant-name">{{ plant.name }}</h1>
          <p class="scientific-name">{{ plant.scientificName }}</p>
          <div class="tags-container">
            <span v-for="tag in plant.tags" :key="tag.id" class="tag-badge">
              {{ tag.name }}
            </span>
          </div>
          <div class="detail-item">
            <h3 class="detail-title">植物档案</h3>
            <p class="detail-text">{{ plant.description || '暂无描述' }}</p>
          </div>
          <div class="detail-item">
            <h3 class="detail-title">花期</h3>
            <p class="detail-text">{{ plant.floweringPeriod || '未知' }}</p>
          </div>
          <div class="detail-item">
            <h3 class="detail-title">在哪里找到我：</h3>
            <ul class="location-list">
              <li v-for="loc in plant.locations" :key="loc.id">
                <strong>{{ loc.areaName }}</strong
                >: {{ loc.specificName }}
              </li>
            </ul>
          </div>
        </div>
      </div>

      <section class="image-gallery-section">
        <h2 class="section-title">植物图库</h2>
        <div v-if="authStore.isAuthenticated" class="image-upload-area">
          <input
            type="file"
            multiple
            @change="onFilesChange"
            accept="image/*"
            ref="fileInput"
          />
          <button
            @click="uploadAdditionalImages"
            :disabled="!selectedFiles.length || isUploadingImage"
          >
            {{ isUploadingImage ? '上传中...' : '上传多张图片' }}
          </button>
          <p v-if="uploadError" class="error-message">{{ uploadError }}</p>
        </div>
        <div v-else class="image-upload-prompt">
          <p>
            <router-link to="/login">登录</router-link>
            后，即可分享更多精彩植物图片~
          </p>
        </div>

        <div
          v-if="plant.images && plant.images.length > 0"
          class="image-gallery-scroll-container"
        >
          <div class="image-gallery">
            <div
              v-for="(image, index) in plant.images"
              :key="image.id"
              class="gallery-item"
              :class="{ 'pending-review-border': isMyPendingImage(image) }"
              @click="openCarousel(index)"
            >
              <img
                :src="image.imageUrl"
                :alt="'植物图片 ' + image.id"
                class="gallery-image"
                @error="onImageError"
              />
              <div class="image-overlay">
                <p class="image-uploader">
                  上传者:
                  {{ image.uploader ? image.uploader.nickname : '未知' }}
                </p>
                <p class="image-date">
                  上传日期: {{ new Date(image.createdAt).toLocaleDateString() }}
                </p>
                <p
                  class="image-status"
                  :class="image.status ? image.status.toLowerCase() : ''"
                >
                  状态: {{ formatImageStatus(image.status) }}
                </p>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-gallery">
          <p>
            各路神仙摄影师,走过路过千万不要错过...这里有新鲜出土的绿植模特一枚~
          </p>
        </div>
      </section>

      <section class="comments-section">
        <h2 class="section-title">评论区</h2>
        <div v-if="authStore.isAuthenticated" class="comment-form">
          <textarea
            v-model="newCommentText"
            placeholder="分享你的见闻与知识..."
            rows="4"
            :disabled="isSubmittingComment"
          ></textarea>
          <button
            @click="submitTopLevelComment"
            :disabled="isSubmittingComment"
          >
            {{ isSubmittingComment ? '提交中...' : '发表评论' }}
          </button>
        </div>
        <div v-else class="comment-login-prompt">
          <p><router-link to="/login">登录</router-link> 以发表评论</p>
        </div>

        <div v-if="topLevelComments.length > 0" class="comment-list">
          <CommentItem
            v-for="comment in topLevelComments"
            :key="comment.id"
            :comment="comment"
            :plant-id="plant.id"
            @comment-submitted="handleNewComment"
          />
        </div>
        <div v-else class="empty-comments">
          <p>还没有人为这位绿色朋友写下赛博留言</p>
        </div>
      </section>
    </article>

    <ImageCarouselModal
      v-if="showCarouselModal && plant && plant.images"
      :images="plant.images"
      :initial-index="carouselInitialIndex"
      :visible="showCarouselModal"
      @close="closeCarousel"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import apiClient from '@/services/apiClient';
import { useAuthStore } from '@/stores/auth';
import CommentItem from '@/components/CommentItem.vue';
import ImageCarouselModal from '@/components/ImageCarouselModal.vue';

const props = defineProps({
  id: { type: [String, Number], required: true },
});
const authStore = useAuthStore();
const plant = ref(null);
const loading = ref(true);
const error = ref(null);
const newCommentText = ref('');
const isSubmittingComment = ref(false);
const selectedFiles = ref([]);
const isUploadingImage = ref(false);
const uploadError = ref(null);
const fileInput = ref(null);
const topLevelComments = ref([]);
const showCarouselModal = ref(false);
const carouselInitialIndex = ref(0);

// 获取植物详情
const fetchPlantDetails = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await apiClient.get(`/plants/${props.id}`);
    if (response.data.success) {
      plant.value = response.data.data;
      // 确保 images 数组存在，即使为空
      if (!plant.value.images) {
        plant.value.images = [];
      }
      await fetchComments();
    } else {
      throw new Error(response.data.message || '返回数据格式不正确');
    }
  } catch (err) {
    console.error('获取植物详情失败:', err);
    error.value = err.response?.data?.message || '找不到该植物或加载失败';
  } finally {
    loading.value = false;
  }
};

// 获取评论
const fetchComments = async () => {
  try {
    const response = await apiClient.get(`/plants/${props.id}/comments`);
    if (response.data.success && response.data.data.content) {
      topLevelComments.value = response.data.data.content;
    }
  } catch (err) {
    console.error('获取评论失败:', err);
  }
};

// 处理新评论（包括顶级评论和回复）
const handleNewComment = (newComment) => {
  if (!newComment.parentId) {
    topLevelComments.value.unshift(newComment);
    return;
  }

  const findAndAddReply = (comments, reply) => {
    for (const comment of comments) {
      if (comment.id === reply.parentId) {
        if (!comment.replies) {
          comment.replies = [];
        }
        comment.replies.push(reply);
        return true;
      }
      if (comment.replies && comment.replies.length > 0) {
        if (findAndAddReply(comment.replies, reply)) {
          return true;
        }
      }
    }
    return false;
  };

  findAndAddReply(topLevelComments.value, newComment);
};

// 提交顶级评论
const submitTopLevelComment = async () => {
  if (!newCommentText.value.trim()) {
    alert('评论内容不能为空！');
    return;
  }
  isSubmittingComment.value = true;
  try {
    const response = await apiClient.post(`/plants/${props.id}/comments`, {
      content: newCommentText.value,
      parentCommentId: null,
    });
    if (response.data.success) {
      handleNewComment(response.data.data);
      newCommentText.value = '';
    }
  } catch (err) {
    alert('发表评论失败: ' + (err.response?.data?.message || err.message));
  } finally {
    isSubmittingComment.value = false;
  }
};

// 图片加载失败时显示占位图
const onImageError = (event) => {
  event.target.src = '/images/placeholder.png';
};

// 处理多文件选择
const onFilesChange = (event) => {
  uploadError.value = null; // 清除之前的错误信息
  const files = Array.from(event.target.files);
  const tooLargeFiles = files.filter((file) => file.size > 5 * 1024 * 1024); // 限制文件大小
  if (tooLargeFiles.length > 0) {
    uploadError.value = '部分文件大小超过 5MB，请重新选择。';
    selectedFiles.value = [];
    if (fileInput.value) fileInput.value.value = ''; // 清空文件输入框
    return;
  }
  selectedFiles.value = files;
};

// 上传多张图片
const uploadAdditionalImages = async () => {
  if (!selectedFiles.value.length) {
    uploadError.value = '请选择至少一个文件！';
    return;
  }

  isUploadingImage.value = true;
  uploadError.value = null;

  const uploadedImagesData = [];
  for (const file of selectedFiles.value) {
    const formData = new FormData();
    formData.append('file', file);
    try {
      const response = await apiClient.post(
        `/plants/${props.id}/additional-images`,
        formData,
        {
          headers: {
            'Content-Type': 'multipart/form-data',
          },
        }
      );
      if (response.data.success) {
        uploadedImagesData.push(response.data.data);
      } else {
        console.error('单张图片上传失败:', file.name, response.data.message);
        uploadError.value = `部分图片上传失败: ${file.name} - ${
          response.data.message || '未知错误'
        }`;
        // 如果有任何一个失败，停止并显示错误
        isUploadingImage.value = false;
        return;
      }
    } catch (err) {
      console.error('上传图片失败:', file.name, err);
      uploadError.value = `上传图片时发生错误: ${file.name} - ${
        err.response?.data?.message || err.message
      }`;
      isUploadingImage.value = false;
      return;
    }
  }

  // 所有图片上传成功后，更新图片列表
  if (!plant.value.images) {
    plant.value.images = [];
  }
  // 将新上传的图片添加到列表开头
  plant.value.images.unshift(...uploadedImagesData);
  selectedFiles.value = []; // 清空文件选择
  if (fileInput.value) fileInput.value.value = ''; // 清空文件输入框
  alert('所有图片上传成功，等待审核！'); // 提示用户图片需审核
  isUploadingImage.value = false;
};

// 判断是否是当前用户上传的待审核图片
const isMyPendingImage = (image) => {
  return (
    authStore.isAuthenticated &&
    authStore.currentUser &&
    image.uploader &&
    authStore.currentUser.id === image.uploader.id &&
    image.status === 'PENDING_REVIEW'
  );
};

// 打开图片轮播模态框
const openCarousel = (index) => {
  console.log(
    'Opening carousel, showCarouselModal before:',
    showCarouselModal.value
  );
  carouselInitialIndex.value = index;
  showCarouselModal.value = true;
  console.log(
    'Opening carousel, showCarouselModal after:',
    showCarouselModal.value
  );
};

// 关闭图片轮播模态框
const closeCarousel = () => {
  showCarouselModal.value = false;
  // 使用 nextTick 确保在 Vue 更新 DOM后执行
  nextTick(() => {
    document.body.style.overflow = ''; // 恢复页面滚动
  });
  console.log('showCarouselModal before:', showCarouselModal.value);
  console.log('showCarouselModal after:', showCarouselModal.value);
};

// 格式化图片状态
const formatImageStatus = (status) => {
  switch (status) {
    case 'PENDING_REVIEW':
      return '待审核';
    case 'APPROVED':
      return '已通过';
    case 'REJECTED':
      return '已拒绝';
    default:
      return '未知';
  }
};

onMounted(() => {
  fetchPlantDetails();
});
</script>

<style scoped>
.plant-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.main-content-grid {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 3rem;
  background-color: #fff;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.05);
}

.image-section {
  grid-column: span 12;
}

@media (min-width: 768px) {
  .image-section {
    grid-column: span 5;
  }
}

.main-image {
  width: 100%;
  height: auto;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  border-radius: 12px;
  border: 1px solid #eee;
}

.info-section {
  grid-column: span 12;
}

@media (min-width: 768px) {
  .info-section {
    grid-column: span 7;
  }
}

.plant-name {
  font-size: 2.5rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
}

.scientific-name {
  font-size: 1.2rem;
  font-style: italic;
  color: #6c757d;
  margin-bottom: 1.5rem;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 2rem;
}

.tag-badge {
  background-color: #e9f5e9;
  color: var(--color-theme-green);
  padding: 0.3rem 0.8rem;
  border-radius: 16px;
  font-size: 0.85rem;
  font-weight: 500;
}

.detail-item {
  margin-bottom: 1.5rem;
}

.detail-title {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-dark);
  margin-bottom: 0.5rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.detail-text {
  color: #495057;
  line-height: 1.7;
}

.location-list {
  list-style-type: none;
  padding-left: 0;
  color: #495057;
}

/* 图片墙样式 */
.image-gallery-section {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 1px solid #e0e0e0;
}

.image-upload-area {
  display: flex;
  gap: 1rem;
  margin-bottom: 2rem;
  align-items: center;
  padding: 1rem;
  background-color: #f0f8f0;
  border-radius: 8px;
}

.image-upload-area input[type='file'] {
  flex-grow: 1;
  padding: 0.5rem;
  border: 1px solid #c8e6c9;
  border-radius: 4px;
}

.image-upload-area button {
  padding: 0.75rem 1.5rem;
  background-color: var(--color-theme-green);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
}

.image-upload-area button:disabled {
  background-color: #9e9e9e;
  cursor: not-allowed;
}

.image-upload-prompt {
  text-align: center;
  padding: 2rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.image-gallery-scroll-container {
  overflow-x: auto;
  white-space: nowrap;
  padding-bottom: 1rem; 
}

.image-gallery {
  display: flex;
  gap: 1.5rem;
  align-items: flex-start;
}

.gallery-item {
  position: relative;
  width: 200px;
  height: 200px;
  flex-shrink: 0;
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.2s ease-in-out;
}

.gallery-item:hover {
  transform: translateY(-5px);
}

.pending-review-border {
  border: 3px solid #4caf50;
}

.gallery-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 0.75rem;
  transform: translateY(100%);
  transition: transform 0.3s ease-in-out;
  font-size: 0.85rem;
  box-sizing: border-box;
  white-space: normal;
}

.gallery-item:hover .image-overlay {
  transform: translateY(0);
}

.image-uploader,
.image-date,
.image-status {
  margin-bottom: 0.25rem;
  line-height: 1.3;
}

.image-status {
  font-weight: bold;
}

.image-status.pending_review {
  color: orange;
}

.image-status.approved {
  color: lightgreen;
}

.image-status.rejected {
  color: red;
}

.error-message {
  color: #dc3545;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.comments-section {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 1px solid #e0e0e0;
}

.section-title {
  font-size: 1.8rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
}

.comment-form {
  margin-bottom: 2rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.comment-form textarea {
  width: 100%;
  padding: 1rem;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 1rem;
  resize: vertical;
}

.comment-form button {
  padding: 0.75rem 1.5rem;
  background-color: var(--color-theme-green);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  align-self: flex-end;
}

.comment-form button:disabled {
  background-color: #9e9e9e;
  cursor: not-allowed;
}

.comment-login-prompt {
  text-align: center;
  padding: 2rem;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 2rem;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.comment-item {
  background-color: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
  border-left: 4px solid var(--color-theme-green);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.comment-author {
  font-weight: 600;
  color: var(--text-dark);
}

.comment-date {
  font-size: 0.85rem;
  color: #6c757d;
}

.comment-body {
  margin: 0;
  line-height: 1.6;
  color: #495057;
}

.empty-comments {
  text-align: center;
  padding: 2rem;
  color: #6c757d;
}

@media (min-width: 768px) {
  .image-section {
    grid-column: span 5;
  }
  .info-section {
    grid-column: span 7;
  }
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
