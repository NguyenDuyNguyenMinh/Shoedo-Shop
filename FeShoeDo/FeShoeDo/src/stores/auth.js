import { defineStore } from 'pinia';
import api from '@/services/api';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('auth_token') || null,
    isLoading: false,
    error: null,
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.user,
    userRole: (state) => state.user?.role,
    userName: (state) => state.user?.name,
    isEmployee: (state) => state.user?.role === 'EMPLOYEE' || state.user?.role === 'ADMIN',
    isCustomer: (state) => state.user?.role === 'CUSTOMER',
    isAdmin: (state) => state.user?.vaiTro === 'Admin',
    isActive: (state) => state.user?.isActive === true,
    maUser: (state) => state.user?.maUser, 
    userName: (state) => state.user?.userName,
  },
  
  actions: {
    async login(credentials) {
      this.isLoading = true;
      this.error = null;
      
      try {
        const response = await api.login(credentials);
        
        if (response.data.success) {
          this.user = response.data.user;
          const token = btoa(JSON.stringify({
            maUser: response.data.user.maUser,
            exp: Date.now() + 24 * 60 * 60 * 1000
          }));
          localStorage.setItem('auth_token', token);
          return { success: true };
        } else {
          this.error = response.data.message;
          return { success: false, message: response.data.message };
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Đăng nhập thất bại';
        return { success: false, message: this.error };
      } finally {
        this.isLoading = false;
      }
    },
    
    async register(credentials) {
      this.isLoading = true;
      this.error = null;
      
      try {
        const registerData = {
          mail: credentials.mail,
          pass: credentials.pass,
          fullname: credentials.fullname,
          phone: credentials.phone,
          remember: credentials.remember || false
        };
        
        const response = await api.register(registerData);
        
        if (response.data.success) {
          this.user = response.data.user;
          const token = btoa(JSON.stringify({
            maUser: response.data.user.maUser,
            exp: Date.now() + 24 * 60 * 60 * 1000
          }));
          localStorage.setItem('auth_token', token);
          return { success: true };
        } else {
          this.error = response.data.message;
          return { success: false, message: response.data.message };
        }
      } catch (error) {
        this.error = error.response?.data?.message || 'Đăng ký thất bại';
        return { success: false, message: this.error };
      } finally {
        this.isLoading = false;
      }
    },
    
    async logout() {
      try {
        await api.logout();
        localStorage.removeItem('user');
        localStorage.removeItem('auth_token');
      } catch (error) {
        console.error('Logout error:', error);
      } finally {
        this.clearAuth();
      }
    },
    
    async fetchCurrentUser() {
      try {
        const response = await api.getCurrentUser();
        if (response.data.success) {
          this.user = response.data.user;
          const token = btoa(JSON.stringify({
            maUser: response.data.user.maUser,
            exp: Date.now() + 24 * 60 * 60 * 1000
          }));
          localStorage.setItem('auth_token', token);
          return true;
        }
      } catch (error) {
        this.clearAuth();
      }
      return false;
    },

    async loadUserFromToken() {
      if (!this.token) return false;
      
      try {
        const tokenData = JSON.parse(atob(this.token));
        
        if (tokenData.exp < Date.now()) {
          this.clearAuth();
          return false;
        }

        const response = await api.getCurrentUser();
        if (response.data.success) {
          this.user = response.data.user;
          return true;
        }
      } catch (error) {
        this.clearAuth();
      }
      return false;
    },

    clearAuth() {
      this.user = null;
      this.token = null;
      localStorage.removeItem('user');
      localStorage.removeItem('auth_token');
    }
  },
});