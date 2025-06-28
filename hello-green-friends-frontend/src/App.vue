<template>
  <div class="app-wrapper">
    <div class="animated-bg"></div>
    <div class="content-container">
      <TopNavbar />
      <main class="flex-grow container mx-auto px-4 py-8">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
      <footer class="text-center py-5 text-sm text-gray-400">
        <p>
          &copy; {{ new Date().getFullYear() }} 校园植物导览. All Rights
          Reserved.
        </p>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import TopNavbar from './components/TopNavbar.vue';
import { useAuthStore } from './stores/auth';

const authStore = useAuthStore();

onMounted(() => {
  authStore.fetchUser();
});
</script>

<style>
.app-wrapper {
  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
}

.animated-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
  background: linear-gradient(-45deg, #ee7752, #e73c7e, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
  filter: blur(80px);
  opacity: 0.3;
}

@keyframes gradientBG {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.content-container {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}
</style>
