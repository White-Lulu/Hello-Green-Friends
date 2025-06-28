<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2 class="auth-title">欢迎回来 🌱</h2>
      <p class="auth-subtitle">登录您的账户，继续探索绿意盎然的世界！</p>

      <form @submit.prevent="handleLogin">
        <div class="input-group">
          <label for="username">账号</label>
          <input
            type="text"
            id="username"
            v-model="username"
            required
            placeholder="请输入您的账号"
          />
        </div>

        <div class="input-group">
          <label for="password">密码</label>
          <input
            type="password"
            id="password"
            v-model="password"
            required
            placeholder="请输入您的密码"
          />
        </div>

        <div v-if="auth.errorMessage" class="error-message">
          {{ auth.errorMessage }}
        </div>

        <button
          type="submit"
          class="auth-button"
          :disabled="auth.status === 'loading'"
        >
          {{ auth.status === 'loading' ? '登录中...' : '登 录' }}
        </button>
      </form>

      <div class="auth-switch">
        <span>还没有账户？</span>
        <router-link to="/register" class="switch-link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const username = ref('');
const password = ref('');

const router = useRouter();
const auth = useAuthStore();

// 组件卸载时清空可能存在的错误信息
onUnmounted(() => {
  auth.clearError();
});

const handleLogin = async () => {
  if (!username.value || !password.value) {
    auth.errorMessage = '用户名和密码不能为空';
    return;
  }

  try {
    await auth.login({
      username: username.value,
      password: password.value,
    });
    router.push('/map');
  } catch (error) {
    console.error('登录流程失败:', error);
  }
};
</script>

<style scoped>
@import '@/assets/auth-form.css';
</style>
