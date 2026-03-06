<template>
  <div class="customer-layout d-flex flex-column min-vh-100">
    <KH_Navbar />

    <main class="container">
      <div v-if="error" class="alert alert-danger" role="alert">{{ error }}</div>

      <div v-if="loading" class="text-center p-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Loading...</span>
        </div>
      </div>

      <div v-else-if="order">
        <div class="d-flex justify-content-between align-items-center">
          <h2 class="mt-4 mb-3">Chi Tiết Đơn Hàng #{{ order.maHD }}</h2>
          <router-link to="/customer/orders" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> Quay Lại
          </router-link>
        </div>

        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">Thông Tin Đơn Hàng</h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-6">
                <p><strong>Mã đơn hàng:</strong> #{{ order.maHD }}</p>
                <p><strong>Ngày đặt hàng:</strong> {{ formatDate(order.ngayMua) }}</p>
                <p><strong>Ngày nhận hàng:</strong> {{ order.ngayDen ? formatDate(order.ngayDen) : 'Chưa nhận' }}</p>
                <p><strong>Trạng thái:</strong>
                  <span :class="getStatusClass(order.trangThai)" class="badge fw-bold">
                    {{ order.trangThai }}
                  </span>
                </p>
                <p><strong>Phương thức thanh toán:</strong> {{ order.phuongThucTT || 'Chưa cập nhật' }}</p>
                <p v-if="order.ghiChu"><strong>Ghi chú:</strong> {{ order.ghiChu }}</p>
              </div>

              <!-- PHẦN HIỂN THỊ ĐỊA CHỈ -->
              <div class="col-md-6">
                <h6><strong>Địa chỉ giao hàng:</strong></h6>

                <!-- Hiển thị từ diaChi đã parse -->
                <div v-if="diaChi" class="p-3 bg-light rounded border">
                  <p class="mb-1"><strong>Người nhận:</strong> {{ diaChi.tenNN || 'Không có thông tin' }}</p>
                  <p class="mb-1"><strong>SĐT:</strong> {{ diaChi.sdt || 'Không có thông tin' }}</p>
                  <p class="mb-0"><strong>Địa chỉ:</strong> {{ diaChi.diemGiao || 'Không có thông tin' }}</p>
                </div>
                <!-- Hiển thị từ diaChi(nếu chưa parse) -->
                <div v-else-if="order.diaChiJson && typeof order.diaChiJson === 'string'" class="p-3 bg-light rounded border">
                  <p class="mb-0">{{ order.diaChiJson }}</p>
                </div>

                <!-- Không có thông tin -->
                <div v-else class="p-3 bg-light rounded border text-muted">
                  <p class="mb-0">Không có thông tin địa chỉ</p>
                </div>

                <div v-if="diaChiError" class="alert alert-warning mt-2">{{ diaChiError }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">Danh Sách Sản Phẩm</h5>
          </div>
          <div class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="table-light">
              <tr>
                <th class="ps-4 col-sm-6 text-start">Sản phẩm</th>
                <th class="col-sm-2 text-center">Số lượng</th>
                <th class="col-sm-2 text-center">Đơn giá</th>
                <th class="col-sm-2 text-center">Thành tiền</th>
              </tr>
              </thead>
              <tbody>
              <!-- Hiển thị từ hoaDonCTs -->
              <template v-if="order.hoaDonCTs && order.hoaDonCTs.length">
                <tr v-for="item in order.hoaDonCTs" :key="item.maHDCT" class="align-middle">
                  <td>
                    <div class="ps-3 col-sm-6 d-flex align-items-center" style="height: 70px;">
                      <div class="product-image-wrapper">
                        <img :src="getImageUrl(item.hinhAnh)"
                             alt="Hình sản phẩm"
                             class="product-image"
                             @error="handleImageError">
                      </div>
                      <div class="product-name-container ms-3">
          <span class="product-name-text" :title="item.plSanPham?.sanPham?.tenSP || item.plSanPham?.tenSP || item.tenSP">
            {{ item.plSanPham?.sanPham?.tenSP || item.plSanPham?.tenSP || item.tenSP }}
          </span>
                        <small class="product-variant-text" v-if="item.plSanPham?.phanLoai || item.tenMau || item.size"
                               :title="`Phân loại: ${item.plSanPham?.phanLoai || ''} ${item.tenMau ? item.tenMau : ''} ${item.size ? 'Size ' + item.size : ''}`">
                          {{ item.plSanPham?.phanLoai || '' }}
                          {{ item.tenMau ? item.tenMau : '' }}
                          {{ item.size ? 'Size ' + item.size : '' }}
                        </small>
                      </div>
                    </div>
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ item.soLuong }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice(item.donGia) }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice(item.soLuong * item.donGia) }}
                  </td>
                </tr>
              </template>

              <!-- Hiển thị từ chiTiet  -->
              <template v-else-if="order.chiTiet && order.chiTiet.length">
                <tr v-for="(item, index) in order.chiTiet" :key="index" class="align-middle">
                  <td>
                    <div class="ps-3 col-sm-6 d-flex align-items-center" style="height: 70px;">
                      <div class="product-image-wrapper">
                        <img :src="getImageUrl(item.hinhAnh)"
                             alt="Hình sản phẩm"
                             class="product-image"
                             @error="handleImageError">
                      </div>
                      <div class="product-name-container ms-3">
          <span class="product-name-text" :title="item.tenSP">
            {{ item.tenSP }}
          </span>
                        <small class="product-variant-text" v-if="item.tenMau || item.size"
                               :title="`Phân loại: ${item.tenMau || ''} ${item.size ? 'Size ' + item.size : ''}`">
                          {{ item.tenMau || '' }}
                          {{ item.size ? 'Size ' + item.size : '' }}
                        </small>
                      </div>
                    </div>
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ item.soLuong }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice(item.donGia) }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice(item.thanhTien || (item.soLuong * item.donGia)) }}
                  </td>
                </tr>
              </template>
              </tbody>
            </table>
          </div>
          <div class="card-footer d-flex justify-content-end align-items-center">
            <h5 class="mb-0 me-3">Tổng Cộng:</h5>
            <h4 class="mb-0 text-danger fw-bold">{{ formatPrice(totalPrice) }}</h4>
          </div>
        </div>
      </div>
      <!-- Nút xác nhận đã nhận hàng -->
      <div class="mt-3 d-flex justify-content-end align-items-center gap-2 flex-wrap">
        <button class="btn btn-success" @click="openConfirmModal(order?.maHD)" :disabled="confirmingOrderId === order?.maHD" v-if="order?.trangThai === 'Đang giao'">
          <span v-if="confirmingOrderId === order?.maHD" class="spinner-border spinner-border-sm me-2"></span>
          <i class="bi bi-check-circle me-1"></i> Xác nhận đã nhận hàng
        </button>
      </div>
      <div v-if="canReturnInDetail">
        <div class="d-flex justify-content-end align-items-center gap-2 flex-wrap">
          <!-- Số ngày còn lại -->
          <span v-if="remainingReturnDays > 0" class="badge bg-info text-dark py-2">
            <i class="bi bi-clock-history me-1"></i>Còn {{ remainingReturnDays }} ngày
          </span>
          <small v-if="remainingReturnDays <= 2 && remainingReturnDays > 0" class="text-warning">
            <i class="bi bi-exclamation-triangle me-1"></i>Sắp hết hạn trả hàng!
          </small>
          <!-- Nút yêu cầu trả hàng -->
          <button class="btn btn-warning" @click="openReturnModal(order)" :disabled="returningOrderId === order.maHD">
            <span v-if="returningOrderId === order.maHD" class="spinner-border spinner-border-sm me-2"></span>
            <i class="bi bi-arrow-return-left me-1"></i> Yêu cầu trả hàng
          </button>
        </div>
        <!-- Thông tin chi tiết -->
        <div class="mt-2 small text-muted text-end">
          <i class="bi bi-info-circle me-1"></i>
          Bạn có thể trả hàng trong vòng 7 ngày kể từ ngày nhận
          ({{ formatDate(order.ngayDen || order.ngayMua) }} -
          {{ formatDate(new Date(new Date(order.ngayDen || order.ngayMua).getTime() + 7 * 24 * 60 * 60 * 1000)) }})
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
import { useRoute } from 'vue-router';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

export default {
  name: 'CTDonHang',
  components: {
    KH_Navbar,
    Footer
  },
  setup() {
    const route = useRoute();
    const order = ref(null);
    const diaChi = ref(null);
    const loading = ref(false);
    const error = ref('');
    const diaChiError = ref('');
    const receivingOrderId = ref(null);
    const returningOrderId = ref(null);
    const confirmingOrderId = ref(null);

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

    const fetchOrderDetail = async () => {
      loading.value = true;
      error.value = '';
      diaChiError.value = '';

      try {
        const orderId = route.params.id;
        console.log('Fetching order detail for ID:', orderId);

        const response = await api.getOrderDetail(orderId);
        console.log('Order detail response:', response.data);

        if (response.data.success) {
          // Backend trả về order trong response.data.order
          order.value = response.data.order || response.data;
          console.log('Order data:', order.value);

          // Parse địa chỉ từ diaChiJson nếu có
          if (order.value.diaChiJson) {
            try {
              if (typeof order.value.diaChiJson === 'string') {
                const parsed = JSON.parse(order.value.diaChiJson);
                console.log('Raw parsed diaChi:', parsed);

                // Chuyển đổi key từ viết hoa sang viết thường nếu cần
                diaChi.value = {
                  tenNN: parsed.TenNN || parsed.tenNN || '',
                  sdt: parsed.SDT || parsed.sdt || '',
                  diemGiao: parsed.DiemGiao || parsed.diemGiao || ''
                };
                console.log('Processed diaChi:', diaChi.value);
              } else {
                // Nếu đã là object
                const parsed = order.value.diaChiJson;
                diaChi.value = {
                  tenNN: parsed.TenNN || parsed.tenNN || '',
                  sdt: parsed.SDT || parsed.sdt || '',
                  diemGiao: parsed.DiemGiao || parsed.diemGiao || ''
                };
              }
            } catch (e) {
              console.error('Error parsing diaChiJson:', e);
              diaChiError.value = 'Lỗi parse địa chỉ';

              // Thử hiển thị raw nếu parse lỗi
              diaChi.value = {
                tenNN: '',
                sdt: '',
                diemGiao: order.value.diaChiJson.substring(0, 100) + '...'
              };
            }
          } else {
            // Nếu không có diaChiJson, tạo diaChi từ các trường riêng lẻ
            diaChi.value = {
              tenNN: order.value.tenNguoiNhan || order.value.tenNN || '',
              sdt: order.value.sdtNguoiNhan || order.value.sdt || '',
              diemGiao: order.value.diaChiGiaoHang || order.value.diemGiao || ''
            };
          }

          // Log để kiểm tra
          console.log('Final diaChi:', diaChi.value);
        } else {
          error.value = response.data.message || 'Không tìm thấy đơn hàng';
        }
      } catch (err) {
        console.error('Error fetching order detail:', err);
        error.value = err.response?.data?.message || 'Lỗi khi tải chi tiết đơn hàng';
      } finally {
        loading.value = false;
      }
    };
    // Mở modal xác nhận
    const openConfirmModal = (orderId) => {
      pendingOrderId.value = orderId;
      showConfirmModal.value = true;
    };

    // Đóng modal
    const closeConfirmModal = () => {
      showConfirmModal.value = false;
      pendingOrderId.value = null;
    };

    // Xử lý xác nhận
    const handleConfirmReceived = async () => {
      confirming.value = true;
      try {
        await confirmReceived(pendingOrderId.value);
      } finally {
        confirming.value = false;
        closeConfirmModal();
      }
    };

    const confirmReceived = async (orderId) => {
      confirmingOrderId.value = orderId;

      try {
        console.log('Updating order status:', orderId, 'to Hoàn tất');

        const response = await api.updateCustomerOrderStatus(orderId, 'Hoàn tất');
        console.log('Update response:', response.data);

        if (response.data.success) {
          if (order.value && order.value.maHD === orderId) {
            order.value.trangThai = 'Hoàn tất';
            order.value.ngayDen = new Date().toISOString();
          }

          // Đóng modal xác nhận
          closeConfirmModal();

          // Hiển thị modal thành công
          successMessage.value = 'Xác nhận thành công! Cảm ơn bạn đã mua hàng.';
          showSuccessModal.value = true;
        } else {
          alert(response.data.message || 'Không thể cập nhật trạng thái');
        }
      } catch (err) {
        console.error('Error updating order status:', err);
        alert(err.response?.data?.message || 'Lỗi khi cập nhật trạng thái');
      } finally {
        confirmingOrderId.value = null;
      }
    };

    // Kiểm tra có thể trả hàng trong chi tiết không
    const canReturnInDetail = computed(() => {
      if (!order.value) return false;
      if (order.value.trangThai !== 'Hoàn tất') return false;

      const receivedDate = order.value.ngayDen ? new Date(order.value.ngayDen) : new Date(order.value.ngayMua);
      const today = new Date();
      const diffTime = today - receivedDate;
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

      return diffDays <= 7;
    });

    // Tính số ngày còn lại được trả hàng
    const remainingReturnDays = computed(() => {
      if (!order.value) return 0;
      if (order.value.trangThai !== 'Hoàn tất') return 0;

      // Lấy ngày nhận hàng
      const receivedDate = order.value.ngayDen ? new Date(order.value.ngayDen) : new Date(order.value.ngayMua);

      // Set về 00:00:00 để tính ngày chính xác
      receivedDate.setHours(0, 0, 0, 0);

      // Ngày hết hạn = ngày nhận + 7 ngày
      const expiryDate = new Date(receivedDate);
      expiryDate.setDate(expiryDate.getDate() + 7);
      expiryDate.setHours(23, 59, 59, 999); // Hết hạn vào cuối ngày thứ 7

      // Ngày hiện tại (set về 00:00:00)
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      // Nếu đã hết hạn
      if (today > expiryDate) {
        return 0;
      }

      // Tính số ngày còn lại
      const diffTime = expiryDate - today;
      const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

      return diffDays;
    });

    const getImageUrl = (imageName) => {
      if (!imageName) return 'https://via.placeholder.com/70';
      if (imageName.startsWith('http')) return imageName;
      if (imageName.startsWith('/')) return imageName;
      return `/images/${imageName}`;
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
        hour: '2-digit',
        minute: '2-digit'
      });
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

    const totalPrice = computed(() => {
      if (!order.value) return 0;

      // Nếu order có hoaDonCTs
      if (order.value.hoaDonCTs && Array.isArray(order.value.hoaDonCTs)) {
        return order.value.hoaDonCTs.reduce((total, item) => {
          return total + (item.soLuong * item.donGia);
        }, 0);
      }

      // Nếu order có chiTiet (từ buildOrderDetail)
      if (order.value.chiTiet && Array.isArray(order.value.chiTiet)) {
        return order.value.chiTiet.reduce((total, item) => {
          return total + (item.soLuong * item.donGia);
        }, 0);
      }

      return order.value.tongTien || 0;
    });

    // Mở modal yêu cầu trả hàng
    const openReturnModal = (orderItem) => {
      selectedOrder.value = orderItem;
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
          // Đóng modal trả hàng
          closeReturnModal();

          // Cập nhật trạng thái đơn hàng nếu cần
          if (order.value && order.value.maHD === selectedOrder.value?.maHD) {
            order.value.trangThai = 'Hoàn hàng/trả hàng';
          }

          // Hiển thị modal thành công
          successMessage.value = 'Yêu cầu trả hàng đã được gửi thành công! Chúng tôi sẽ xử lý trong thời gian sớm nhất.';
          showSuccessModal.value = true;
        } else {
          errorMessage.value = response.data.message || 'Không thể gửi yêu cầu trả hàng';
          showErrorModal.value = true;
        }
      } catch (err) {
        console.error('Error submitting return request:', err);
        errorMessage.value = err.response?.data?.message || 'Lỗi khi gửi yêu cầu trả hàng';
        showErrorModal.value = true;
      } finally {
        submitting.value = false;
        returningOrderId.value = null;
      }
    };

    onMounted(() => {
      fetchOrderDetail();
    });

    return {
      order,
      diaChi,
      loading,
      error,
      diaChiError,
      totalPrice,
      getImageUrl,
      formatPrice,
      formatDate,
      getStatusClass,
      canReturnInDetail,
      remainingReturnDays,
      confirmingOrderId,
      confirmReceived,
      // Modal xác nhận
      showConfirmModal,
      confirming,
      pendingOrderId,
      openConfirmModal,
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
  border: 2px solid #000000;
  border-radius: 10px;
}

.card-header {
  font-weight: bold;
  background-color: #000000;
  color: #ffffff;
  border-bottom: 2px solid #000000;
}

.badge {
  font-size: 0.8rem;
  padding: 5px 10px;
}

.table thead th {
  background-color: #f8f9fa;
  border-bottom: 2px solid #000000;
}

.product-image-wrapper {
  width: 70px;
  height: 70px;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 5px;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  transition: transform 0.2s ease;
}

.product-name-container {
  max-width: 300px;
}

.product-name-text {
  display: block;
  font-weight: bold;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-variant-text {
  display: block;
  font-size: 0.8rem;
  color: #6c757d;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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

.modal-header {
  border-bottom: 1px solid #eee;
  padding: 1rem 1.5rem;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  border-top: 1px solid #eee;
  padding: 1rem 1.5rem;
}

@media (max-width: 768px) {
  .product-name-container {
    max-width: 150px;
  }
}
</style>