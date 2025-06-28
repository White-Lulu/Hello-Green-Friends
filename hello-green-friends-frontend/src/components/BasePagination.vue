<template>
  <div v-if="totalPages > 1" class="pagination-container">
    <button @click="changePage(currentPage - 1)" :disabled="currentPage <= 0">
      &laquo; 上一页
    </button>
    <span>第 {{ currentPage + 1 }} 页 / 共 {{ totalPages }} 页</span>
    <button
      @click="changePage(currentPage + 1)"
      :disabled="currentPage >= totalPages - 1"
    >
      下一页 &raquo;
    </button>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, defineOptions } from 'vue';

defineOptions({
  name: 'BasePagination',
});

defineProps({
  currentPage: {
    type: Number,
    required: true,
  },
  totalPages: {
    type: Number,
    required: true,
  },
});

const emit = defineEmits(['page-change']);

const changePage = (page) => {
  emit('page-change', page);
};
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-top: 2.5rem;
  gap: 1rem;
}
button {
  padding: 0.6rem 1.2rem;
  border: 1px solid #ddd;
  background-color: #fff;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
}
button:hover:not(:disabled) {
  background-color: #f0f0f0;
  border-color: #ccc;
}
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
span {
  color: #555;
  font-weight: 500;
}
</style>
