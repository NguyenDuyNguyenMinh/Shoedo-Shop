import { createApp } from 'vue'
import { createPinia } from 'pinia'
import './style.css'
import App from './App.vue';
import router from './router';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-icons/font/bootstrap-icons.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import '@fortawesome/fontawesome-free/css/all.min.css';

const app = createApp(App);
const pinia = createPinia();

app.use(router);
app.use(pinia); // Sử dụng Pinia
app.mount('#app');
