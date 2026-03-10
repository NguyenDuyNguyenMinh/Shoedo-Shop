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
                <option value="returned">Trả hàng ({{ filterByStatus('Hoàn hàng/trả hàng').length }})</option>
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

                <!-- Hotline -->
                <div class="d-flex align-items-center mb-2 small">
                  <i class="bi bi-telephone-fill me-1"></i>
                  <span>Hotline: <strong>1900 0001</strong></span>
                </div>

                <!-- Các nút hành động -->
                <div class="d-flex flex-column gap-2 mt-2">
                  <!-- Nút Đã nhận hàng -->
                  <button v-if="order.trangThai === 'Đang giao'" class="btn btn-success btn-sm w-100" @click="openConfirmReceivedModal(order.maHD)" :disabled="receivingOrderId === order.maHD">
                    <span v-if="receivingOrderId === order.maHD" class="spinner-border spinner-border-sm me-2"></span>
                    <i class="bi bi-check-circle me-1"></i> Đã nhận hàng
                  </button>

                  <!-- Nút Trả hàng -->
                  <button v-if="canReturnOrder(order)" class="btn btn-warning btn-sm w-100" @click="openReturnModal(order)" :disabled="returningOrderId === order.maHD">
                    <span v-if="returningOrderId === order.maHD" class="spinner-border spinner-border-sm me-2"></span>
                    <i class="bi bi-arrow-return-left me-1"></i> Yêu cầu trả hàng
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

    <!-- Modal yêu cầu trả hàng -->
    <div v-if="showReturnModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">Yêu cầu trả hàng</h5>
            <button type="button" class="btn-close" @click="closeReturnModal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedOrder">
              <p><strong>Mã đơn hàng:</strong> #{{ selectedOrder.maHD }}</p>
              <p><strong>Ngày nhận hàng:</strong> {{ formatDate(selectedOrder.ngayDen || selectedOrder.ngayMua) }}</p>

              <div class="mb-3">
                <label class="form-label fw-bold">Lý do trả hàng <span class="text-danger">*</span></label>
                <select class="form-select" v-model="returnReason">
                  <option value="">-- Chọn lý do --</option>
                  <option value="Sản phẩm bị lỗi">Sản phẩm bị lỗi</option>
                  <option value="Sai kích thước">Sai kích thước</option>
                  <option value="Sai màu sắc">Sai màu sắc</option>
                  <option value="Sai mẫu mã">Sai mẫu mã</option>
                  <option value="Không ưng ý">Không ưng ý</option>
                  <option value="Lý do khác">Lý do khác</option>
                </select>
              </div>

              <div class="mb-3" v-if="returnReason === 'Lý do khác'">
                <label class="form-label">Ghi chú thêm</label>
                <textarea class="form-control" rows="3" v-model="returnNote" placeholder="Nhập lý do chi tiết..."></textarea>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeReturnModal">Hủy</button>
            <button type="button" class="btn btn-warning" @click="submitReturnRequest" :disabled="!returnReason || submitting">
              <span v-if="submitting" class="spinner-border spinner-border-sm me-2"></span>
              Gửi yêu cầu
            </button>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>

</template>

<script>
import { ref, onMounted, computed } from 'vue';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

export default {
  name: 'QLDonHang',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    const currentTab = ref('all');
    const sortDirection = ref('desc');
    const orders = ref([]);
    const loading = ref(false);
    const error = ref('');
    const receivingOrderId = ref(null);
    const returningOrderId = ref(null);

    //State cho modal xác nhận
    const showConfirmModal = ref(false);
    const confirming = ref(false);
    const pendingOrderId = ref(null);

    // State cho modal trả hàng
    const showReturnModal = ref(false);
    const selectedOrder = ref(null);
    const returnReason = ref('');
    const returnNote = ref('');
    const submitting = ref(false);

    // State cho modal thông báo thành công
    const showSuccessModal = ref(false);
    const successMessage = ref('');

    // State cho modal lỗi
    const showErrorModal = ref(false);
    const errorMessage = ref('');

    const sortedOrders = computed(() => {
      return [...orders.value].sort((a, b) => {
        return new Date(b.ngayMua) - new Date(a.ngayMua);
      });
    });

    const fetchOrders = async () => {
      loading.value = true;
      error.value = '';

      try {
        const response = await api.getOrders();
        console.log('API Response:', response.data);

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
          sortOrders(); // Sắp xếp sau khi nhận dữ liệu
        } else {
          error.value = response.data.message || 'Không thể tải danh sách đơn hàng';
        }
      } catch (err) {
        console.error('Error fetching orders:', err);
        error.value = err.response?.data?.message || 'Lỗi kết nối máy chủ';
      } finally {
        loading.value = false;
      }
    };

    // Lọc đơn hàng theo trạng thái
    const filterByStatus = (status) => {
      return orders.value.filter(order => order.trangThai === status);
    };

    // Lọc đơn hàng theo tab hiện tại
    const filteredOrders = computed(() => {
      let result = [];

      // Lọc theo tab
      switch (currentTab.value) {
        case 'all':
          result = [...orders.value];
          break;
        case 'pending':
          result = filterByStatus('Đang xử lý');
          break;
        case 'shipping':
          result = filterByStatus('Đang giao');
          break;
        case 'completed':
          result = filterByStatus('Hoàn tất');
          break;
        case 'cancelled':
          result = filterByStatus('Đã từ chối');
          break;
        case 'error':
          result = filterByStatus('Báo lỗi');
          break;
        case 'returned':
          result = filterByStatus('Hoàn hàng/trả hàng');
          break;
        default:
          result = [...orders.value];
      }

      // Sắp xếp theo sortDirection
      return result.sort((a, b) => {
        const dateA = new Date(a.ngayMua);
        const dateB = new Date(b.ngayMua);

        if (sortDirection.value === 'desc') {
          return dateB - dateA; // Mới nhất lên đầu
        } else {
          return dateA - dateB; // Cũ nhất lên đầu
        }
      });
    });

    // Đổi tab
    const changeTab = () => {
      //console.log('Tab changed to:', currentTab.value);
    };

    const changeSort = () => {
      // Sắp xếp lại orders theo sortDirection
      sortOrders();
    };

    const toggleSort = () => {
      sortDirection.value = sortDirection.value === 'desc' ? 'asc' : 'desc';
      sortOrders();
    };

    const sortOrders = () => {
      const sorted = [...orders.value].sort((a, b) => {
        const dateA = new Date(a.ngayMua);
        const dateB = new Date(b.ngayMua);

        if (sortDirection.value === 'desc') {
          return dateB - dateA; // Mới nhất xuống cũ nhất
        } else {
          return dateA - dateB; // Cũ nhất lên mới nhất
        }
      });

      orders.value = sorted;
    };

    // Kiểm tra đơn hàng có thể trả trong vòng 7 ngày không
    const canReturnOrder = (order) => {
      if (order.trangThai !== 'Hoàn tất') return false;
      const receivedDate = order.ngayDen ? new Date(order.ngayDen) : new Date(order.ngayMua);
      const today = new Date();
      const diffTime = today - receivedDate;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays <= 7;
    };

    // Lấy tên sản phẩm đầu tiên từ order
    const getFirstProductName = (order) => {
      if (order.chiTiet && order.chiTiet.length > 0) {
        return order.chiTiet[0].tenSP || 'Sản phẩm';
      }
      if (order.hoaDonCTs && order.hoaDonCTs.length > 0) {
        const item = order.hoaDonCTs[0];
        return item.plSanPham?.sanPham?.tenSP ||
            item.plSanPham?.tenSP ||
            item.tenSP ||
            'Sản phẩm';
      }
      if (order.productName) {
        return order.productName;
      }
      return 'Sản phẩm';
    };

    // Lấy ảnh sản phẩm đầu tiên
    const getFirstProductImage = (order) => {
      if (order.chiTiet && order.chiTiet.length > 0) {
        return order.chiTiet[0].hinhAnh;
      }
      if (order.hoaDonCTs && order.hoaDonCTs.length > 0) {
        const item = order.hoaDonCTs[0];
        return item.plSanPham?.hinh || item.hinhAnh;
      }
      if (order.productImage) {
        return order.productImage;
      }
      return null;
    };

    // Đếm số lượng sản phẩm trong đơn hàng
    const getProductCount = (order) => {
      if (order.chiTiet && order.chiTiet.length > 0) {
        return order.chiTiet.length;
      }
      if (order.hoaDonCTs && order.hoaDonCTs.length > 0) {
        return order.hoaDonCTs.length;
      }
      if (order.totalItems) {
        return order.totalItems;
      }
      return 1;
    };

    const calculateOrderTotal = (order) => {
      if (order.tongTien) return order.tongTien;
      if (order.chiTiet && order.chiTiet.length > 0) {
        return order.chiTiet.reduce((total, item) => {
          return total + (item.soLuong * item.donGia);
        }, 0);
      }
      if (order.hoaDonCTs && order.hoaDonCTs.length > 0) {
        return order.hoaDonCTs.reduce((total, item) => {
          return total + (item.soLuong * item.donGia);
        }, 0);
      }
      return 0;
    };

    const formatPrice = (price) => {
      if (!price) return '0 ₫';
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price);
    };

    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
      });
    };

    const getImageUrl = (imageName) => {
      if (!imageName) return 'https://via.placeholder.com/60';
      if (imageName.startsWith('http')) return imageName;
      if (imageName.startsWith('/')) return imageName;
      return `/images/${imageName}`;
    };

    const getStatusClass = (status) => {
      switch (status) {
        case 'Hoàn tất':
          return 'badge bg-success';
        case 'Đang giao':
          return 'badge bg-primary';
        case 'Đang xử lý':
          return 'badge bg-warning text-dark';
        case 'Đã từ chối':
          return 'badge bg-danger';
        case 'Hoàn hàng/trả hàng':
          return 'badge bg-secondary';
        case 'Báo lỗi':
          return 'badge bg-dark';
        default:
          return 'badge bg-secondary';
      }
    };

    // Xác nhận đã nhận hàng
    const openConfirmReceivedModal = (orderId) => {
      pendingOrderId.value = orderId;
      showConfirmModal.value = true;
    };

    // Xử lý xác nhận từ modal
    const handleConfirmReceived = async () => {
      confirming.value = true;
      try {
        await processConfirmReceived(pendingOrderId.value);
      } finally {
        confirming.value = false;
        closeConfirmModal();
      }
    };

    // Đóng modal xác nhận
    const closeConfirmModal = () => {
      showConfirmModal.value = false;
      pendingOrderId.value = null;
    };

    // Xử lý API cập nhật trạng thái
    const processConfirmReceived = async (orderId) => {
      receivingOrderId.value = orderId;

      try {
        //console.log('Updating order status:', orderId, 'to Hoàn tất');

        const response = await api.updateCustomerOrderStatus(orderId, 'Hoàn tất');
        //console.log('Update response:', response.data);

        if (response.data.success) {
          const orderIndex = orders.value.findIndex(o => o.maHD === orderId);
          if (orderIndex !== -1) {
            orders.value[orderIndex].trangThai = 'Hoàn tất';
            orders.value[orderIndex].ngayDen = new Date().toISOString();
            orders.value = [...orders.value];
          }

          successMessage.value = 'Xác nhận thành công! Cảm ơn bạn đã mua hàng.';
          showSuccessModal.value = true;
        } else {
          errorMessage.value = response.data.message || 'Không thể cập nhật trạng thái';
          showErrorModal.value = true;
        }
      } catch (err) {
        //console.error('Error updating order status:', err);
        errorMessage.value = err.response?.data?.message || 'Lỗi khi cập nhật trạng thái';
        showErrorModal.value = true;
      } finally {
        receivingOrderId.value = null;
      }
    };

    // Mở modal yêu cầu trả hàng
    const openReturnModal = (order) => {
      selectedOrder.value = order;
      returnReason.value = '';
      returnNote.value = '';
      showReturnModal.value = true;
    };

    // Đóng modal
    const closeReturnModal = () => {
      showReturnModal.value = false;
      selectedOrder.value = null;
      returnReason.value = '';
      returnNote.value = '';
    };

    // Gửi yêu cầu trả hàng
    const submitReturnRequest = async () => {
      if (!returnReason.value) {
        errorMessage.value = 'Vui lòng chọn lý do trả hàng';
        showErrorModal.value = true;
        return;
      }

      submitting.value = true;
      returningOrderId.value = selectedOrder.value?.maHD;

      try {
        const response = await api.requestReturn({
          orderId: selectedOrder.value?.maHD,
          reason: returnReason.value,
          note: returnNote.value
        });

        if (response.data.success) {
          closeReturnModal();
          successMessage.value = 'Yêu cầu trả hàng đã được gửi thành công! Chúng tôi sẽ xử lý trong thời gian sớm nhất.';
          showSuccessModal.value = true;

          setTimeout(() => {
            window.location.reload();
          }, 1000);
        } else {
          errorMessage.value = response.data.message || 'Không thể gửi yêu cầu trả hàng';
          showErrorModal.value = true;
        }
      } catch (err) {
        //console.error('Error submitting return request:', err);
        errorMessage.value = err.response?.data?.message || 'Lỗi khi gửi yêu cầu trả hàng';
        showErrorModal.value = true;
      } finally {
        submitting.value = false;
        returningOrderId.value = null;
      }
    };

    onMounted(() => {
      fetchOrders();
    });

    return {
      orders,
      loading,
      error,
      currentTab,
      filteredOrders,
      changeTab,
      filterByStatus,
      receivingOrderId,
      sortedOrders,
      sortDirection,
      changeSort,
      toggleSort,
      getFirstProductName,
      getFirstProductImage,
      getProductCount,
      calculateOrderTotal,
      formatPrice,
      formatDate,
      getImageUrl,
      getStatusClass,
      canReturnOrder,
      // Modal xác nhận
      showConfirmModal,
      confirming,
      pendingOrderId,
      openConfirmReceivedModal,
      closeConfirmModal,
      handleConfirmReceived,
      // Modal trả hàng
      showReturnModal,
      selectedOrder,
      returnReason,
      returnNote,
      submitting,
      returningOrderId,
      openReturnModal,
      closeReturnModal,
      submitReturnRequest,
      // Modal thành công
      showSuccessModal,
      successMessage,
      // Modal lỗi
      showErrorModal,
      errorMessage
    };
  }
};
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

/* Kích thước ảnh sản phẩm */
.product-image {
  width: 70px !important;
  height: 70px !important;
  border-radius: 8px;
  object-fit: cover;
}

/* Kích thước chữ */
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

/* Kích thước badge */
.badge {
  font-size: 0.8rem;
  padding: 6px 10px;
  white-space: nowrap;
  font-weight: 500;
}

/*Kích thước thành tiền */
.text-danger {
  font-size: 1.1rem;
}

/* Khoảng cách giữa các dòng */
.mb-2 {
  margin-bottom: 0.75rem !important;
}

.mb-3 {
  margin-bottom: 1rem !important;
}

/* Kích thước nút */
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

/* Responsive adjustments */
@media (max-width: 768px) {
  .row {
    --bs-gutter-y: 1rem;
  }

  .product-name {
    font-size: 0.95rem;
  }

  .btn-sm {
    font-size: 0.85rem;
  }

  .card {
    padding: 1rem !important;
  }

  .product-image {
    width: 60px !important;
    height: 60px !important;
  }
}
</style>