<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2 class="auth-title">加入绿色大家庭🌼</h2>
      <p class="auth-subtitle">创建一个新账户，开启您的校园植物探索之旅！</p>

      <form @submit.prevent="handleRegister">
        <div class="input-group">
          <label for="username">账号</label>
          <input
            type="text"
            id="username"
            v-model="username"
            required
            placeholder="设置您的账号"
          />
        </div>

        <div class="input-group">
          <label for="nickname">昵称</label>
          <input
            type="text"
            id="nickname"
            v-model="nickname"
            required
            placeholder="您希望被如何称呼"
          />
        </div>

        <div class="input-group">
          <label for="email">邮箱地址</label>
          <input
            type="email"
            id="email"
            v-model="email"
            required
            placeholder="请输入您的邮箱"
          />
        </div>
        <div class="input-group">
          <label for="password">密码</label>
          <input
            type="password"
            id="password"
            v-model="password"
            required
            placeholder="设置您的密码"
          />
        </div>

        <div class="input-group">
          <label for="confirmPassword">确认密码</label>
          <input
            type="password"
            id="confirmPassword"
            v-model="confirmPassword"
            required
            placeholder="再次输入密码"
          />
        </div>

        <div v-if="clientError || auth.errorMessage" class="error-message">
          {{ clientError || auth.errorMessage }}
        </div>

        <div v-if="successMessage" class="success-message">
          {{ successMessage }}
        </div>

        <button
          type="submit"
          class="auth-button"
          :disabled="auth.status === 'loading'"
        >
          {{ auth.status === 'loading' ? '注册中...' : '注 册' }}
        </button>
      </form>

      <div class="auth-switch">
        <span>已经有账户了？</span>
        <router-link to="/login" class="switch-link">直接登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const username = ref('');
const nickname = ref('');
const email = ref('');
const password = ref('');
const confirmPassword = ref('');
const clientError = ref('');
const successMessage = ref('');

const router = useRouter();
const auth = useAuthStore();

onUnmounted(() => {
  auth.clearError();
});

const handleRegister = async () => {
  clientError.value = '';
  if (!username.value || !nickname.value || !email.value || !password.value) {
    clientError.value = '请完整填写所有必填信息哦！';
    return;
  }
  if (password.value !== confirmPassword.value) {
    clientError.value = '两次输入的密码不一致，请检查。';
    return;
  }

  try {
    const success = await auth.register({
      username: username.value,
      nickname: nickname.value,
      email: email.value,
      password: password.value,
    });

    if (success) {
      successMessage.value = '注册成功🎉！正在跳转到登录页面...';
      setTimeout(() => {
        router.push('/login');
      }, 2000);
    }
  } catch (error) {
    console.error('注册流程失败:', error);
  }
};
</script>

<style scoped>
@import '@/assets/auth-form.css';
</style>
