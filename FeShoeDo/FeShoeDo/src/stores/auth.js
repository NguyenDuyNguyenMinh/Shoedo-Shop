import { defineStore } from 'pinia';
import axios from 'axios'; 

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
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
  },
  
  actions: {
    async initAuth() {
      if (this.isInitialized) return;
      this.isLoading = true;
      
      try {
        const success = await this.checkSession();
        if (success) {
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
        console.error('Init auth error:', error);
        this.clearAuth();
      } finally {
        this.isLoading = false;
        this.isInitialized = true;
      }
    },

    async checkSession() {
      try {
        const response = await axios.get('/api/auth/current-user', {
          withCredentials: true
        });

        if (response.data.success && response.data.user) {
          this.user = response.data.user;
          this.cartCount = response.data.user.cartCount || 0;
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
      } catch (error) {
        console.error('Logout error:', error);
      } finally {
        window.location.href = '/auth/login';
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
          return true;
        }
      } catch (error) {
        console.error('Fetch current user error:', error);
        this.clearAuth();
      }
      return false;
    },

    async updateCartCount() {
      if (!this.isAuthenticated) {
        this.cartCount = 0;
        return;
      }
      
      try {
        const response = await axios.get('/api/customer/cart/count', {
          withCredentials: true
        });

        if (response.data.success) {
          this.cartCount = response.data.cartCount || 0;
        } else {
          this.cartCount = 0;
        }
      } catch (error) {
        console.error('Error updating cart count:', error);
        this.cartCount = 0;
      }
    },

    setCartCount(count) {
      this.cartCount = Math.max(0, count);
    },

    incrementCartCount(delta = 1) {
      this.cartCount = Math.max(0, this.cartCount + delta);
    },

    clearAuth() {
      this.user = null;
      this.cartCount = 0;
      this.logout;
    }
  }
});