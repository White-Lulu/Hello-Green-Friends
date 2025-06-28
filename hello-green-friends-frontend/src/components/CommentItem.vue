<template>
  <div class="comment-item-container">
    <div class="comment-item-main">
      <div v-if="comment.user" class="comment-header">
        <img
          :src="comment.user.avatarUrl || '/images/default-avatar.png'"
          alt="用户头像"
          class="comment-avatar"
          @error="onAvatarError"
        />
        <div class="author-info">
          <span class="comment-author">{{ comment.user.nickname }}</span>
          <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
        </div>
      </div>
      <div v-else class="comment-header">
        <img
          src="/images/default-delete-avatar.png"
          alt="匿名用户头像"
          class="comment-avatar anonymous"
        />
        <div class="author-info">
          <span class="comment-author anonymous-text">神秘小草</span>
          <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
        </div>
      </div>

      <p class="comment-body">{{ comment.content }}</p>

      <div v-if="comment.user" class="comment-actions">
        <button @click="showReplyForm = !showReplyForm" class="reply-button">
          {{ showReplyForm ? '取消回复' : '回复' }}
        </button>
      </div>
    </div>

    <div v-if="showReplyForm" class="reply-form-wrapper">
      <textarea
        v-model="replyText"
        :placeholder="`回复 @${comment.user.nickname}...`"
        rows="3"
        class="reply-textarea"
      ></textarea>
      <button @click="submitReply" class="submit-reply-button">提交回复</button>
    </div>

    <div
      v-if="comment.replies && comment.replies.length > 0"
      class="replies-list"
    >
      <CommentItem
        v-for="reply in comment.replies"
        :key="reply.id"
        :comment="reply"
        :plant-id="plantId"
        @comment-submitted="$emit('comment-submitted', $event)"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'CommentItem',
};
</script>

<script setup>
import { ref } from 'vue';
import apiClient from '@/services/apiClient';

const props = defineProps({
  comment: { type: Object, required: true },
  plantId: { type: [String, Number], required: true },
});

const emit = defineEmits(['comment-submitted']);

const showReplyForm = ref(false);
const replyText = ref('');

const submitReply = async () => {
  if (!replyText.value.trim()) return;
  try {
    const payload = {
      content: replyText.value,
      parentCommentId: props.comment.id,
    };
    const response = await apiClient.post(
      `/plants/${props.plantId}/comments`,
      payload
    );
    if (response.data.success) {
      emit('comment-submitted', response.data.data);
      replyText.value = '';
      showReplyForm.value = false;
    }
  } catch (error) {
    alert('回复失败: ' + (error.response?.data?.message || error.message));
  }
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  return new Date(dateString).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const onAvatarError = (event) => {
  event.target.src = '/images/default-avatar.png';
};
</script>

<style scoped>
.comment-item-main {
  background-color: #f8f9fa;
  padding: 1.5rem;
  border-radius: 8px;
  border-left: 4px solid var(--color-theme-green);
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}
.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #ddd;
}
.author-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.comment-author {
  font-weight: 600;
  line-height: 1.2;
}
.comment-date {
  font-size: 0.8rem;
  color: #6c757d;
  line-height: 1.2;
}
.comment-body {
  margin: 0;
  line-height: 1.6;
  color: #495057;
}
.comment-actions {
  margin-top: 1rem;
}
.reply-button {
  background: none;
  border: none;
  color: var(--color-theme-green);
  cursor: pointer;
  font-weight: 600;
  padding: 0;
}
.replies-list {
  margin-top: 1.5rem;
  padding-left: 2rem;
  border-left: 2px solid #e9ecef;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.reply-form-wrapper {
  margin-top: 1rem;
  padding-left: 1rem;
}
.reply-textarea {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 0.9rem;
  margin-bottom: 0.5rem;
}
.submit-reply-button {
  padding: 0.5rem 1rem;
  background-color: var(--color-theme-green);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  float: right;
  font-size: 0.9rem;
}

.comment-avatar.anonymous {
  filter: grayscale(100%);
  opacity: 0.6;
}
.anonymous-text {
  color: #888;
  font-style: italic;
}
</style>
