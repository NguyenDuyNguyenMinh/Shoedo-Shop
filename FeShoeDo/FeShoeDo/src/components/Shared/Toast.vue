<template>
  <div class="toast-container position-fixed top-0 start-50 translate-middle-x p-3 mt-2" style="z-index: 99999;">
    <transition-group name="toast-fade" tag="div">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="toast show align-items-center text-white border-0 shadow-lg overflow-hidden mb-2"
        :class="`bg-${toast.type}`"
        role="alert"
        style="min-width: 300px;"
      >
        <div class="d-flex position-relative z-1">
          <div class="toast-body d-flex align-items-center fs-6">
            <i
              class="bi me-2 fs-5"
              :class="{
                'bi-check-circle-fill': toast.type === 'success',
                'bi-exclamation-triangle-fill': toast.type === 'warning',
                'bi-x-circle-fill': toast.type === 'danger',
                'bi-info-circle-fill': toast.type === 'info'
              }"
            ></i>
            {{ toast.message }}
          </div>
          <button
            type="button"
            class="btn-close btn-close-white me-2 m-auto"
            @click="removeToast(toast.id)"
          ></button>
        </div>
        <div class="toast-progress-bar" :style="{ animationDuration: toast.duration + 'ms' }"></div>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { ref, onUnmounted } from 'vue';

const toasts = ref([]);
let nextId = 0;

const addToast = (message, type = 'success', duration = 3000) => {
  const id = nextId++;
  const toast = {
    id,
    message,
    type,
    duration,
    timer: null
  };
  
  toasts.value.push(toast);
  
  toast.timer = setTimeout(() => {
    removeToast(id);
  }, duration);
  
  return id;
};

const removeToast = (id) => {
  const index = toasts.value.findIndex(t => t.id === id);
  if (index !== -1) {
    if (toasts.value[index].timer) {
      clearTimeout(toasts.value[index].timer);
    }
    toasts.value.splice(index, 1);
  }
};

// Global functions
if (typeof window !== 'undefined') {
  window.showToast = addToast;
}

onUnmounted(() => {
  toasts.value.forEach(toast => {
    if (toast.timer) clearTimeout(toast.timer);
  });
});
</script>

<style scoped>
.toast-container {
  z-index: 99999;
}

.toast {
  position: relative;
  border-radius: 12px;
  backdrop-filter: blur(10px);
}

.toast-body {
  padding: 12px 16px;
  font-weight: 500;
}

.btn-close-white {
  filter: brightness(0) invert(1);
  opacity: 0.8;
  transition: opacity 0.2s;
}

.btn-close-white:hover {
  opacity: 1;
}

.toast-progress-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 3px;
  background-color: rgba(255, 255, 255, 0.7);
  animation: shrinkProgress linear forwards;
}

@keyframes shrinkProgress {
  from {
    width: 100%;
  }
  to {
    width: 0%;
  }
}

/* Sử dụng transition-group thay vì transition */
.toast-fade-move,
.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.3s ease;
}

.toast-fade-enter-from {
  opacity: 0;
  transform: translateY(-30px);
}

.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

.toast-fade-leave-active {
  position: absolute;
}

/* Toast colors */
.bg-success {
  background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
}

.bg-warning {
  background: linear-gradient(135deg, #ffc107 0%, #ff9f43 100%);
}

.bg-danger {
  background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
}

.bg-info {
  background: linear-gradient(135deg, #17a2b8 0%, #138496 100%);
}
</style>