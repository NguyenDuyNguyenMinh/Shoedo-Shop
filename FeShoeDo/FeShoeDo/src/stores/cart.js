import { defineStore } from 'pinia';
import api from '@/services/api';
import { useAuthStore } from './auth';

export const useCartStore = defineStore('cart', {
  state: () => ({
    cartItems: [],
    cartItemCount: 0,
    cartTotalQuantity: 0,
    loading: false
  }),
  
  actions: {
    async fetchCart() {
      const authStore = useAuthStore();
      if (!authStore.isAuthenticated) {
        this.cartItems = [];
        this.cartItemCount = 0;
        return;
      }
      
      this.loading = true;
       try {
        const response = await api.getCartCount();
        if (response.data.success) {
          this.cartItemCount = response.data.count || 0;        // Số phân loại
          this.cartTotalQuantity = response.data.itemCount || 0; // Tổng số lượng
        }
      } catch (error) {
        console.error('Error fetching cart count:', error);
        this.cartItemCount = 0;
        this.cartTotalQuantity = 0;
      }
    },
    
    async fetchCartCount() {
      const authStore = useAuthStore();
      if (!authStore.isAuthenticated) {
        this.cartItemCount = 0;
        return;
      }
      
      try {
        const response = await api.getCartCount();
        if (response.data.success) {
          this.cartItemCount = response.data.count || 0;
        }
      } catch (error) {
        console.error('Error fetching cart count:', error);
        this.cartItemCount = 0;
      }
    },
    
    async addToCart(productId, quantity = 1, plspId = null) {
      try {
        const response = await api.addToCart({
          maSP: productId,
          maPLSP: plspId,
          soLuong: quantity
        });
        
        if (response.data.success) {
          await this.fetchCartCount(); // Chỉ cập nhật count
          return true;
        }
        return false;
      } catch (error) {
        console.error('Error adding to cart:', error);
        return false;
      }
    },
    
    async updateCartItem(itemId, quantity) {
      try {
        const response = await api.updateCartItem(itemId, quantity);
        if (response.data.success) {
          await this.fetchCartCount();
          return true;
        }
        return false;
      } catch (error) {
        console.error('Error updating cart item:', error);
        return false;
      }
    },
    
    async removeFromCart(itemId) {
      try {
        const response = await api.removeFromCart(itemId);
        if (response.data.success) {
          await this.fetchCartCount();
          return true;
        }
        return false;
      } catch (error) {
        console.error('Error removing from cart:', error);
        return false;
      }
    },
    
    clearCart() {
      this.cartItems = [];
      this.cartItemCount = 0;
    }
  }
});