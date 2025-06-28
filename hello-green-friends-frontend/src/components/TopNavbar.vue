<template>
  <header class="top-navbar">
    <div class="navbar-content">
      <div class="logo-container">
        <router-link to="/home" class="logo">
          <span class="logo-icon">🌿</span>
          <span class="logo-text">Hello Green Friends</span>
        </router-link>
      </div>

      <nav class="main-nav">
        <router-link to="/map" class="nav-link">🗺 地图 Map </router-link>
        <router-link to="/encyclopedia" class="nav-link"
          >📚 图鉴 Encyclopedia
        </router-link>
        <router-link to="/search" class="nav-link">🔍 搜索 Search </router-link>
      </nav>

      <div class="right-options">
        <div
          v-if="isAuthenticated && user"
          class="user-menu"
          @click="toggleDropdown"
        >
          <img
            :src="user?.avatarUrl || '/images/default-avatar.png'"
            alt="用户头像"
            class="user-avatar"
            @error="onAvatarError"
          />
          <span class="username">你好, {{ user.nickname }}</span>

          <div v-if="showDropdown" class="dropdown-content" @click.stop>
            <RouterLink to="/profile">🤗 我的信息</RouterLink>
            <RouterLink to="/my-encyclopedia">📕 我的图鉴</RouterLink>
            <RouterLink to="/my-comments">✍ 我的评论</RouterLink>
            <a href="#" @click.prevent="handleLogout">🔙 退出登录</a>
          </div>
        </div>

        <div v-else class="auth-links">
          <router-link to="/login" class="nav-link login-button">
            Sign In / Sign Up
          </router-link>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue';
import { RouterLink, useRouter } from 'vue-router';
import { storeToRefs } from 'pinia';
import { useAuthStore } from '@/stores/auth';

// 响应式地从 Pinia store 获取状态和 getters
const authStore = useAuthStore();
const { isAuthenticated, currentUser: user } = storeToRefs(authStore);

const router = useRouter();

// 控制下拉菜单的显示/隐藏
const showDropdown = ref(false);

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

// 处理用户登出逻辑
const handleLogout = async () => {
  await authStore.logout();
  showDropdown.value = false;
  router.push({ name: 'Login' });
};

const onAvatarError = (event) => {
  event.target.src = '/images/default-avatar.png';
};
</script>

<style scoped>
.top-navbar {
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  width: 100%;
  z-index: 1000;
  transition: background-color 0.3s ease;
}

.navbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 2rem;
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1.25rem;
  font-weight: 600;
  color: #1a202c;
  text-decoration: none;
}

.logo-icon {
  font-size: 1.75rem;
}

.main-nav {
  display: flex;
  gap: 2.5rem;
}

.nav-link {
  text-decoration: none;
  color: #4a5568;
  font-size: 1rem;
  font-weight: 500;
  padding: 0.5rem 0;
  position: relative;
  transition: color 0.3s;
}

.nav-link::after {
  content: '';
  position: absolute;
  width: 0;
  height: 2px;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  background-color: #42b983;
  transition: width 0.3s ease;
}

.nav-link:hover,
.nav-link.router-link-exact-active {
  color: #2d3748;
}

.nav-link:hover::after,
.nav-link.router-link-exact-active::after {
  width: 100%;
}

.right-options {
  min-width: 150px;
  display: flex;
  justify-content: flex-end;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 1rem;
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.username {
  font-weight: 500;
  color: #2d3748;
}

.dropdown-content {
  position: absolute;
  top: 120%;
  right: 0;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 0.5rem 0;
  min-width: 160px;
  z-index: 1001;
  display: flex;
  flex-direction: column;
}

.dropdown-content a {
  padding: 0.75rem 1.5rem;
  color: #2d3748;
  text-decoration: none;
  display: block;
}

.dropdown-content a:hover {
  background-color: #f7fafc;
}

.login-button {
  border: 1px solid #42b983;
  color: #42b983;
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  transition: all 0.3s;
}

.login-button:hover {
  background-color: #42b983;
  color: white;
}
</style>
