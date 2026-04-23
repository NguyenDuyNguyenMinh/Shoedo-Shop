import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue';
import router from './router';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import '@fortawesome/fontawesome-free/css/all.min.css';
import axios from 'axios';

axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response && error.response.status === 401) {
      if (window.location.pathname !== '/auth/login') {
        window.location.href = '/auth/login';
      }
    }
    return Promise.reject(error);
  }
);

window.addEventListener('storage', (event) => {
  if (event.key === 'auth-event' && event.newValue && event.newValue.startsWith('logout')) {
    if (window.location.pathname !== '/auth/login') {
      window.location.href = '/auth/login';
    }
  }
});

const app = createApp(App);
const pinia = createPinia();

app.use(router);
app.use(pinia);
app.mount('#app');
