import { defineStore } from 'pinia';
import axios from 'axios';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: localStorage.getItem('auth_token') || null,
    isLoading: false,
    error: null,
    cartCount: 0,
    isInitialized: false,
  }),
  
  getters: {
    isAuthenticated: (state) => !!state.user,
    userRole: (state) => state.user?.role,
    userName: (state) => state.user?.name,
    lastName: (state) => {
      if (state.user?.name) {
        const nameParts = state.user.name.split(' ');
        return nameParts[nameParts.length - 1];
      }
      return '';
    },
    isEmployee: (state) => state.user?.role === 'EMPLOYEE' || state.user?.role === 'ADMIN',
    isCustomer: (state) => state.user?.role === 'CUSTOMER',
    isAdmin: (state) => state.user?.vaiTro === 'Admin',
    isActive: (state) => state.user?.isActive === true,
    maUser: (state) => state.user?.maUser,
    userName: (state) => state.user?.userName,
  },
  
  actions: {
    
    async initAuth() {
      if (this.isInitialized) return;
      this.isLoading = true;
      
      try {
        if (this.token) {
          const success = await this.loadUserFromToken();
          if (success) {
            this.isInitialized = true;
            return;
          }
        }

        const sessionSuccess = await this.checkSession();
        if (sessionSuccess) {
          this.isInitialized = true;
          return;
        }

        const cookieSuccess = await this.autoLoginFromCookie();
        if (cookieSuccess) {
          this.isInitialized = true;
          return;
        }
        this.clearAuth();
      } catch (error) {
        this.clearAuth();
      } finally {
        this.isLoading = false;
        this.isInitialized = true;
      }
    },

    async checkSession() {
      try {
        const response = await axios.get('/api/auth/check-session', {
          withCredentials: true
        });
        
        if (response.data.success && response.data.user) {
          this.user = response.data.user;
          this.cartCount = response.data.user.cartCount || 0;

          if (!this.token) {
            const token = btoa(JSON.stringify({
              maUser: response.data.user.maUser,
              exp: Date.now() + 24 * 60 * 60 * 1000
            }));
            localStorage.setItem('auth_token', token);
            this.token = token;
          }
          return true;
        }
      } catch (error) {
        console.error('Check session error:', error);
      }
      return false;
    },

    async autoLoginFromCookie() {
      try {
        const response = await axios.get('/api/auth/auto-login', {
          withCredentials: true
        });
        
        if (response.data.success && response.data.user) {
          this.user = response.data.user;
          this.cartCount = response.data.user.cartCount || 0;
          const token = btoa(JSON.stringify({
            maUser: response.data.user.maUser,
            exp: Date.now() + 24 * 60 * 60 * 1000
          }));
          localStorage.setItem('auth_token', token);
          this.token = token;
          return true;
        }
      } catch (error) {
        console.error('Auto login error:', error);
      }
      return false;
    },

    async logout() {
      try {
        await axios.post('/api/auth/logout', {}, {
          withCredentials: true
        });
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
        const response = await axios.get('/api/auth/current-user', {
          withCredentials: true
        });
        if (response.data.success) {
          this.user = response.data.user;
          this.cartCount = response.data.user.cartCount || 0;
          
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
    
    async updateCartCount() {
      if (this.isAuthenticated) {
        try {
          const response = await axios.get('/api/auth/current-user', {
            withCredentials: true
          });
          if (response.data.success) {
            this.cartCount = response.data.user.cartCount || 0;
          }
        } catch (error) {
          console.error('Error updating cart count:', error);
        }
      }
    },
    
    async loadUserFromToken() {
      if (!this.token) return false;
      
      try {
        const tokenData = JSON.parse(atob(this.token));
        
        if (tokenData.exp < Date.now()) {
          this.clearAuth();
          return false;
        }

        const response = await axios.get('/api/auth/current-user');
        if (response.data.success) {
          this.user = response.data.user;
          this.cartCount = response.data.user.cartCount || 0;
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
      this.cartCount = 0;
      localStorage.removeItem('user');
      localStorage.removeItem('auth_token');
    }
  },
});