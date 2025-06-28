import { defineStore } from 'pinia';
import apiClient from '@/services/apiClient';

export const useAuthStore = defineStore('auth', {
  // 状态：存放所有响应式数据
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: null,
    status: 'idle',
    errorMessage: '',
  }),

  getters: {
    isAuthenticated: (state) =>
      !!state.token && state.status === 'authenticated',
    currentUser: (state) => state.user,
  },

  // actions：定义所有方法
  actions: {
    async register(credentials) {
      this.status = 'loading';
      this.clearError();
      try {
        const response = await apiClient.post('/auth/register', {
          username: credentials.username,
          nickname: credentials.nickname,
          email: credentials.email,
          password: credentials.password,
        });

        if (response.data.success) {
          this.status = 'success';
          return true;
        } else {
          throw new Error(response.data.message || '注册失败');
        }
      } catch (error) {
        this.status = 'error';
        this.errorMessage =
          error.response?.data?.message || '注册时发生未知错误';
        throw error;
      }
    },

    /**
     * 用户登录
     */
    async login(credentials) {
      this.status = 'loading';
      this.clearError();
      try {
        const response = await apiClient.post('/auth/login', credentials);
        if (response.data.success) {
          const token = response.data.data.accessToken;
          this.token = token;
          this.status = 'success';
          localStorage.setItem('token', token);
          // 立即获取用户信息
          await this.fetchUser();
        } else {
          throw new Error(response.data.message || '登录失败');
        }
      } catch (error) {
        this.status = 'error';
        this.errorMessage =
          error.response?.data?.message || '登录时发生未知错误';
        this.token = null;
        this.user = null;
        localStorage.removeItem('token');
        throw error;
      }
    },

    /**
     * 用户登出
     */
    async logout() {
      // 这里的登出只是前端操作
      this.token = null;
      this.user = null;
      this.status = 'idle';
      localStorage.removeItem('token');
    },

    /**
     * 获取当前登录的用户信息
     */
    async fetchUser() {
      if (!this.token) {
        return;
      }
      this.status = 'loading';
      try {
        const response = await apiClient.get('/users/me');
        if (response.data.success) {
          this.user = response.data.data;
          this.status = 'authenticated';
        } else {
          // 如果token无效，清空状态
          await this.logout();
        }
      } catch (error) {
        // 如果请求失败（如token过期导致401），也清空状态
        await this.logout();
      }
    },

    /**
     * 清除错误信息
     */
    clearError() {
      this.errorMessage = '';
    },
  },
});
