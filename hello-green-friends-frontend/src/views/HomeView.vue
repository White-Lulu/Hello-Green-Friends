<template>
  <div class="home-container">
    <div class="hero-section">
      <h1 class="hero-title">Hello Green Friends ! 🍀</h1>
      <p class="hero-subtitle">探索校园内的每一片绿意，发现身边的植物世界</p>
      <div class="hero-actions">
        <router-link to="/map" class="button-start">
          开启探索之旅
          <span class="arrow-icon">→</span>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
// 页面很静态不需要任何脚本逻辑
</script>

<style scoped>
@keyframes gradient-animation {
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

.home-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 60px);
  text-align: center;
  color: white;
  padding: 2rem;
}
.hero-section {
  background-color: rgba(0, 0, 0, 0.25);
  padding: 3rem 4rem;
  border-radius: 20px;
  max-width: 800px;
  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.18);
  transform: translateY(-20px);
}

.hero-title {
  font-size: 3.5rem;
  font-weight: 700;
  margin-bottom: 1rem;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.hero-subtitle {
  font-size: 1.5rem;
  margin-bottom: 3rem;
  font-weight: 300;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 5px rgba(0, 0, 0, 0.2);
}

.hero-actions .button-start {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2.5rem;
  font-size: 1.25rem;
  font-weight: 600;
  color: white;
  background-color: var(--color-theme-green);
  border-radius: 50px;
  text-decoration: none;
  border: none;
  box-shadow: 0 5px 15px rgba(45, 181, 143, 0.4);
  transition: all 0.3s ease;
}

.hero-actions .button-start:hover {
  transform: translateY(-5px) scale(1.05);
  box-shadow: 0 12px 25px rgba(45, 181, 143, 0.6);
  filter: brightness(1.1);
}

.arrow-icon {
  transition: transform 0.3s ease;
}

.hero-actions .button-start:hover .arrow-icon {
  transform: translateX(5px);
}
</style>
