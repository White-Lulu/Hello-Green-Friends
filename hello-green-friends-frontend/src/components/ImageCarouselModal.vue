<template>
  <transition name="modal-fade">
    <div class="image-carousel-modal-overlay" @click.self="close">
      <div class="image-carousel-modal-content">
        <button class="close-button" @click="close">&times;</button>
        <button
          class="nav-button prev-button"
          @click="prevImage"
          :disabled="currentIndex === 0"
        >
          &#10094;
        </button>
        <img
          :src="currentImage.imageUrl"
          :alt="`植物图片 ${currentImage.id}`"
          class="carousel-image"
          @error="onImageError"
        />
        <button
          class="nav-button next-button"
          @click="nextImage"
          :disabled="currentIndex === images.length - 1"
        >
          &#10095;
        </button>

        <div class="image-meta-info">
          <p>
            上传者:
            {{
              currentImage.uploader ? currentImage.uploader.nickname : '未知'
            }}
          </p>
          <p>
            上传日期:
            {{ new Date(currentImage.createdAt).toLocaleDateString() }}
          </p>
          <p
            :class="
              currentImage.status ? currentImage.status.toLowerCase() : ''
            "
          >
            状态: {{ formatImageStatus(currentImage.status) }}
          </p>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, watch, computed, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  images: {
    type: Array,
    required: true,
  },
  initialIndex: {
    type: Number,
    default: 0,
  },
  visible: {
    type: Boolean,
    required: true,
  },
});

const emit = defineEmits(['close']);
const currentIndex = ref(props.initialIndex);

watch(
  () => props.initialIndex,
  (newVal) => {
    currentIndex.value = newVal;
  }
);

const currentImage = computed(() => {
  return props.images[currentIndex.value];
});

const prevImage = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--;
  }
};

const nextImage = () => {
  if (currentIndex.value < props.images.length - 1) {
    currentIndex.value++;
  }
};

const close = () => {
  emit('close');
  console.log('Modal close called');
};

const onImageError = (event) => {
  event.target.src = '/images/placeholder.png';
};

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

const handleKeyDown = (event) => {
  if (!props.visible) return;
  if (event.key === 'ArrowLeft') {
    prevImage();
  } else if (event.key === 'ArrowRight') {
    nextImage();
  } else if (event.key === 'Escape') {
    close();
  }
};

onMounted(() => {
  window.addEventListener('keydown', handleKeyDown);
});

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyDown);
});

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = '';
    }
  },
  { immediate: true }
);
</script>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.image-carousel-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.image-carousel-modal-content {
  position: relative;
  max-width: 90%;
  max-height: 90%;
  background-color: #1a1a1a;
  border-radius: 12px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.carousel-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  display: block;
}

.close-button {
  position: absolute;
  top: 15px;
  right: 20px;
  background: none;
  border: none;
  font-size: 3rem;
  color: white;
  cursor: pointer;
  z-index: 2001;
  transition: color 0.2s ease;
}

.close-button:hover {
  color: #ccc;
}

.nav-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  padding: 15px 20px;
  font-size: 2.5rem;
  cursor: pointer;
  z-index: 2001;
  border-radius: 50%;
  line-height: 1;
  transition: background-color 0.2s ease;
}

.nav-button:hover:not(:disabled) {
  background-color: rgba(0, 0, 0, 0.7);
}

.nav-button:disabled {
  opacity: 0.3;
  cursor: not-allowed;
}

.prev-button {
  left: 20px;
}

.next-button {
  right: 20px;
}

.image-meta-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 1rem;
  font-size: 0.95rem;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.image-meta-info p {
  margin: 0;
}

.image-meta-info .pending_review {
  color: orange;
}

.image-meta-info .approved {
  color: lightgreen;
}

.image-meta-info .rejected {
  color: red;
}
</style>
