import axios from 'axios';

const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use(
  async (config) => {
    const { useAuthStore } = await import('@/stores/auth');
    const authStore = useAuthStore();
    const token = authStore.token;

    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  // 对成功响应不执行任何操作
  (response) => response,
  // 对错误响应执行操作
  async (error) => {
    // 在拦截器函数内部动态导入并使用 store
    const { useAuthStore } = await import('@/stores/auth');
    const authStore = useAuthStore();
    // 检查是否有响应体，并且错误状态码是 401 或 403
    if (
      error.response &&
      (error.response.status === 401 || error.response.status === 403)
    ) {
      console.error('认证失败或凭证无效，将自动登出。');
      // 调用 Pinia store 中的登出方法
      authStore.logout();
    }
    // 将错误继续抛出，使组件中的 catch 块可以捕获
    return Promise.reject(error);
  }
);

export default apiClient;
