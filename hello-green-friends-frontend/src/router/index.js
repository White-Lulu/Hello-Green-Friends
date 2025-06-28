import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const HomeView = () => import('../views/HomeView.vue');
const MapView = () => import('../views/MapView.vue');
const PlantDetailView = () => import('../views/PlantDetailView.vue');
const EncyclopediaView = () => import('../views/EncyclopediaView.vue');
const SearchView = () => import('../views/SearchView.vue');
const UserProfileView = () => import('../views/UserProfileView.vue');
const MyEncyclopediaView = () => import('../views/MyEncyclopediaView.vue');
const MyCommentsView = () => import('../views/MyCommentsView.vue');
const LoginView = () => import('../views/LoginView.vue');
const RegisterView = () => import('../views/RegisterView.vue');
const PlantEditorView = () => import('../views/PlantEditorView.vue');
const AdminDashboardView = () => import('../views/AdminDashboardView.vue');

const routes = [
  {
    path: '/',
    redirect: '/home',
    meta: { title: '欢迎' },
  },
  {
    path: '/home',
    name: 'Home',
    component: HomeView,
    meta: { title: '欢迎' },
  },
  {
    path: '/map',
    name: 'Map',
    component: MapView,
    meta: { title: '地图导览', requiresAuth: false },
  },
  {
    path: '/plant/:id',
    name: 'PlantDetail',
    component: PlantDetailView,
    props: true,
    meta: { title: '植物详情' },
  },
  {
    path: '/encyclopedia',
    name: 'Encyclopedia',
    component: EncyclopediaView,
    meta: { title: '植物图鉴' },
  },
  {
    path: '/search',
    name: 'Search',
    component: SearchView,
    meta: { title: '搜索植物' },
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: UserProfileView,
    meta: { title: '个人信息', requiresAuth: true },
  },
  {
    path: '/my-encyclopedia',
    name: 'MyEncyclopedia',
    component: MyEncyclopediaView,
    meta: { title: '我的图鉴', requiresAuth: true },
  },
  {
    path: '/my-encyclopedia/new',
    name: 'PlantCreate',
    component: PlantEditorView,
    meta: { requiresAuth: true },
  },
  {
    path: '/my-encyclopedia/edit/:id',
    name: 'PlantEdit',
    component: PlantEditorView,
    meta: { requiresAuth: true },
  },
  {
    path: '/my-comments',
    name: 'MyComments',
    component: MyCommentsView,
    meta: { title: '我的评论', requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'register',
    component: RegisterView,
    meta: { guest: true },
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboardView,
    meta: { title: '管理员控制台', requiresAuth: true, requiresAdmin: true }, // 需要认证且需要管理员权限
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

// 全局路由守卫
router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title} - 校园植物导览` || '校园植物导览';
  const authStore = useAuthStore();

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next({ name: 'Login' });
  }
  else if (
    to.meta.requiresAdmin &&
    (!authStore.currentUser || authStore.currentUser.role !== 'ADMIN')
  ) {
    alert('您没有权限访问此页面！');
    next({ name: 'Home' });
  } else {
    next();
  }
});

export default router;
