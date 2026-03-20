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
                <th class="col-sm-1 text-center">Số lượng</th>
                <th class="col-sm-2 text-center">Đơn giá</th>
                <th class="col-sm-2 text-center">Thành tiền</th>
                <th class="col-sm-2 text-center">Đánh giá</th>
              </tr>
              </thead>
              <tbody>
              <!-- Hiển thị từ hoaDonCTs -->
              <template v-if="order.hoaDonCTs && order.hoaDonCTs.length">
                <tr v-for="item in order.hoaDonCTs" :key="item.maHDCT" class="align-middle">
                  <td>
                    <div class="ps-3 col-sm-6 d-flex align-items-center" style="height: 70px;">
                      <div class="product-image-wrapper">
                        <img :src="getImageUrl(item.hinhAnh)" alt="Hình sản phẩm" class="product-image" @error="handleImageError">
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
                  <td class="col-sm-1 text-center" style="height: 70px;">
                    {{ item.soLuong }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice(item.donGia) }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice(item.soLuong * item.donGia) }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px; min-width: 120px;">
                    <div v-if="item.daDanhGia">
                      <button class="btn btn-outline-warning btn-sm" @click="openViewReviewModal(item)" title="Xem đánh giá">
                        <i class="bi bi-star-fill text-warning me-1"></i>Đã đánh giá
                      </button>
                    </div>
                    <button v-else-if="canReview(item)" class="btn btn-outline-warning btn-sm" @click="openReviewModal(item)">
                      <i class="bi bi-star me-1"></i>Đánh giá
                    </button>
                  </td>
                </tr>
              </template>

              <!-- Hiển thị từ chiTiet  -->
              <template v-else-if="order.chiTiet && order.chiTiet.length">
                <tr v-for="(item, index) in order.chiTiet" :key="index" class="align-middle">
                  <td>
                    <div class="ps-3 col-sm-5 d-flex align-items-center" style="height: 70px;">
                      <div class="product-image-wrapper">
                        <img :src="getImageUrl(item.hinhAnh)" alt="Hình sản phẩm" class="product-image" @error="handleImageError">
                      </div>
                      <div class="product-name-container ms-3">
                        <span class="product-name-text" :title="item.tenSP">
                          {{ item.tenSP }}
                        </span>
                        <small class="product-variant-text" v-if="item.tenMau || item.size" :title="`Phân loại: ${item.tenMau || ''} ${item.size ? 'Size ' + item.size : ''}`">{{ item.tenMau || '' }} {{ item.size ? 'Size ' + item.size : '' }}
                        </small>
                      </div>
                    </div>
                  </td>
                  <td class="col-sm-1 text-center" style="height: 70px;">
                    {{ item.soLuong }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice(item.donGia) }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px;">
                    {{ formatPrice(item.thanhTien || (item.soLuong * item.donGia)) }}
                  </td>
                  <td class="col-sm-2 text-center" style="height: 70px; min-width: 120px;">
                    <div v-if="item.daDanhGia">
                      <button class="btn btn-outline-warning btn-sm" @click="openViewReviewModal(item)" title="Xem đánh giá">
                        <i class="bi bi-star-fill text-warning me-1"></i>Đã đánh giá
                      </button>
                    </div>
                    <button v-else-if="canReview(item)" class="btn btn-outline-warning btn-sm" @click="openReviewModal(item)">
                      <i class="bi bi-star me-1"></i>Đánh giá
                    </button>
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

        <!-- Các nút hành động -->
        <div class="mt-3 d-flex justify-content-end align-items-center gap-2 flex-wrap">
          <!-- Nút xác nhận đã nhận hàng -->
          <button class="btn btn-success" @click="openConfirmModal(order?.maHD)" :disabled="confirmingOrderId === order?.maHD" v-if="order?.trangThai === 'Đang giao'">
            <span v-if="confirmingOrderId === order?.maHD" class="spinner-border spinner-border-sm me-2"></span>
            <i class="bi bi-check-circle me-1"></i> Xác nhận đã nhận hàng
          </button>

          <!-- Nút báo lỗi -->
          <button class="btn btn-danger" @click="openReportIssueModal(order)" :disabled="reportingOrderId === order?.maHD" v-if="canReportIssue(order)">
            <span v-if="reportingOrderId === order?.maHD" class="spinner-border spinner-border-sm me-2"></span>
            <i class="bi bi-exclamation-triangle me-1"></i> Báo lỗi
          </button>
        </div>

        <!-- Thông tin báo lỗi chi tiết -->
        <div v-if="canReportIssue(order)" class="mt-2">
          <div class="d-flex justify-content-end">
            <div class="report-info d-flex align-items-center gap-3 flex-wrap justify-content-end">
              <div class="timer-display-small">
                <i class="bi bi-clock-history me-1"></i>
                <span>Còn {{ remainingTime.formatted }}</span>
              </div>
              <div class="deadline-info-small">
                <i class="bi bi-calendar-check me-1"></i>
                Hạn: {{ formatDeadline }}
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

    <!-- Modal báo lỗi -->
    <div v-if="showReportIssueModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-danger text-white">
            <h5 class="modal-title">
              <i class="bi bi-exclamation-triangle me-2"></i>Báo lỗi đơn hàng
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeReportIssueModal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedOrder">
              <p><strong>Mã đơn hàng:</strong> #{{ selectedOrder.maHD }}</p>
              <p><strong>Ngày mua:</strong> {{ formatDate(selectedOrder.ngayMua) }}</p>
              <p><strong>Ngày nhận:</strong> {{ formatDate(selectedOrder.ngayDen) }}</p>

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
            <button type="button" class="btn btn-danger" @click="submitReportIssue" :disabled="!reportReason || reporting">
              <span v-if="reporting" class="spinner-border spinner-border-sm me-2"></span>
              Gửi báo lỗi
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal đánh giá sản phẩm -->
    <div v-if="showReviewModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-primary text-white">
            <h5 class="modal-title">
              <i class="bi bi-star-fill me-2"></i>Đánh giá sản phẩm
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeReviewModal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedProduct">
              <div class="d-flex align-items-center mb-3">
                <img :src="getImageUrl(selectedProduct.hinhAnh)" alt="Product" class="me-3" style="width: 60px; height: 60px; object-fit: cover; border-radius: 5px;">
                <div>
                  <strong>{{ selectedProduct.tenSP }}</strong>
                  <small class="text-muted d-block">
                    {{ selectedProduct.tenMau || '' }} {{ selectedProduct.size ? 'Size ' + selectedProduct.size : '' }}
                  </small>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold">Đánh giá của bạn <span class="text-danger">*</span></label>
                <div class="d-flex gap-2 mb-2">
                  <span v-for="star in 5" :key="star"
                        class="star-rating"
                        @click="reviewRating = star"
                        @mouseover="hoverRating = star"
                        @mouseleave="hoverRating = 0">
                    <i :class="star <= (hoverRating || reviewRating) ? 'bi bi-star-fill text-warning' : 'bi bi-star text-secondary'"></i>
                  </span>
                </div>
                <small class="text-muted">{{ reviewRating }}/5 sao</small>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold">Nhận xét của bạn</label>
                <textarea class="form-control" rows="4" v-model="reviewComment"
                          placeholder="Chia sẻ cảm nhận của bạn về sản phẩm..."></textarea>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeReviewModal">Hủy</button>
            <button type="button" class="btn btn-primary" @click="submitReview" :disabled="reviewRating === 0 || submittingReview">
              <span v-if="submittingReview" class="spinner-border spinner-border-sm me-2"></span>
              <i class="bi bi-check-circle me-1"></i>Gửi đánh giá
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal xem đánh giá -->
    <div v-if="showViewReviewModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-info text-white">
            <h5 class="modal-title">
              <i class="bi bi-star-fill me-2"></i>Chi tiết đánh giá
            </h5>
            <button type="button" class="btn-close btn-close-white" @click="closeViewReviewModal"></button>
          </div>
          <div class="modal-body position-relative">
            <div v-if="viewReviewData">
              <div class="text-center">
                <div class="d-flex justify-content-center gap-4 mb-3">
              <span v-for="star in 5" :key="star" class="review-star-large">
                <i :class="star <= viewReviewData.sao ? 'bi bi-star-fill text-warning' : 'bi bi-star text-secondary'"></i>
              </span>
                </div>
                <div class="text-end" style="color: #afaeae;">{{ viewReviewData.sao }}/5</div>

              </div>

              <div class="mb-3" v-if="viewReviewData.danhGiaCT">
                <label class="form-label fw-bold">Nhận xét:</label>
                <p class="p-3 bg-light rounded">{{ viewReviewData.danhGiaCT }}</p>
              </div>

              <div class="text-muted small text-end">
                <i class="bi bi-clock me-1"></i>
                {{ formatDate(viewReviewData.ngayDG) }}
              </div>


            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeViewReviewModal">
              <i class="bi bi-x-circle me-1"></i>Đóng
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
import { useRoute } from 'vue-router';
import api from '@/services/api';
import KH_Navbar from '@/components/shared/KH_Navbar.vue';
import Footer from '@/components/shared/Footer.vue';

const route = useRoute();
const order = ref(null);
const diaChi = ref(null);
const loading = ref(false);
const error = ref('');
const diaChiError = ref('');
const receivingOrderId = ref(null);
const reportingOrderId = ref(null);
const confirmingOrderId = ref(null);

// State cho modal xác nhận
const showConfirmModal = ref(false);
const confirming = ref(false);
const pendingOrderId = ref(null);

// State cho modal báo lỗi
const showReportIssueModal = ref(false);
const selectedOrder = ref(null);
const reportReason = ref('');
const reportNote = ref('');
const reporting = ref(false);

// State cho modal đánh giá
const showReviewModal = ref(false);
const selectedProduct = ref(null);
const reviewRating = ref(0);
const hoverRating = ref(0);
const reviewComment = ref('');
const submittingReview = ref(false);

// State cho modal xem đánh giá
const showViewReviewModal = ref(false);
const viewReviewData = ref(null);

// State cho modal thông báo thành công
const showSuccessModal = ref(false);
const successMessage = ref('');

// State cho modal lỗi
const showErrorModal = ref(false);
const errorMessage = ref('');

// Timer realtime
let timerInterval = null;
const timeTick = ref(Date.now());

// Bắt đầu timer
const startTimer = () => {
  if (timerInterval) clearInterval(timerInterval);
  timerInterval = setInterval(() => {
    timeTick.value = Date.now();
  }, 1000);
};

// Dừng timer
const stopTimer = () => {
  if (timerInterval) {
    clearInterval(timerInterval);
    timerInterval = null;
  }
};

const fetchOrderDetail = async () => {
  loading.value = true;
  error.value = '';
  diaChiError.value = '';

  try {
    const orderId = route.params.id;
    const response = await api.getOrderDetail(orderId);

    if (response.data.success) {
      order.value = response.data.order || response.data;

      // Parse địa chỉ từ diaChiJson nếu có
      if (order.value.diaChiJson) {
        try {
          if (typeof order.value.diaChiJson === 'string') {
            const parsed = JSON.parse(order.value.diaChiJson);
            diaChi.value = {
              tenNN: parsed.TenNN || parsed.tenNN || '',
              sdt: parsed.SDT || parsed.sdt || '',
              diemGiao: parsed.DiemGiao || parsed.diemGiao || ''
            };
          } else {
            const parsed = order.value.diaChiJson;
            diaChi.value = {
              tenNN: parsed.TenNN || parsed.tenNN || '',
              sdt: parsed.SDT || parsed.sdt || '',
              diemGiao: parsed.DiemGiao || parsed.diemGiao || ''
            };
          }
        } catch (e) {
          diaChiError.value = 'Lỗi parse địa chỉ';
          diaChi.value = {
            tenNN: '',
            sdt: '',
            diemGiao: order.value.diaChiJson.substring(0, 100) + '...'
          };
        }
      } else {
        diaChi.value = {
          tenNN: order.value.tenNguoiNhan || order.value.tenNN || '',
          sdt: order.value.sdtNguoiNhan || order.value.sdt || '',
          diemGiao: order.value.diaChiGiaoHang || order.value.diemGiao || ''
        };
      }
    } else {
      error.value = response.data.message || 'Không tìm thấy đơn hàng';
    }
  } catch (err) {
    error.value = err.response?.data?.message || 'Lỗi khi tải chi tiết đơn hàng';
  } finally {
    loading.value = false;
  }
};

// Kiểm tra có thể báo lỗi không
const canReportIssue = (order) => {
  if (!order) return false;

  if (order.trangThai === 'Hoàn tất') {
    const receivedDate = order.ngayDen ? new Date(order.ngayDen) : new Date(order.ngayMua);
    const deadline = new Date(receivedDate);
    deadline.setDate(deadline.getDate() + 30);
    const now = new Date();
    return now <= deadline;
  }
  return false;
};

// Tính thời gian còn lại (cập nhật mỗi giây)
const remainingTime = computed(() => {
  // Dùng timeTick để force re-compute
  const _ = timeTick.value;

  if (!order.value || !canReportIssue(order.value)) {
    return {
      days: 0,
      hours: 0,
      minutes: 0,
      seconds: 0,
      total: 0,
      formatted: '00 ngày 00:00:00'
    };
  }

  const receivedDate = order.value.ngayDen ? new Date(order.value.ngayDen) : new Date(order.value.ngayMua);
  const deadline = new Date(receivedDate);
  deadline.setDate(deadline.getDate() + 30);

  const now = new Date();
  const diffMs = deadline - now;

  if (diffMs <= 0) {
    return {
      days: 0,
      hours: 0,
      minutes: 0,
      seconds: 0,
      total: 0,
      formatted: '00 ngày 00:00:00'
    };
  }

  const totalSeconds = Math.floor(diffMs / 1000);
  const days = Math.floor(totalSeconds / (24 * 60 * 60));
  const hours = Math.floor((totalSeconds % (24 * 60 * 60)) / (60 * 60));
  const minutes = Math.floor((totalSeconds % (60 * 60)) / 60);
  const seconds = totalSeconds % 60;

  const formattedDays = String(days).padStart(2, '0');
  const formattedHours = String(hours).padStart(2, '0');
  const formattedMinutes = String(minutes).padStart(2, '0');
  const formattedSeconds = String(seconds).padStart(2, '0');

  const formatted = `${formattedDays} ngày ${formattedHours}:${formattedMinutes}:${formattedSeconds}`;

  return {
    days,
    hours,
    minutes,
    seconds,
    total: diffMs,
    formatted
  };
});

// Format deadline
const formatDeadline = computed(() => {
  if (!order.value) return '';
  const receivedDate = order.value.ngayDen ? new Date(order.value.ngayDen) : new Date(order.value.ngayMua);
  const deadline = new Date(receivedDate);
  deadline.setDate(deadline.getDate() + 30);
  return formatFullDateTime(deadline);
});

// Format ngày giờ đầy đủ
const formatFullDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).replace(',', '');
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
    const response = await api.updateCustomerOrderStatus(orderId, 'Hoàn tất');

    if (response.data.success) {
      if (order.value && order.value.maHD === orderId) {
        order.value.trangThai = 'Hoàn tất';
        order.value.ngayDen = new Date().toISOString();
      }

      closeConfirmModal();
      successMessage.value = 'Xác nhận thành công! Cảm ơn bạn đã mua hàng.';
      showSuccessModal.value = true;
    } else {
      alert(response.data.message || 'Không thể cập nhật trạng thái');
    }
  } catch (err) {
    alert(err.response?.data?.message || 'Lỗi khi cập nhật trạng thái');
  } finally {
    confirmingOrderId.value = null;
  }
};

// Kiểm tra có thể đánh giá sản phẩm không
const canReview = (item) => {
  if (!order.value) return false;
  if (order.value.trangThai !== 'Hoàn tất') return false;
  return !item.daDanhGia;
};

// Mở modal báo lỗi
const openReportIssueModal = (orderItem) => {
  selectedOrder.value = orderItem;
  reportReason.value = '';
  reportNote.value = '';
  showReportIssueModal.value = true;
};

// Đóng modal báo lỗi
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

      // Cập nhật trạng thái đơn hàng
      if (order.value && order.value.maHD === selectedOrder.value?.maHD) {
        order.value.trangThai = 'Báo lỗi';
      }

      // Reload trang sau 1 giây để hiển thị trạng thái mới
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

// Mở modal đánh giá
const openReviewModal = (product) => {
  selectedProduct.value = product;
  reviewRating.value = 0;
  reviewComment.value = '';
  showReviewModal.value = true;
};

// Đóng modal đánh giá
const closeReviewModal = () => {
  showReviewModal.value = false;
  selectedProduct.value = null;
  reviewRating.value = 0;
  reviewComment.value = '';
  hoverRating.value = 0;
};

// Gửi đánh giá
const submitReview = async () => {
  if (reviewRating.value === 0) {
    errorMessage.value = 'Vui lòng chọn số sao đánh giá';
    showErrorModal.value = true;
    return;
  }

  submittingReview.value = true;

  try {
    const response = await api.addReview({
      maHDCT: selectedProduct.value.maHDCT,
      sao: reviewRating.value,
      danhGiaCT: reviewComment.value
    });

    if (response.data.success) {
      closeReviewModal();
      successMessage.value = 'Cảm ơn bạn đã đánh giá sản phẩm!';
      showSuccessModal.value = true;
      setTimeout(() => {
        fetchOrderDetail();
      }, 1000);
    } else {
      errorMessage.value = response.data.message || 'Không thể gửi đánh giá';
      showErrorModal.value = true;
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Lỗi khi gửi đánh giá';
    showErrorModal.value = true;
  } finally {
    submittingReview.value = false;
  }
};

// Mở modal xem đánh giá
const openViewReviewModal = (product) => {
  if (product.danhGia) {
    viewReviewData.value = product.danhGia;
  } else {
    fetchReviewDetail(product.maHDCT);
  }
  showViewReviewModal.value = true;
};

// Đóng modal xem đánh giá
const closeViewReviewModal = () => {
  showViewReviewModal.value = false;
  viewReviewData.value = null;
};

// Lấy chi tiết đánh giá từ API
const fetchReviewDetail = async (maHDCT) => {
  try {
    const response = await api.getReview(maHDCT);
    if (response.data.success && response.data.danhGia) {
      viewReviewData.value = response.data.danhGia;
    } else {
      errorMessage.value = 'Không tìm thấy đánh giá';
      showErrorModal.value = true;
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Lỗi khi lấy đánh giá';
    showErrorModal.value = true;
  }
};

const getImageUrl = (imageName) => {
  if (!imageName) return 'https://via.placeholder.com/70';
  if (imageName.startsWith('http')) return imageName;
  if (imageName.startsWith('/')) return imageName;
  return `/images/${imageName}`;
};

const handleImageError = (e) => {
  e.target.src = 'https://via.placeholder.com/70';
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
    case 'Hoàn tất': return 'badge bg-success';
    case 'Đang giao': return 'badge bg-primary';
    case 'Đang xử lý': return 'badge bg-warning text-dark';
    case 'Đã từ chối': return 'badge bg-danger';
    case 'Báo lỗi': return 'badge bg-dark';
    case 'Hoàn hàng/trả hàng': return 'badge bg-secondary';
    default: return 'badge bg-secondary';
  }
};

const totalPrice = computed(() => {
  if (!order.value) return 0;
  if (order.value.hoaDonCTs && Array.isArray(order.value.hoaDonCTs)) {
    return order.value.hoaDonCTs.reduce((total, item) => {
      return total + (item.soLuong * item.donGia);
    }, 0);
  }
  if (order.value.chiTiet && Array.isArray(order.value.chiTiet)) {
    return order.value.chiTiet.reduce((total, item) => {
      return total + (item.soLuong * item.donGia);
    }, 0);
  }
  return order.value.tongTien || 0;
});

// Theo dõi khi order thay đổi để bắt đầu/dừng timer
watch(() => order.value, (newOrder) => {
  if (newOrder && canReportIssue(newOrder)) {
    startTimer();
  } else {
    stopTimer();
  }
}, { immediate: true });

// Dọn dẹp khi component unmount
onBeforeUnmount(() => {
  stopTimer();
});

onMounted(() => {
  fetchOrderDetail();
});
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

/* Star rating styles */
.star-rating {
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.2s;
}

.star-rating:hover {
  transform: scale(1.1);
}

.star-rating i {
  transition: color 0.2s;
}

/* Review star large */
.review-star-large {
  font-size: 3.6rem;
  cursor: default;
  transition: transform 0.2s;
}

.review-star-large:hover {
  transform: scale(1.1);
}

.review-star-large i {
  transition: color 0.2s;
}

.bi-star-fill.text-warning {
  color: #ffc107 !important;
}

/* Cột đánh giá */
.col-sm-2.text-center {
  min-width: 150px;
}

/* Nút đánh giá và xem đánh giá */
.btn-outline-warning {
  color: #ffc107;
  border-color: #ffc107;
  background-color: transparent;
  transition: all 0.3s;
  font-weight: 500;
  padding: 0.4rem 0.75rem;
  border-width: 2px;
}

.btn-outline-warning:hover {
  background-color: #ffc107;
  border-color: #ffc107;
  color: #000000;
}

.btn-outline-warning i {
  font-size: 0.9rem;
}

.btn-outline-warning .bi-star-fill {
  color: #ffde07 !important;
}

.btn-outline-warning:hover .bi-star-fill {
  color: #000000 !important;
}

/* Timer styles */
.report-info {
  color: #bf0707;
  font-size: 1rem;
  background: transparent;
  padding: 0;
}

.timer-display-small {
  color: #bf0707;
  font-family: 'Courier New', monospace;
  background: transparent;
  padding: 2px 0;
}

.timer-display-small i {
  color: #bf0707;
  font-size: 1rem;
}

.deadline-info-small {
  color: #717171;
  font-family: 'Courier New', monospace;
  background: transparent;
  padding: 2px 0;
}

.deadline-info-small i {
  color: #717171;
  font-size: 1rem;
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

.modal-header.bg-primary,
.modal-header.bg-success,
.modal-header.bg-danger,
.modal-header.bg-info {
  background: #000000 !important;
  border-bottom: 2px solid #ffffff;
}

.modal-header .modal-title {
  color: #ffffff;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.modal-header .btn-close-white {
  filter: brightness(0) invert(1);
}

.modal-body {
  padding: 1.5rem;
  position: relative;
}

.modal-body.position-relative {
  padding-top: 2.5rem;
}

.modal-body .bg-light {
  background-color: #1a1a1a !important;
  color: #ffffff;
  border: 1px solid #333;
}

.modal-body .text-warning {
  color: #ffc107 !important;
}

.modal-body .text-muted {
  color: #999 !important;
}

.modal-body .form-label.fw-bold {
  color: #000000;
  border-bottom: 1px solid #333;
  padding-bottom: 5px;
  margin-bottom: 10px;
}

.modal-footer {
  border-top: 1px solid #eee;
  padding: 1rem 1.5rem;
}

.modal-footer .btn-primary,
.modal-footer .btn-secondary,
.modal-footer .btn-danger,
.modal-footer .btn-success {
  background-color: #000000;
  border-color: #ffffff;
  color: #ffffff;
  transition: all 0.3s;
}

.modal-footer .btn-primary:hover,
.modal-footer .btn-secondary:hover,
.modal-footer .btn-danger:hover,
.modal-footer .btn-success:hover {
  background-color: #ffffff;
  color: #000000;
  border-color: #000000;
}

/* Responsive */
@media (max-width: 768px) {
  .product-name-container {
    max-width: 150px;
  }
  .star-rating {
    font-size: 1.2rem;
  }
  .report-info {
    min-width: 280px;
  }
}
</style>