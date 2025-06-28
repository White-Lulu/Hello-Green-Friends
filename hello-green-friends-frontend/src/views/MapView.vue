<template>
  <div class="map-page-container">
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <p>正在加载生动的地图数据...</p>
    </div>
    <div v-if="error" class="error-message">
      <p>哎呀，地图加载失败了：{{ error }}，请稍后再试</p>
    </div>

    <div v-if="clickedCoords && !selectedPlant" class="coords-display-panel">
      <p>
        这里的坐标为: <br />
        <strong>X:</strong> {{ clickedCoords.x }}, <strong>Y:</strong>
        {{ clickedCoords.y }}
      </p>
      <button @click="copyCoords" class="copy-button">复制</button>
      <button @click="clickedCoords = null" class="close-button">×</button>
    </div>

    <div class="map-container" :class="{ 'is-loading': loading }">
      <l-map
        ref="mapRef"
        v-model:zoom="zoom"
        :center="center"
        :crs="crs"
        :min-zoom="-1"
        :max-zoom="2"
        :zoom-snap="1"
        :use-global-leaflet="false"
        @click="handleMapClick"
        @ready="onMapReady"
        @move="updateMarkerPositions"
        @zoom="updateMarkerPositions"
        @mousemove="handleMouseMove"
      >
        <l-image-overlay
          :url="mapImageUrl"
          :bounds="mapBounds"
        ></l-image-overlay>
      </l-map>

      <div class="custom-marker-pane">
        <div
          v-for="marker in customMarkers"
          :key="marker.plantId"
          class="custom-marker"
          :style="{
            transform: `translate(${marker.x}px, ${marker.y}px)`,
          }"
        ></div>
      </div>
    </div>

    <div v-if="selectedPlant" class="selected-plant-overlay">
      <PlantCard :plant="selectedPlant" />
      <button
        @click="selectedPlant = null"
        class="close-card-button"
        title="关闭"
      >
        &times;
      </button>
    </div>
  </div>
</template>

<script setup>
import { LMap, LImageOverlay } from '@vue-leaflet/vue-leaflet';
import { CRS } from 'leaflet';
import apiClient from '@/services/apiClient';
import PlantCard from '@/components/PlantCard.vue';
import { ref, onMounted } from 'vue';

// 状态定义
const loading = ref(true);
const error = ref(null);
const plantPoints = ref([]);
const clickedCoords = ref(null);
const selectedPlant = ref(null);
const customMarkers = ref([]);
const isHoveringClickableArea = ref(false);

// 地图配置
const mapRef = ref(null);
const crs = CRS.Simple;
const zoom = ref(0);
const mapImageUrl = ref('/campus-map.png');
const mapBounds = ref([
  [0, 0],
  [1750, 2800],
]);
const center = ref([1000, 1700]);


let mapInstance = null;

// 地图准备好后触发
const onMapReady = () => {
  mapInstance = mapRef.value.leafletObject;
  updateMarkerPositions(); // 初始化标记位置
};

// 更新所有自定义标记的屏幕位置
const updateMarkerPositions = () => {
  if (!mapInstance || plantPoints.value.length === 0) {
    return;
  }
  customMarkers.value = plantPoints.value.map((plant) => {
    const screenPoint = mapInstance.latLngToContainerPoint([
      plant.mapCoordY,
      plant.mapCoordX,
    ]);
    return {
      plantId: plant.plantId,
      x: screenPoint.x - 19,
      y: screenPoint.y - 38,
    };
  });
};

const handleMapClick = (event) => {
  if (!event || !event.latlng) return;
  const clickRadius = 30;
  const clickedLat = event.latlng.lat;
  const clickedLng = event.latlng.lng;
  let foundPlant = null;
  for (const plant of plantPoints.value) {
    const distance = Math.sqrt(
      Math.pow(clickedLng - plant.mapCoordX, 2) +
        Math.pow(clickedLat - plant.mapCoordY, 2)
    );
    if (distance <= clickRadius) {
      foundPlant = plant;
      break;
    }
  }
  if (foundPlant) {
    selectedPlant.value = foundPlant;
    clickedCoords.value = null;
  } else {
    selectedPlant.value = null;
    clickedCoords.value = {
      x: clickedLng.toFixed(2),
      y: clickedLat.toFixed(2),
    };
  }
};

// 处理鼠标移动的函数
const handleMouseMove = (event) => {
  if (!event || !event.latlng || plantPoints.value.length === 0) return;

  const clickRadius = 30;
  const mouseLat = event.latlng.lat;
  const mouseLng = event.latlng.lng;

  // 检查鼠标是否在任何一个植物点的半径内
  const isNearMarker = plantPoints.value.some((plant) => {
    const distance = Math.sqrt(
      Math.pow(mouseLng - plant.mapCoordX, 2) +
        Math.pow(mouseLat - plant.mapCoordY, 2)
    );
    return distance <= clickRadius;
  });

  isHoveringClickableArea.value = isNearMarker;
};

// 复制坐标到剪贴板
const copyCoords = () => {
  if (!clickedCoords.value) return;
  const coords = `X: ${clickedCoords.value.x}, Y: ${clickedCoords.value.y}`;
  navigator.clipboard.writeText(coords).then(() => {
    console.log('坐标已复制到剪贴板:', coords);
  });
};

// 生命周期钩子
onMounted(async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await apiClient.get('/plants/map-points');
    if (response.data.success) {
      plantPoints.value = response.data.data;
      updateMarkerPositions(); // 获取数据后也更新一次
      if (plantPoints.value.length > 0) {
        center.value = [
          plantPoints.value[0].mapCoordY,
          plantPoints.value[0].mapCoordX,
        ];
      }
    } else {
      throw new Error(response.data.message || '无法获取植物数据');
    }
  } catch (err) {
    error.value = err.message || '请求失败，请检查网络连接。';
    console.error('获取地图数据失败:', err);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.custom-marker-pane {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 500;
}

.custom-marker {
  position: absolute;
  width: 38px;
  height: 38px;
  background-image: url('@/assets/images/leaf-marker.png');
  background-size: contain;
  background-repeat: no-repeat;
  will-change: transform;
}

.map-page-container {
  position: relative;
  width: 100%;
  height: calc(100vh - 64px);
  overflow: hidden;
}

.map-container {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #f0f4f8;
  cursor: v-bind("isHoveringClickableArea ? 'pointer' : 'default'");
}

.selected-plant-overlay {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 1001;
  width: 90%;
  max-width: 340px;
  border-radius: 12px;
  animation: slide-up 0.5s ease-out;
}

@keyframes slide-up {
  from {
    transform: translate(-50%, 100px);
    opacity: 0;
  }
  to {
    transform: translateX(-50%);
    opacity: 1;
  }
}

.coords-display-panel {
  position: absolute;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(5px);
  padding: 1rem 1.5rem;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1001;
}
.coords-display-panel p {
  margin: 0;
  font-size: 0.9rem;
  color: #2d3748;
}
.copy-button,
.close-button {
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 600;
}
.copy-button {
  color: #42b983;
}
.close-button {
  font-size: 1.5rem;
  color: #a0aec0;
  line-height: 1;
}
.plant-popup {
  font-family: 'Helvetica Neue', Arial, sans-serif;
  min-width: 200px;
}
.popup-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 0.75rem;
}
.popup-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin: 0 0 0.75rem 0;
  color: #2d3748;
}
.popup-link {
  display: inline-block;
  text-decoration: none;
  color: #42b983;
  font-weight: 500;
  transition: color 0.3s;
  cursor: pointer;
}
.popup-link:hover {
  color: #2d3748;
}
.spinner {
  border: 4px solid rgba(0, 0, 0, 0.1);
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border-left-color: #42b983;
  animation: spin 1s ease infinite;
  margin: 0 auto 1rem;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
