import axios from 'axios';

const apiUrl = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: `${apiUrl}/api`,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

apiClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      if (window.location.pathname !== '/auth/login') {
        window.location.href = '/auth/login';
      }
    }
    return Promise.reject(error);
  }
);

export default {
  // ========== AUTH APIs ==========
  login(credentials) {
    return apiClient.post('/auth/login', credentials);
  },
  register(credentials) {
    return apiClient.post('/auth/register', credentials);
  },
  logout() {
    return apiClient.post('/auth/logout');
  },
  getCurrentUser() {
    return apiClient.get('/auth/current-user');
  },
  
  // ========== PUBLIC APIs ==========
  getPublicProducts(params = {}) {
    return apiClient.get('/public/products', { params });
  },
  
  searchPublicProducts(params = {}) {
    return apiClient.get('/public/search', { params });
  },
  
  getPublicProductById(id) {
    return apiClient.get(`/public/products/${id}`);
  },
  
  getProductPLSanPhams(productId) {
    return apiClient.get(`/public/products/${productId}/plsanphams`);
  },
  
  getCategories() {
    return apiClient.get('/categories');
  },

  // ========== SẢN PHẨM (TRANG CHỦ) ==========
  getTrangChu()        { return apiClient.get('/san-pham/trang-chu'); },
  getFlashSales()      { return apiClient.get('/san-pham/flash-sales'); },
  getNoiBat()          { return apiClient.get('/san-pham/noi-bat'); },
  getBanChay()         { return apiClient.get('/san-pham/ban-chay'); },

  // ========== ĐÁNH GIÁ SẢN PHẨM ==========
  getDanhGia(maSP)     { return apiClient.get(`/danh-gia/san-pham/${maSP}`); },

  // ========== SẢN PHẨM (TRANG CHI TIẾT) ==========
  getSanPhamChiTiet(id)  { return apiClient.get(`/san-pham/detail/${id}`); },
  getSanPhamLienQuan(id) { return apiClient.get(`/san-pham/detail/${id}/lien-quan`); },

  // ========== TÌM KIẾM / LỊCH SỬ ==========
  /**
   * GET /api/tim-kiem/lich-su/{maKH}
   * Trả về List<TimKiemDTO>: tối đa 10 từ khóa gần nhất của tài khoản
   */
  getLichSuTimKiem(maKH) {
    return apiClient.get(`/tim-kiem/lich-su/${maKH}`);
  },

  /**
   * POST /api/tim-kiem/luu  { maKH, keyword }
   * Lưu/cập nhật từ khóa tìm kiếm vào DB
   */
  luuTimKiem(maKH, keyword) {
    return apiClient.post('/tim-kiem/luu', { maKH, keyword });
  },

  /**
   * DELETE /api/tim-kiem/xoa-tat-ca/{maKH}
   * Xóa toàn bộ lịch sử tìm kiếm của tài khoản
   */
  xoaLichSuTimKiem(maKH) {
    return apiClient.delete(`/tim-kiem/xoa-tat-ca/${maKH}`);
  },

  /**
   * DELETE /api/tim-kiem/xoa/{maKH}?keyword=...
   * Xóa 1 từ khóa cụ thể
   */
  xoaMotTuKhoa(maKH, keyword) {
    return apiClient.delete(`/tim-kiem/xoa/${maKH}`, { params: { keyword } });
  },

  // ========== CUSTOMER APIs ==========
  // Customer Products
  getCustomerProducts(params = {}) {
    return apiClient.get('/customer/products', { params });
  },
  
  searchCustomerProducts(params = {}) {
    return apiClient.get('/customer/search', { params });
  },
  
  // Customer Cart
  getCart() {
    return apiClient.get('/customer/cart');
  },
  
  addToCart(data) {
    return apiClient.post('/customer/cart', data);
  },
  
  updateCartItem(maGH, soLuong) {
    return apiClient.put(`/customer/cart/${maGH}`, { soLuong });
  },
  
  removeFromCart(maGH) {
    return apiClient.delete(`/customer/cart/${maGH}`);
  },
  
  getCartCount() {
    return apiClient.get('/customer/cart/count');
  },
  
  // Customer Checkout
  checkout(data) {
    return apiClient.post('/customer/checkout', data);
  },

  // Customer Vouchers
  getMyVouchers() {
    return apiClient.get('/customer/vouchers');
  },

  // VNPay Payment
  createVNPayOrder(data) {
    return apiClient.post('/payment/create-order', data);
  },

  getVNPayStatus() {
    return apiClient.get('/payment/vnpay-status');
  },

  buyNow(data) {
    return apiClient.post('/customer/buy-now', data);
  },
  
  checkoutBuyNow(data) {
    return apiClient.post('/customer/checkout/buy-now', data);
  },
  
  // Customer Profile & Orders
  getProfile() {
    return apiClient.get('/customer/profile');
  },
  
  updateProfile(data) {
    return apiClient.put('/customer/profile', data);
  },

  getOrders() {
    return apiClient.get('/customer/orders');
  },

  getOrderDetail(id) {
    return apiClient.get(`/customer/orders/${id}`);
  },

  updateCustomerOrderStatus(id, status) {
    return apiClient.put(`/customer/orders/${id}/status?status=${encodeURIComponent(status)}`);
  },

  cancelOrder(id, cancelReason) {
    return apiClient.post(`/customer/orders/${id}/cancel`, { cancelReason });
  },

  reportIssue(data) {
    return apiClient.post('/customer/orders/report-issue', data);
  },

  addReview(data) {
    return apiClient.post('/customer/orders/review', data);
  },

  updateReview(data) {
    return apiClient.put('/customer/orders/review', data);
  },

  getReview(maHDCT) {
    return apiClient.get(`/customer/orders/review/${maHDCT}`);
  },

  getAddresses() {
    return apiClient.get('/customer/addresses');
  },
  
  addAddress(data) {
    return apiClient.post('/customer/address', data);
  },
  
  updateAddress(id, data) {
    return apiClient.put(`/customer/address/${id}`, data);
  },
  
  deleteAddress(id) {
    return apiClient.delete(`/customer/address/${id}`);
  },
  
  setDefaultAddress(id) {
    return apiClient.post(`/customer/address/${id}/set-default`);
  },

  changePassword(data) {
    return apiClient.put('/customer/change-password', data);
  },
  
  // ========== EMPLOYEE APIs ==========
  // Employee Dashboard & Statistics
  getDashboardStats() {
    return apiClient.get('/employee/dashboard');
  },

  getStatistics() {
    return apiClient.get('/employee/statistics');
  },

  // Employee Users Management
  getUsers(params = {}) {
    return apiClient.get('/employee/users', { params });
  },

  searchUsers(params = {}) {
    return apiClient.get('/employee/users/search', { params });
  },

  getUserDetails(id) {
    return apiClient.get(`/employee/users/${id}`);
  },

  createUser(data) {
    return apiClient.post('/employee/users', data);
  },

  updateUser(id, userData) {
    return axios.put(`/api/employee/users/${id}`, userData);
  },

  toggleUserStatus(id) {
    return apiClient.put(`/employee/users/${id}/toggle-status`);
  },

  resetPassword(id) {
    return apiClient.post(`/employee/users/${id}/reset-password`);
  },

  // Employee Stock Import
  getPLSanPhams() {
    return apiClient.get('/employee/import/plsanphams');
  },
  
  getImportHistory(params = {}) {
    return apiClient.get('/employee/import/history', { params });
  },
  
  importSingleProduct(data) {
    return apiClient.post('/employee/import/single', data);
  },
  
  bulkImportProducts(data) {
    return apiClient.post('/employee/import/bulk', data);
  },
  
  getProductsForFilter() {
    return apiClient.get('/employee/import/products');
  },
  
  getLowStockProducts(threshold = 10) {
    return apiClient.get(`/employee/import/low-stock?threshold=${threshold}`);
  },
  
  importProduct(data) {
    return apiClient.post('/employee/import', data);
  },
  
  searchImportHistory(params = {}) {
    return apiClient.get('/employee/import/history/search', { params });
  },
  
  // Employee Orders Management
  getOrdersByStatus(status) {
    return apiClient.get(`/employee/orders?status=${encodeURIComponent(status)}`);
  },

  getAllOrders() {
    return apiClient.get('/employee/orders/all');
  },

  getEmployeeOrderDetail(id) {
    return apiClient.get(`/employee/orders/${id}`);
  },

  approveOrder(id) {
    return apiClient.post(`/employee/orders/${id}/approve`);
  },

  rejectOrder(id) {
    return apiClient.post(`/employee/orders/${id}/reject`);
  },

  updateOrderStatus(id, status) {
    return apiClient.put(`/employee/orders/${id}/status?status=${encodeURIComponent(status)}`);
  },

  debugOrders() {
    return apiClient.get('/employee/orders/debug/all');
  },
  
  // Employee Product Management
  getEmployeeProducts(params = {}) {
    return apiClient.get('/employee/products', { params });
  },

  getEmployeeProductDetail(id) {
    return apiClient.get(`/employee/products/${id}`);
  },

  createEmployeeProduct(data) {
    return apiClient.post('/employee/products', data);
  },

  updateEmployeeProduct(id, data) {
    return apiClient.put(`/employee/products/${id}`, data);
  },

  deleteEmployeeProduct(id) {
    return apiClient.delete(`/employee/products/${id}`);
  },
  
  // Employee Product Variants
  getProductVariants(id) {
    return apiClient.get(`/employee/products/${id}/variants`);
  },  

  deleteProductVariant(variantId) {
    return apiClient.delete(`/employee/products/variants/${variantId}`);
  },
  
  // Employee Categories
  getEmployeeCategories() {
    return apiClient.get('/employee/products/categories');
  },

  createEmployeeCategory(data) {
    return apiClient.post('/employee/products/categories/create', data);
  },
  
  // Employee Images
  getAvailableImages() {
    return apiClient.get('/employee/images');
  },

  uploadProductImage(formData) {
    return apiClient.post('/employee/products/upload-image-file', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
  },
  
  getProductDetail(id) {
    return apiClient.get(`/employee/products/${id}`);
  },

  // ========== STATISTICS APIs ==========
  // Thống kê mặc định - 30 ngày gần nhất
  getThongKeMacDinh() {
    return apiClient.get('/employee/thongke');
  },

  // Thống kê tổng quan
  getThongKeTongQuan(startDate, endDate) {
    return apiClient.get('/employee/thongke/tong-quan', {
      params: { startDate, endDate }
    });
  },

  // Thống kê theo ngày
  getThongKeNgay(startDate, endDate) {
    return apiClient.get('/employee/thongke/ngay', {
      params: { startDate, endDate }
    });
  },

  // Thống kê theo tháng
  getThongKeThang(startDate, endDate) {
    return apiClient.get('/employee/thongke/thang', {
      params: { startDate, endDate }
    });
  },

  // Thống kê theo năm
  getThongKeNam(startDate, endDate) {
    return apiClient.get('/employee/thongke/nam', {
      params: { startDate, endDate }
    });
  },

  // Thống kê theo danh mục
  getThongKeDanhMuc(startDate, endDate) {
    return apiClient.get('/employee/thongke/danh-muc', {
      params: { startDate, endDate }
    });
  },

  // Top sản phẩm bán chạy
  getTopSanPham(startDate, endDate, limit = 10) {
    return apiClient.get('/employee/thongke/top-san-pham', {
      params: { startDate, endDate, limit }
    });
  },

  // Top khách hàng
  getTopKhachHang(startDate, endDate, limit = 5) {
    return apiClient.get('/employee/thongke/top-khach-hang', {
      params: { startDate, endDate, limit }
    });
  },

  // Thống kê theo trạng thái
  getThongKeTrangThai(startDate, endDate) {
    return apiClient.get('/employee/thongke/trang-thai', {
      params: { startDate, endDate }
    });
  },

  // Đơn hàng gần đây
  getDonHangGanDay(limit = 5) {
    return apiClient.get('/employee/thongke/don-hang-gan-day', {
      params: { limit }
    });
  }
};