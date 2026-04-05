<template>
  <div class="customer-layout" style="display: grid; grid-template-rows: auto 1fr auto; min-height: 100vh;">
    <KH_Navbar />

    <main class="container">
      <h2 class="mt-4 mb-3">Đơn Hàng Của Bạn</h2>

      <div v-if="error" class="alert alert-danger">{{ error }}</div>

      <div v-if="loading" class="text-center p-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>

      <div v-else>
        <!-- Combo box với icon sắp xếp -->
        <div class="row mb-4 align-items-center">
          <div class="col-md-5 col-lg-4">
            <div class="d-flex align-items-center gap-2">
              <label class="fw-bold text-nowrap">Lọc theo:</label>
              <select class="form-select" v-model="currentTab" @change="changeTab" style="max-width: 250px;">
                <option value="all">Tất cả đơn hàng ({{ orders.length }})</option>
                <option value="pending">Chờ xử lý ({{ filterByStatus('Đang xử lý').length }})</option>
                <option value="shipping">Đang giao ({{ filterByStatus('Đang giao').length }})</option>
                <option value="completed">Hoàn tất ({{ filterByStatus('Hoàn tất').length }})</option>
                <option value="error">Báo lỗi ({{ filterByStatus('Báo lỗi').length }})</option>
                <option value="cancelled">Đã hủy ({{ filterByStatus('Đã từ chối').length }})</option>
              </select>
            </div>
          </div>

          <div class="col-md-4">
            <div class="d-flex align-items-center gap-2">
              <label class="fw-bold text-nowrap">Sắp xếp:</label>
              <div class="input-group" style="max-width: 200px;">
                <select class="form-select" v-model="sortDirection" @change="changeSort">
                  <option value="desc">Mới nhất</option>
                  <option value="asc">Cũ nhất</option>
                </select>
                <span class="input-group-text bg-light" @click="toggleSort" style="cursor: pointer;">
                  <i :class="sortDirection === 'desc' ? 'bi bi-sort-down' : 'bi bi-sort-up'"></i>
                </span>
              </div>
            </div>
          </div>

          <div class="col-md-3 text-md-end">
            <span class="text-muted">
              Tổng số: <strong>{{ orders.length }}</strong> đơn hàng
            </span>
          </div>
        </div>

        <!-- Hiển thị đơn hàng theo tab -->
        <div v-if="filteredOrders.length === 0" class="alert alert-info text-center">
          Không có đơn hàng nào ở trạng thái này.
        </div>

        <div v-else>
          <div class="row row-cols-1 row-cols-md-2 row-cols-lg-4 g-4">
            <div v-for="order in filteredOrders" :key="order.maHD" class="col">
              <div class="card h-100 p-3 d-flex flex-column">
                <!-- Nội dung phía trên - sẽ đẩy nút xuống dưới -->
                <div class="flex-grow-1">
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="text-muted small">
                      <i class="bi bi-calendar me-1"></i>
                      {{ formatDate(order.ngayMua) }}
                    </span>
                    <span :class="getStatusClass(order.trangThai)" class="badge">
                      {{ order.trangThai }}
                    </span>
                  </div>

                  <!-- Thông tin sản phẩm đầu tiên + ảnh -->
                  <div class="d-flex align-items-center mb-3">
                    <img v-if="getFirstProductImage(order)" :src="getImageUrl(getFirstProductImage(order))" alt="Product" class="me-2 product-image" style="width: 60px; height: 60px; object-fit: cover; border-radius: 5px; border: 1px solid #eee;" @error="handleImageError">
                    <div class="flex-grow-1">
                      <strong class="product-name">{{ getFirstProductName(order) }}</strong>
                      <small class="text-muted d-block" v-if="getProductCount(order) > 1">
                        và {{ getProductCount(order) - 1 }} sản phẩm khác
                      </small>
                    </div>
                  </div>

                  <hr class="my-2 w-100">

                  <!-- Thành tiền -->
                  <div class="d-flex justify-content-between align-items-center mb-2">
                    <span class="text-muted">Thành Tiền: </span>
                    <strong class="text-danger">
                      {{ formatPrice(order.tongTien || calculateOrderTotal(order)) }}
                    </strong>
                  </div>

                  <!-- Thêm dòng tiết kiệm -->
                  <div v-if="order.tongTienGoc && order.tongTienGoc > (order.tongTien || calculateOrderTotal(order))" class="d-flex justify-content-between align-items-center mb-2">
                    <span class="text-muted small">Tiết kiệm:</span>
                    <span class="text-success small">
                      -{{ formatPrice(order.tongTienGoc - (order.tongTien || calculateOrderTotal(order))) }}
                    </span>
                  </div>

                  <!-- Hotline -->
                  <div class="d-flex align-items-center mb-2 small">
                    <i class="bi bi-telephone-fill me-1"></i>
                    <span>Hotline: <strong>1900 6869</strong></span>
                  </div>
                </div>

                <!-- Các nút hành động - luôn ở dưới cùng -->
                <div class="d-flex flex-column gap-2 mt-auto pt-2">
                  <!-- Nút Hủy đơn hàng -->
                  <button v-if="canCancelOrder(order)" class="btn btn-danger btn-sm w-100" @click="openCancelModal(order)" :disabled="cancellingOrderId === order.maHD">
                    <span v-if="cancellingOrderId === order.maHD" class="spinner-border spinner-border-sm me-2"></span>
                    <i class="bi bi-x-circle me-1"></i> Hủy đơn hàng
                  </button>

                  <!-- Nút Đã nhận hàng -->
                  <button v-if="order.trangThai === 'Đang giao'" class="btn btn-success btn-sm w-100" @click="openConfirmReceivedModal(order.maHD)" :disabled="receivingOrderId === order.maHD">
                    <span v-if="receivingOrderId === order.maHD" class="spinner-border spinner-border-sm me-2"></span>
                    <i class="bi bi-check-circle me-1"></i> Đã nhận hàng
                  </button>

                  <!-- Nút Báo lỗi -->
                  <button v-if="canReportIssue(order)" class="btn btn-warning btn-sm w-100" @click="openReportIssueModal(order)" :disabled="reportingOrderId === order.maHD">
                    <span v-if="reportingOrderId === order.maHD" class="spinner-border spinner-border-sm me-2"></span>
                    <i class="bi bi-exclamation-triangle me-1"></i> Báo lỗi
                  </button>

                  <!-- Nút Xem chi tiết -->
                  <router-link :to="`/customer/orders/${order.maHD}`" class="btn btn-outline-dark btn-sm w-100">
                    <i class="bi bi-eye me-1"></i> Xem chi tiết
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Modal xác nhận đã nhận hàng -->
    <div v-if="showConfirmModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title">
              <i class="bi bi-check-circle me-2"></i>Xác nhận đã nhận hàng
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeConfirmModal"></button>
          </div>
          <div class="modal-body">
            <div class="text-center py-3">
              <i class="bi bi-question-circle text-warning" style="font-size: 4rem;"></i>
              <h5 class="mt-3">Xác nhận bạn đã nhận được hàng?</h5>
              <p class="text-muted">Hành động này không thể hoàn tác.</p>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeConfirmModal">
              <i class="bi bi-x-circle me-1"></i>Hủy
            </button>
            <button type="button" class="btn btn-success" @click="handleConfirmReceived" :disabled="confirming">
              <span v-if="confirming" class="spinner-border spinner-border-sm me-2"></span>
              <i class="bi bi-check-circle me-1"></i>Xác nhận
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Hủy đơn hàng -->
    <div v-if="showCancelModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">
              <i class="bi bi-x-circle me-2"></i>Hủy đơn hàng
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeCancelModal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedCancelOrder">
              <p class="mb-3"><strong>Mã đơn hàng:</strong> #{{ selectedCancelOrder.maHD }}</p>
              <p class="mb-3"><strong>Ngày đặt:</strong> {{ formatDate(selectedCancelOrder.ngayMua) }}</p>
              <p class="mb-3"><strong>Tổng tiền:</strong> {{ formatPrice(selectedCancelOrder.tongTien || calculateOrderTotal(selectedCancelOrder)) }}</p>

              <div class="mb-3">
                <label class="form-label fw-bold">Lý do hủy đơn <span class="text-danger">*</span></label>
                <select class="form-select" v-model="cancelReason">
                  <option value="">-- Chọn lý do hủy --</option>
                  <option value="Đặt nhầm sản phẩm">Đặt nhầm sản phẩm</option>
                  <option value="Thay đổi ý định mua hàng">Thay đổi ý định mua hàng</option>
                  <option value="Tìm thấy sản phẩm giá tốt hơn">Tìm thấy sản phẩm giá tốt hơn</option>
                  <option value="Thời gian giao hàng quá lâu">Thời gian giao hàng quá lâu</option>
                  <option value="Sản phẩm không còn nhu cầu">Sản phẩm không còn nhu cầu</option>
                  <option value="Lý do khác">Lý do khác</option>
                </select>
              </div>

              <div class="mb-3">
                <label class="form-label">Mô tả chi tiết (tùy chọn)</label>
                <textarea class="form-control" rows="3" v-model="cancelNote" placeholder="Vui lòng mô tả chi tiết lý do hủy đơn hàng..."></textarea>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary m-2" @click="closeCancelModal">
              <i class="bi bi-arrow-left me-1"></i>Quay lại
            </button>
            <button type="button" class="btn btn-danger" @click="submitCancelOrder" :disabled="!cancelReason || cancelling">
              <span v-if="cancelling" class="spinner-border spinner-border-sm me-2"></span>
              <i class="bi bi-check-circle me-1"></i>Xác nhận hủy
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal báo lỗi -->
    <div v-if="showReportIssueModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-warning text-dark">
            <h5 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2"></i>Báo lỗi đơn hàng
            </h5>
            <button type="button" class="btn-close" @click="closeReportIssueModal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedOrder">
              <p class="mb-2"><strong>Mã đơn hàng:</strong> #{{ selectedOrder.maHD }}</p>
              <p class="mb-2"><strong>Ngày mua:</strong> {{ formatDate(selectedOrder.ngayMua) }}</p>
              <p class="mb-3"><strong>Ngày nhận:</strong> {{ formatDate(selectedOrder.ngayDen || selectedOrder.ngayMua) }}</p>

              <!-- Đếm ngược đơn giản -->
              <div class="timer-simple mb-4" v-if="remainingTimeDetail">
                <div class="d-flex align-items-center gap-2 text-muted">
                  <i class="bi bi-clock-history"></i>
                  <span class="timer-text">{{ remainingTimeDetail.formatted }}</span>
                  <i class="bi bi-calendar-check"></i>
                  <span class="deadline-text small">Hạn: {{ formatDate(getReportDeadline(selectedOrder)) }}</span>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold">Lý do báo lỗi <span class="text-danger">*</span></label>
                <select class="form-select" v-model="reportReason">
                  <option value="">-- Chọn lý do --</option>
                  <option value="Sản phẩm bị lỗi">Sản phẩm bị lỗi</option>
                  <option value="Sai kích thước">Sai kích thước</option>
                  <option value="Sai màu sắc">Sai màu sắc</option>
                  <option value="Sai mẫu mã">Sai mẫu mã</option>
                  <option value="Giao thiếu hàng">Giao thiếu hàng</option>
                  <option value="Hàng bị hư hỏng">Hàng bị hư hỏng</option>
                  <option value="Lý do khác">Lý do khác</option>
                </select>
              </div>

              <div class="mb-3">
                <label class="form-label">Mô tả chi tiết</label>
                <textarea class="form-control" rows="4" v-model="reportNote" placeholder="Vui lòng mô tả chi tiết vấn đề bạn gặp phải..."></textarea>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeReportIssueModal">Hủy</button>
            <button type="button" class="btn btn-warning" @click="submitReportIssue" :disabled="!reportReason || reporting">
              <span v-if="reporting" class="spinner-border spinner-border-sm me-2"></span>
              Gửi báo lỗi
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal thông báo thành công -->
    <div v-if="showSuccessModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-success text-white">
            <h5 class="modal-title">
              <i class="bi bi-check-circle me-2"></i>Thành công
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="showSuccessModal = false"></button>
          </div>
          <div class="modal-body text-center py-4">
            <i class="bi bi-check-circle-fill text-success" style="font-size: 4rem;"></i>
            <h5 class="mt-3">{{ successMessage }}</h5>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-success" @click="showSuccessModal = false">
              <i class="bi bi-check me-1"></i>Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal thông báo lỗi -->
    <div v-if="showErrorModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2"></i>Lỗi
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="showErrorModal = false"></button>
          </div>
          <div class="modal-body text-center py-4">
            <i class="bi bi-x-circle-fill text-danger" style="font-size: 4rem;"></i>
            <h5 class="mt-3">{{ errorMessage }}</h5>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-danger" @click="showErrorModal = false">
              <i class="bi bi-x me-1"></i>Đóng
            </button>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

const currentTab = ref('all');
const sortDirection = ref('desc');
const orders = ref([]);
const loading = ref(false);
const error = ref('');
const receivingOrderId = ref(null);
const reportingOrderId = ref(null);
const cancellingOrderId = ref(null);

// State cho modal xác nhận đã nhận hàng
const showConfirmModal = ref(false);
const confirming = ref(false);
const pendingOrderId = ref(null);

// State cho modal hủy đơn
const showCancelModal = ref(false);
const cancelling = ref(false);
const selectedCancelOrder = ref(null);
const cancelReason = ref('');
const cancelNote = ref('');

// State cho modal báo lỗi
const showReportIssueModal = ref(false);
const selectedOrder = ref(null);
const reportReason = ref('');
const reportNote = ref('');
const reporting = ref(false);

// State cho modal thông báo thành công
const showSuccessModal = ref(false);
const successMessage = ref('');

// State cho modal lỗi
const showErrorModal = ref(false);
const errorMessage = ref('');

// Timer realtime cho modal
let modalTimerInterval = null;
const modalTimeTick = ref(Date.now());

// Bắt đầu timer cho modal
const startModalTimer = () => {
  if (modalTimerInterval) clearInterval(modalTimerInterval);
  modalTimerInterval = setInterval(() => {
    modalTimeTick.value = Date.now();
  }, 1000);
};

// Dừng timer modal
const stopModalTimer = () => {
  if (modalTimerInterval) {
    clearInterval(modalTimerInterval);
    modalTimerInterval = null;
  }
};

const fetchOrders = async () => {
  loading.value = true;
  error.value = '';

  try {
    const response = await api.getOrders();
    if (response.data.success) {
      let allOrders = [];
      if (response.data.data) {
        Object.values(response.data.data).forEach(statusOrders => {
          allOrders.push(...statusOrders);
        });
      } else if (response.data.orders) {
        allOrders = response.data.orders;
      } else if (Array.isArray(response.data)) {
        allOrders = response.data;
      }
      orders.value = allOrders;
      sortOrders();
    } else {
      error.value = response.data.message || 'Không thể tải danh sách đơn hàng';
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Lỗi kết nối máy chủ';
  } finally {
    loading.value = false;
  }
};

// Kiểm tra có thể hủy đơn không
const canCancelOrder = (order) => {
  if (!order) return false;
  return order.trangThai === 'Đang xử lý';
};

// Lọc đơn hàng theo trạng thái
const filterByStatus = (status) => {
  return orders.value.filter(order => order.trangThai === status);
};

// Lọc đơn hàng theo tab hiện tại
const filteredOrders = computed(() => {
  let result = [];

  switch (currentTab.value) {
    case 'all': result = [...orders.value]; break;
    case 'pending': result = filterByStatus('Đang xử lý'); break;
    case 'shipping': result = filterByStatus('Đang giao'); break;
    case 'completed': result = filterByStatus('Hoàn tất'); break;
    case 'cancelled': result = filterByStatus('Đã từ chối'); break;
    case 'error': result = filterByStatus('Báo lỗi'); break;
    default: result = [...orders.value];
  }

  return result.sort((a, b) => {
    const dateA = new Date(a.ngayMua);
    const dateB = new Date(b.ngayMua);
    return sortDirection.value === 'desc' ? dateB - dateA : dateA - dateB;
  });
});

const changeTab = () => {};
const changeSort = () => sortOrders();
const toggleSort = () => {
  sortDirection.value = sortDirection.value === 'desc' ? 'asc' : 'desc';
  sortOrders();
};

const sortOrders = () => {
  const sorted = [...orders.value].sort((a, b) => {
    const dateA = new Date(a.ngayMua);
    const dateB = new Date(b.ngayMua);
    return sortDirection.value === 'desc' ? dateB - dateA : dateA - dateB;
  });
  orders.value = sorted;
};

// Kiểm tra có thể báo lỗi không
const canReportIssue = (order) => {
  if (order.trangThai !== 'Hoàn tất') return false;
  const receivedDate = order.ngayDen ? new Date(order.ngayDen) : new Date(order.ngayMua);
  const deadline = new Date(receivedDate);
  deadline.setDate(deadline.getDate() + 30);
  return new Date() <= deadline;
};

// Tính thời gian còn lại cho modal (cập nhật mỗi giây)
const remainingTimeDetail = computed(() => {
  const _ = modalTimeTick.value;

  if (!selectedOrder.value || !canReportIssue(selectedOrder.value)) {
    return { formatted: '00 ngày 00:00:00' };
  }

  const receivedDate = selectedOrder.value.ngayDen ? new Date(selectedOrder.value.ngayDen) : new Date(selectedOrder.value.ngayMua);
  const deadline = new Date(receivedDate);
  deadline.setDate(deadline.getDate() + 30);

  const now = new Date();
  const diffMs = deadline - now;

  if (diffMs <= 0) return { formatted: '00 ngày 00:00:00' };

  const totalSeconds = Math.floor(diffMs / 1000);
  const days = Math.floor(totalSeconds / (24 * 60 * 60));
  const hours = Math.floor((totalSeconds % (24 * 60 * 60)) / (60 * 60));
  const minutes = Math.floor((totalSeconds % (60 * 60)) / 60);
  const seconds = totalSeconds % 60;

  const formattedDays = String(days).padStart(2, '0');
  const formattedHours = String(hours).padStart(2, '0');
  const formattedMinutes = String(minutes).padStart(2, '0');
  const formattedSeconds = String(seconds).padStart(2, '0');

  return {
    days, hours, minutes, seconds,
    formatted: `${formattedDays} ngày ${formattedHours}:${formattedMinutes}:${formattedSeconds}`
  };
});

// Lấy deadline
const getReportDeadline = (order) => {
  if (!order) return '';
  const receivedDate = order.ngayDen ? new Date(order.ngayDen) : new Date(order.ngayMua);
  const deadline = new Date(receivedDate);
  deadline.setDate(deadline.getDate() + 30);
  return deadline;
};

// Các helper functions
const getFirstProductName = (order) => {
  if (order.chiTiet?.length > 0) return order.chiTiet[0].tenSP || 'Sản phẩm';
  if (order.hoaDonCTs?.length > 0) {
    const item = order.hoaDonCTs[0];
    return item.plSanPham?.sanPham?.tenSP || item.plSanPham?.tenSP || item.tenSP || 'Sản phẩm';
  }
  return order.productName || 'Sản phẩm';
};

const getFirstProductImage = (order) => {
  if (order.chiTiet?.length > 0) return order.chiTiet[0].hinhAnh;
  if (order.hoaDonCTs?.length > 0) {
    const item = order.hoaDonCTs[0];
    return item.plSanPham?.hinh || item.hinhAnh;
  }
  return order.productImage || null;
};

const getProductCount = (order) => {
  if (order.chiTiet?.length > 0) return order.chiTiet.length;
  if (order.hoaDonCTs?.length > 0) return order.hoaDonCTs.length;
  return order.totalItems || 1;
};

const calculateOrderTotal = (order) => {
  if (order.tongTien) return order.tongTien;

  if (order.chiTiet?.length > 0) {
    const hasSauKm = order.chiTiet.some(item => item.thanhTienSauKmSp);
    if (hasSauKm) {
      return order.chiTiet.reduce((total, item) => total + (item.thanhTienSauKmSp || 0), 0);
    }
    return order.chiTiet.reduce((total, item) => total + (item.soLuong * item.donGia), 0);
  }

  if (order.hoaDonCTs?.length > 0) {
    return order.hoaDonCTs.reduce((total, item) => total + (item.soLuong * item.donGia), 0);
  }

  return 0;
};

const formatPrice = (price) => {
  if (!price) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
};

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit', month: '2-digit', year: 'numeric'
  });
};

const getImageUrl = (imageName) => {
  if (!imageName) return 'https://via.placeholder.com/60';
  if (imageName.startsWith('http')) return imageName;
  if (imageName.startsWith('/')) return imageName;
  return `/images/${imageName}`;
};

const handleImageError = (e) => {
  e.target.src = 'https://via.placeholder.com/60';
};

const getStatusClass = (status) => {
  switch (status) {
    case 'Hoàn tất': return 'badge bg-success';
    case 'Đang giao': return 'badge bg-primary';
    case 'Đang xử lý': return 'badge bg-warning text-dark';
    case 'Đã từ chối': return 'badge bg-danger';
    case 'Báo lỗi': return 'badge bg-dark';
    case 'Hoàn hàng/trả hàng': return 'badge bg-secondary';
    default: return 'badge bg-secondary';
  }
};

// Xác nhận đã nhận hàng
const openConfirmReceivedModal = (orderId) => {
  pendingOrderId.value = orderId;
  showConfirmModal.value = true;
};

const handleConfirmReceived = async () => {
  confirming.value = true;
  try {
    await processConfirmReceived(pendingOrderId.value);
  } finally {
    confirming.value = false;
    closeConfirmModal();
  }
};

const closeConfirmModal = () => {
  showConfirmModal.value = false;
  pendingOrderId.value = null;
};

const processConfirmReceived = async (orderId) => {
  receivingOrderId.value = orderId;
  try {
    const response = await api.updateCustomerOrderStatus(orderId, 'Hoàn tất');
    if (response.data.success) {
      const orderIndex = orders.value.findIndex(o => o.maHD === orderId);
      if (orderIndex !== -1) {
        orders.value[orderIndex].trangThai = 'Hoàn tất';
        orders.value[orderIndex].ngayDen = new Date().toISOString();
        orders.value = [...orders.value];
      }
      successMessage.value = 'Xác nhận thành công!';
      showSuccessModal.value = true;
    } else {
      errorMessage.value = response.data.message || 'Không thể cập nhật trạng thái';
      showErrorModal.value = true;
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Lỗi khi cập nhật trạng thái';
    showErrorModal.value = true;
  } finally {
    receivingOrderId.value = null;
  }
};

// Mở modal hủy đơn
const openCancelModal = (order) => {
  selectedCancelOrder.value = order;
  cancelReason.value = '';
  cancelNote.value = '';
  showCancelModal.value = true;
};

const closeCancelModal = () => {
  showCancelModal.value = false;
  selectedCancelOrder.value = null;
  cancelReason.value = '';
  cancelNote.value = '';
};

// Gửi hủy đơn
const submitCancelOrder = async () => {
  if (!cancelReason.value) {
    errorMessage.value = 'Vui lòng chọn lý do hủy đơn';
    showErrorModal.value = true;
    return;
  }

  cancelling.value = true;
  cancellingOrderId.value = selectedCancelOrder.value?.maHD;

  try {
    const fullReason = cancelNote.value
        ? `${cancelReason.value} - ${cancelNote.value}`
        : cancelReason.value;

    const response = await api.cancelOrder(selectedCancelOrder.value?.maHD, fullReason);

    if (response.data.success) {
      closeCancelModal();
      successMessage.value = 'Đơn hàng đã được hủy thành công!';
      showSuccessModal.value = true;

      const orderIndex = orders.value.findIndex(o => o.maHD === selectedCancelOrder.value?.maHD);
      if (orderIndex !== -1) {
        orders.value[orderIndex].trangThai = 'Đã từ chối';
        orders.value[orderIndex].ghiChu = fullReason;
        orders.value = [...orders.value];
      }

      setTimeout(() => {
        window.location.reload();
      }, 1000);

    } else {
      errorMessage.value = response.data.message || 'Không thể hủy đơn hàng';
      showErrorModal.value = true;
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || err.message || 'Lỗi khi hủy đơn hàng';
    showErrorModal.value = true;
  } finally {
    cancelling.value = false;
    cancellingOrderId.value = null;
  }
};

// Mở modal báo lỗi
const openReportIssueModal = (order) => {
  selectedOrder.value = order;
  reportReason.value = '';
  reportNote.value = '';
  showReportIssueModal.value = true;
};

const closeReportIssueModal = () => {
  showReportIssueModal.value = false;
  selectedOrder.value = null;
  reportReason.value = '';
  reportNote.value = '';
};

// Gửi báo lỗi
const submitReportIssue = async () => {
  if (!reportReason.value) {
    errorMessage.value = 'Vui lòng chọn lý do báo lỗi';
    showErrorModal.value = true;
    return;
  }

  reporting.value = true;
  reportingOrderId.value = selectedOrder.value?.maHD;

  try {
    const response = await api.reportIssue({
      orderId: selectedOrder.value?.maHD,
      reason: reportReason.value,
      note: reportNote.value
    });

    if (response.data.success) {
      closeReportIssueModal();
      successMessage.value = 'Báo lỗi đã được ghi nhận!';
      showSuccessModal.value = true;

      const orderIndex = orders.value.findIndex(o => o.maHD === selectedOrder.value?.maHD);
      if (orderIndex !== -1) {
        orders.value[orderIndex].trangThai = 'Báo lỗi';
        orders.value = [...orders.value];
      }

      setTimeout(() => {
        window.location.reload();
      }, 1000);

    } else {
      errorMessage.value = response.data.message || 'Không thể gửi báo lỗi';
      showErrorModal.value = true;
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Lỗi khi gửi báo lỗi';
    showErrorModal.value = true;
  } finally {
    reporting.value = false;
    reportingOrderId.value = null;
  }
};

// Theo dõi khi modal mở/đóng
watch(() => showReportIssueModal.value, (isOpen) => {
  if (isOpen && selectedOrder.value && canReportIssue(selectedOrder.value)) {
    startModalTimer();
  } else {
    stopModalTimer();
  }
});

// Dọn dẹp khi component unmount
onBeforeUnmount(() => {
  stopModalTimer();
});

onMounted(() => {
  fetchOrders();
});
</script>

<style scoped>
.card {
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  height: 100%;
  padding: 1.2rem !important;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.product-image {
  width: 70px !important;
  height: 70px !important;
  border-radius: 8px;
  object-fit: cover;
}

.product-name {
  font-size: 1rem;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
  font-weight: 600;
}

.badge {
  font-size: 0.8rem;
  padding: 6px 10px;
  white-space: nowrap;
  font-weight: 500;
}

.text-danger {
  font-size: 1.1rem;
}

.mb-2 { margin-bottom: 0.75rem !important; }
.mb-3 { margin-bottom: 1rem !important; }

.btn-sm {
  font-size: 0.9rem;
  padding: 0.5rem 0.75rem;
  font-weight: 500;
}

hr {
  margin-top: 0.75rem !important;
  margin-bottom: 0.75rem !important;
  opacity: 0.5;
}

/* Modal styles */
.modal {
  z-index: 1050;
}

.modal-content {
  border-radius: 12px;
  border: none;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.modal-header.bg-success,
.modal-header.bg-danger,
.modal-header.bg-warning {
  background: #000000 !important;
  border-bottom: 2px solid #ffffff;
}

.modal-header.bg-warning .modal-title {
  color: #ffffff;
}

.modal-header .modal-title {
  color: #ffffff;
  font-weight: 600;
}

.modal-header .btn-close-white,
.modal-header .btn-close {
  filter: brightness(0) invert(1);
}

.timer-simple {
  background-color: transparent;
}

.timer-simple .timer-text {
  font-family: 'Courier New', monospace;
  font-size: 1rem;
  color: #495057;
  font-weight: 500;
}

.timer-simple .deadline-text {
  color: #6c757d;
  font-family: 'Courier New', monospace;
}

.timer-simple i {
  color: #6c757d;
  font-size: 1rem;
}

.modal-footer .btn-secondary,
.modal-footer .btn-danger,
.modal-footer .btn-success,
.modal-footer .btn-warning {
  background-color: #000000;
  border-color: #ffffff;
  color: #ffffff;
  transition: all 0.3s;
}

.modal-footer .btn-secondary:hover,
.modal-footer .btn-danger:hover,
.modal-footer .btn-success:hover,
.modal-footer .btn-warning:hover {
  background-color: #ffffff;
  color: #000000;
  border-color: #000000;
}

/* Responsive */
@media (max-width: 768px) {
  .row { --bs-gutter-y: 1rem; }
  .product-name { font-size: 0.95rem; }
  .btn-sm { font-size: 0.85rem; }
  .card { padding: 1rem !important; }
  .product-image { width: 60px !important; height: 60px !important; }
}
</style>