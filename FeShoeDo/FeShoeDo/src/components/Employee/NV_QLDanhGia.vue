<template>
  
  <div class="employee-layout">
<NV_Sidebar @toggle-collapse="handleSidebarCollapse" />
    <!-- modal xem chi tiết -->
    <div class="modal fade" id="detailModal" tabindex="-1" aria-hidden="true" ref="detailModal">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i class="bi bi-chat-square-text me-2"></i>Chi tiết đánh giá
            </h5>
            <button type="button" data-bs-dismiss="modal"></button>
          </div>
          <div class="modal-body">
            <div v-if="selectedReview">
              <div class="row mb-3">
                <div class="col-md-6">
                  <label class="form-label fw-bold">Tên khách hàng</label>
                  <p class="form-control-plaintext">{{ getTenKhachHang(selectedReview) }}</p>
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-bold">Username</label>
                  <p class="form-control-plaintext">{{ getUsername(selectedReview) }}</p>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold">Sản phẩm</label>
                <p class="form-control-plaintext">{{ getTenSanPham(selectedReview) }}</p>
              </div>
              
              <div class="row mb-3">
                <div class="col-md-6">
                  <label class="form-label fw-bold">Phân loại (Màu sắc)</label>
                  <p class="form-control-plaintext">
                    <i class="bi bi-palette me-1"></i>
                    {{ getMauSac(selectedReview) || 'Không xác định' }}
                  </p>
                </div>
                <div class="col-md-6">
                  <label class="form-label fw-bold">Size</label>
                  <p class="form-control-plaintext">
                    <i class="bi bi-rulers me-1"></i>
                    {{ getSize(selectedReview) || 'Không xác định' }}
                  </p>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold">Đơn giá khi mua</label>
                <p class="form-control-plaintext text-primary fw-bold">
                  {{ formatPrice(getDonGia(selectedReview)) }}
                </p>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold">Đánh giá</label>
                <div class="rating mb-2">
                  <span v-for="star in 5" :key="star" class="star">
                    <i  class="bi" :class="star <= selectedReview.sao ? 'bi-star-fill text-warning' : 'bi-star text-muted'"></i>
                  </span>
                  <span class="ms-2 text-muted small">
                    {{ formatDate(selectedReview.ngayDG) }}
                  </span>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label fw-bold">Nội dung đánh giá</label>
                <div class="border rounded p-3 bg-light" style="max-height: 300px; overflow-y: auto;">
                  <p class="mb-0" style="white-space: pre-wrap;">{{ selectedReview.danhGiaCT || 'Không có nội dung' }}</p>
                </div>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
              Hủy
            </button>
            <button v-if="selectedReview" type="button" class="btn btn-danger" @click="deleteFromModal">
              <i class="bi bi-trash me-2"></i>Xóa đánh giá
            </button>
          </div>
        </div>
      </div>
    </div>

  <!-- modal xác nhận xóa -->
  <div class="modal fade" id="confirmDeleteModal" tabindex="-1" aria-hidden="true" ref="confirmDeleteModal">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">
            <i class="bi bi-exclamation-triangle me-2"></i>Xác nhận xóa
          </h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
        </div>
        <div class="modal-body">
          <p>Bạn có chắc chắn muốn xóa đánh giá này?</p>
          <p class="text-muted small">Hành động này không thể hoàn tác.</p>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">
            Hủy
          </button>
          <button type="button" class="btn btn-danger" @click="executeDelete" :disabled="deleting">
            <span v-if="deleting" class="spinner-border spinner-border-sm me-2"></span>
            <i v-else class="bi bi-trash me-2"></i>Xóa
          </button>
        </div>
      </div>
    </div>
  </div>


<main class="main-content" :class="{ 'expanded': isSidebarCollapsed }">
      <div class="page-container">
        <div v-if="successMessage" class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
          <i class="bi bi-check-circle-fill me-2"></i>
          <span>{{ successMessage }}</span>
          <button type="button" class="btn-close" @click="successMessage = ''"></button>
        </div>

        <!-- tổng quan đánh giá -->
        <div class="row g-3 mb-4">
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-1">
              <h4>{{ totalReviews }}</h4>
              <p>Tổng đánh giá</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-2">
              <h4>{{ averageRating }}</h4>
              <p>Đánh giá trung bình</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-4">
              <h4>{{ positiveReviews }}</h4>
              <p>Đánh giá tích cực (4-5⭐)</p>
            </div>
          </div>
          <div class="col-md-3">
            <div class="stats-mini bg-gradient-3">
              <h4>{{ negativeReviews }}</h4>
              <p>Đánh giá tiêu cực (1-3⭐)</p>
            </div>
          </div>
        </div>

        <!-- danh sách đánh giá -->
        <div class="content-card">
          <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
              <h5 class="page-title">Quản lý đánh giá</h5>
              <p class="page-subtitle">
                Quản lý và xử lý các đánh giá từ khách hàng
              </p>
            </div>
          </div>

          <!-- bộ lọc -->
          <div class="filter-section">
            <div class="row g-3">
              <div class="col-md-3">
                <div class="input-group">
                  <span class="input-group-text bg-light">
                    <i class="bi bi-search"></i>
                  </span>
                  <input type="text" v-model="filterKeyword" class="form-control" placeholder="Tìm theo tên KH hoặc SP"/>
                </div>
              </div>
              <div class="col-md-2">
                <select v-model="filterRating" class="form-select">
                  <option value="">Tất cả số sao</option>
                  <option value="5">5 sao</option>
                  <option value="4">4 sao</option>
                  <option value="3">3 sao</option>
                  <option value="2">2 sao</option>
                  <option value="1">1 sao</option>
                </select>
              </div>
              <div class="col-md-2">
                <select v-model="sortOrder" class="form-select">
                  <option value="desc">Mới nhất</option>
                  <option value="asc">Cũ nhất</option>
                </select>
              </div>
              <div class="col-md-2">
                <input type="date" v-model="filterDate" class="form-control" placeholder="Lọc theo ngày"/>
              </div>
              <div class="col-md-3">
                <button @click="resetFilters" class="btn btn-outline-secondary w-100">
                  <i class="bi bi-arrow-clockwise me-2"></i>Reset
                </button>
              </div>
            </div>
          </div>

          <!-- list đánh giá -->
          <div class="reviews-list">
            <div class="row">
              <div class="col-12" v-for="item in paginatedReviews" :key="item.maDG">
                <div class="review-card">
                  <div class="row align-items-start">
                    <div class="col-md-9">
                      <div class="d-flex align-items-center mb-2">
                        <div class="reviewer-info">
                          <i class="bi bi-person-circle me-2"></i>
                          <strong>{{ getTenKhachHang(item) }}</strong>
                          <span class="text-muted ms-2">|</span>
                          <span class="text-muted ms-2">
                            <i class="bi bi-tag me-1"></i>
                            {{ getTenSanPham(item) }}
                          </span>
                        </div>
                      </div>
                      
                      <div class="rating mb-2">
                        <span v-for="star in 5" :key="star" class="star">
                          <i 
                            class="bi" 
                            :class="star <= item.sao ? 'bi-star-fill text-warning' : 'bi-star text-muted'"
                          ></i>
                        </span>
                        <span class="ms-2 text-muted small">
                          {{ formatDate(item.ngayDG) }}
                        </span>
                      </div>

                      <div class="review-content">
                        <p class="mb-0">
                          {{ getShortContent(item.danhGiaCT) }}
                          <button
                            v-if="item.danhGiaCT && item.danhGiaCT.length > 100"
                            class="btn btn-link btn-sm p-0 ms-2"
                            @click="viewDetail(item)"
                            style="text-decoration: none;"
                          >
                            Xem thêm...
                          </button>
                        </p>
                      </div>
                    </div>
                    
                    <div class="col-md-3 text-end">
                      <div class="action-buttons">
                        <button
                          type="button"
                          class="btn btn-outline-primary btn-sm me-2"
                          @click="viewDetail(item)"
                          title="Xem chi tiết"
                        >
                          <i class="bi bi-eye"></i> Xem chi tiết
                        </button>
                        <button
                          type="button"
                          class="btn btn-outline-danger btn-sm"
                          @click="confirmDelete(item)"
                          title="Xóa đánh giá"
                        >
                          <i class="bi bi-trash"></i> Xóa
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- phân trang -->
              <div v-if="totalPages > 1" class="d-flex justify-content-center align-items-center mt-4 mb-2">
                <nav aria-label="Page navigation">
                  <ul class="pagination pagination-sm mb-0">
                    <li class="page-item" :class="{ disabled: currentPage === 1 }">
                      <button class="page-link text-dark" @click="goToPage(currentPage - 1)">
                        <i class="bi bi-chevron-left"></i> Trước
                      </button>
                    </li>

                    <li class="page-item" v-for="page in totalPages" :key="page" :class="{ active: currentPage === page }">
                      <button class="page-link" :class=" currentPage === page? 'bg-dark border-dark text-white' : 'text-dark'" @click="goToPage(page)">
                        {{ page }}
                      </button>
                    </li>

                    <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                      <button class="page-link text-dark" @click="goToPage(currentPage + 1)">
                        Sau <i class="bi bi-chevron-right"></i>
                      </button>
                    </li>
                  </ul>
                </nav>
              </div>

              <div v-if="filteredReviews.length === 0 && !loading" class="text-center py-5 text-muted">
                <i class="bi bi-chat-square-text display-4"></i>
                <p class="mt-3">Chưa có đánh giá nào</p>
              </div>
              
              <div v-if="loading" class="text-center py-5">
                <div class="spinner-border text-dark" role="status">
                  <span class="visually-hidden">Loading...</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>

  
  
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import axios from "axios";
import NV_Sidebar from "@/components/Shared/NV_Sidebar.vue";
import { Modal } from "bootstrap";

axios.defaults.withCredentials = true;

const successMessage = ref("");
const reviews = ref([]);
const filterKeyword = ref("");
const filterRating = ref("");
const filterDate = ref("");
const sortOrder = ref("desc");
const currentPage = ref(1);
const itemsPerPage = 10;
const selectedReview = ref(null);
const deleteTargetId = ref(null);
const loading = ref(false);
const deleting = ref(false);

const detailModal = ref(null);
const confirmDeleteModal = ref(null);
let detailModalInstance = null;
let confirmDeleteModalInstance = null;

const toast = ref({
  id: 0,
  show: false,
  message: "",
  type: "success",
});

let toastTimeout = null;

const showToast = (message, type = "success") => {
  toast.value = { id: Date.now(), show: true, message, type };
  if (toastTimeout) clearTimeout(toastTimeout);
  toastTimeout = setTimeout(() => {
    toast.value.show = false;
  }, 5000);
};

const fetchReviews = async () => {
  loading.value = true;
  try {
    const response = await axios.get("http://localhost:8080/api/danhgia/list");
    if (response.data.success) {
      reviews.value = response.data.data;
    } else {
      showToast(response.data.message || "Không thể tải danh sách đánh giá", "danger");
    }
  } catch (error) {
    console.error("Lỗi lấy danh sách đánh giá:", error);
    showToast("Không thể tải danh sách đánh giá", "danger");
  } finally {
    loading.value = false;
  }
};

const getTenKhachHang = (review) => {
  try {
    if (review.hoaDonCT?.hoaDon?.khachHang?.tenKH) {
      return review.hoaDonCT.hoaDon.khachHang.tenKH;
    }
  } catch (e) {
    console.error("Lỗi lấy tên khách hàng:", e);
  }
  return "Khách hàng ẩn danh";
};

const getUsername = (review) => {
  try {
    const user = review.hoaDonCT?.hoaDon?.khachHang?.user;
    if (user && user.userName) {
      return user.userName;
    }
  } catch (e) {
    console.error("Lỗi lấy username:", e);
  }
  return "Không có username";
};

const getTenSanPham = (review) => {
  try {
    if (review.hoaDonCT?.sanPhamChiTiet?.sanPham?.tenSP) {
      return review.hoaDonCT.sanPhamChiTiet.sanPham.tenSP;
    }
  } catch (e) {
    console.error("Lỗi lấy tên sản phẩm:", e);
  }
  return "Sản phẩm không xác định";
};

const getMauSac = (review) => {
  try {
    const spct = review.hoaDonCT?.sanPhamChiTiet;
    if (spct && spct.tenMau) {
      return spct.tenMau;
    }
  } catch (e) {
    console.error("Lỗi lấy màu sắc:", e);
  }
  return null;
};

const getSize = (review) => {
  try {
    const size = review.hoaDonCT?.sanPhamChiTiet?.size;
    if (size) {
      return size.coGiay === 0 ? "Freesize" : size.coGiay;
    }
  } catch (e) {
    console.error("Lỗi lấy size:", e);
  }
  return null;
};

const getDonGia = (review) => {
  try {
    return review.hoaDonCT?.donGia || 0;
  } catch (e) {
    return 0;
  }
};

const formatPrice = (price) => {
  if (!price || price === 0) return "0 ₫";
  return price.toLocaleString("vi-VN") + " ₫";
};

const getShortContent = (content) => {
  if (!content) return "Không có nội dung";
  if (content.length <= 100) return content;
  return content.substring(0, 100) + "...";
};

const formatDate = (dateString) => {
  if (!dateString) return "";
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  } catch (e) {
    return dateString;
  }
};

const matchesFilters = (item) => {
  const tenKH = getTenKhachHang(item).toLowerCase();
  const tenSP = getTenSanPham(item).toLowerCase();
  const userName = getUsername(item).toLowerCase();
  const keyword = filterKeyword.value.toLowerCase();
  
  const matchKeyword = !filterKeyword.value || 
    tenKH.includes(keyword) || 
    tenSP.includes(keyword) ||
    userName.includes(keyword);

  const matchRating = !filterRating.value || item.sao === parseInt(filterRating.value);

  let matchDate = true;
  if (filterDate.value && item.ngayDG) {
    const reviewDate = new Date(item.ngayDG).toISOString().split('T')[0];
    matchDate = reviewDate === filterDate.value;
  }

  return matchKeyword && matchRating && matchDate;
};

const filteredReviews = computed(() => {
  let result = reviews.value.filter(matchesFilters);
  return result.sort((a, b) => {
    const dateA = new Date(a.ngayDG);
    const dateB = new Date(b.ngayDG);
    return sortOrder.value === 'desc' ? dateB - dateA : dateA - dateB;
  });
});

const totalReviews = computed(() => filteredReviews.value.length);

const averageRating = computed(() => {
  const filtered = filteredReviews.value;
  if (filtered.length === 0) return "0.0";
  const sum = filtered.reduce((acc, r) => acc + r.sao, 0);
  return (sum / filtered.length).toFixed(1);
});

const positiveReviews = computed(() => filteredReviews.value.filter(r => r.sao >= 4).length);
const negativeReviews = computed(() => filteredReviews.value.filter(r => r.sao <= 3).length);

const totalPages = computed(() => Math.ceil(filteredReviews.value.length / itemsPerPage));

const paginatedReviews = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredReviews.value.slice(start, end);
});

const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page;
  }
};

const resetFilters = () => {
  filterKeyword.value = "";
  filterRating.value = "";
  filterDate.value = "";
  sortOrder.value = "desc";
  currentPage.value = 1;
};

const viewDetail = (review) => {
  selectedReview.value = review;
  if (detailModalInstance) {
    detailModalInstance.show();
  }
};

const confirmDelete = (review) => {
  deleteTargetId.value = review.maDG;
  if (confirmDeleteModalInstance) {
    confirmDeleteModalInstance.show();
  }
};

const deleteFromModal = () => {
  if (selectedReview.value) {
    if (detailModalInstance) {
      detailModalInstance.hide();
    }
    deleteTargetId.value = selectedReview.value.maDG;
    if (confirmDeleteModalInstance) {
      confirmDeleteModalInstance.show();
    }
  }
};

const executeDelete = async () => {
  if (!deleteTargetId.value) return;
  
  deleting.value = true;
  try {
    const response = await axios.delete(`http://localhost:8080/api/danhgia/delete/${deleteTargetId.value}`);
    if (response.data.success) {
      showToast("Xóa đánh giá thành công!", "success");
      await fetchReviews();
      
      if (confirmDeleteModalInstance) {
        confirmDeleteModalInstance.hide();
      }
      
      selectedReview.value = null;
    } else {
      showToast(response.data.message || "Xóa thất bại", "danger");
    }
  } catch (error) {
    console.error("Lỗi xóa đánh giá:", error);
    showToast(error.response?.data?.message || "Không thể xóa đánh giá", "danger");
  } finally {
    deleting.value = false;
    deleteTargetId.value = null;
  }
};

const handleFilterChange = () => {
  currentPage.value = 1;
};

watch([filterKeyword, filterRating, filterDate, sortOrder], () => {
  handleFilterChange();
});

const isSidebarCollapsed = ref(false);

const handleSidebarCollapse = (collapsedState) => {
  isSidebarCollapsed.value = collapsedState;
};

onMounted(() => {
  fetchReviews();

  if (detailModal.value) {
    detailModalInstance = new Modal(detailModal.value);
  }
  if (confirmDeleteModal.value) {
    confirmDeleteModalInstance = new Modal(confirmDeleteModal.value);
  }
});
</script>

<style scoped>
.main-content {
  margin-left: 260px;
  min-height: 100vh;
}

.page-container {
  padding: 30px;
  background: #f7f7f9;
  min-height: 100vh;
}

.stats-mini {
  padding: 25px 20px;
  border-radius: 12px;
  text-align: center;
  color: rgb(255, 255, 255);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.stats-mini h4 {
  font-size: 32px;
  font-weight: bold;
  margin: 0;
}

.stats-mini p {
  margin: 8px 0 0;
  font-size: 14px;
  opacity: 0.9;
}

.bg-gradient-1,
.bg-gradient-2,
.bg-gradient-3,
.bg-gradient-4 {
  background: linear-gradient(135deg, #212529, #000000);
}

.content-card {
  background: white;
  border-radius: 16px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.page-subtitle {
  color: #666;
  font-size: 14px;
  margin-bottom: 0;
}

.filter-section {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 25px;
  border: 1px solid #eee;
}

.review-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s;
  margin-bottom: 15px;
}

.review-card:hover {
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
  border-color: #d0d0d0;
}

.reviewer-info {
  font-size: 16px;
}

.star {
  font-size: 18px;
  margin-right: 2px;
}

.review-content {
  color: #555;
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.toast-container {
  z-index: 1090;
}

.toast {
  position: relative;
  min-width: 300px;
}

.toast-progress-bar {
  height: 4px;
  background-color: rgba(255, 255, 255, 0.7);
  width: 100%;
  position: absolute;
  bottom: 0;
  left: 0;
  border-bottom-left-radius: var(--bs-toast-border-radius);
  animation: shrinkProgress 3s linear forwards;
}

@keyframes shrinkProgress {
  from {
    width: 100%;
  }
  to {
    width: 0%;
  }
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.toast-fade-enter-from {
  opacity: 0;
  transform: translateY(-50px);
}
.toast-fade-leave-to {
  opacity: 0;
  transform: translateY(-50px);
}

/* Responsive */
@media (max-width: 768px) {
  .main-content {
    margin-left: 0;
  }

  .page-container {
    padding: 15px;
  }

  .review-card .row {
    flex-direction: column;
  }

  .action-buttons {
    justify-content: flex-start;
    margin-top: 15px;
  }
}
.main-content {
  margin-left: 260px; /* Trạng thái Sidebar mặc định */
  min-height: 100vh;
  background: #f8f9fa;
  transition: margin-left 0.3s cubic-bezier(0.25, 0.8, 0.25, 1); /* Thêm dòng này để mượt */
}

/* Khi Sidebar thu nhỏ thì nới rộng nội dung chính ra */
.main-content.expanded {
  margin-left: 80px; 
}
</style>