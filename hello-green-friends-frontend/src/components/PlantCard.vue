<template>
  <div class="plant-card" @click="navigateToDetail">
    <div class="card-image-container">
      <img
        :src="plant.mainImageUrl || '/images/placeholder.png'"
        :alt="plant.name"
        class="plant-image"
        @error="onImageError"
      />
      <span
        v-if="plant.status"
        :class="['status-badge', getStatusClass(plant.status)]"
      >
        {{ getStatusText(plant.status) }}
      </span>
    </div>
    <div class="card-content">
      <h3 class="plant-name">{{ plant.name }}</h3>
      <p class="scientific-name">{{ plant.scientificName || '暂无学名' }}</p>

      <div v-if="plant.tags && plant.tags.length" class="tags-container">
        <span v-for="tag in plant.tags" :key="tag.id" class="tag-badge-card">
          {{ tag.name }}
        </span>
      </div>
      <p class="update-time">最后更新: {{ formattedDate }}</p>
    </div>
    <div class="card-actions" @click.stop>
      <slot name="actions"></slot>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps({
  plant: {
    type: Object,
    required: true,
  },
});

const router = useRouter();

const navigateToDetail = () => {
  const plantId = props.plant?.id || props.plant?.plantId;

  if (plantId) {
    router.push(`/plant/${plantId}`);
  }
};
const getStatusText = (status) => {
  const statusMap = {
    DRAFT: '草稿',
    PENDING_REVIEW: '审核中',
    PUBLISHED: '已发布',
    REJECTED: '已驳回',
  };
  return statusMap[status] || '未知';
};

const getStatusClass = (status) => {
  const classMap = {
    DRAFT: 'status-draft',
    PENDING_REVIEW: 'status-pending',
    PUBLISHED: 'status-published',
    REJECTED: 'status-rejected',
  };
  return classMap[status] || 'status-unknown';
};

const formattedDate = computed(() => {
  if (!props.plant.updatedAt) return 'N/A';
  return new Date(props.plant.updatedAt).toLocaleDateString();
});

const onImageError = (event) => {
  event.target.src = '/images/placeholder.png';
};
</script>

<style scoped>
.plant-card {
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  cursor: pointer;
}
.plant-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}
.card-image-container {
  position: relative;
  height: 200px;
}
.plant-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.status-badge {
  position: absolute;
  top: 1rem;
  right: 1rem;
  padding: 0.3rem 0.8rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
  color: #fff;
}
.status-draft {
  background-color: #718096;
}
.status-pending {
  background-color: #d69e2e;
}
.status-published {
  background-color: #38a169;
}
.status-rejected {
  background-color: #e53e3e;
}
.status-unknown {
  background-color: #a0aec0;
}

.card-content {
  padding: 1rem;
  flex-grow: 1;
}
.plant-name {
  font-size: 1.25rem;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 0.25rem;
}
.scientific-name {
  font-size: 0.9rem;
  color: #718096;
  font-style: italic;
  margin: 0 0 0.75rem;
}
.update-time {
  font-size: 0.8rem;
  color: #a0aec0;
  margin-top: 1rem;
}
.card-actions {
  padding: 0 1rem 1rem;
  display: flex;
  gap: 0.5rem;
  justify-content: flex-end;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-badge-card {
  background-color: #e9f5e9;
  color: var(--color-theme-green);
  padding: 0.2rem 0.6rem;
  border-radius: 16px;
  font-size: 0.75rem;
  font-weight: 500;
}
</style>
