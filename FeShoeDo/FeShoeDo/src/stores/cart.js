import { defineStore } from 'pinia';
import api from '@/services/api';
import { useAuthStore } from './auth';

export const useCartStore = defineStore('cart', {
  state: () => ({
    /** @type {Array} Cart items from API */
    items: [],
    /** @type {Array<number>} maGH values of selected items */
    selectedIds: [],
    /** @type {boolean} Loading state */
    loading: false,
    /** @type {string|null} Error message */
    error: null,
    /** @type {boolean} Whether cart has been fetched */
    isLoaded: false,
  }),

  getters: {
    /**
     * Items currently checked for checkout.
     * @returns {Array}
     */
    selectedItems: (state) => state.items.filter((item) => state.selectedIds.includes(item.maGH)),

    /**
     * True when all items are selected.
     * @returns {boolean}
     */
    allSelected: (state) => state.items.length > 0 && state.selectedIds.length === state.items.length,

    /**
     * Sum of thanhTien for selected items.
     * @returns {number}
     */
    subtotal: (state) =>
      state.items
        .filter((item) => state.selectedIds.includes(item.maGH))
        .reduce((sum, item) => sum + (item.thanhTien || 0), 0),

    /**
     * Total original price - discounted price for selected items.
     * @returns {number}
     */
    totalDiscount: (state) =>
      state.items
        .filter((item) => state.selectedIds.includes(item.maGH))
        .reduce((sum, item) => {
          const giaGoc = item.giaGoc || 0;
          const giaSauKM = item.giaSauKM || giaGoc;
          return sum + (giaGoc - giaSauKM) * (item.soLuong || 0);
        }, 0),

    /**
     * Total unique item count in cart.
     * @returns {number}
     */
    cartCount: (state) => state.items.length,
  },

  actions: {
    /**
     * Fetch cart from API and populate state.
     * Auto-selects all items after fetch.
     */
    async fetchCart() {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.getCart();
        const data = response.data;
        // Handle multiple possible response shapes
        this.items = (data.items || data.data?.items || [])?.map((item) => ({
          ...item,
          updating: false,
        })) || [];
        // Auto-select all
        this.selectedIds = this.items.map((item) => item.maGH);
        this.isLoaded = true;
      } catch (err) {
        console.error('[cartStore] fetchCart error:', err);
        this.error = 'Không thể tải giỏ hàng.';
        this.items = [];
        this.selectedIds = [];
      } finally {
        this.loading = false;
      }
    },

    /**
     * Add a SKU to cart.
     * @param {number} maSKU - Product SKU ID
     * @param {number} soLuong - Quantity to add
     * @returns {Promise<{success: boolean, cartCount?: number, message?: string}>}
     */
    async addItem(maSKU, soLuong = 1) {
      try {
        const response = await api.addToCart({ maSKU, soLuong });
        const data = response.data;
        // Update auth cart count badge
        if (data.cartCount !== undefined) {
          const authStore = useAuthStore();
          authStore.cartCount = data.cartCount;
        }
        // Refresh cart to get updated state
        await this.fetchCart();
        return { success: true, cartCount: data.cartCount };
      } catch (err) {
        console.error('[cartStore] addItem error:', err);
        const message = err.response?.data?.message || 'Không thể thêm vào giỏ hàng.';
        return { success: false, message };
      }
    },

    /**
     * Update quantity of a cart item.
     * @param {number} maGH - Cart item ID (GioHang maGH)
     * @param {number} soLuong - New quantity
     * @returns {Promise<{success: boolean, message?: string}>}
     */
    async updateItem(maGH, soLuong) {
      // Optimistic update
      const item = this.items.find((i) => i.maGH === maGH);
      if (item) {
        item.updating = true;
        if (soLuong <= 0) {
          // Remove if qty goes to 0
          await this.removeItem(maGH);
          return { success: true };
        }
      }
      try {
        await api.updateCartItem(maGH, soLuong);
        if (item) {
          item.soLuong = soLuong;
          // Recalculate thanhTien client-side
          item.thanhTien = (item.giaSauKM || item.giaGoc) * soLuong;
        }
        // Update auth badge
        const authStore = useAuthStore();
        await authStore.updateCartCount();
        return { success: true };
      } catch (err) {
        console.error('[cartStore] updateItem error:', err);
        const message = err.response?.data?.message || 'Cập nhật số lượng thất bại.';
        return { success: false, message };
      } finally {
        if (item) item.updating = false;
      }
    },

    /**
     * Remove one item from cart.
     * @param {number} maGH - Cart item ID
     * @returns {Promise<{success: boolean}>}
     */
    async removeItem(maGH) {
      try {
        await api.removeFromCart(maGH);
        this.items = this.items.filter((item) => item.maGH !== maGH);
        this.selectedIds = this.selectedIds.filter((id) => id !== maGH);
        const authStore = useAuthStore();
        await authStore.updateCartCount();
        return { success: true };
      } catch (err) {
        console.error('[cartStore] removeItem error:', err);
        return { success: false };
      }
    },

    /**
     * Remove all selected items from cart.
     * @returns {Promise<{success: boolean}>}
     */
    async removeSelected() {
      const idsToRemove = [...this.selectedIds];
      this.loading = true;
      try {
        await Promise.allSettled(idsToRemove.map((id) => api.removeFromCart(id)));
        this.items = this.items.filter((item) => !idsToRemove.includes(item.maGH));
        this.selectedIds = [];
        const authStore = useAuthStore();
        await authStore.updateCartCount();
        return { success: true };
      } catch (err) {
        console.error('[cartStore] removeSelected error:', err);
        return { success: false };
      } finally {
        this.loading = false;
      }
    },

    /**
     * Set selected item IDs (e.g. for checkout session).
     * @param {number[]} ids
     */
    setSelectedIds(ids) {
      this.selectedIds = ids;
    },

    /**
     * Clear all cart state (after successful checkout).
     */
    clearCart() {
      this.items = [];
      this.selectedIds = [];
      this.error = null;
      const authStore = useAuthStore();
      authStore.cartCount = 0;
    },
  },
});
